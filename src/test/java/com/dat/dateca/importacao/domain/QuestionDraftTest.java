package com.dat.dateca.importacao.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionDraftTest {

    @Test
    void completenessScoreGrowsWithStatementAlternativesAndImages() {
        QuestionDraft bare = new QuestionDraft(1, "Enunciado curto", false, null, 1, false, 0);

        QuestionDraft complete = new QuestionDraft(1, "Enunciado bem mais completo e detalhado", false, 'A', 1, false, 0);
        complete.addAlternative(new AlternativeDraft('A', "Alternativa A", 0));
        complete.addAlternative(new AlternativeDraft('B', "Alternativa B", 1));
        complete.addImage(new QuestionImageDraft("img-key", "image/png", 0));

        assertThat(complete.completenessScore()).isGreaterThan(bare.completenessScore());
    }

    @Test
    void mergeFromReplacesStatementAnswerAndAlternatives() {
        QuestionDraft target = new QuestionDraft(5, "Versão incompleta", false, null, 3, true, 0);
        target.addAlternative(new AlternativeDraft('A', "Só uma alternativa", 0));

        QuestionDraft moreComplete = new QuestionDraft(5, "Versão completa do enunciado", false, 'B', 3, false, 0);
        moreComplete.addAlternative(new AlternativeDraft('A', "Primeira", 0));
        moreComplete.addAlternative(new AlternativeDraft('B', "Segunda", 1));

        target.mergeFrom(moreComplete);

        assertThat(target.getStatement()).isEqualTo("Versão completa do enunciado");
        assertThat(target.getCorrectAlternativeLabel()).isEqualTo('B');
        assertThat(target.isNeedsReview()).isFalse();
        assertThat(target.getAlternatives()).hasSize(2);
        assertThat(target.getAlternatives().get(1).getAlternativeText()).isEqualTo("Segunda");
    }

    @Test
    void applyExternalAnswerFillsMissingAnswerWithoutFlaggingReview() {
        QuestionDraft question = new QuestionDraft(7, "Enunciado", false, null, 1, false, 0);

        question.applyExternalAnswer('C');

        assertThat(question.getCorrectAlternativeLabel()).isEqualTo('C');
        assertThat(question.isNeedsReview()).isFalse();
    }

    @Test
    void applyExternalAnswerFlagsReviewWhenAnswerDivergesFromExisting() {
        QuestionDraft question = new QuestionDraft(7, "Enunciado", false, 'A', 1, false, 0);

        question.applyExternalAnswer('D');

        assertThat(question.getCorrectAlternativeLabel()).isEqualTo('A');
        assertThat(question.isNeedsReview()).isTrue();
    }

    @Test
    void reorderUpdatesOrdem() {
        QuestionDraft question = new QuestionDraft(1, "Enunciado", false, null, 1, false, 9);

        question.reorder(3);

        assertThat(question.getOrdem()).isEqualTo(3);
    }
}
