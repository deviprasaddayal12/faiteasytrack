package com.faiteasytrack.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.faiteasytrack.R;
import com.faiteasytrack.constants.Error;
import com.faiteasytrack.listeners.PhoneAuthListener;
import com.faiteasytrack.managers.PhoneAuthManager;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NPhoneAuthActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NPhoneAuthActivity";

    private PhoneAuthManager phoneAuthManager;

    private View loader;
    private ViewFlipper phnAuthFlipper;
    private ViewSwitcher otpResponseSwitcher;

    private TextView tvNetworkInfo;

    private EditText etPhoneNumber, etManualOTP;
    private MaterialButton btnSendOtp, btnLetsGo, btnResendOtp, btnHaveOtp, btnVerifyOtp;
    private Chronometer chronometerOTPCDown;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_auth_new);

        phoneAuthManager = new PhoneAuthManager(this, onVerificationListener);
        registerListener();
    }

    private void registerListener() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, AppPermissions.ACCESS_READ_MESSAGE);
        } else {
            phoneAuthManager.registerSMSBroadcast();
        }
    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void initUI() {
        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        loader = viewStubLoader.inflate();
        ViewUtils.hideViews(loader);

        tvNetworkInfo = findViewById(R.id.tv_network_info);

        phnAuthFlipper = findViewById(R.id.view_flipper_phn_auth);
        otpResponseSwitcher = findViewById(R.id.v_switcher_otp_response);

        etPhoneNumber = findViewById(R.id.et_enter_number);
        etPhoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        etManualOTP = findViewById(R.id.et_manual_otp);
        etManualOTP.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        btnSendOtp = findViewById(R.id.btn_send_otp);
        btnLetsGo = findViewById(R.id.btn_lets_go);
        btnResendOtp = findViewById(R.id.btn_resend_otp);
        btnHaveOtp = findViewById(R.id.btn_have_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_manual_otp);

        chronometerOTPCDown = findViewById(R.id.chrono_otp_countdown);
        chronometerOTPCDown.setBase(SystemClock.elapsedRealtime());
//        chronometerOTPCDown.setCountDown(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Just! Hang On.");
        progressDialog.setMessage("Please wait while we look for your credentials...");
        progressDialog.setCancelable(false);
    }

    @Override
    public void setUpListeners() {
        loader.setOnClickListener(this);

        etPhoneNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onClick(btnSendOtp);
                return true;
            }
        });
        etManualOTP.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onClick(btnVerifyOtp);
                return true;
            }
        });
        btnSendOtp.setOnClickListener(this);
        btnLetsGo.setOnClickListener(this);
        btnResendOtp.setOnClickListener(this);
        btnHaveOtp.setOnClickListener(this);
        btnVerifyOtp.setOnClickListener(this);
    }

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {

    }

    private String phoneNumber;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send_otp: {
                if (!isOnline) {
                    DialogUtils.showSorryAlert(this, "We can't process your request! " +
                            "Please try after some time, when we have a working network connection.", null);
                    return;
                }

                if (etPhoneNumber.getText().toString().equals("")) {
                    etPhoneNumber.setError("Please enter phone number!");
                } else if (etPhoneNumber.getText().length() < 10) {
                    etPhoneNumber.setError("Please enter a valid number.");
                } else {

                    // current layout is : "Whats your number?"
                    phoneNumber = etPhoneNumber.getText().toString();

                    Utils.hideSoftKeyboard(this);
                    ViewUtils.showViews(loader);

                    lastUiState = UI_STATE.SEND_OTP;
                    phoneAuthManager.onRequestVerification(etPhoneNumber.getText().toString());
                }
            }
            break;
            case R.id.btn_lets_go: {
                // current layout is : "Cheers!"

                Intent profileIntent = new Intent(NPhoneAuthActivity.this, NUserProfileActivity.class);
                profileIntent.putExtra("CALLED_FROM", NUserProfileActivity.CALLED_FROM_PHONE_AUTH);
                startActivity(profileIntent);
                finish();
            }
            break;
            case R.id.btn_resend_otp: {
                // current layout is : "Sorry!"

                lastUiState = UI_STATE.RESEND_OTP;

                ViewUtils.showViews(loader);
                phoneAuthManager.onReRequestVerification();
            }
            break;
            case R.id.btn_have_otp: {
                // current layout is : "Sorry!"
                // Following ensures response layout is set to default(success layout) to maintain the order
                otpResponseSwitcher.showPrevious();
                lastUiState = UI_STATE.HAVE_OTP;
                phnAuthFlipper.showNext();
            }
            break;
            case R.id.btn_verify_manual_otp: {
                // current layout is : "Have an otp?"

                if (etManualOTP.getText().toString().equals("")) {
                    etManualOTP.setError("Please enter an otp!");
                } else if (etManualOTP.getText().length() < 6) {
                    etManualOTP.setError("Please enter a valid otp.");
                } else {

                    Utils.hideSoftKeyboard(this);
                    ViewUtils.showViews(loader);
                    phoneAuthManager.onManualCodeVerification(etManualOTP.getText().toString());
                }
            }
            break;
        }
    }

    private int lastUiState = UI_STATE.DEFAULT;
    private boolean isAutoVerified = true;

    private PhoneAuthListener.OnVerificationListener onVerificationListener = new PhoneAuthListener.OnVerificationListener() {
        @Override
        public void onError(Error error) {
            // handle error
            Log.i(TAG, "onError: " + error.getErrorMsg());

            ViewUtils.hideViews(loader);
            DialogUtils.showSorryAlert(NPhoneAuthActivity.this, error.getErrorMsg(), null);
        }

        @Override
        public void onCodeSent() {
            // update ui to time running
            Log.i(TAG, "onCodeSent: ");
            ViewUtils.hideViews(loader);

            isAutoVerified = false;
            // Following changes to timer layout
            if (lastUiState == UI_STATE.RESEND_OTP) {
                // Following ensures response layout is set to default(success layout) to maintain the order
                otpResponseSwitcher.showPrevious();
                // Following changes to timer layout from response layout
                phnAuthFlipper.showPrevious();

            } else if (lastUiState == UI_STATE.SEND_OTP) {
                // Following changes to timer layout from default(whats your number) layout
                phnAuthFlipper.showNext();
            }

            resetChronometerAndStart();
        }

        @Override
        public void onCodeTimeout() {
            // update ui state to timeout, resend code, enter code manually
            Log.i(TAG, "onCodeTimeout: ");
            ViewUtils.hideViews(loader);

            // Following changes to response layout
            phnAuthFlipper.showNext();
            // Following changes to error layout of response layout
            otpResponseSwitcher.showNext();

            chronometerOTPCDown.stop();
        }

        @Override
        public void onCodeVerified() {
            Log.i(TAG, "onCodeVerified: ");
            ViewUtils.hideViews(loader);

            if (isAutoVerified) {
                // In case auto verification, onCodeSent is not invoked
                // Following ensures the traversal through timer layout
                phnAuthFlipper.showNext();
                chronometerOTPCDown.stop();
            }
            // Following changes to success layout of response layout
            if (lastUiState == UI_STATE.HAVE_OTP) {
                phnAuthFlipper.showPrevious();
            } else if (lastUiState == UI_STATE.SEND_OTP || lastUiState == UI_STATE.RESEND_OTP) {
                phnAuthFlipper.showNext();
            }
        }

        @Override
        public void onNewUser() {

        }

        @Override
        public void onUserExists(UserModel userModel, boolean hasProfile) {

        }
    };

    private void resetChronometerAndStart() {
        chronometerOTPCDown.setBase(SystemClock.elapsedRealtime());
        chronometerOTPCDown.stop();
        chronometerOTPCDown.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppPermissions.ACCESS_READ_MESSAGE) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NPhoneAuthActivity.this,
                        new String[]{Manifest.permission.RECEIVE_SMS}, AppPermissions.ACCESS_READ_MESSAGE);
            } else
                registerListener();
        }
    }

    private boolean isOnline = true;
    @Override
    public void updateInternetStatus(boolean online) {
        isOnline = online;
//        String message = isOnlineNow ? "Cheers! We are back." : "Sorry! Could not connect to internet.";
//        int backgroundColor = isOnlineNow ? getResources().getColor(android.R.color.holo_green_dark)
//                : getResources().getColor(android.R.color.holo_red_dark);
//
//        tvNetworkInfo.setText(message);
//        tvNetworkInfo.setBackgroundColor(backgroundColor);
//
//        if (tvNetworkInfo.getVisibility() == View.GONE)
//            ViewUtils.showViews(tvNetworkInfo);
//
//        if (isOnlineNow) {
//            handlerUIThread.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ViewUtils.hideViews(tvNetworkInfo);
//                }
//            }, 2000);
//        }
    }

    private boolean isRequestValid() {
        if (etPhoneNumber.getText().length() < 10) {
            etPhoneNumber.setError("Invalid phone");
            return false;
        }
        return true;
    }

    public static class UI_STATE {
        public static final int DEFAULT = 0;
        public static final int SEND_OTP = 1;
        public static final int RESEND_OTP = 2;
        public static final int HAVE_OTP = 3;
    }
}
