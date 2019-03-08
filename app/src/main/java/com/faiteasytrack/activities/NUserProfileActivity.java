package com.faiteasytrack.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;

import com.faiteasytrack.R;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Locale;

import androidx.annotation.Nullable;

public class NUserProfileActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NUserProfileActivity";
    public static final int CALLED_FROM_PHONE_AUTH = 0;
    public static final int CALLED_FROM_NAVIGATION = 1;

    private FloatingActionButton fabUploadPhoto;
    private TextInputEditText etUserName, etUserEmail, etUserPhone;

    private View loader;
    private MaterialButton btnUpdate, btnSkip;

    private String name, email;
    private FirebaseUser firebaseUser;

    private boolean isCalledFromAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        isCalledFromAuth = getIntent().getIntExtra("CALLED_FROM",
                CALLED_FROM_NAVIGATION) == CALLED_FROM_PHONE_AUTH;
        setContentView(R.layout.activity_user_profile);
    }

    @Override
    public void setUpActionBar() {
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
    }

    @Override
    public void initUI() {
        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        loader = viewStubLoader.inflate();
        loader.setOnClickListener(this);

        ViewUtils.hideViews(loader);

        etUserName = findViewById(R.id.et_user_name);
        etUserEmail = findViewById(R.id.et_user_email);
        etUserPhone = findViewById(R.id.et_user_phone);

        fabUploadPhoto = findViewById(R.id.fab_upload_photo);
        fabUploadPhoto.hide();

        btnUpdate = findViewById(R.id.btn_update);
        btnSkip = findViewById(R.id.btn_skip);

        if (!isCalledFromAuth)
            ViewUtils.hideViews(btnSkip);
    }

    @Override
    public void setUpListeners() {
        btnUpdate.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        fabUploadPhoto.setOnClickListener(this);
    }

    @Override
    public void setUpData() {
        if (firebaseUser.getPhoneNumber() != null)
            etUserPhone.setText(String.format(Locale.getDefault(), "%s", firebaseUser.getPhoneNumber()));
        if (firebaseUser.getDisplayName() != null)
            etUserName.setText(String.format(Locale.getDefault(), "%s", firebaseUser.getDisplayName()));
        if (firebaseUser.getEmail() != null)
            etUserEmail.setText(String.format(Locale.getDefault(), "%s", firebaseUser.getEmail()));
    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_update) {
            name = etUserName.getText().toString().trim();
            email = etUserEmail.getText().toString().trim();

            etUserName.clearFocus();
            etUserEmail.clearFocus();

            Utils.hideSoftKeyboard(this);
            updateUserProfile();

        } else if (v.getId() == R.id.btn_skip){
            startActivity(new Intent(this, NIAmActivity.class));
            finish();
        }
    }

    private boolean isNameUpdated = false, isEmailUpdated = false;

    private void updateUserProfile() {
        updateName();
        updateEmail();
//        updateProfilePic();
    }

    private void updateName() {
        if (/*!Pattern.compile("[a-zA-Z]").matcher(name).matches()*/ name.isEmpty()) {
            etUserName.setError("Invalid name!");
            etUserName.requestFocus();
            return;
        }
        if (firebaseUser.getDisplayName() != null && name.equals(firebaseUser.getDisplayName()))
            return;

        ViewUtils.showViews(loader);

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        builder.setDisplayName(name);

        firebaseUser.updateProfile(builder.build()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ViewUtils.hideViews(loader);
                ViewUtils.makeToast(NUserProfileActivity.this, "Your name updated successfully.");

                isNameUpdated = true;

                if (isEmailUpdated)
                    gotoDashboard();
            }
        });
    }

    private void gotoDashboard() {
        if (getIntent().getIntExtra("CALLED_FROM", CALLED_FROM_NAVIGATION) == CALLED_FROM_PHONE_AUTH)
            startActivity(new Intent(this, NDashboardActivity.class));
        else
            onBackPressed();
    }

    private void updateEmail() {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etUserEmail.setError("Invalid email!");
            etUserEmail.requestFocus();
            return;
        }
        if (firebaseUser.getEmail() != null && email.equals(firebaseUser.getEmail()))
            return;

        ViewUtils.showViews(loader);

        firebaseUser.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ViewUtils.hideViews(loader);
                ViewUtils.makeToast(NUserProfileActivity.this, "Your email updated successfully.");

                isEmailUpdated = true;

                if (isNameUpdated)
                    gotoDashboard();
            }
        });
    }

    private void updateProfilePic() {
        ViewUtils.showViews(loader);

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        Uri photoUri = new Uri.Builder().build();
        builder.setPhotoUri(photoUri);

        firebaseUser.updateProfile(builder.build()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ViewUtils.hideViews(loader);
                ViewUtils.makeToast(NUserProfileActivity.this, "Your profile photo updated successfully.");
            }
        });
    }

    @Override
    public void updateInternetError(boolean isOnline) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
