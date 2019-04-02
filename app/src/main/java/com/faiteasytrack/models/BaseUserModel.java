package com.faiteasytrack.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseUserModel implements Parcelable {

    private String uid;
    private String phoneNumber;
    private String name;

    public BaseUserModel() {}

    protected BaseUserModel(Parcel in) {
        uid = in.readString();
        phoneNumber = in.readString();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(phoneNumber);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BaseUserModel> CREATOR = new Creator<BaseUserModel>() {
        @Override
        public BaseUserModel createFromParcel(Parcel in) {
            return new BaseUserModel(in);
        }

        @Override
        public BaseUserModel[] newArray(int size) {
            return new BaseUserModel[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
