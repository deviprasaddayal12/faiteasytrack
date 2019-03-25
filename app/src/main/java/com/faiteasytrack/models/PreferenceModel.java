package com.faiteasytrack.models;

public class PreferenceModel {

    private boolean doShareLocation;
    private int shareLocationTo;
    private boolean doShareProfilePicture;
    private int storageForProfilePhoto;

    public PreferenceModel() {

    }

    public PreferenceModel(boolean doShareLocation, int shareLocationTo, boolean doShareProfilePicture, int storageForProfilePhoto) {
        this.doShareLocation = doShareLocation;
        this.shareLocationTo = shareLocationTo;
        this.doShareProfilePicture = doShareProfilePicture;
        this.storageForProfilePhoto = storageForProfilePhoto;
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

    public boolean isDoShareProfilePicture() {
        return doShareProfilePicture;
    }

    public void setDoShareProfilePicture(boolean doShareProfilePicture) {
        this.doShareProfilePicture = doShareProfilePicture;
    }

    public int getStorageForProfilePhoto() {
        return storageForProfilePhoto;
    }

    public void setStorageForProfilePhoto(int storageForProfilePhoto) {
        this.storageForProfilePhoto = storageForProfilePhoto;
    }
}
