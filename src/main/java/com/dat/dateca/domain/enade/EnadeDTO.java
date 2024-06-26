package com.dat.dateca.domain.enade;

import com.dat.dateca.domain.question.PointsEnum;
import jakarta.annotation.Nullable;

import java.time.Year;

public record EnadeDTO(
        @Nullable
        Long id,
        Integer year,
        Integer number,
        String statement,
        String pointsEnum,
        Character correctAnswer,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE
) {
    public EnadeDTO(Enade enade) {
        this(
                enade.getId(),
                enade.getYear(),
                enade.getNumber(),
                enade.getStatement(),
                enade.getPointsEnum().getDescription(),
                enade.getCorrectAnswer(),
                enade.getAlternativeA(),
                enade.getAlternativeB(),
                enade.getAlternativeC(),
                enade.getAlternativeD(),
                enade.getAlternativeE()
        );
    }
}
