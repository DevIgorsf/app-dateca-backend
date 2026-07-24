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
import org.springframework.web.client.RestClientResponseException;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Implementação local de {@link VisionExtractionPort} contra o Ollama, pela API compatível com o
 * formato OpenAI ({@code /v1/chat/completions}). Reproduz as mesmas três chamadas e os mesmos
 * schemas do adapter da Anthropic (ver {@link ExtractionPrompts}), usando tool calling para forçar
 * a saída estruturada — mantendo o princípio de nunca parsear prosa.
 *
 * <p>Uso pretendido como provedor de desenvolvimento/MVP para o caminho de <em>texto nativo</em>. O
 * caminho de páginas rasterizadas (provas escaneadas) fica no adapter da Anthropic via
 * {@link RoutingVisionExtractionAdapter}, por ser mais exigente em qualidade de visão.
 */
public class OllamaVisionExtractionAdapter implements VisionExtractionPort {

    private static final Logger log = LoggerFactory.getLogger(OllamaVisionExtractionAdapter.class);

    private static final String CHAT_URI = "/v1/chat/completions";
    private static final String FINISH_REASON_LENGTH = "length";
    private static final long RETRY_BASE_DELAY_MS = 500L;

    private final RestClient restClient;
    private final OllamaProperties properties;
    private final ObjectMapper objectMapper;

    public OllamaVisionExtractionAdapter(OllamaProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, RestClient.builder()
                .requestFactory(RestClientFactories.withTimeouts(properties.getConnectTimeout(), properties.getReadTimeout()))
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("content-type", "application/json")
                .build());
    }

    // Visível para testes: permite injetar um RestClient ligado a um MockRestServiceServer.
    OllamaVisionExtractionAdapter(OllamaProperties properties, ObjectMapper objectMapper, RestClient restClient) {
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

    private ToolCallOutcome callTool(String toolName, String systemPrompt, String schemaJson, List<PageInput> pages) {
        int maxTokens = Math.max(1, properties.getMaxTokens());
        int ceiling = Math.max(maxTokens, properties.getMaxTokensCeiling());

        while (true) {
            JsonNode response = post(buildRequestBody(toolName, systemPrompt, schemaJson, pages, maxTokens));
            JsonNode choice = response.path("choices").path(0);
            JsonNode input = findToolArguments(choice, toolName);

            if (!FINISH_REASON_LENGTH.equals(choice.path("finish_reason").asText(""))) {
                if (input == null) {
                    throw new ExtractionFailedException(
                            "O Ollama não retornou o resultado estruturado esperado para " + toolName);
                }
                return new ToolCallOutcome(input, false);
            }

            if (maxTokens >= ceiling) {
                log.warn("Resposta de {} truncada mesmo com num_predict={} (teto configurado); "
                        + "o bloco será reprocessado em partes menores", toolName, maxTokens);
                return new ToolCallOutcome(input != null ? input : objectMapper.createObjectNode(), true);
            }

            int escalated = Math.min(maxTokens * 2, ceiling);
            log.warn("Resposta de {} truncada com num_predict={}; repetindo o bloco com num_predict={}",
                    toolName, maxTokens, escalated);
            maxTokens = escalated;
        }
    }

    private JsonNode post(ObjectNode body) {
        int attempts = Math.max(1, properties.getMaxRetries() + 1);
        String payload = serialize(body);
        RuntimeException lastFailure = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                // Serializa/parseia com o ObjectMapper (Jackson 2) em vez dos conversores do
                // RestClient — o Spring 7 pode preferir Jackson 3 e falhar ao mapear JsonNode.
                String raw = restClient.post()
                        .uri(CHAT_URI)
                        .body(payload)
                        .retrieve()
                        .body(String.class);
                if (raw == null || raw.isBlank()) {
                    throw new ExtractionFailedException("O Ollama devolveu uma resposta vazia");
                }
                return parse(raw);
            } catch (RestClientResponseException e) {
                if (!isRetryable(e.getStatusCode())) {
                    throw new ExtractionFailedException("Falha na chamada ao Ollama (HTTP "
                            + e.getStatusCode().value() + "): " + e.getResponseBodyAsString());
                }
                lastFailure = e;
                log.warn("Tentativa {}/{} da chamada ao Ollama falhou com HTTP {}",
                        attempt, attempts, e.getStatusCode().value());
            } catch (ResourceAccessException e) {
                lastFailure = e;
                log.warn("Tentativa {}/{} da chamada ao Ollama falhou por erro de conexão/timeout: {}",
                        attempt, attempts, e.getMessage());
            }

            if (attempt < attempts) {
                sleepBeforeRetry(attempt);
            }
        }

        throw new ExtractionFailedException("Falha ao chamar o Ollama após " + attempts + " tentativas: "
                + (lastFailure != null ? lastFailure.getMessage() : "erro desconhecido")
                + ". Verifique se o serviço está no ar em " + properties.getBaseUrl()
                + " e se o modelo '" + properties.getModel() + "' foi baixado (ollama pull).");
    }

    private String serialize(ObjectNode body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar o corpo da requisição para o Ollama", e);
        }
    }

    private JsonNode parse(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            throw new ExtractionFailedException("O Ollama devolveu um corpo que não é JSON válido");
        }
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

    /**
     * No formato compatível com OpenAI, {@code arguments} é uma <em>string</em> JSON (não um
     * objeto), então precisa ser parseada. É aqui que o "nunca parsear prosa" se mantém: só
     * lemos o objeto que o modelo produziu contra o schema da ferramenta.
     */
    private JsonNode findToolArguments(JsonNode choice, String toolName) {
        for (JsonNode toolCall : choice.path("message").path("tool_calls")) {
            JsonNode function = toolCall.path("function");
            if (toolName.equals(function.path("name").asText())) {
                JsonNode arguments = function.path("arguments");
                if (arguments.isObject()) {
                    return arguments;
                }
                String raw = arguments.asText("");
                if (raw.isBlank()) {
                    return null;
                }
                try {
                    return objectMapper.readTree(raw);
                } catch (JsonProcessingException e) {
                    throw new ExtractionFailedException(
                            "O Ollama retornou argumentos de ferramenta que não são JSON válido para " + toolName);
                }
            }
        }
        return null;
    }

    private ObjectNode buildRequestBody(String toolName, String systemPrompt, String schemaJson,
                                         List<PageInput> pages, int maxTokens) {
        ObjectNode function = objectMapper.createObjectNode();
        function.put("name", toolName);
        function.put("description", ExtractionPrompts.TOOL_DESCRIPTION);
        function.set("parameters", parseSchema(schemaJson));

        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("type", "function");
        tool.set("function", function);

        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.set("content", buildUserContent(pages));

        ObjectNode toolChoice = objectMapper.createObjectNode();
        toolChoice.put("type", "function");
        ObjectNode toolChoiceFunction = objectMapper.createObjectNode();
        toolChoiceFunction.put("name", toolName);
        toolChoice.set("function", toolChoiceFunction);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getModel());
        body.put("max_tokens", maxTokens);
        body.set("messages", objectMapper.createArrayNode().add(systemMessage).add(userMessage));
        body.set("tools", objectMapper.createArrayNode().add(tool));
        body.set("tool_choice", toolChoice);
        return body;
    }

    /**
     * Conteúdo da mensagem do usuário. Páginas de texto viram blocos de texto; páginas de imagem
     * viram {@code image_url} com data URI base64 (formato compatível com OpenAI). O caminho de
     * imagem só funciona com um modelo de visão configurado — o roteamento padrão manda imagens
     * para a Anthropic, mas o suporte fica aqui para quem quiser um modelo local multimodal.
     */
    private ArrayNode buildUserContent(List<PageInput> pages) {
        ArrayNode content = objectMapper.createArrayNode();
        for (PageInput page : pages) {
            if (page.isImage()) {
                content.add(imageBlock(page));
            } else {
                content.add(textBlock(page));
            }
        }
        return content;
    }

    private ObjectNode imageBlock(PageInput page) {
        ObjectNode imageUrl = objectMapper.createObjectNode();
        imageUrl.put("url", "data:" + page.imageMediaType() + ";base64,"
                + Base64.getEncoder().encodeToString(page.imageBytes()));

        ObjectNode block = objectMapper.createObjectNode();
        block.put("type", "image_url");
        block.set("image_url", imageUrl);
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
