package com.dat.dateca.domain.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentCadastro(
        @NotNull(message = "Número de matrícula é obrigatório")
        Long registrationNumber,
        @NotBlank(message = "Nome é obrigatório")
        String name,
        @NotBlank(message = "Telefone é obrigatório")
        String phone,
        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Formato de email é obrigatório")
        String email
) {
}
