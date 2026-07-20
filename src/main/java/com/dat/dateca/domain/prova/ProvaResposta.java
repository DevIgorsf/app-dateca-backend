package com.dat.dateca.domain.prova;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProvaResposta {

    @Column(name = "prova_questao_id", nullable = false)
    private UUID provaQuestaoId;

    private Character respostaEscolhida;
}
