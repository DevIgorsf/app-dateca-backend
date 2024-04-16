package com.dat.dateca.domain.enade;

import com.dat.dateca.domain.question.PointsEnum;
import jakarta.annotation.Nullable;

public record EnadeAllDTO(
        @Nullable
        Long id,
        Integer year,
        Integer number,
        String statement,
        String pointsEnum
) {
    public EnadeAllDTO(Enade enade) {
        this(
                enade.getId(),
                enade.getYear(),
                enade.getNumber(),
                enade.getStatement(),
                enade.getPointsEnum().getDescription()
        );
    }
}
