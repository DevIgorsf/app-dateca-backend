package com.dat.dateca.domain.prova;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ProvaAdminDetalheDTO(
        UUID id,
        String titulo,
        String descricao,
        String disciplina,
        String dificuldade,
        String capaUrl,
        String status,
        boolean publicada,
        String visibilidade,
        long participantes,
        boolean rankingDisponivel,
        LocalDate dataAbertura,
        LocalTime horaAbertura,
        LocalDate dataEncerramento,
        Integer maxParticipantes,
        UUID criadorId,
        String criadorNome,
        LocalDateTime criadaEm,
        List<ProvaQuestaoDTO> questoes
) {
    public ProvaAdminDetalheDTO(Prova prova, long participantes, String criadorNome) {
        this(
                prova.getId(),
                prova.getTitulo(),
                prova.getDescricao(),
                prova.getDisciplina(),
                prova.getDificuldade().getDescription(),
                prova.getCapaUrl(),
                prova.getStatusCalculado(),
                prova.isPublicada(),
                prova.getVisibilidade().name(),
                participantes,
                prova.isRankingDisponivel(),
                prova.getDataAbertura(),
                prova.getHoraAbertura(),
                prova.getDataEncerramento(),
                prova.getMaxParticipantes(),
                prova.getCriadorId(),
                criadorNome,
                prova.getCriadaEm(),
                prova.getQuestoes().stream().map(ProvaQuestaoDTO::new).toList()
        );
    }
}
