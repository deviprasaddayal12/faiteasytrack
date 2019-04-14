package com.faiteasytrack.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.faiteasytrack.R;
import com.faiteasytrack.firebase.FirebaseKeys;
import com.faiteasytrack.firebase.FirebaseUtils;
import com.faiteasytrack.models.AdminModel;
import com.faiteasytrack.models.VendorModel;
import com.faiteasytrack.utils.Constants;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;

public class AddAdminActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = AddAdminActivity.class.getCanonicalName();

    private ContentLoadingProgressBar progressBar;

    private TextInputEditText etPhone, etName, etCode, etPassword;
    private MaterialButton btnAddAdmin;

    private FirebaseUser firebaseUser;
    private DatabaseReference adminReference, userReference;

    private TextWatcher phoneTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 10) checkIfAdminExists();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseUtils.getUser();
        userReference = FirebaseUtils.getUserReference();
        adminReference = FirebaseUtils.getAdminReference();

        setContentView(R.layout.activity_add_admin);
    }

    @Override
    public void setUpActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void initUI() {
        progressBar = findViewById(R.id.pb_loading_prompt);

        etPhone = findViewById(R.id.et_phone);
        etName = findViewById(R.id.et_name);
        etCode = findViewById(R.id.et_code);
        etPassword = findViewById(R.id.et_password);

        etName.setFilters(Utils.getLengthFilter(Constants.MAX_LENGTH_NAME));
        etPhone.setFilters(Utils.getLengthFilter(Constants.MAX_LENGTH_PHONE));
        etCode.setFilters(Utils.getLengthFilter(Constants.MAX_LENGTH_CODE));
        etPassword.setFilters(Utils.getLengthFilter(Constants.MAX_LENGTH_PASSWORD));

        btnAddAdmin = findViewById(R.id.btn_add_admin);
    }

    @Override
    public void setUpListeners() {
        btnAddAdmin.setOnClickListener(this);
        etPhone.addTextChangedListener(phoneTextWatcher);
    }

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_admin:
                if (isAddAdminValid()) addNewAdmin();
                break;
        }
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

            DialogUtils.showSorryAlert(AddAdminActivity.this, databaseError.getMessage(), null);
        }
    };

    private ValueEventListener addAdminEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "addAdminEventListener.onDataChange: " + dataSnapshot);

            DialogUtils.showCheersAlert(AddAdminActivity.this, "Admin added successfully.",
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

            DialogUtils.showSorryAlert(AddAdminActivity.this, "" + databaseError.getMessage(),
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

        DialogUtils.showSorryAlert(this, "An admin is already registered with the given number." +
                " Please use a different number.", null);
    }

    private void onAdminIsNew() {
        queryFindAdmin.removeEventListener(findAdminEventListener);
        Snackbar.make(btnAddAdmin, "You're good to go!", Snackbar.LENGTH_SHORT).show();

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

        adminModel.setUid_(firebaseUser.getUid());
        adminModel.setName_(firebaseUser.getDisplayName());
        adminModel.setPhoneNumber_(firebaseUser.getPhoneNumber());

        adminModel.setName(etName.getText().toString());
        adminModel.setCode(etCode.getText().toString());
        adminModel.setPhoneNumber(etPhone.getText().toString());
        adminModel.setPassword(etPassword.getText().toString());

        adminReference.addValueEventListener(addAdminEventListener);
        adminReference.push().setValue(adminModel);
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
    protected void onDestroy() {
        super.onDestroy();

        if (adminReference != null) adminReference.removeEventListener(addAdminEventListener);
    }

    // todo : send push to phone number with code and password
}
