package com.faiteasytrack.models;

public class AdminModel {

    private String registeredByAdminUid;
    private String registeredByAdminName;
    private String registeredByAdminPhone;
    private String name;
    private String phoneNumber;
    private String code;
    private String password;

    public AdminModel() {
    }

    public String getRegisteredByAdminUid() {
        return registeredByAdminUid;
    }

    public void setRegisteredByAdminUid(String registeredByAdminUid) {
        this.registeredByAdminUid = registeredByAdminUid;
    }

    public String getRegisteredByAdminName() {
        return registeredByAdminName;
    }

    public void setRegisteredByAdminName(String registeredByAdminName) {
        this.registeredByAdminName = registeredByAdminName;
    }

    public String getRegisteredByAdminPhone() {
        return registeredByAdminPhone;
    }

    public void setRegisteredByAdminPhone(String registeredByAdminPhone) {
        this.registeredByAdminPhone = registeredByAdminPhone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
