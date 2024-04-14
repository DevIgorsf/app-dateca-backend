package com.dat.dateca.domain.enade;

import java.time.Year;
import java.util.List;

public record EnadeRandDTO(
        Long id,
        Integer year,
        Integer number,
        String statement,
        String alternativeA,
        String alternativeB,
        String alternativeC,
        String alternativeD,
        String alternativeE,
        List<ImageEnadeDTO> images
) {
    public EnadeRandDTO(Enade enade) {
        this(
                enade.getId(),
                enade.getYear(),
                enade.getNumber(),
                enade.getStatement(),
                enade.getAlternativeA(),
                enade.getAlternativeB(),
                enade.getAlternativeC(),
                enade.getAlternativeD(),
                enade.getAlternativeE(),
                enade.getImages().stream().map(ImageEnadeDTO::new).toList()
        );
    }
}
