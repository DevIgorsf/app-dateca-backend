package com.dat.dateca.domain.enade;

import java.time.Year;
import java.util.List;

public record EnadeRandDTO(
        Long id,
//        List<Long> idImages,
        Year ano,
        Integer number,
        String statement,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE
) {
    public EnadeRandDTO(Enade enade) {
        this(
                enade.getId(),
//                enade.getIdImages(),
                enade.getAno(),
                enade.getNumber(),
                enade.getStatement(),
                enade.getAlternativeA(),
                enade.getAlternativeB(),
                enade.getAlternativeC(),
                enade.getAlternativeD(),
                enade.getAlternativeE()
        );
    }
}
