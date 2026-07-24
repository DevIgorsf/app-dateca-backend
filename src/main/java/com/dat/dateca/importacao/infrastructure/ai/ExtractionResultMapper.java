package com.dat.dateca.importacao.infrastructure.ai;

import com.dat.dateca.importacao.domain.ports.AlternativeExtractionResult;
import com.dat.dateca.importacao.domain.ports.CoverExtractionResult;
import com.dat.dateca.importacao.domain.ports.QuestionExtractionResult;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converte o JSON já validado contra o schema (ver {@link ExtractionPrompts}) nos records de
 * domínio. Compartilhado entre os adapters: os provedores diferem em como a chamada é feita, não
 * no formato do resultado.
 */
final class ExtractionResultMapper {

    static CoverExtractionResult toCover(JsonNode input) {
        return new CoverExtractionResult(
                textOrNull(input, "title"),
                textOrNull(input, "institution"),
                intOrNull(input, "year"),
                textOrNull(input, "edition"),
                textOrNull(input, "subjectArea"),
                input.toString());
    }

    static List<QuestionExtractionResult> toQuestions(JsonNode input) {
        List<QuestionExtractionResult> results = new ArrayList<>();

        for (JsonNode question : input.path("questions")) {
            List<AlternativeExtractionResult> alternatives = new ArrayList<>();
            for (JsonNode alternative : question.path("alternatives")) {
                String label = alternative.path("label").asText("");
                if (!label.isBlank()) {
                    alternatives.add(new AlternativeExtractionResult(
                            Character.toUpperCase(label.charAt(0)), alternative.path("text").asText("")));
                }
            }

            String correctAnswerText = question.path("correctAnswer").asText("");
            Character correctAnswer = !correctAnswerText.isBlank()
                    ? Character.toUpperCase(correctAnswerText.charAt(0))
                    : null;

            results.add(new QuestionExtractionResult(
                    question.path("number").asInt(),
                    question.path("statement").asText(""),
                    alternatives,
                    correctAnswer,
                    question.path("annulled").asBoolean(false),
                    question.path("needsReview").asBoolean(false),
                    question.path("sourcePageNumber").asInt()));
        }
        return results;
    }

    static Map<Integer, Character> toAnswerKey(JsonNode input) {
        Map<Integer, Character> result = new LinkedHashMap<>();
        for (JsonNode entry : input.path("answers")) {
            String letter = entry.path("answer").asText("");
            if (!letter.isBlank()) {
                result.put(entry.path("number").asInt(), Character.toUpperCase(letter.charAt(0)));
            }
        }
        return result;
    }

    private static String textOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) && !node.path(field).asText().isBlank() ? node.path(field).asText() : null;
    }

    private static Integer intOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.path(field).asInt() : null;
    }

    private ExtractionResultMapper() {
    }
}
