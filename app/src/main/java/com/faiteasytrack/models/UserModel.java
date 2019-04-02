package com.faiteasytrack.models;

import android.content.Context;
import android.os.Parcelable;

import com.faiteasytrack.utils.SharePreferences;

public class UserModel extends BaseUserModel implements Parcelable {

    private String code;
    private String password;
    private int i_am;

    public UserModel() {
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

    public int getI_am() {
        return i_am;
    }

    public void setI_am(int i_am) {
        this.i_am = i_am;
    }
}
