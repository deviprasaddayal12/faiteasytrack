package com.faiteasytrack.models;

import java.util.ArrayList;

public class ContactModel {

    private String contactId;
    private boolean isHeader;
    private String titleHeader;
    private String contactName;
    private ArrayList<String> phones;
    private boolean isFriendAlready;

    private boolean isExpanded;
    private RequestStatusModel requestStatusModel;

    public ContactModel() {
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
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

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public ArrayList<String> getPhones() {
        return phones;
    }

    public void setPhones(ArrayList<String> phones) {
        this.phones = phones;
    }

    public boolean isFriendAlready() {
        return isFriendAlready;
    }

    public void setFriendAlready(boolean friendAlready) {
        isFriendAlready = friendAlready;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public RequestStatusModel getRequestStatusModel() {
        return requestStatusModel;
    }

    public void setRequestStatusModel(RequestStatusModel requestStatusModel) {
        this.requestStatusModel = requestStatusModel;
    }
}
