package com.dat.dateca.domain.enade;

import jakarta.annotation.Nullable;

import java.util.List;

public record EnadeWithImageDTO(
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
        String alternativeE,

        List<ImageEnadeDTO> images
) {
    public EnadeWithImageDTO(Enade enade) {
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
                enade.getAlternativeE(),
                enade.getImages().stream().map(ImageEnadeDTO::new).toList()
        );
    }
}

