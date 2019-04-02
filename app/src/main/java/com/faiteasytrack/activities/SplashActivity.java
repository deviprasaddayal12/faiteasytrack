package com.faiteasytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.faiteasytrack.R;
import com.faiteasytrack.firebase.FirebaseUtils;
import com.faiteasytrack.utils.SharePreferences;

public class SplashActivity extends BaseActivity implements View.OnClickListener {

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
        boolean isUserVerified = FirebaseUtils.getUser() != null;
        boolean isUserLoggedIn = SharePreferences.isUserLoggedIn(this);

        if (isUserLoggedIn) {
            onUserLoggedIn();
        } else if (isUserVerified){
            gotoSigningIn();
        } else {
            gotoVerification();
        }
    }

    @Override
    public void setUpRecycler() {

    }

    private void gotoVerification() {
        startActivity(new Intent(this, PhoneAuthActivity.class));
        finish();
    }

    private void gotoSigningIn(){
        startActivity(new Intent(this, IAmActivity.class));
        finish();
    }

    private void onUserLoggedIn() {
        startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
        finish();
    }

    @Override
    public void updateInternetStatus(boolean online) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {

    }
}
