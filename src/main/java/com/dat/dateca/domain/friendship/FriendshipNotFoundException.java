package com.dat.dateca.domain.friendship;

import java.util.UUID;

public class FriendshipNotFoundException extends RuntimeException {
    public FriendshipNotFoundException(UUID id) {
        super("Amizade não encontrada: " + id);
    }
}
