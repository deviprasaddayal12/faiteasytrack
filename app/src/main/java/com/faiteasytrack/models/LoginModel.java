package com.faiteasytrack.models;

public class LoginModel {

    private int i_am;
    private String code;
    private String password;

    public LoginModel() {
    }

    public LoginModel(int i_am, String code, String password) {
        this.i_am = i_am;
        this.code = code;
        this.password = password;
    }

    public int getI_am() {
        return i_am;
    }

    public void setI_am(int i_am) {
        this.i_am = i_am;
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
