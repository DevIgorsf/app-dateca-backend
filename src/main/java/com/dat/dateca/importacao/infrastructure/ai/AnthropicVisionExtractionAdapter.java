package com.dat.dateca.importacao.infrastructure.ai;

import com.dat.dateca.importacao.domain.exceptions.ExtractionFailedException;
import com.dat.dateca.importacao.domain.ports.AlternativeExtractionResult;
import com.dat.dateca.importacao.domain.ports.CoverExtractionResult;
import com.dat.dateca.importacao.domain.ports.PageInput;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionResult;
import com.dat.dateca.importacao.domain.ports.VisionExtractionPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extração estruturada via modelo de IA com visão (Anthropic Messages API + tool-use com schema
 * forçado). Páginas sem texto nativo confiável (ou com layout ambíguo) chegam aqui como imagem
 * rasterizada em vez de texto — a mesma chamada faz OCR e estruturação, sem Tesseract.
 */
@Component
public class AnthropicVisionExtractionAdapter implements VisionExtractionPort {

    private static final String COVER_TOOL = "extrair_capa_prova";
    private static final String QUESTIONS_TOOL = "extrair_questoes_prova";
    private static final String ANSWER_KEY_TOOL = "extrair_gabarito_prova";

    private static final String COVER_SYSTEM_PROMPT = """
            Você recebe as primeiras páginas de uma prova (texto ou imagem da página escaneada).
            Extraia apenas os metadados da capa/cabeçalho: título da prova, instituição/banca
            responsável, ano, edição (ex.: "1º semestre", "caderno 1") e área/disciplina, caso a
            prova seja de uma única área. Não invente informações que não estejam no documento;
            deixe o campo vazio quando não encontrar.
            """;

    private static final String QUESTION_SYSTEM_PROMPT = """
            Você recebe um bloco de páginas consecutivas de uma prova de múltipla escolha (texto
            nativo ou imagem da página escaneada). Extraia CADA questão completa encontrada nesse
            bloco: número da questão, enunciado completo, alternativas (rótulo de uma letra e
            texto), e a resposta correta quando o gabarito aparecer junto à questão. Se a questão
            estiver marcada como anulada (ex.: carimbo "ANULADA"), marque annulled=true e deixe
            correctAnswer vazio. Se não tiver certeza da transcrição (ex.: fórmula complexa,
            imagem de difícil leitura), marque needsReview=true. Não invente alternativas nem
            texto que não esteja no documento. Ignore questões que apareçam apenas parcialmente
            no início ou no fim do bloco caso o enunciado esteja cortado sem alternativas visíveis.
            """;

    private static final String ANSWER_KEY_SYSTEM_PROMPT = """
            Você recebe a(s) página(s) de gabarito de uma prova (tabela de número da questão para
            letra da alternativa correta). Extraia todos os pares (número, letra) que encontrar.
            """;

    private static final String COVER_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "title": {"type": "string"},
                "institution": {"type": "string"},
                "year": {"type": "integer"},
                "edition": {"type": "string"},
                "subjectArea": {"type": "string"}
              },
              "required": ["title"]
            }
            """;

    private static final String QUESTIONS_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "questions": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "number": {"type": "integer"},
                      "statement": {"type": "string"},
                      "sourcePageNumber": {"type": "integer"},
                      "annulled": {"type": "boolean"},
                      "needsReview": {"type": "boolean"},
                      "correctAnswer": {"type": "string"},
                      "alternatives": {
                        "type": "array",
                        "items": {
                          "type": "object",
                          "properties": {
                            "label": {"type": "string"},
                            "text": {"type": "string"}
                          },
                          "required": ["label", "text"]
                        }
                      }
                    },
                    "required": ["number", "statement", "alternatives", "sourcePageNumber"]
                  }
                }
              },
              "required": ["questions"]
            }
            """;

    private static final String ANSWER_KEY_SCHEMA = """
            {
              "type": "object",
              "properties": {
                "answers": {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "number": {"type": "integer"},
                      "answer": {"type": "string"}
                    },
                    "required": ["number", "answer"]
                  }
                }
              },
              "required": ["answers"]
            }
            """;

    private final RestClient restClient;
    private final AnthropicProperties properties;
    private final ObjectMapper objectMapper;

    public AnthropicVisionExtractionAdapter(AnthropicProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("x-api-key", properties.getApiKey())
                .defaultHeader("anthropic-version", properties.getVersion())
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public CoverExtractionResult extractCover(List<PageInput> coverPages) {
        JsonNode input = callTool(COVER_TOOL, COVER_SYSTEM_PROMPT, COVER_SCHEMA, coverPages);
        return new CoverExtractionResult(
                textOrNull(input, "title"), textOrNull(input, "institution"), intOrNull(input, "year"),
                textOrNull(input, "edition"), textOrNull(input, "subjectArea"), input.toString());
    }

    @Override
    public List<QuestionExtractionResult> extractQuestions(List<PageInput> chunkPages) {
        JsonNode input = callTool(QUESTIONS_TOOL, QUESTION_SYSTEM_PROMPT, QUESTIONS_SCHEMA, chunkPages);
        List<QuestionExtractionResult> results = new ArrayList<>();

        for (JsonNode q : input.path("questions")) {
            List<AlternativeExtractionResult> alternatives = new ArrayList<>();
            for (JsonNode a : q.path("alternatives")) {
                String label = a.path("label").asText();
                if (!label.isBlank()) {
                    alternatives.add(new AlternativeExtractionResult(Character.toUpperCase(label.charAt(0)), a.path("text").asText()));
                }
            }

            String correctAnswerText = q.path("correctAnswer").asText("");
            Character correctAnswer = !correctAnswerText.isBlank() ? Character.toUpperCase(correctAnswerText.charAt(0)) : null;

            results.add(new QuestionExtractionResult(
                    q.path("number").asInt(), q.path("statement").asText(), alternatives, correctAnswer,
                    q.path("annulled").asBoolean(false), q.path("needsReview").asBoolean(false),
                    q.path("sourcePageNumber").asInt()));
        }
        return results;
    }

    @Override
    public Map<Integer, Character> extractAnswerKey(List<PageInput> answerKeyPages) {
        JsonNode input = callTool(ANSWER_KEY_TOOL, ANSWER_KEY_SYSTEM_PROMPT, ANSWER_KEY_SCHEMA, answerKeyPages);
        Map<Integer, Character> result = new LinkedHashMap<>();
        for (JsonNode entry : input.path("answers")) {
            String letter = entry.path("answer").asText("");
            if (!letter.isBlank()) {
                result.put(entry.path("number").asInt(), Character.toUpperCase(letter.charAt(0)));
            }
        }
        return result;
    }

    private JsonNode callTool(String toolName, String systemPrompt, String schemaJson, List<PageInput> pages) {
        ObjectNode body = buildRequestBody(toolName, systemPrompt, schemaJson, pages);

        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= properties.getMaxRetries() + 1; attempt++) {
            try {
                JsonNode response = restClient.post()
                        .uri("/v1/messages")
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);

                for (JsonNode block : response.path("content")) {
                    if ("tool_use".equals(block.path("type").asText()) && toolName.equals(block.path("name").asText())) {
                        return block.path("input");
                    }
                }
                throw new ExtractionFailedException("A IA não retornou o resultado estruturado esperado para " + toolName);
            } catch (RuntimeException e) {
                lastFailure = e;
            }
        }
        throw new ExtractionFailedException("Falha ao chamar o serviço de IA (" + toolName + ") após tentativas: "
                + (lastFailure != null ? lastFailure.getMessage() : "erro desconhecido"));
    }

    private ObjectNode buildRequestBody(String toolName, String systemPrompt, String schemaJson, List<PageInput> pages) {
        ObjectNode tool = objectMapper.createObjectNode();
        tool.put("name", toolName);
        tool.put("description", "Retorna os dados extraídos em formato estruturado.");
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
        body.put("max_tokens", 4096);
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

    private String textOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) && !node.path(field).asText().isBlank() ? node.path(field).asText() : null;
    }

    private Integer intOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.path(field).asInt() : null;
    }
}
