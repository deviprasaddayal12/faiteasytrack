package com.faiteasytrack.models;

import android.content.Context;

import com.faiteasytrack.utils.SharePreferences;

public class UserModel {

//    private static UserModel userModel = null;

//    public static synchronized UserModel getUserModel(Context context){
//        if (userModel == null){
//            userModel = SharePreferences.getUserModel(context);
//            if (userModel == null)
//                userModel = new UserModel();
//        }
//        return userModel;
//    }

    private String uid;
    private String name;
    private String phoneNumber;
    private String code;
    private String password;
    private int i_am;
//    private PreferenceModel preferenceModel;

    public UserModel() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public int getI_am() {
        return i_am;
    }

    public void setI_am(int i_am) {
        this.i_am = i_am;
    }

//    public PreferenceModel getPreferenceModel() {
//        return preferenceModel;
//    }
//
//    public void setPreferenceModel(PreferenceModel preferenceModel) {
//        this.preferenceModel = preferenceModel;
//    }
}
