package com.faiteasytrack.models;

import java.util.ArrayList;

public class ProfileModel {

    private String authorizationId;
    private String name;
    private String alias;
    private String profilePhotoUrl;
    private ArrayList<String> emails;
    private ArrayList<String> phones;
    private int registrationType;
    private long registrationMillis;
    private long lastUpdateAtMillis;

    public ProfileModel() {

    }

    public String getAuthorizationId() {
        return authorizationId;
    }

    public void setAuthorizationId(String authorizationId) {
        this.authorizationId = authorizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public ArrayList<String> getEmails() {
        return emails;
    }

    public void setEmails(ArrayList<String> emails) {
        this.emails = emails;
    }

    public ArrayList<String> getPhones() {
        return phones;
    }

    public void setPhones(ArrayList<String> phones) {
        this.phones = phones;
    }

    public int getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(int registrationType) {
        this.registrationType = registrationType;
    }

    public long getRegistrationMillis() {
        return registrationMillis;
    }

    public void setRegistrationMillis(long registrationMillis) {
        this.registrationMillis = registrationMillis;
    }

    public long getLastUpdateAtMillis() {
        return lastUpdateAtMillis;
    }

    public void setLastUpdateAtMillis(long lastUpdateAtMillis) {
        this.lastUpdateAtMillis = lastUpdateAtMillis;
    }
}
