package com.faiteasytrack.listeners;

import com.faiteasytrack.enums.Error;
import com.faiteasytrack.models.UserModel;

public interface PhoneAuthListener {

    interface OnVerificationListener {
        void onError(Error error);

        void onCodeSent();

        void onCodeTimeout();

        void onCodeVerified();

        void onNewUser();

        void onUserExists(UserModel userModel, boolean hasProfile);
    }

    interface OnOTPSendListener{
        void onOTPSent();

        void onOTPReceived(String otp);

        void onTimeOut();
    }

    interface OnIsExistingUserListener{
        void onIsNewUser();

        void onIsExistingUser(UserModel userModel, boolean hasProfileDetails);
    }
}
