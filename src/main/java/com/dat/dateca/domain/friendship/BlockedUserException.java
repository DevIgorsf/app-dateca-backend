package com.dat.dateca.domain.friendship;

public class BlockedUserException extends RuntimeException {
    public BlockedUserException() {
        super("Esta interação não é permitida pois há um bloqueio entre os usuários");
    }
}
