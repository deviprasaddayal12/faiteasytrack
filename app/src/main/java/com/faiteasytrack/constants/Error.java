package com.faiteasytrack.constants;

public class Error {
    public static final int ERROR_CODE_UNDEFINED = -2;

    private int errorId;
    private String errorMsg;

    public Error() {
    }

    public Error(int errorId, String errorMsg) {
        this.errorId = errorId;
        this.errorMsg = errorMsg;
    }

    public int getErrorId() {
        return errorId;
    }

    public void setErrorId(int errorId) {
        this.errorId = errorId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public enum ErrorType {
        ERROR_NOT_DEFINED(-1, "Error not defined."),
        USER_NOT_REGISTERED(0, "User is not signed in to the app."),
        CONTACT_NOT_REGISTERED(1, "Contact is not registered with our app."),
        OPERATION_WAS_CANCELLED(2, "Operation was cancelled."),
        INVALID_AUTH_CREDENTIALS(3, "Invalid credentials!"),
        AUTH_VERIFICATION_FAILED(4, "Credential verification failed!");

        private int error_id;
        private String error_msg;

        ErrorType(int error_id, String error_msg) {
            this.error_id = error_id;
            this.error_msg = error_msg;
        }
    }

    public static Error getError(int errorTypeId) {
        Error error = new Error(ErrorType.ERROR_NOT_DEFINED.error_id, ErrorType.ERROR_NOT_DEFINED.error_msg);
        for (ErrorType errorType : ErrorType.values()){
            if (errorType.error_id == errorTypeId) {
                error.setErrorId(errorType.error_id);
                error.setErrorMsg(errorType.error_msg);
            }
        }
        return error;
    }
}
