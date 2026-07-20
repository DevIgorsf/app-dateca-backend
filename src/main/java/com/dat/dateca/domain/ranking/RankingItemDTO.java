package com.dat.dateca.domain.ranking;

import java.util.UUID;

public record RankingItemDTO(
        int position,
        UUID userId,
        String name,
        int xp
) {
}
