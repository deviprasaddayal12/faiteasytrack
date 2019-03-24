package com.faiteasytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;

import com.faiteasytrack.R;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.ViewUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class NSplashActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NSplashActivity";

    private View loader;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void initUI() {
    }

    @Override
    public void setUpListeners() {

    }

    @Override
    public void setUpData() {
        moveAhead();
    }

    private void moveAhead() {
        boolean isLoggedIn = SharePreferences.isUserLoggedIn(this);
        if (isLoggedIn) {
            onUserLoggedIn();
        } else {
            onUserSignIn();
        }
    }

    @Override
    public void setUpRecycler() {

    }

    private void onUserSignIn() {
        startActivity(new Intent(this, NPhoneAuthActivity.class));
        finish();
    }

    private void onUserLoggedIn() {
        startActivity(new Intent(NSplashActivity.this, NDashboardActivity.class));
        finish();
    }

    @Override
    public void updateInternetError(boolean isOnline) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {

    }
}
