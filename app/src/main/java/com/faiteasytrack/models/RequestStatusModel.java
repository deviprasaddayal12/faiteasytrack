package com.faiteasytrack.models;

public class RequestStatusModel {

    private int status;
    private String title;
    private long updatedAtMillis;

    public RequestStatusModel() {
    }

    public RequestStatusModel(int status, String title) {
        this.status = status;
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }

    public void setUpdatedAtMillis(long updatedAtMillis) {
        this.updatedAtMillis = updatedAtMillis;
    }
}
