package com.dat.dateca.domain.prova;

import com.dat.dateca.domain.question.PointsEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ProvaForm(
        @NotBlank String titulo,
        String descricao,
        @NotBlank String disciplina,
        @NotNull PointsEnum dificuldade,
        String capaUrl,
        @Valid List<ProvaQuestaoForm> questoes,
        LocalDate dataAbertura,
        LocalTime horaAbertura,
        LocalDate dataEncerramento,
        Integer maxParticipantes,
        @NotNull VisibilidadeProva visibilidade,
        String status
) {
}
