package com.dat.dateca.domain.friendship;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FriendshipRequestDTO(
        @NotNull(message = "receiverId é obrigatório")
        UUID receiverId
) {
}
