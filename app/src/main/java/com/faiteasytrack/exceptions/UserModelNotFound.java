package com.faiteasytrack.exceptions;

public class UserModelNotFound extends RuntimeException {
    public UserModelNotFound(String message) {
        super(message);
    }

    public UserModelNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
