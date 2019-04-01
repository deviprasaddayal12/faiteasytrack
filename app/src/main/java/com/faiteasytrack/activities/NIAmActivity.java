package com.faiteasytrack.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import com.faiteasytrack.R;
import com.faiteasytrack.constants.FirebaseKeys;
import com.faiteasytrack.constants.User;
import com.faiteasytrack.models.AdminModel;
import com.faiteasytrack.models.DriverModel;
import com.faiteasytrack.models.LoginModel;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.models.VendorModel;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NIAmActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NIAmActivity";

    private ProgressDialog progressDialog;
    private MaterialButton btnVerifyMe;

    private int userType;
    private boolean isUserRegistered;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference, adminReference, vendorReference, driverReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        userReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.USERS_DB);
        adminReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.ADMINS_DB);
        vendorReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.VENDORS_DB);
        driverReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.DRIVERS_DB);

        setContentView(R.layout.activity_i_am);
        checkUserExists();
    }

    @Override
    public void setUpActionBar() {

    }

    private void checkUserExists() {
        showProgressDialog(null, "Verifying...");

        queryFindUser = userReference.orderByChild(FirebaseKeys.PHONE_NUMBER)
                .equalTo(firebaseUser.getPhoneNumber());
        queryFindUser.addValueEventListener(findUserEventListener);
    }

    private Query queryFindUser;
    private ValueEventListener findUserEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "findUserEventListener.onDataChange: " + dataSnapshot);

            isUserRegistered = dataSnapshot.exists();

            if (isUserRegistered) {
                UserModel userModel = null;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    userModel = snapshot.getValue(UserModel.class);
                }
                checkUserType(userModel);
            } else {
                hideProgressDialog();
                Snackbar.make(btnVerifyMe, "Select a category and click verify.", Snackbar.LENGTH_LONG).show();
            }

            queryFindUser.removeEventListener(findUserEventListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "findUserEventListener.onCancelled: " + databaseError.getMessage());

            hideProgressDialog();
            DialogUtils.showSorryAlert(NIAmActivity.this, "" + databaseError.getMessage(), null);

            queryFindUser.removeEventListener(findUserEventListener);
        }
    };

    private void checkUserType(@NonNull UserModel userModel) {
        if (userModel.getI_am() == User.TYPE_PARENT) {
            SharePreferences.saveUserModel(this, userModel);
            gotoDashboard();
        } else
            askForCredentials(userModel);
    }

    private void askForCredentials(final UserModel userModel) {
        hideProgressDialog();

        verifyLoginCredentials(new LoginModel(userModel.getI_am(), userModel.getCode(), userModel.getPassword()),
                new OnCredentialsVerificationCompleteListener() {
            @Override
            public void onCredentialsVerified(boolean isSuccess) {
                if (isSuccess){
                    SharePreferences.saveUserModel(NIAmActivity.this, userModel);
                    gotoDashboard();
                }
            }
        });
    }

    @Override
    public void initUI() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        btnVerifyMe = findViewById(R.id.btn_verify_me);
    }

    @Override
    public void setUpListeners() {
        findViewById(R.id.btn_verify_me).setOnClickListener(this);
        ((RadioGroup) findViewById(R.id.rg_i_am)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbtn_admin:
                        userType = User.TYPE_ADMIN;
                        break;
                    case R.id.rbtn_vendor:
                        userType = User.TYPE_VENDOR;
                        break;
                    case R.id.rbtn_driver:
                        userType = User.TYPE_DRIVER;
                        break;
                    default:
                        userType = User.TYPE_PARENT;
                        break;
                }
            }
        });
    }

    @Override
    public void setUpData() {
        userType = User.TYPE_PARENT;
    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_verify_me) {
            if (userType == User.TYPE_PARENT)
                addNewUser(null, true);
            else
                verifyNewUser();
        }
    }

    private void addNewUser(UserModel userModel, boolean isParent) {
        showProgressDialog(null, "Uploading your information...");

        userReference.addValueEventListener(addNewUserEventListener);
        if (isParent) {
            userModel = new UserModel();
            userModel.setI_am(userType);
            userModel.setCode("");
            userModel.setPassword("");
            userModel.setName(firebaseUser.getDisplayName());
            userModel.setPhoneNumber(firebaseUser.getPhoneNumber());
            userModel.setUid(firebaseUser.getUid());
        }
        userReference.push().setValue(userModel);
        SharePreferences.saveUserModel(this, userModel);
    }

    private ValueEventListener addNewUserEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "addNewUserEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            DialogUtils.showCheersAlert(NIAmActivity.this, "Your information was saved successfully.",
                    new Runnable() {
                        @Override
                        public void run() {
                            gotoDashboard();
                        }
                    });

            userReference.removeEventListener(addNewUserEventListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "addNewUserEventListener.onCancelled: " + databaseError.getMessage());

            userReference.removeEventListener(addNewUserEventListener);
        }
    };

    private void verifyNewUser() {
        switch (userType) {
            case User.TYPE_ADMIN: {
                queryLookForAdmin = adminReference.orderByChild(FirebaseKeys.PHONE_NUMBER)
                        .equalTo(Utils.getPhoneNumberWithoutCode(firebaseUser.getPhoneNumber()));
                queryLookForAdmin.addValueEventListener(lookForAdminEventListener);
            }
            break;
            case User.TYPE_VENDOR: {
                queryLookForVendor = vendorReference.orderByChild(FirebaseKeys.PHONE_NUMBER)
                        .equalTo(Utils.getPhoneNumberWithoutCode(firebaseUser.getPhoneNumber()));
                queryLookForVendor.addValueEventListener(lookForVendorEventListener);
            }
            break;
            case User.TYPE_DRIVER: {
                queryLookForDriver = driverReference.orderByChild(FirebaseKeys.PHONE_NUMBER)
                        .equalTo(Utils.getPhoneNumberWithoutCode(firebaseUser.getPhoneNumber()));
                queryLookForDriver.addValueEventListener(lookForDriverEventListener);
            }
            break;
        }
    }

    private Query queryLookForAdmin;
    private ValueEventListener lookForAdminEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "lookForAdminEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            AdminModel adminModel = null;
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                adminModel = snapshot.getValue(AdminModel.class);
            }

            if (adminModel != null) {
                final LoginModel loginModel =
                        new LoginModel(User.TYPE_ADMIN, adminModel.getCode(), adminModel.getPassword());
                verifyLoginCredentials(loginModel, new OnCredentialsVerificationCompleteListener() {
                    @Override
                    public void onCredentialsVerified(boolean isSuccess) {
                        if (isSuccess)
                            addNewUser(getUserModel(loginModel), false);
                    }
                });
            } else {
                DialogUtils.showSorryAlert(NIAmActivity.this, "No access found." +
                        " Please request an admin to give you access.", new Runnable() {
                    @Override
                    public void run() {
                        // todo : create a request to admin
                        onBackPressed();
                    }
                });
            }
            queryLookForAdmin.removeEventListener(lookForAdminEventListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "lookForAdminEventListener.onCancelled: " + databaseError.getMessage());

            queryLookForAdmin.removeEventListener(lookForAdminEventListener);
        }
    };

    private Query queryLookForVendor;
    private ValueEventListener lookForVendorEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "lookForVendorEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            VendorModel vendorModel = null;
            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                vendorModel = snapshot.getValue(VendorModel.class);
            }

            if (vendorModel != null) {
                final LoginModel loginModel =
                        new LoginModel(User.TYPE_VENDOR, vendorModel.getCode(), vendorModel.getPassword());
                verifyLoginCredentials(loginModel, new OnCredentialsVerificationCompleteListener() {
                    @Override
                    public void onCredentialsVerified(boolean isSuccess) {
                        if (isSuccess)
                            addNewUser(getUserModel(loginModel), false);
                    }
                });
            } else {
                DialogUtils.showSorryAlert(NIAmActivity.this, "No access found." +
                        " Please request an admin to give you access.", new Runnable() {
                    @Override
                    public void run() {
                        // todo : create a request to admin
                        onBackPressed();
                    }
                });
            }
            queryLookForVendor.removeEventListener(lookForVendorEventListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "lookForVendorEventListener.onCancelled: " + databaseError.getMessage());

            queryLookForVendor.removeEventListener(lookForVendorEventListener);
        }
    };

    private Query queryLookForDriver;
    private ValueEventListener lookForDriverEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "lookForDriverEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            DriverModel driverModel = null;
            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                driverModel = snapshot.getValue(DriverModel.class);
            }

            if (driverModel != null) {
                final LoginModel loginModel =
                        new LoginModel(User.TYPE_DRIVER, driverModel.getCode(), driverModel.getPassword());
                verifyLoginCredentials(loginModel, new OnCredentialsVerificationCompleteListener() {
                    @Override
                    public void onCredentialsVerified(boolean isSuccess) {
                        if (isSuccess)
                            addNewUser(getUserModel(loginModel), false);
                    }
                });
            } else {
                DialogUtils.showSorryAlert(NIAmActivity.this, "No access found." +
                        " Please request vendor to give you access.", new Runnable() {
                    @Override
                    public void run() {
                        // todo : create a request to admin
                        onBackPressed();
                    }
                });
            }

            queryLookForDriver.removeEventListener(lookForDriverEventListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "lookForDriverEventListener.onCancelled: " + databaseError.getMessage());

            queryLookForDriver.removeEventListener(lookForDriverEventListener);
        }
    };

    private void verifyLoginCredentials(final LoginModel loginModel,
                                        final OnCredentialsVerificationCompleteListener credentialsVerificationCompleteListener) {
        String title;
        if (loginModel.getI_am() == User.TYPE_ADMIN)
            title = "Admin";
        else if (loginModel.getI_am() == User.TYPE_VENDOR)
            title = "Vendor";
        else
            title = "Driver";

        final Dialog dialog = new Dialog(this);
        dialog.setTitle(String.format("Verify yourself: %s", title));
        dialog.setContentView(R.layout.dialog_new_user_verification);

        final TextInputEditText etCode = dialog.findViewById(R.id.et_code);
        final TextInputEditText etPassword = dialog.findViewById(R.id.et_password);

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btn_verify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                boolean isCodeOk = etCode.getText().toString().equals(loginModel.getCode());
                boolean isPasswordOk = etPassword.getText().toString().equals(loginModel.getPassword());

                if (isCodeOk && isPasswordOk) {
                    ViewUtils.makeToast(NIAmActivity.this, "Password verified!");

                    if (credentialsVerificationCompleteListener !=  null)
                        credentialsVerificationCompleteListener.onCredentialsVerified(true);
                } else {
                    // todo : handle wrong credentials
                    if (!isCodeOk)
                        ViewUtils.makeToast(NIAmActivity.this, "Incorrect code!");
                    else
                        ViewUtils.makeToast(NIAmActivity.this, "Incorrect password!");

                    if (credentialsVerificationCompleteListener !=  null)
                        credentialsVerificationCompleteListener.onCredentialsVerified(false);
                }
            }
        });
        dialog.setCancelable(false);
        dialog.show();
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

    private void showProgressDialog(String title, String message) {
        if (title != null)
            progressDialog.setTitle(title);
        if (message != null)
            progressDialog.setMessage(message);

        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void gotoDashboard() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        startActivity(new Intent(this, NDashboardActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public interface OnCredentialsVerificationCompleteListener{
        void onCredentialsVerified(boolean isSuccess);
    }

    private UserModel getUserModel(LoginModel loginModel){
        UserModel userModel = new UserModel();
        userModel.setI_am(loginModel.getI_am());
        userModel.setCode(loginModel.getCode());
        userModel.setPassword(loginModel.getPassword());
        userModel.setName(firebaseUser.getDisplayName());
        userModel.setPhoneNumber(firebaseUser.getPhoneNumber());
        userModel.setUid(firebaseUser.getUid());

        return userModel;
    }
}
