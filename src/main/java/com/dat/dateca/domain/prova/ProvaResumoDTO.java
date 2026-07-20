package com.dat.dateca.domain.prova;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ProvaResumoDTO(
        UUID id,
        String titulo,
        String disciplina,
        String dificuldade,
        String capaUrl,
        String status,
        long participantes,
        boolean rankingDisponivel,
        LocalDate dataAbertura,
        LocalTime horaAbertura,
        LocalDate dataEncerramento,
        Integer maxParticipantes,
        String visibilidade,
        LocalDateTime criadaEm
) {
    public ProvaResumoDTO(Prova prova, long participantes) {
        this(
                prova.getId(),
                prova.getTitulo(),
                prova.getDisciplina(),
                prova.getDificuldade().getDescription(),
                prova.getCapaUrl(),
                prova.getStatusCalculado(),
                participantes,
                prova.isRankingDisponivel(),
                prova.getDataAbertura(),
                prova.getHoraAbertura(),
                prova.getDataEncerramento(),
                prova.getMaxParticipantes(),
                prova.getVisibilidade().name(),
                prova.getCriadaEm()
        );
    }
}
