package com.dat.dateca.domain.friendship;

public class CannotFriendYourselfException extends RuntimeException {
    public CannotFriendYourselfException() {
        super("Não é possível enviar uma solicitação de amizade para si mesmo");
    }
}
