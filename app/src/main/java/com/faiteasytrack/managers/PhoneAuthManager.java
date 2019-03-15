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
                onVerificationListener.onError(Error.ErrorStatus.INVALID_AUTH_CREDENTIALS);
            } else
                onVerificationListener.onError(Error.ErrorStatus.ERROR_NOT_DEFINED);
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

    public void onRequestVerification(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        PhoneAuthProvider.getInstance().verifyPhoneNumber("+91"+phoneNumber, 120, TimeUnit.SECONDS, context, gVerificationCallbacks);
        isVerificationInProgress = true;
    }

    public void onReRequestVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 120, TimeUnit.SECONDS, context, gVerificationCallbacks, resendToken);
        isVerificationInProgress = true;
    }

    public void onManualCodeVerification(String code) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            if (credential.getSmsCode() != null && credential.getSmsCode().equals(code))
                onVerificationComplete(credential);

        } catch (Exception e) {
            e.printStackTrace();
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
                        onVerificationListener.onError(Error.ErrorStatus.AUTH_VERIFICATION_FAILED);
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
