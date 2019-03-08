package com.faiteasytrack.models;

public class RequestModel {

    private boolean isHeader;
    private String titleHeader;

    private String fromId;
    private String toId;
    private boolean isRequestedByMe;
    private String requesteeName;
    private String requesteePhone;
    private String requesteeProfilePicUrl;
    private String message;
    private long requestedAtMillis;
    private int priority;
    private RequestStatusModel requestStatusModel;
    private RequestStatusModel lastStatusUpdated;

    public RequestModel() {
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public String getTitleHeader() {
        return titleHeader;
    }

    public void setTitleHeader(String titleHeader) {
        this.titleHeader = titleHeader;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public boolean isRequestedByMe() {
        return isRequestedByMe;
    }

    public void setRequestedByMe(boolean requestedByMe) {
        isRequestedByMe = requestedByMe;
    }

    public String getRequesteeName() {
        return requesteeName;
    }

    public void setRequesteeName(String requesteeName) {
        this.requesteeName = requesteeName;
    }

    public String getRequesteePhone() {
        return requesteePhone;
    }

    public void setRequesteePhone(String requesteePhone) {
        this.requesteePhone = requesteePhone;
    }

    public String getRequesteeProfilePicUrl() {
        return requesteeProfilePicUrl;
    }

    public void setRequesteeProfilePicUrl(String requesteeProfilePicUrl) {
        this.requesteeProfilePicUrl = requesteeProfilePicUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getRequestedAtMillis() {
        return requestedAtMillis;
    }

    public void setRequestedAtMillis(long requestedAtMillis) {
        this.requestedAtMillis = requestedAtMillis;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public RequestStatusModel getRequestStatusModel() {
        return requestStatusModel;
    }

    public void setRequestStatusModel(RequestStatusModel requestStatusModel) {
        this.requestStatusModel = requestStatusModel;
    }

    public RequestStatusModel getLastStatusUpdated() {
        return lastStatusUpdated;
    }

    public void setLastStatusUpdated(RequestStatusModel lastStatusUpdated) {
        this.lastStatusUpdated = lastStatusUpdated;
    }
}
