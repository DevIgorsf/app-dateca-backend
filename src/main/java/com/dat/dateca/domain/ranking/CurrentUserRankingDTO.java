package com.dat.dateca.domain.ranking;

import java.util.UUID;

public record CurrentUserRankingDTO(
        UUID userId,
        int position,
        int xp
) {
}
