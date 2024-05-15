package com.dat.dateca.domain.question;

import jakarta.annotation.Nullable;

import java.util.List;

public record QuestionForm(
        String statement,
        PointsEnum pointsEnum,
        Long course,
        Character correctAnswer,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE
) {
}
