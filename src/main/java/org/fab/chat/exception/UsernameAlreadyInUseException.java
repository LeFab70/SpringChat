package org.fab.chat.exception;

public class UsernameAlreadyInUseException extends RuntimeException {

    public UsernameAlreadyInUseException(String username) {
        super("Le pseudo '" + username + "' est déjà utilisé.");
    }

}
