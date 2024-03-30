package com.dat.dateca.domain.enade;

import com.dat.dateca.domain.question.PointsEnum;

import java.time.Year;

public record EnadeForm(
        Year ano,
        Integer number,
        String statement,
        PointsEnum pointsEnum,
        Character correctAnswer,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE
) {
}
