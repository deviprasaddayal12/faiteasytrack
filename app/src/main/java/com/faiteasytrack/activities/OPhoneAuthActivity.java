package com.faiteasytrack.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.broadcasts.OneTimePasswordReceiver;
import com.faiteasytrack.constants.Error;
import com.faiteasytrack.helpers.FirebaseHelper;
import com.faiteasytrack.helpers.ProfileHelper;
import com.faiteasytrack.helpers.UserHelper;
import com.faiteasytrack.listeners.ProfileListener;
import com.faiteasytrack.listeners.UserListener;
import com.faiteasytrack.models.ProfileModel;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class OPhoneAuthActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener,
        FirebaseHelper.Constants {

    public static final String TAG = "OPhoneAuthActivity", KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final int REQUEST_RECEIVE_SMS = 123;

    private View loader;

    private EditText etPhoneNumber, etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private FloatingActionButton fabNext;

    private LinearLayout llOtpPrompt, llOtpRequested, llOtpSentPrompt, llOnOtpFailed;
    private TextView tvOtpTimer, tvResendOtp, tvErrorMsg;

    private boolean isRequestOtp = true;
    private CountDownTimer countDownTimer;

    private FirebaseAuth gFirebaseAuth;
    private boolean isVerificationInProgress = false;
    private String gVerificationId;
    private PhoneAuthProvider.ForceResendingToken gResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks gVerificationCallbacks;

    private String otpCode;
    private ProgressBar progressBar;

    private UserHelper userHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_activity_phone_auth);

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_RECEIVE_SMS);
//        } else {
//            registerSMSBroadcast();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECEIVE_SMS) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                Handler receiveSmsHandler = new Handler();
                receiveSmsHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(fabNext, "Receive SMS denied.", Snackbar.LENGTH_LONG).setAction("GRANT", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(OPhoneAuthActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_RECEIVE_SMS);
                            }
                        }).show();
                    }
                }, 500);
            } else {
//                registerSMSBroadcast();
            }
        }
    }

    private void registerSMSBroadcast() {
        OneTimePasswordReceiver.bindListener(new OneTimePasswordReceiver.SMSReceivedListener() {
            @Override
            public void onOTPReceived(final String otp) {
                otpCode = otp;
                if (otp.length() == 6) {
                    etOtp1.setText(String.format("%s", otp.charAt(0)));
                    etOtp2.setText(String.format("%s", otp.charAt(1)));
                    etOtp3.setText(String.format("%s", otp.charAt(2)));
                    etOtp4.setText(String.format("%s", otp.charAt(3)));
                    etOtp5.setText(String.format("%s", otp.charAt(4)));
                    etOtp6.setText(String.format("%s", otp.charAt(5)));
                }
            }
        });
    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void initUI() {
        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        loader = viewStubLoader.inflate();
        loader.setOnClickListener(this);

        ViewUtils.hideViews(loader);

        llOtpPrompt = findViewById(R.id.layout_otp_prompt);
        llOtpRequested = findViewById(R.id.layout_otp_requested);
        llOtpSentPrompt = findViewById(R.id.layout_otp_sent_prompt);
        llOnOtpFailed = findViewById(R.id.layout_on_otp_failed);

        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp1.addTextChangedListener(new EditTextTextWatcher(etOtp1));
        etOtp1.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp2.addTextChangedListener(new EditTextTextWatcher(etOtp2));
        etOtp2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp3.addTextChangedListener(new EditTextTextWatcher(etOtp3));
        etOtp3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp4.addTextChangedListener(new EditTextTextWatcher(etOtp4));
        etOtp4.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        etOtp5 = findViewById(R.id.et_otp_5);
        etOtp5.addTextChangedListener(new EditTextTextWatcher(etOtp5));
        etOtp5.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        etOtp6 = findViewById(R.id.et_otp_6);
        etOtp6.addTextChangedListener(new EditTextTextWatcher(etOtp6));
        etOtp6.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

        tvOtpTimer = findViewById(R.id.tv_otp_timer);
        tvResendOtp = findViewById(R.id.tv_resend_otp);
        tvErrorMsg = findViewById(R.id.tv_error_message);

        progressBar = findViewById(R.id.progress_circular);
        Drawable progressDrawable = progressBar.getIndeterminateDrawable().mutate();
        progressDrawable.setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setIndeterminateDrawable(progressDrawable);

        etPhoneNumber = findViewById(R.id.et_enter_number);
        etPhoneNumber.addTextChangedListener(new EditTextTextWatcher(etPhoneNumber));
        etPhoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        fabNext = findViewById(R.id.fab_next);
        updateUI(STATE_INITIALIZED);
    }

    @Override
    public void setUpListeners() {
        fabNext.setOnClickListener(this);
        tvResendOtp.setOnClickListener(this);
    }

    @Override
    public void setUpData() {
        gFirebaseAuth = FirebaseAuth.getInstance();
        gVerificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                isVerificationInProgress = false;
                signInWithPhoneAuthCredential(phoneAuthCredential, true);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                ViewUtils.hideViews(progressBar);

                if (e instanceof FirebaseAuthInvalidCredentialsException)
                    etPhoneNumber.setError("Invalid phone number");

//                makeSnackbar(fabNext, "" + e.getMessage());

                updateUI(STATE_VERIFY_FAILED);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                if (isVerificationSuccess)
                    return;

                ViewUtils.hideViews(progressBar);
//                makeSnackbar(fabNext, "OTP receiving time out.", "RESEND", new Runnable() {
//                    @Override
//                    public void run() {
//                        resendVerificationCode("+91" + etPhoneNumber.getText().toString(), gResendToken);
//                        updateUI(STATE_INITIALIZED);
//                    }
//                });

                updateUI(STATE_VERIFY_TIMEOUT);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                ViewUtils.showViews(progressBar);

                gVerificationId = s;
                gResendToken = forceResendingToken;

                updateUI(STATE_CODE_SENT);
            }
        };
    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void onClick(View v) {
        Utils.hideSoftKeyboard(this);
        switch (v.getId()) {
            case R.id.fab_next: {
                if (!isPhoneNumberValid()) {
                    return;
                }
                updateUI(STATE_INITIALIZED);
                startPhoneNumberVerification("+91" + etPhoneNumber.getText().toString());
            }
            break;
            case R.id.tv_resend_otp: {
                resendVerificationCode("+91" + etPhoneNumber.getText().toString(), gResendToken);
                updateUI(STATE_INITIALIZED);
            }
            break;
        }
    }

    private boolean isPhoneNumberValid() {
        if (etPhoneNumber.getText().toString().isEmpty() || etPhoneNumber.getText().length() != 10) {
            etPhoneNumber.requestFocus();
            etPhoneNumber.setError("Invalid phone");
//            makeSnackbar(fabNext, "Please enter a valid phone number.");
            return false;
        } else
            return true;
    }

    private void startPhoneNumberVerification(String phoneNumber) {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,              // Phone number to verify
                120,                    // Timeout duration
                TimeUnit.SECONDS,         // Unit of timeout
                this,              // Activity (for callback binding)
                gVerificationCallbacks);  // OnVerificationStateChangedCallbacks

        isVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {

        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential, false);
        } catch (Exception e) {
            Log.e(TAG, "verifyPhoneNumberWithCode: e = " + e.getLocalizedMessage());
//            makeSnackbar(fabNext, "" + e.getMessage());
        }
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,            // Phone number to verify
                120,                  // Timeout duration
                TimeUnit.SECONDS,       // Unit of timeout
                this,            // Activity (for callback binding)
                gVerificationCallbacks,  // OnVerificationStateChangedCallbacks
                token);                  // ForceResendingToken from callbacks
    }

    private boolean isVerificationSuccess = false;

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, boolean isAutoVerified) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                /*.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        makeToast("Authorization completed.");
                    }
                })*/
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        isVerificationSuccess = true;
                        FirebaseUser user = authResult.getUser();

//                        makeToast("addOnSuccessListener" + user.getUid());
                        SharePreferences.saveUid(OPhoneAuthActivity.this, user.getPhoneNumber());
                        userHelper = new UserHelper(OPhoneAuthActivity.this, userListener);
                        userHelper.checkUserExists(user);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ViewUtils.hideViews(progressBar);

//                        makeToast("addOnFailureListener");
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            updateUI(STATE_VERIFY_FAILED);
                            tvErrorMsg.setError("" + e.getMessage());
//                            makeSnackbar(fabNext, "" + e.getMessage(), "RESEND", new Runnable() {
//                                @Override
//                                public void run() {
//                                    resendVerificationCode("+91" + etPhoneNumber.getText().toString(), gResendToken);
//                                    updateUI(STATE_INITIALIZED);
//                                }
//                            });
                        }
                    }
                });
    }

    private void setUpOtpTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        countDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secs = millisUntilFinished / 1000;
                if (secs % 60 < 10)
                    tvOtpTimer.setText(String.format(Locale.getDefault(), "0%d:0%d", secs / 60, secs % 60));
                else
                    tvOtpTimer.setText(String.format(Locale.getDefault(), "0%d:%d", secs / 60, secs % 60));
            }

            @Override
            public void onFinish() {
                updateUI(STATE_VERIFY_TIMEOUT);
            }
        };
        countDownTimer.start();
    }

    private void updateUI(int uiState) {
        switch (uiState) {
            case STATE_INITIALIZED: {

                etPhoneNumber.clearFocus();
                ViewUtils.hideViews(llOtpRequested);
            }
            break;
            case STATE_CODE_SENT: {

                setUpOtpTimer();
                ViewUtils.showViews(llOtpRequested, tvOtpTimer, llOtpSentPrompt);
                ViewUtils.hideViews(llOnOtpFailed, tvErrorMsg);
//                disableEditTexts(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6);
            }
            break;
            case STATE_VERIFY_FAILED: {

//                enablePhoneField(etPhoneNumber, etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6);
                etOtp1.requestFocus();
                ViewUtils.hideViews(tvOtpTimer, llOtpSentPrompt);
                ViewUtils.showViews(tvErrorMsg, llOnOtpFailed);
            }
            break;
            case STATE_VERIFY_TIMEOUT: {

//                enablePhoneField(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6);
                etOtp1.requestFocus();
                ViewUtils.hideViews(tvOtpTimer, llOtpSentPrompt);
                ViewUtils.showViews(tvErrorMsg, llOnOtpFailed);
            }
            break;
        }
    }

    private UserListener userListener = new UserListener() {
        @Override
        public void onCheckedUser(boolean exists) {

            SharePreferences.setLaunchedAlready(OPhoneAuthActivity.this);

            if (exists) {

//                makeProgressDialog("Bit More Patience!", "Please be patient while we fetch your data...");
                userHelper.getUserDetails();
            } else {

//                makeProgressDialog("Bit More Patience!", "Please be patient while we upload your data...");

                UserModel userModel = new UserModel();
                userModel.setUid(SharePreferences.getUid(OPhoneAuthActivity.this));
                userModel.setCode(etPhoneNumber.getText().toString().trim());
                userModel.setPassword(String.valueOf(12345678));
//                userModel.setUserProfileExists(false);

                userHelper.updateUserDetails(userModel);
            }
        }

        private ProfileListener profileListener = new ProfileListener() {
            @Override
            public void onProfileRetrieveSuccess(ProfileModel profileModel) {
                SharePreferences.saveProfileModel(OPhoneAuthActivity.this, profileModel);

//                hideProgressDialog();
//                if (userModel_.isUserProfileExists() && profileModel != null) {
//
//                    startActivity(new Intent(OPhoneAuthActivity.this, NMapActivity.class));
//                } else {
//
//                    startActivity(new Intent(OPhoneAuthActivity.this, NUserProfileActivity.class));
//                }
//                finish();
            }

            @Override
            public void onProfileRetrieveFailure(Error.ErrorType ERROR_Status_NOT_DEFINED) {

            }

            @Override
            public void onProfileUpdateSuccess(ProfileModel profileModel) {

            }

            @Override
            public void onProfileUpdateFailure(Error.ErrorType ERROR_Status_NOT_DEFINED) {

            }

            @Override
            public void onTaskComplete() {

            }
        };

        private UserModel userModel_;

        @Override
        public void onUserRetrieveSuccess(UserModel userModel) {
            Log.e(TAG, "onUserRetrieveSuccess: userMOdel" + userModel);

            userModel_ = userModel;
            new ProfileHelper(OPhoneAuthActivity.this, profileListener).getProfileDetails();

            SharePreferences.saveUserModel(OPhoneAuthActivity.this, userModel);
        }

        @Override
        public void onUserRetrieveFailure(Error.ErrorType ERROR_Status_NOT_DEFINED) {
//            hideProgressDialog();

//            makeSnackbar(fabNext, "" + ERROR_Status_NOT_DEFINED);
        }

        @Override
        public void onUserUpdateSuccess(UserModel userModel) {
            Log.d(TAG, "onUserUpdateSuccess: userModel" + userModel);
//            hideProgressDialog();

            SharePreferences.saveUserModel(OPhoneAuthActivity.this, userModel);
//            if (userModel.isUserProfileExists()) {
//
//                startActivity(new Intent(OPhoneAuthActivity.this, NMapActivity.class));
//            } else {
//
//                startActivity(new Intent(OPhoneAuthActivity.this, NUserProfileActivity.class));
//            }
//            finish();
        }

        @Override
        public void onUserUpdateFailure(Error.ErrorType ERROR_Status_NOT_DEFINED) {
//            hideProgressDialog();

//            makeSnackbar(fabNext, "" + ERROR_Status_NOT_DEFINED);
        }

        @Override
        public void onChildUpdated(DataSnapshot dataSnapshot, String key) {

        }

        @Override
        public void onTaskComplete() {

        }

        @Override
        public void onUserWithNumberFound(boolean exists, DataSnapshot dataSnapshot) {

        }
    };

    private void onMyDetailsNotFound() {
//        hideProgressDialog();
        DialogUtils.showSorryAlert(this, "It seems that you're not registered with us." +
                "\nNo worries! Tell us something about you, and, Voila! you're in.", new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(OPhoneAuthActivity.this, NUserProfileActivity.class));
                        finish();
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private class EditTextTextWatcher implements TextWatcher {

        private EditText editText;

        public EditTextTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (editText.getId()) {
                case R.id.et_enter_number: {
                    if (llOtpRequested.getVisibility() == View.VISIBLE)
                        llOtpRequested.setVisibility(View.GONE);
                }
                break;
                case R.id.et_otp_1: {
                    if (s.length() == 1)
                        etOtp2.requestFocus();
                }
                break;
                case R.id.et_otp_2: {
                    if (s.length() == 0)
                        etOtp1.requestFocus();
                    else
                        etOtp3.requestFocus();
                }
                break;
                case R.id.et_otp_3: {
                    if (s.length() == 0)
                        etOtp2.requestFocus();
                    else
                        etOtp4.requestFocus();
                }
                break;
                case R.id.et_otp_4: {
                    if (s.length() == 0)
                        etOtp3.requestFocus();
                    else
                        etOtp5.requestFocus();
                }
                break;
                case R.id.et_otp_5: {
                    if (s.length() == 0)
                        etOtp4.requestFocus();
                    else
                        etOtp6.requestFocus();
                }
                break;
                case R.id.et_otp_6: {
                    if (s.length() == 0)
                        etOtp5.requestFocus();
                    else {
                        verifyPhoneNumberWithCode(gVerificationId, otpCode);
                    }
                    // perform next action
                }
                break;
            }
        }
    }

    @Override
    public void updateInternetError(boolean isOnline) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, isVerificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }
}
