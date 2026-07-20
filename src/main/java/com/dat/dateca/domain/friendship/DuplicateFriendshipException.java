package com.dat.dateca.domain.friendship;

public class DuplicateFriendshipException extends RuntimeException {
    public DuplicateFriendshipException(String message) {
        super(message);
    }
}
