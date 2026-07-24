package com.dat.dateca.domain.prova;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProvaAdminResumoDTO(
        UUID id,
        String titulo,
        String disciplina,
        String dificuldade,
        String status,
        boolean publicada,
        String visibilidade,
        long participantes,
        int totalQuestoes,
        UUID criadorId,
        String criadorNome,
        LocalDate dataAbertura,
        LocalDate dataEncerramento,
        LocalDateTime criadaEm
) {
    public ProvaAdminResumoDTO(Prova prova, long participantes, String criadorNome) {
        this(
                prova.getId(),
                prova.getTitulo(),
                prova.getDisciplina(),
                prova.getDificuldade().getDescription(),
                prova.getStatusCalculado(),
                prova.isPublicada(),
                prova.getVisibilidade().name(),
                participantes,
                prova.getQuestoes().size(),
                prova.getCriadorId(),
                criadorNome,
                prova.getDataAbertura(),
                prova.getDataEncerramento(),
                prova.getCriadaEm()
        );
    }
}
