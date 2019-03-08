package com.faiteasytrack.exceptions;

public class EasyTrackDatabaseError extends RuntimeException {

    public EasyTrackDatabaseError(String message) {
        super(message);
    }

    public EasyTrackDatabaseError(String message, Throwable cause) {
        super(message, cause);
    }
}
