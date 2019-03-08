package com.faiteasytrack.listeners;

import com.faiteasytrack.enums.Error;
import com.faiteasytrack.models.ProfileModel;

public interface ProfileListener {

    void onProfileRetrieveSuccess(ProfileModel profileModel);

    void onProfileRetrieveFailure(Error.ErrorStatus errorStatus);

    void onProfileUpdateSuccess(ProfileModel profileModel);

    void onProfileUpdateFailure(Error.ErrorStatus errorStatus);

    void onTaskComplete();
}
