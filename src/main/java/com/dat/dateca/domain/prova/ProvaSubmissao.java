package com.dat.dateca.domain.prova;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProvaSubmissao {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prova_id", nullable = false)
    private Prova prova;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @ElementCollection
    @CollectionTable(name = "prova_submissao_respostas", joinColumns = @JoinColumn(name = "submissao_id"))
    private List<ProvaResposta> respostas;

    private int pontuacao;

    private int acertos;

    @Column(name = "respondido_em", nullable = false)
    private LocalDateTime respondidoEm;

    public ProvaSubmissao(Prova prova, UUID studentId, List<ProvaResposta> respostas, int pontuacao, int acertos) {
        this.prova = prova;
        this.studentId = studentId;
        this.respostas = respostas;
        this.pontuacao = pontuacao;
        this.acertos = acertos;
        this.respondidoEm = LocalDateTime.now();
    }
}
