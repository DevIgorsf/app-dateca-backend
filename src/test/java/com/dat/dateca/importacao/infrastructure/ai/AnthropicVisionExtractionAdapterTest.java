package com.dat.dateca.importacao.infrastructure.ai;

import com.dat.dateca.importacao.domain.exceptions.ExtractionFailedException;
import com.dat.dateca.importacao.domain.ports.PageInput;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionBatch;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AnthropicVisionExtractionAdapterTest {

    private static final String BASE_URL = "http://anthropic.test";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AnthropicProperties properties;
    private RestClient.Builder builder;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        properties = new AnthropicProperties();
        properties.setApiKey("test-key");
        properties.setMaxRetries(2);
        properties.setMaxTokens(100);
        properties.setMaxTokensCeiling(400);

        builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
    }

    private AnthropicVisionExtractionAdapter adapter() {
        return new AnthropicVisionExtractionAdapter(properties, objectMapper, builder.build());
    }

    @Test
    void escalatesMaxTokensWhenResponseIsTruncatedAndReturnsCompleteBatch() {
        // 1ª resposta: cortada por max_tokens, só a questão 1 coube
        server.expect(once(), requestTo(BASE_URL + "/v1/messages"))
                .andExpect(header("x-api-key", "test-key"))
                .andRespond(withSuccess(toolResponse("max_tokens", """
                        {"questions":[{"number":1,"statement":"Q1","sourcePageNumber":1,
                          "alternatives":[{"label":"A","text":"a"},{"label":"B","text":"b"}]}]}
                        """), MediaType.APPLICATION_JSON));

        // 2ª resposta (após escalar max_tokens): completa, com as duas questões
        server.expect(once(), requestTo(BASE_URL + "/v1/messages"))
                .andRespond(withSuccess(toolResponse("end_turn", """
                        {"questions":[
                          {"number":1,"statement":"Q1","sourcePageNumber":1,
                            "alternatives":[{"label":"A","text":"a"},{"label":"B","text":"b"}]},
                          {"number":2,"statement":"Q2","sourcePageNumber":2,
                            "alternatives":[{"label":"A","text":"a"},{"label":"B","text":"b"}]}]}
                        """), MediaType.APPLICATION_JSON));

        QuestionExtractionBatch batch = adapter().extractQuestions(List.of(PageInput.ofText(1, "texto")));

        server.verify();
        assertThat(batch.truncated()).isFalse();
        assertThat(batch.questions()).hasSize(2);
    }

    @Test
    void reportsTruncatedWhenCeilingReachedWithoutCompleting() {
        properties.setMaxTokens(400);
        properties.setMaxTokensCeiling(400); // sem espaço para escalar

        server.expect(once(), requestTo(BASE_URL + "/v1/messages"))
                .andRespond(withSuccess(toolResponse("max_tokens", """
                        {"questions":[{"number":1,"statement":"Q1","sourcePageNumber":1,
                          "alternatives":[{"label":"A","text":"a"}]}]}
                        """), MediaType.APPLICATION_JSON));

        QuestionExtractionBatch batch = adapter().extractQuestions(List.of(PageInput.ofText(1, "texto")));

        server.verify();
        assertThat(batch.truncated()).isTrue();
        assertThat(batch.questions()).hasSize(1);
    }

    @Test
    void doesNotRetryOn4xx() {
        // 401 é permanente (chave inválida): deve falhar na 1ª tentativa, sem repetir.
        server.expect(once(), requestTo(BASE_URL + "/v1/messages"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("{\"error\":\"x-api-key\"}"));

        assertThatThrownBy(() -> adapter().extractCover(List.of(PageInput.ofText(1, "texto"))))
                .isInstanceOf(ExtractionFailedException.class)
                .hasMessageContaining("401");

        server.verify();
    }

    @Test
    void failsFastWithClearMessageWhenApiKeyMissing() {
        properties.setApiKey("");

        assertThatThrownBy(() -> adapter().extractCover(List.of(PageInput.ofText(1, "texto"))))
                .isInstanceOf(ExtractionFailedException.class)
                .hasMessageContaining("ANTHROPIC_API_KEY");
    }

    private String toolResponse(String stopReason, String inputJson) {
        return """
                {"stop_reason":"%s","content":[
                  {"type":"tool_use","name":"%s","input":%s}]}
                """.formatted(stopReason, extractToolName(inputJson), inputJson);
    }

    // A ferramenta muda conforme o schema: questões vs capa. Detecta pelo conteúdo do JSON de teste.
    private String extractToolName(String inputJson) {
        return inputJson.contains("questions") ? ExtractionPrompts.QUESTIONS_TOOL : ExtractionPrompts.COVER_TOOL;
    }
}
