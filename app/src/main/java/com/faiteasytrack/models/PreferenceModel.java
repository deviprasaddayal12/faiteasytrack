package com.faiteasytrack.models;

public class PreferenceModel {

    private boolean shouldShareLocation;
    private int shareLocationWith;

    public PreferenceModel() {
    }

    public boolean isShouldShareLocation() {
        return shouldShareLocation;
    }

    public void setShouldShareLocation(boolean shouldShareLocation) {
        this.shouldShareLocation = shouldShareLocation;
    }

    public int getShareLocationWith() {
        return shareLocationWith;
    }

    public void setShareLocationWith(int shareLocationWith) {
        this.shareLocationWith = shareLocationWith;
    }
}
