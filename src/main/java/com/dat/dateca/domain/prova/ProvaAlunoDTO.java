package com.dat.dateca.domain.prova;

import java.util.List;
import java.util.UUID;

public record ProvaAlunoDTO(
        UUID id,
        String titulo,
        String descricao,
        String disciplina,
        String dificuldade,
        String capaUrl,
        List<ProvaQuestaoAlunoDTO> questoes
) {
    public ProvaAlunoDTO(Prova prova) {
        this(
                prova.getId(),
                prova.getTitulo(),
                prova.getDescricao(),
                prova.getDisciplina(),
                prova.getDificuldade().getDescription(),
                prova.getCapaUrl(),
                prova.getQuestoes().stream().map(ProvaQuestaoAlunoDTO::new).toList()
        );
    }
}
