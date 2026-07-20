package com.dat.dateca.domain.prova;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ProvaDetalheDTO(
        UUID id,
        String titulo,
        String descricao,
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
        UUID criadorId,
        LocalDateTime criadaEm,
        List<ProvaQuestaoDTO> questoes
) {
    public ProvaDetalheDTO(Prova prova, long participantes) {
        this(
                prova.getId(),
                prova.getTitulo(),
                prova.getDescricao(),
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
                prova.getCriadorId(),
                prova.getCriadaEm(),
                prova.getQuestoes().stream().map(ProvaQuestaoDTO::new).toList()
        );
    }
}
