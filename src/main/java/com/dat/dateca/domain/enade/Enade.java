package com.dat.dateca.domain.enade;

import com.dat.dateca.domain.question.PointsEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Enade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;

    private Integer number;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String statement;

    @Enumerated(EnumType.STRING)
    private PointsEnum pointsEnum;

    private Character correctAnswer;

    private String alternativeA;

    private String alternativeB;

    private String alternativeC;

    private String alternativeD;

    private String alternativeE;

    @OneToMany(mappedBy = "enade", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ImageEnade> images;

    public Enade(EnadeForm enadeForm) {
        this.year = enadeForm.year();
        this.number = enadeForm.number();
        this.statement = enadeForm.statement();
        this.pointsEnum = enadeForm.pointsEnum();
        this.correctAnswer = enadeForm.correctAnswer();
        this.alternativeA = enadeForm.alternativeA();
        this.alternativeB = enadeForm.alternativeB();
        this.alternativeC = enadeForm.alternativeC();
        this.alternativeD = enadeForm.alternativeD();
        this.alternativeE = enadeForm.alternativeE();
    }

    public Enade(int year, int number, String statement, PointsEnum pointsEnum, Character correctAnswer, String alternativeA, String alternativeB, String alternativeC, String alternativeD, String alternativeE) {
        this.year = year;
        this.number = number;
        this.statement = statement;
        this.pointsEnum = pointsEnum;
        this.correctAnswer = correctAnswer;
        this.alternativeA = alternativeA;
        this.alternativeB = alternativeB;
        this.alternativeC = alternativeC;
        this.alternativeD = alternativeD;
        this.alternativeE = alternativeE;
    }

    public void updateEnade(EnadeForm enadeForm) {
        this.year = enadeForm.year();
        this.number = enadeForm.number();
        this.statement = enadeForm.statement();
        this.pointsEnum = enadeForm.pointsEnum();
        this.correctAnswer = enadeForm.correctAnswer();
        this.alternativeA = enadeForm.alternativeA();
        this.alternativeB = enadeForm.alternativeB();
        this.alternativeC = enadeForm.alternativeC();
        this.alternativeD = enadeForm.alternativeD();
        this.alternativeE = enadeForm.alternativeE();
    }
}
