package com.faiteasytrack.exceptions;

public class UserNotFoundException extends RuntimeException {
    String message;

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
