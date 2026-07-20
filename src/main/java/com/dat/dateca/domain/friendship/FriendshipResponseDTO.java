package com.dat.dateca.domain.friendship;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendshipResponseDTO(
        UUID id,
        UUID requesterId,
        String requesterName,
        UUID receiverId,
        String receiverName,
        FriendshipStatus status,
        LocalDateTime createdAt,
        LocalDateTime acceptedAt,
        LocalDateTime updatedAt
) {
    public FriendshipResponseDTO(Friendship friendship, String requesterName, String receiverName) {
        this(
                friendship.getId(),
                friendship.getRequesterId(),
                requesterName,
                friendship.getReceiverId(),
                receiverName,
                friendship.getStatus(),
                friendship.getCreatedAt(),
                friendship.getAcceptedAt(),
                friendship.getUpdatedAt()
        );
    }
}
