package com.dat.dateca.importacao.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateQuestionDraftCommand(@NotNull Integer number, @NotBlank String statement, boolean annulled,
                                          Character correctAlternativeLabel, Integer sourcePageNumber,
                                          @NotNull @Valid List<UpdateAlternativeDraftCommand> alternatives) {
}
