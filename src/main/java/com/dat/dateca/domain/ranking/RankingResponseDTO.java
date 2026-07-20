package com.dat.dateca.domain.ranking;

import org.springframework.data.domain.Page;

public record RankingResponseDTO(
        CurrentUserRankingDTO currentUser,
        Page<RankingItemDTO> content
) {
}
