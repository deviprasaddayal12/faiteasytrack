package com.faiteasytrack.models;

import android.os.Parcelable;

public class AccountModel extends BaseUserModel implements Parcelable {

    private String generatedByUid;
    private String code;
    private String password;
}
