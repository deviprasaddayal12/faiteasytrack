package com.faiteasytrack.listeners;

import com.faiteasytrack.enums.Error;
import com.faiteasytrack.models.UserModel;
import com.google.firebase.database.DataSnapshot;

public interface UserListener {

    void onCheckedUser(boolean exists);

    void onUserRetrieveSuccess(UserModel userModel);

    void onUserRetrieveFailure(Error.ErrorStatus errorStatus);

    void onUserUpdateSuccess(UserModel userModel);

    void onUserUpdateFailure(Error.ErrorStatus errorStatus);

    void onChildUpdated(DataSnapshot dataSnapshot, String key);

    void onTaskComplete();

    void onUserWithNumberFound(boolean exists, DataSnapshot dataSnapshot);

    interface onFindUserListener{
        void onUserFound(boolean exists, String u_id);

        void onUserNotFound(Error.ErrorStatus errorStatus);
    }
}
