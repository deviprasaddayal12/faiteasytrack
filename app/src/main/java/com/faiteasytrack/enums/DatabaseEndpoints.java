package com.faiteasytrack.enums;

public class DatabaseEndpoints {

    public static final String ADMIN_LOGIN = "admin/admin_login";

    public static String REGISTRATION_TYPE(String uid){
        return String.format("users/%s/i_am", uid);
    }
}
