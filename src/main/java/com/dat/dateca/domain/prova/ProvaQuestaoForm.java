package com.dat.dateca.domain.prova;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProvaQuestaoForm(
        @NotBlank String statement,
        @NotBlank String alternativeA,
        @NotBlank String alternativeB,
        @NotBlank String alternativeC,
        @NotBlank String alternativeD,
        @NotBlank String alternativeE,
        @NotNull Character correctAnswer,
        String comment
) {
}
