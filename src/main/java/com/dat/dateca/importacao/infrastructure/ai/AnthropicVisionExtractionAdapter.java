package com.dat.dateca.importacao.infrastructure.ai;

import com.dat.dateca.importacao.domain.exceptions.ExtractionFailedException;
import com.dat.dateca.importacao.domain.ports.CoverExtractionResult;
import com.dat.dateca.importacao.domain.ports.PageInput;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionBatch;
import com.dat.dateca.importacao.domain.ports.VisionExtractionPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Extração estruturada via Anthropic Messages API (tool-use com schema forçado). Páginas sem texto
 * nativo confiável (ou com layout ambíguo) chegam aqui como imagem rasterizada em vez de texto — a
 * mesma chamada faz OCR e estruturação, sem Tesseract.
 *
 * <p>Duas garantias que o pipeline depende: quando a geração é cortada por {@code max_tokens} o
 * adapter escala o limite e, se ainda assim não couber, devolve
 * {@link QuestionExtractionBatch#truncated} em vez de fingir que a resposta veio completa; e erros
 * 4xx (chave inválida, payload malformado) falham na primeira tentativa em vez de consumir o
 * orçamento de retry.
 */
public class AnthropicVisionExtractionAdapter implements VisionExtractionPort {

    private static final Logger log = LoggerFactory.getLogger(AnthropicVisionExtractionAdapter.class);

    private static final String MESSAGES_URI = "/v1/messages";
    private static final String STOP_REASON_MAX_TOKENS = "max_tokens";
    private static final long RETRY_BASE_DELAY_MS = 500L;

    private final RestClient restClient;
    private final AnthropicProperties properties;
    private final ObjectMapper objectMapper;

    public AnthropicVisionExtractionAdapter(AnthropicProperties properties, ObjectMapper objectMapper) {
        // A leitura da resposta é feita via exchange() (ver post()), sem conversor de leitura, então
        // o builder pelado basta — não dependemos de um bean RestClient.Builder (que o Spring Boot 4
        // não auto-configura) nem do conjunto de conversores do contexto.
        this(properties, objectMapper, RestClient.builder()
                .requestFactory(RestClientFactories.withTimeouts(properties.getConnectTimeout(), properties.getReadTimeout()))
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("anthropic-version", properties.getVersion())
                .defaultHeader("content-type", "application/json")
                .build());
    }

    // Visível para testes: permite injetar um RestClient ligado a um MockRestServiceServer.
    AnthropicVisionExtractionAdapter(AnthropicProperties properties, ObjectMapper objectMapper, RestClient restClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    @Override
    public CoverExtractionResult extractCover(List<PageInput> coverPages) {
        ToolCallOutcome outcome = callTool(ExtractionPrompts.COVER_TOOL, ExtractionPrompts.COVER_SYSTEM_PROMPT,
                ExtractionPrompts.COVER_SCHEMA, coverPages);
        return ExtractionResultMapper.toCover(outcome.input());
    }

    @Override
    public QuestionExtractionBatch extractQuestions(List<PageInput> chunkPages) {
        ToolCallOutcome outcome = callTool(ExtractionPrompts.QUESTIONS_TOOL, ExtractionPrompts.QUESTION_SYSTEM_PROMPT,
                ExtractionPrompts.QUESTIONS_SCHEMA, chunkPages);
        return new QuestionExtractionBatch(ExtractionResultMapper.toQuestions(outcome.input()), outcome.truncated());
    }

    @Override
    public Map<Integer, Character> extractAnswerKey(List<PageInput> answerKeyPages) {
        ToolCallOutcome outcome = callTool(ExtractionPrompts.ANSWER_KEY_TOOL, ExtractionPrompts.ANSWER_KEY_SYSTEM_PROMPT,
                ExtractionPrompts.ANSWER_KEY_SCHEMA, answerKeyPages);
        return ExtractionResultMapper.toAnswerKey(outcome.input());
    }

    /**
     * Chama a ferramenta escalando {@code max_tokens} enquanto a resposta vier cortada. Devolve
     * {@code truncated=true} apenas quando nem o teto configurado foi suficiente — a partir daí a
     * decisão (quebrar o bloco, marcar para revisão) é do orquestrador.
     */
    private ToolCallOutcome callTool(String toolName, String systemPrompt, String schemaJson, List<PageInput> pages) {
        int maxTokens = Math.max(1, properties.getMaxTokens());
        int ceiling = Math.max(maxTokens, properties.getMaxTokensCeiling());

        while (true) {
            JsonNode response = post(buildRequestBody(toolName, systemPrompt, schemaJson, pages, maxTokens));
            JsonNode input = findToolInput(response, toolName);

            if (!STOP_REASON_MAX_TOKENS.equals(response.path("stop_reason").asText(""))) {
                if (input == null) {
                    throw new ExtractionFailedException(
                            "A IA não retornou o resultado estruturado esperado para " + toolName);
                }
                return new ToolCallOutcome(input, false);
            }

            if (maxTokens >= ceiling) {
                log.warn("Resposta de {} truncada mesmo com max_tokens={} (teto configurado); "
                        + "o bloco será reprocessado em partes menores", toolName, maxTokens);
                return new ToolCallOutcome(input != null ? input : objectMapper.createObjectNode(), true);
            }

            int escalated = Math.min(maxTokens * 2, ceiling);
            log.warn("Resposta de {} truncada com max_tokens={}; repetindo o bloco com max_tokens={}",
                    toolName, maxTokens, escalated);
            maxTokens = escalated;
        }
    }

    private JsonNode post(ObjectNode body) {
        int attempts = Math.max(1, properties.getMaxRetries() + 1);
        String payload = serialize(body);
        String apiKey = requireApiKey();
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                // exchange() dá acesso ao corpo cru da resposta, sem passar por um HttpMessageConverter
                // de leitura — o Ollama/serviços podem negociar application/octet-stream, que os
                // conversores default não leem. Lemos os bytes e parseamos com o ObjectMapper (Jackson 2),
                // independente do mapper do contexto (Jackson 3 no Spring 7).
                RawResponse raw = restClient.post()
                        .uri(MESSAGES_URI)
                        .header("x-api-key", apiKey)
                        .body(payload)
                        .exchange((request, response) -> new RawResponse(
                                response.getStatusCode(), response.getBody().readAllBytes()), false);

                if (raw.status().is2xxSuccessful()) {
                    if (raw.body().length == 0) {
                        throw new ExtractionFailedException("A Anthropic devolveu uma resposta vazia");
                    }
                    return parse(raw.body());
                }

                // 4xx é erro permanente (chave inválida, payload malformado): repetir só gasta tempo.
                if (!isRetryable(raw.status())) {
                    throw new ExtractionFailedException("Falha na chamada à Anthropic (HTTP "
                            + raw.status().value() + "): " + new String(raw.body(), StandardCharsets.UTF_8));
                }
                lastFailure = new ExtractionFailedException("HTTP " + raw.status().value());
                log.warn("Tentativa {}/{} da chamada à Anthropic falhou com HTTP {}",
                        attempt, attempts, raw.status().value());
            } catch (ResourceAccessException | UncheckedIOException e) {
                lastFailure = e instanceof RuntimeException re ? re : new ExtractionFailedException(e.getMessage());
                log.warn("Tentativa {}/{} da chamada à Anthropic falhou por erro de conexão: {}",
                        attempt, attempts, e.getMessage());
            }

            if (attempt < attempts) {
                sleepBeforeRetry(attempt);
            }
        }

        throw new ExtractionFailedException("Falha ao chamar o serviço de IA após " + attempts + " tentativas: "
                + (lastFailure != null ? lastFailure.getMessage() : "erro desconhecido"));
    }

    private String serialize(ObjectNode body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar o corpo da requisição para a Anthropic", e);
        }
    }

    private JsonNode parse(byte[] raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (IOException e) {
            throw new ExtractionFailedException("A Anthropic devolveu um corpo que não é JSON válido");
        }
    }

    private String requireApiKey() {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new ExtractionFailedException("Chave da API da Anthropic não configurada. Defina a variável de "
                    + "ambiente ANTHROPIC_API_KEY (ou a propriedade dateca.ai.anthropic.api-key).");
        }
        return apiKey;
    }

    private boolean isRetryable(HttpStatusCode status) {
        return status.is5xxServerError() || status.value() == 429;
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(RETRY_BASE_DELAY_MS * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExtractionFailedException("Importação interrompida durante a espera entre tentativas");
        }
    }

    private JsonNode findToolInput(JsonNode response, String toolName) {
        for (JsonNode block : response.path("content")) {
            if ("tool_use".equals(block.path("type").asText()) && toolName.equals(block.path("name").asText())) {
                return block.path("input");
            }
        }
        return null;
    }

    private ObjectNode buildRequestBody(String toolName, String systemPrompt, String schemaJson,
                                         List<PageInput> pages, int maxTokens) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("name", toolName);
        tool.put("description", ExtractionPrompts.TOOL_DESCRIPTION);
        tool.set("input_schema", parseSchema(schemaJson));

        ArrayNode content = objectMapper.createArrayNode();
        for (PageInput page : pages) {
            content.add(page.isImage() ? imageBlock(page) : textBlock(page));
        }

        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.set("content", content);

        ObjectNode toolChoice = objectMapper.createObjectNode();
        toolChoice.put("type", "tool");
        toolChoice.put("name", toolName);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getModel());
        body.put("max_tokens", maxTokens);
        body.put("system", systemPrompt);
        body.set("messages", objectMapper.createArrayNode().add(message));
        body.set("tools", objectMapper.createArrayNode().add(tool));
        body.set("tool_choice", toolChoice);
        return body;
    }

    private ObjectNode imageBlock(PageInput page) {
        ObjectNode block = objectMapper.createObjectNode();
        block.put("type", "image");
        ObjectNode source = objectMapper.createObjectNode();
        source.put("type", "base64");
        source.put("media_type", page.imageMediaType());
        source.put("data", Base64.getEncoder().encodeToString(page.imageBytes()));
        block.set("source", source);
        return block;
    }

    private ObjectNode textBlock(PageInput page) {
        ObjectNode block = objectMapper.createObjectNode();
        block.put("type", "text");
        block.put("text", "Página " + page.pageNumber() + ":\n" + page.text());
        return block;
    }

    private ObjectNode parseSchema(String json) {
        try {
            return (ObjectNode) objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Schema JSON inválido no adapter de IA", e);
        }
    }
}
