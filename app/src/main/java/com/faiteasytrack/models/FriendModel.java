package com.faiteasytrack.models;

import android.os.Parcel;
import android.os.Parcelable;

public class FriendModel implements Parcelable {

    private boolean isHeader;
    private String titleHeader;

    private String f_uid;
    private String name;
    private String profilePicUrl;
    private String phone;
    private long friendsAtMillis;

    public FriendModel() {
    }


    protected FriendModel(Parcel in) {
        isHeader = in.readByte() != 0;
        titleHeader = in.readString();
        f_uid = in.readString();
        name = in.readString();
        profilePicUrl = in.readString();
        phone = in.readString();
        friendsAtMillis = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (isHeader ? 1 : 0));
        dest.writeString(titleHeader);
        dest.writeString(f_uid);
        dest.writeString(name);
        dest.writeString(profilePicUrl);
        dest.writeString(phone);
        dest.writeLong(friendsAtMillis);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FriendModel> CREATOR = new Creator<FriendModel>() {
        @Override
        public FriendModel createFromParcel(Parcel in) {
            return new FriendModel(in);
        }

        @Override
        public FriendModel[] newArray(int size) {
            return new FriendModel[size];
        }
    };

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getF_uid() {
        return f_uid;
    }

    public void setF_uid(String f_uid) {
        this.f_uid = f_uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public long getFriendsAtMillis() {
        return friendsAtMillis;
    }

    public void setFriendsAtMillis(long friendsAtMillis) {
        this.friendsAtMillis = friendsAtMillis;
    }
}
