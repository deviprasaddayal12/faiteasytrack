package com.faiteasytrack.models;

public class PreferenceModel {

    private boolean doShareLocation;
    private int shareLocationTo;
//    private String lastSavedPassword;

    public PreferenceModel() {

    }

    public PreferenceModel(boolean doShareLocation, int shareLocationTo) {
        this.doShareLocation = doShareLocation;
        this.shareLocationTo = shareLocationTo;
    }

    public boolean isDoShareLocation() {
        return doShareLocation;
    }

    public void setDoShareLocation(boolean doShareLocation) {
        this.doShareLocation = doShareLocation;
    }

    public int getShareLocationTo() {
        return shareLocationTo;
    }

    public void setShareLocationTo(int shareLocationTo) {
        this.shareLocationTo = shareLocationTo;
    }

//    public String getLastSavedPassword() {
//        return lastSavedPassword;
//    }
//
//    public void setLastSavedPassword(String lastSavedPassword) {
//        this.lastSavedPassword = lastSavedPassword;
//    }
}
