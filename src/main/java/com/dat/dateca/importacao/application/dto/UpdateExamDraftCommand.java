package com.dat.dateca.importacao.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateExamDraftCommand(@NotBlank String title, String institution, Integer year, String edition,
                                      String subjectArea, @NotNull @Valid List<UpdateQuestionDraftCommand> questions) {
}
