package com.faiteasytrack.enums;

public class Error {

    public enum ErrorStatus {
        USER_NOT_REGISTERED(0, "User is not signed in to the app."),
        CONTACT_NOT_REGISTERED(1, "Contact is not registered with our app."),
        ERROR_NOT_DEFINED(2, "ErrorStatus not defined in class."),
        OPERATION_WAS_CANCELLED(3, "Operation was cancelled."),
        INVALID_AUTH_CREDENTIALS(4, "Invalid credentials!"),
        AUTH_VERIFICATION_FAILED(5, "Credential verification failed!");

        private int error_id;
        private String error_msg;

        ErrorStatus(int error_id, String error_msg) {
            this.error_id = error_id;
            this.error_msg = error_msg;
        }

        public String getError_msg(int error_id) {
            for (ErrorStatus error : values()){
                if (error.error_id == error_id)
                    return error.error_msg;
            }
            return error_msg;
        }
    }
}
