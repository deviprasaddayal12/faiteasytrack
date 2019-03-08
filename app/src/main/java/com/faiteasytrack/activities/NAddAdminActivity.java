package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.faiteasytrack.R;
import com.faiteasytrack.enums.FirebaseKeys;
import com.faiteasytrack.models.AdminModel;
import com.faiteasytrack.models.VendorModel;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.views.EasytrackButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import androidx.appcompat.widget.Toolbar;

public class NAddAdminActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NAddAdminActivity";

    private ProgressDialog progressDialog;

    private TextInputLayout tilAdminName, tilAdminPhone, tilPhone, tilName, tilCode, tilPassword;
    private TextInputEditText etAdminName, etAdminPhone, etPhone, etName, etCode, etPassword;
    private MaterialButton btnAddAdmin;

    private FirebaseUser firebaseUser;
    private DatabaseReference adminReference, userReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        userReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.USERS_DB);
        adminReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.ADMINS_DB);

        setContentView(R.layout.activity_add_admin);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_admin:
                if (isAddAdminValid())
                    addNewAdmin();
                break;
        }
    }

    @Override
    public void setUpActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void initUI() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        tilAdminName = findViewById(R.id.til_admin_name);
        tilAdminPhone = findViewById(R.id.til_admin_phone);
        tilPhone = findViewById(R.id.til_phone);
        tilName = findViewById(R.id.til_name);
        tilCode = findViewById(R.id.til_code);
        tilPassword = findViewById(R.id.til_password);

        etAdminName = findViewById(R.id.et_reg_admin_name);
        etAdminPhone = findViewById(R.id.et_reg_admin_phone);
        etPhone = findViewById(R.id.et_admin_phone);
        etPhone.setFilters(Utils.getLengthFilter(10));
        etName = findViewById(R.id.et_admin_name);
        etCode = findViewById(R.id.et_admin_code);
        etCode.setFilters(Utils.getLengthFilter(10));
        etPassword = findViewById(R.id.et_admin_password);
        etPassword.setFilters(Utils.getLengthFilter(10));

        btnAddAdmin = findViewById(R.id.btn_add_admin);
    }

    @Override
    public void setUpListeners() {
        btnAddAdmin.setOnClickListener(this);
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    showProgressDialog(null, "Looking for existing vendor...");
                    checkIfAdminExists();
                }
            }
        });
    }

    private void checkIfAdminExists() {
        queryFindAdmin = userReference.orderByChild(FirebaseKeys.PHONE_NUMBER)
                .equalTo(etPhone.getText().toString());
        queryFindAdmin.addValueEventListener(findAdminEventListener);
    }

    private Query queryFindAdmin;
    private ValueEventListener findAdminEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot querySnapshot) {
            Log.i(TAG, "findAdminEventListener.onDataChange: " + querySnapshot);
            boolean isVendorExists = false;

            for (DataSnapshot dataSnapshot : querySnapshot.getChildren()) {
                VendorModel vendorModel = dataSnapshot.getValue(VendorModel.class);
                if (vendorModel != null && vendorModel.getPhoneNumber().equals(etPhone.getText().toString()))
                    isVendorExists = true;
            }

            if (isVendorExists)
                onAdminExistsAlready();
            else
                onAdminIsNew();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "findAdminEventListener.onCancelled: " + databaseError.getMessage());
            hideProgressDialog();
            DialogUtils.showSorryAlert(NAddAdminActivity.this, databaseError.getMessage(), null);
        }
    };

    private ValueEventListener addAdminEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "addAdminEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            DialogUtils.showCheersAlert(NAddAdminActivity.this, "Admin added successfully.",
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "addAdminEventListener.onCancelled: " + databaseError.getMessage());

            hideProgressDialog();
            DialogUtils.showSorryAlert(NAddAdminActivity.this, "" + databaseError.getMessage(),
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
        }
    };

    private void onAdminExistsAlready() {
        queryFindAdmin.removeEventListener(findAdminEventListener);
        hideProgressDialog();

        DialogUtils.showSorryAlert(this, "An admin is already registered with the given number." +
                " Please use a different number.", null);
    }

    private void onAdminIsNew() {
        queryFindAdmin.removeEventListener(findAdminEventListener);
        Snackbar.make(btnAddAdmin, "You're good to go!", Snackbar.LENGTH_SHORT).show();
        hideProgressDialog();
    }

    private boolean isAddAdminValid() {
        boolean isAddAdminValid = true;

        if (Utils.isInvalidString(etName.getText().toString())) {
            isAddAdminValid = false;
            etName.setError("Give valid name!");
        }
        if (Utils.isInvalidString(etPhone.getText().toString())) {
            isAddAdminValid = false;
            etPhone.setError("Give valid phone!");
        }
        if (Utils.isInvalidString(etCode.getText().toString())) {
            isAddAdminValid = false;
            etCode.setError("Give valid code!");
        }
        if (Utils.isInvalidString(etPassword.getText().toString())) {
            isAddAdminValid = false;
            etPassword.setError("Give valid password!");
        }

        return isAddAdminValid;
    }

    private void addNewAdmin() {
        AdminModel adminModel = new AdminModel();

        adminModel.setRegisteredByAdminUid(firebaseUser.getUid());
        adminModel.setRegisteredByAdminName(firebaseUser.getDisplayName());
        adminModel.setRegisteredByAdminPhone(firebaseUser.getPhoneNumber());
        adminModel.setName(etName.getText().toString());
        adminModel.setCode(etCode.getText().toString());
        adminModel.setPhoneNumber(etPhone.getText().toString());
        adminModel.setPassword(etPassword.getText().toString());

        adminReference.addValueEventListener(addAdminEventListener);
        adminReference.push().setValue(adminModel);
    }

    @Override
    public void setUpData() {
        etAdminPhone.setText(firebaseUser.getPhoneNumber());
        etAdminPhone.setInputType(InputType.TYPE_NULL);
        etAdminName.setText(firebaseUser.getDisplayName());
        etAdminName.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void setUpRecycler() {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adminReference != null)
            adminReference.removeEventListener(addAdminEventListener);
    }

    // todo : send push to phone number with code and password
}
