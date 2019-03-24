package com.faiteasytrack.listeners;

import com.faiteasytrack.constants.Error;
import com.faiteasytrack.models.ProfileModel;

public interface ProfileListener {

    void onProfileRetrieveSuccess(ProfileModel profileModel);

    void onProfileRetrieveFailure(Error.ErrorType errorType);

    void onProfileUpdateSuccess(ProfileModel profileModel);

    void onProfileUpdateFailure(Error.ErrorType errorType);

    void onTaskComplete();
}
