package com.dat.dateca.importacao.infrastructure.ai;

import com.dat.dateca.importacao.domain.ports.PageInput;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionBatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaVisionExtractionAdapterTest {

    private static final String BASE_URL = "http://ollama.test";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private OllamaProperties properties;
    private RestClient.Builder builder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        properties = new OllamaProperties();
        properties.setModel("qwen2.5:7b-instruct-q4_K_M");
        properties.setMaxRetries(1);
        properties.setMaxTokens(100);
        properties.setMaxTokensCeiling(400);

        builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
    }

    private OllamaVisionExtractionAdapter adapter() {
        return new OllamaVisionExtractionAdapter(properties, objectMapper, builder.build());
    }

    @Test
    void parsesToolArgumentsThatComeAsJsonString() {
        // No formato compatível com OpenAI, "arguments" é uma STRING JSON, não um objeto.
        String argumentsAsString = """
                {"questions":[{"number":1,"statement":"Q1","sourcePageNumber":1,
                  "alternatives":[{"label":"A","text":"a"},{"label":"B","text":"b"}],"correctAnswer":"A"}]}
                """;

        server.expect(once(), requestTo(BASE_URL + "/v1/chat/completions"))
                .andRespond(withSuccess(
                        chatResponse("tool_calls", ExtractionPrompts.QUESTIONS_TOOL, argumentsAsString),
                        MediaType.APPLICATION_JSON));

        QuestionExtractionBatch batch = adapter().extractQuestions(List.of(PageInput.ofText(1, "texto")));

        server.verify();
        assertThat(batch.truncated()).isFalse();
        assertThat(batch.questions()).hasSize(1);
        assertThat(batch.questions().get(0).statement()).isEqualTo("Q1");
        assertThat(batch.questions().get(0).correctAnswer()).isEqualTo('A');
    }

    @Test
    void reportsTruncationWhenFinishReasonIsLengthAtCeiling() {
        properties.setMaxTokens(400);
        properties.setMaxTokensCeiling(400); // sem espaço para escalar

        String argumentsAsString = """
                {"questions":[{"number":1,"statement":"Q1","sourcePageNumber":1,
                  "alternatives":[{"label":"A","text":"a"}]}]}
                """;

        server.expect(once(), requestTo(BASE_URL + "/v1/chat/completions"))
                .andRespond(withSuccess(
                        chatResponse("length", ExtractionPrompts.QUESTIONS_TOOL, argumentsAsString),
                        MediaType.APPLICATION_JSON));

        QuestionExtractionBatch batch = adapter().extractQuestions(List.of(PageInput.ofText(1, "texto")));

        server.verify();
        assertThat(batch.truncated()).isTrue();
        assertThat(batch.questions()).hasSize(1);
    }

    /** Monta a resposta no formato OpenAI, com "arguments" como string JSON. */
    private String chatResponse(String finishReason, String toolName, String argumentsJsonString) {
        ObjectNode function = objectMapper.createObjectNode();
        function.put("name", toolName);
        function.put("arguments", argumentsJsonString); // string, não objeto

        ObjectNode toolCall = objectMapper.createObjectNode();
        toolCall.set("function", function);

        ArrayNode toolCalls = objectMapper.createArrayNode().add(toolCall);

        ObjectNode message = objectMapper.createObjectNode();
        message.set("tool_calls", toolCalls);

        ObjectNode choice = objectMapper.createObjectNode();
        choice.put("finish_reason", finishReason);
        choice.set("message", message);

        ObjectNode root = objectMapper.createObjectNode();
        root.set("choices", objectMapper.createArrayNode().add(choice));
        return root.toString();
    }
}
