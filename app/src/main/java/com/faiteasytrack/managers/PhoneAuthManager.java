package com.faiteasytrack.managers;

import android.app.Activity;
import android.util.Log;

import com.faiteasytrack.broadcasts.OTPReceiver;
import com.faiteasytrack.enums.Error;
import com.faiteasytrack.listeners.PhoneAuthListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class PhoneAuthManager implements PhoneAuthListener {

    public static final String TAG = "PhoneAuthManager";
    public static final String TEST_PHONE_NUMBER = "7776665432";
    public static final String TEST_OTP = "123456";

    private static final long CODE_TIMEOUT = 30;

    private Activity context;
    private OnVerificationListener onVerificationListener;

    private PhoneAuthProvider.ForceResendingToken resendToken;
    private boolean isVerificationSuccess = false, isVerificationInProgress = false;
    private String phoneNumber, verificationId;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks gVerificationCallbacks
            = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Log.i(TAG, "onVerificationCompleted: ");
            isVerificationSuccess = true;
            onVerificationComplete(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.e(TAG, "onVerificationFailed: " + e.getMessage());
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                onVerificationListener.onError(Error.getError(Error.ErrorType.INVALID_AUTH_CREDENTIALS.ordinal()));
            } else {
                Error error = new Error(Error.ERROR_CODE_UNDEFINED, e.getMessage());
                onVerificationListener.onError(error);
            }
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(String s) {
            Log.i(TAG, "onCodeAutoRetrievalTimeOut: ");
            if (!isVerificationSuccess)
                onVerificationListener.onCodeTimeout();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            Log.i(TAG, "onCodeSent: ");
            verificationId = s;
            resendToken = forceResendingToken;

            onVerificationListener.onCodeSent();
        }
    };

    public PhoneAuthManager(Activity context, OnVerificationListener onVerificationListener) {
        this.context = context;
        this.onVerificationListener = onVerificationListener;
    }

    public void onRequestVerification(String phoneNumberWithoutCode) {
        this.phoneNumber = "+91" + phoneNumberWithoutCode;

        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, CODE_TIMEOUT,
                TimeUnit.SECONDS, context, gVerificationCallbacks);
        isVerificationInProgress = true;
    }

    public void onReRequestVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, CODE_TIMEOUT, TimeUnit.SECONDS,
                context, gVerificationCallbacks, resendToken);
        isVerificationInProgress = true;
    }

    public void onManualCodeVerification(String code) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            if (credential.getSmsCode() != null && credential.getSmsCode().equals(code))
                onVerificationComplete(credential);
            else {
                Error error = new Error(Error.ERROR_CODE_UNDEFINED, "");
                if (credential.getSmsCode() == null)
                    error.setErrorMsg("Unable to find sms token.");
                else if (!credential.getSmsCode().equals(code))
                    error.setErrorMsg("Wrong code used.");
                onVerificationListener.onError(error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Error error = new Error(Error.ERROR_CODE_UNDEFINED, e.getMessage());
            onVerificationListener.onError(error);
        }
    }

    private void onVerificationComplete(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        onVerificationListener.onCodeVerified();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Error error = new Error(Error.ERROR_CODE_UNDEFINED, e.getMessage());
                        onVerificationListener.onError(error);
                    }
                });
    }

    public void registerSMSBroadcast() {
        OTPReceiver.bindListener(new OTPReceiver.SMSReceivedListener() {
            @Override
            public void onOTPReceived(String otp) {

            }
        });
    }
}
