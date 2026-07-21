package com.dat.dateca.importacao.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAlternativeDraftCommand(@NotNull Character label, @NotBlank String text) {
}
