package com.faiteasytrack.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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
import com.faiteasytrack.enums.Error;
import com.faiteasytrack.listeners.PhoneAuthListener;
import com.faiteasytrack.managers.PhoneAuthManager;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.faiteasytrack.views.EasytrackButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NPhoneAuthActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "";

    private PhoneAuthManager phoneAuthManager;

    private View loader;
    private ViewFlipper phnAuthFlipper;
    private ViewSwitcher otpResponseSwitcher;

    private TextView tvNetworkInfo;

    private EditText etPhoneNumber, etManualOTP;
    private EasytrackButton btnSendOtp, btnLetsGo, btnResendOtp, btnHaveOtp, btnVerifyOtp;
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, AppPermissions.REQUESTS.ACCESS_MESSAGE_READ_REQUEST);
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
        switch (v.getId()){
            case R.id.btn_send_otp:{
                if (!isOnline){
                    DialogUtils.showSorryAlert(this, "We can't process your request! " +
                            "Please try after some time, when we have a working network connection.", null);
                    return;
                }
                if (isRequestValid()) {
                    phoneNumber = etPhoneNumber.getText().toString();

                    Utils.hideSoftKeyboard(this);
                    ViewUtils.showViews(loader);
                    phoneAuthManager.onRequestVerification(etPhoneNumber.getText().toString());
                }
            } break;
            case R.id.btn_lets_go:{
                Intent profileIntent = new Intent(NPhoneAuthActivity.this, NUserProfileActivity.class);
                profileIntent.putExtra("CALLED_FROM", NUserProfileActivity.CALLED_FROM_PHONE_AUTH);
                startActivity(profileIntent);
                finish();
            } break;
            case R.id.btn_resend_otp:{

                lastUiState = UI_STATE.REQUESTED_OTP_RESEND;
                phnAuthFlipper.showPrevious();
                lastUiState = UI_STATE.WAITING_FOR_OTP;

                phoneAuthManager.onReRequestVerification();
            } break;
            case R.id.btn_have_otp:{

                phnAuthFlipper.showNext();
                lastUiState = UI_STATE.VERIFYING_MANUALLY;
            } break;
            case R.id.btn_verify_manual_otp:{

                phoneAuthManager.onManualCodeVerification(etManualOTP.getText().toString());
            } break;
        }
    }

    private int lastUiState = UI_STATE.PROVIDE_NUMBER_FOR_OTP;
    private PhoneAuthListener.OnVerificationListener onVerificationListener = new PhoneAuthListener.OnVerificationListener() {
        @Override
        public void onError(Error.ErrorStatus errorStatus) {
            // handle errorStatus
            Log.i(TAG, "onError: " + errorStatus.name() + errorStatus.ordinal());

//            ViewUtils.hideViews(loader);
            Snackbar.make(etPhoneNumber, "" + errorStatus.name(), Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent() {
            // update ui to time running
            Log.i(TAG, "onCodeSent: ");
            ViewUtils.hideViews(loader);

            phnAuthFlipper.showNext();
            chronometerOTPCDown.start();
            lastUiState = UI_STATE.WAITING_FOR_OTP;
        }

        @Override
        public void onCodeTimeout() {
            // update ui state to timeout, resend code, enter code manually
            Log.i(TAG, "onCodeTimeout: ");

            phnAuthFlipper.showNext();
            otpResponseSwitcher.showNext();
            chronometerOTPCDown.stop();
            lastUiState = UI_STATE.WAITING_FAILED_BY_TIMEOUT;
        }

        @Override
        public void onCodeVerified() {
            // update ui state to verified
            // check if user already exists in app
            Log.i(TAG, "onCodeVerified: ");
            if (lastUiState == UI_STATE.PROVIDE_NUMBER_FOR_OTP){
                ViewUtils.hideViews(loader);

                chronometerOTPCDown.start();
                phnAuthFlipper.showNext();
            }
            phnAuthFlipper.showNext();
            chronometerOTPCDown.stop();
            lastUiState = UI_STATE.WAITING_SUCCEED_BY_AUTO_VERIFY;
        }

        @Override
        public void onNewUser() {
            // goto profile activity
//            Log.i(TAG, "onNewUser: ");
//            progressDialog.dismiss();
//
//            startActivity(new Intent(NPhoneAuthActivity.this, NUserProfileActivity.class));
//            finish();
        }

        @Override
        public void onUserExists(UserModel userModel, boolean hasProfile) {
            // goto dashboard
//            Log.i(TAG, "onUserExists: ");
//            progressDialog.dismiss();
//
//            startActivity(new Intent(NPhoneAuthActivity.this, NMapActivity.class));
//            finish();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AppPermissions.REQUESTS.ACCESS_MESSAGE_READ_REQUEST) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(NPhoneAuthActivity.this,
                        new String[]{Manifest.permission.RECEIVE_SMS}, AppPermissions.REQUESTS.ACCESS_MESSAGE_READ_REQUEST);
            } else
                registerListener();
        }
    }

    private Handler handlerUIThread = new Handler();
    private boolean isOnline = true;
    @Override
    public void updateInternetError(boolean isOnlineNow) {
        isOnline = isOnlineNow;
        String message = isOnlineNow ? "Cheers! We are back." : "Sorry! Could not connect to internet.";
        int backgroundColor = isOnlineNow ? getResources().getColor(android.R.color.holo_green_dark)
                : getResources().getColor(android.R.color.holo_red_dark);

        tvNetworkInfo.setText(message);
        tvNetworkInfo.setBackgroundColor(backgroundColor);

        if (tvNetworkInfo.getVisibility() == View.GONE)
            ViewUtils.showViews(tvNetworkInfo);

        if (isOnlineNow) {
            handlerUIThread.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ViewUtils.hideViews(tvNetworkInfo);
                }
            }, 2000);
        }
    }

    private boolean isRequestValid() {
        if(etPhoneNumber.getText().length() < 10){
            etPhoneNumber.setError("Invalid phone");
            return false;
        }
        return true;
    }

    public interface UI_STATE{
        int PROVIDE_NUMBER_FOR_OTP = 0;
        int WAITING_FOR_OTP = 1;
        int WAITING_FAILED_BY_TIMEOUT = 2;
        int WAITING_SUCCEED_BY_AUTO_VERIFY = 3;
        int REQUESTED_OTP_RESEND = 4;
        int VERIFYING_MANUALLY = 5;
    }
}
