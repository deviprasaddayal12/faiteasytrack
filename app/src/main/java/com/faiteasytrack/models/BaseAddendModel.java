package com.faiteasytrack.models;

import android.os.Parcel;
import android.os.Parcelable;

public class BaseAddendModel implements Parcelable {

    // details_userRegistering
    private String uid_;
    private String name_;
    private String phoneNumber_;

    // details_userRegistered
    private String name;
    private String phoneNumber;
    private String code;
    private String password;

    public BaseAddendModel() {
    }

    protected BaseAddendModel(Parcel in) {
        uid_ = in.readString();
        name_ = in.readString();
        phoneNumber_ = in.readString();
        name = in.readString();
        phoneNumber = in.readString();
        code = in.readString();
        password = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid_);
        dest.writeString(name_);
        dest.writeString(phoneNumber_);
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeString(code);
        dest.writeString(password);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BaseAddendModel> CREATOR = new Creator<BaseAddendModel>() {
        @Override
        public BaseAddendModel createFromParcel(Parcel in) {
            return new BaseAddendModel(in);
        }

        @Override
        public BaseAddendModel[] newArray(int size) {
            return new BaseAddendModel[size];
        }
    };

    public String getUid_() {
        return uid_;
    }

    public void setUid_(String uid_) {
        this.uid_ = uid_;
    }

    public String getName_() {
        return name_;
    }

    public void setName_(String name_) {
        this.name_ = name_;
    }

    public String getPhoneNumber_() {
        return phoneNumber_;
    }

    public void setPhoneNumber_(String phoneNumber_) {
        this.phoneNumber_ = phoneNumber_;
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
