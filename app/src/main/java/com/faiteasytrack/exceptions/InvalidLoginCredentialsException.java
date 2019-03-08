package com.faiteasytrack.exceptions;

public class InvalidLoginCredentialsException extends RuntimeException {

    public InvalidLoginCredentialsException(String message) {
        super(message);
    }

    public InvalidLoginCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
