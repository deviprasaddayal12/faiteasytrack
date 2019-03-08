package com.faiteasytrack.exceptions;

public class UserLogInRequired extends RuntimeException {

    public UserLogInRequired(String message) {
        super(message);
    }

    public UserLogInRequired(String message, Throwable cause) {
        super(message, cause);
    }
}
