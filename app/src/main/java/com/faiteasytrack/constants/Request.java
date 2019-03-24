package com.faiteasytrack.constants;

import com.faiteasytrack.models.RequestStatusModel;

public class Request {

    public static final int REQUEST_NOT_YET = -1;
    public static final int REQUEST_CREATED = 0;
    public static final int REQUEST_SENT = 2;
    public static final int REQUEST_SEND_FAILED = 3;
    public static final int REQUEST_UNSEEN = 6;
    public static final int REQUEST_ACCEPTED = 7;
    public static final int REQUEST_DENIED = 8;

    public enum Status {
        REQUEST_NOT_YET(Request.REQUEST_NOT_YET, "Request"),
        REQUEST_CREATED(Request.REQUEST_CREATED, "Request created"),
        REQUEST_SENT(Request.REQUEST_SENT, "Requested"),
        REQUEST_SEND_FAILED(Request.REQUEST_SEND_FAILED, "Requesting failed"),
        REQUEST_UNSEEN(Request.REQUEST_UNSEEN, "Approval Pending"),
        REQUEST_ACCEPTED(Request.REQUEST_ACCEPTED, "Friends Now"),
        REQUEST_DENIED(Request.REQUEST_DENIED, "Unwilling to Accept");

        private int status;
        private String title;

        public static RequestStatusModel getRequestStatusModel(int status){
            RequestStatusModel requestStatusModel = new RequestStatusModel(REQUEST_NOT_YET.status, REQUEST_NOT_YET.title);
            for (Status requestStatus : values()){
                if (requestStatus.status == status){
                    requestStatusModel.setStatus(requestStatus.status);
                    requestStatusModel.setTitle(requestStatus.title);
                }
            }
            return requestStatusModel;
        }

        Status(int status, String title) {
            this.status = status;
            this.title = title;
        }
    }
}
