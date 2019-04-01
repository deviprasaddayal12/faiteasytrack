package com.faiteasytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.faiteasytrack.R;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.ViewUtils;
import com.faiteasytrack.views.MoveUpBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

public class OSignInPromptActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "OSignInPromptActivity";

    private LinearLayout llGoogleSignIn, llEmailSignIn, llPhoneSignIn, llRegisterOnly, llLogin, llRegisterWithFabs;
    private TextInputEditText etUserId, etPassword;
    private AppCompatCheckBox cbRememberMe;
    private TextView tvForgotDetails;
    private MaterialButton btnLogIn;
    private FloatingActionButton fabGoogle, fabPhone, fabEmail;

    private CoordinatorLayout.LayoutParams layoutParams;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.z_activity_sign_in_prompt);
    }

    @Override
    public void setUpActionBar() {
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setTitle("Sign In");
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }
    }

    @Override
    public void initUI() {
        llLogin = findViewById(R.id.login_layout);
        llRegisterOnly = findViewById(R.id.register_only_layouts);
        llRegisterWithFabs = findViewById(R.id.register_fabs_layout);

        if (SharePreferences.isFirstLaunch(this)){
            showRegisterOnlyView();
        } else {
            SharePreferences.setLaunchedAlready(this);
            hideRegisterOnlyView();
        }

        llGoogleSignIn = findViewById(R.id.ll_google_sign_in);
        llEmailSignIn = findViewById(R.id.ll_email_sign_in);
        llPhoneSignIn = findViewById(R.id.ll_phone_sign_in);

        etUserId = findViewById(R.id.et_user_id);
        etPassword = findViewById(R.id.et_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
        tvForgotDetails = findViewById(R.id.tv_forgot_details);
        btnLogIn = findViewById(R.id.btn_log_in);
        fabGoogle = findViewById(R.id.fab_google);
        fabPhone = findViewById(R.id.fab_phone);
        fabEmail = findViewById(R.id.fab_email);
    }

    private void showRegisterOnlyView(){
        layoutParams = (CoordinatorLayout.LayoutParams) ((View) llRegisterWithFabs).getLayoutParams();
        layoutParams.setBehavior(null);
        ViewUtils.hideViews((View) llRegisterWithFabs, (View) llLogin);
        layoutParams = (CoordinatorLayout.LayoutParams) ((View) llRegisterOnly).getLayoutParams();
        layoutParams.setBehavior(new MoveUpBehavior());
        ViewUtils.showViews((View) llRegisterOnly);
    }

    private void hideRegisterOnlyView(){
        layoutParams = (CoordinatorLayout.LayoutParams) ((View) llRegisterWithFabs).getLayoutParams();
        layoutParams.setBehavior(new MoveUpBehavior());
        ViewUtils.showViews((View) llRegisterWithFabs, (View) llLogin);
        layoutParams = (CoordinatorLayout.LayoutParams) ((View) llRegisterOnly).getLayoutParams();
        layoutParams.setBehavior(null);
        ViewUtils.hideViews((View) llRegisterOnly);
    }

    @Override
    public void setUpListeners() {
        llGoogleSignIn.setOnClickListener(this);
        llEmailSignIn.setOnClickListener(this);
        llPhoneSignIn.setOnClickListener(this);
        tvForgotDetails.setOnClickListener(this);
        btnLogIn.setOnClickListener(this);
        fabEmail.setOnClickListener(this);
        fabPhone.setOnClickListener(this);
        fabGoogle.setOnClickListener(this);
    }

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_google_sign_in:{
//                makeSnackbar(llGoogleSignIn, "Google sign in will be updated soon...");
            }
            break;
            case R.id.ll_email_sign_in:{
//                makeSnackbar(llEmailSignIn, "Email sign in will be updated soon...");
            }
            break;
            case R.id.ll_phone_sign_in:{
                startActivity(new Intent(OSignInPromptActivity.this, OPhoneAuthActivity.class));
                finish();
            }
            break;
            case R.id.fab_google:{
//                makeSnackbar(llGoogleSignIn, "Google sign in will be updated soon...");
            }
            break;
            case R.id.fab_email:{
//                makeSnackbar(llEmailSignIn, "Email sign in will be updated soon...");
            }
            break;
            case R.id.fab_phone:{
                startActivity(new Intent(OSignInPromptActivity.this, OPhoneAuthActivity.class));
                finish();
            }
            break;
            case R.id.btn_log_in:{
//                makeSnackbar(llGoogleSignIn, "Log in will be updated soon...", "REGISTER",
//                        new Runnable() {
//                            @Override
//                            public void run() {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        showRegisterOnlyView();
//                                    }
//                                });
//                            }
//                        });
            }
            break;
            case R.id.tv_forgot_details:{
//                makeToast("Forgot details will be updated soon...", Toast.LENGTH_LONG);
            }
            break;
        }
    }

    @Override
    public void updateInternetStatus(boolean online) {

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
}
