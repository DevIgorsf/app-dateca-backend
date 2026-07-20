package com.dat.dateca.domain.prova;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProvaQuestao {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prova_id", nullable = false)
    private Prova prova;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String statement;

    private String alternativeA;

    private String alternativeB;

    private String alternativeC;

    private String alternativeD;

    private String alternativeE;

    private Character correctAnswer;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String comment;

    private int ordem;

    public ProvaQuestao(Prova prova, ProvaQuestaoForm form, int ordem) {
        this.prova = prova;
        this.statement = form.statement();
        this.alternativeA = form.alternativeA();
        this.alternativeB = form.alternativeB();
        this.alternativeC = form.alternativeC();
        this.alternativeD = form.alternativeD();
        this.alternativeE = form.alternativeE();
        this.correctAnswer = form.correctAnswer();
        this.comment = form.comment();
        this.ordem = ordem;
    }
}
