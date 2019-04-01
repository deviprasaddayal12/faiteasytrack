package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.faiteasytrack.R;
import com.faiteasytrack.constants.FirebaseKeys;
import com.faiteasytrack.models.VendorModel;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.Utils;
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
import androidx.appcompat.widget.Toolbar;

public class NAddVendorActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NAddVendorActivity";

    private ProgressDialog progressDialog;

    private TextInputEditText etAdminName, etAdminPhone, etPhone, etName, etCode, etPassword;
    private MaterialButton btnAddVendor;

    private FirebaseUser firebaseUser;
    private DatabaseReference vendorsReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        vendorsReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.VENDORS_DB);

        setContentView(R.layout.activity_add_vendor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_vendor:
                if (isAddVendorValid())
                    addNewVendor();
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

        etAdminName = findViewById(R.id.et_admin_name);
        etAdminPhone = findViewById(R.id.et_admin_phone);
        etPhone = findViewById(R.id.et_vendor_phone);
        etPhone.setFilters(Utils.getLengthFilter(10));
        etName = findViewById(R.id.et_vendor_name);
        etCode = findViewById(R.id.et_vendor_code);
        etCode.setFilters(Utils.getLengthFilter(10));
        etPassword = findViewById(R.id.et_vendor_password);
        etPassword.setFilters(Utils.getLengthFilter(10));

        btnAddVendor = findViewById(R.id.btn_add_vendor);
    }

    @Override
    public void setUpListeners() {
        btnAddVendor.setOnClickListener(this);

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
                    checkIfVendorExists();
                }
            }
        });
    }

    private void checkIfVendorExists() {
        queryFindVendor = vendorsReference.orderByChild(FirebaseKeys.PHONE_NUMBER)
                .equalTo(etPhone.getText().toString());
        queryFindVendor.addValueEventListener(findVendorEventListener);
    }

    private Query queryFindVendor;
    private ValueEventListener findVendorEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot querySnapshot) {
            Log.i(TAG, "findVendorEventListener.onDataChange: " + querySnapshot);
            boolean isVendorExists = false;

            for (DataSnapshot dataSnapshot : querySnapshot.getChildren()) {
                VendorModel vendorModel = dataSnapshot.getValue(VendorModel.class);
                if (vendorModel != null && vendorModel.getPhoneNumber().equals(etPhone.getText().toString()))
                    isVendorExists = true;
            }

            if (isVendorExists)
                onVendorExistsAlready();
            else
                onVendorIsNew();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "findVendorEventListener.onCancelled: " + databaseError.getMessage());
            hideProgressDialog();
            DialogUtils.showSorryAlert(NAddVendorActivity.this, databaseError.getMessage(), null);
        }
    };

    private ValueEventListener addVendorEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "addVendorEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            DialogUtils.showCheersAlert(NAddVendorActivity.this, "Vendor added successfully.",
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "addVendorEventListener.onCancelled: " + databaseError.getMessage());

            hideProgressDialog();
            DialogUtils.showSorryAlert(NAddVendorActivity.this, "" + databaseError.getMessage(),
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
        }
    };

    private void onVendorExistsAlready() {
        queryFindVendor.removeEventListener(findVendorEventListener);
        hideProgressDialog();

        DialogUtils.showSorryAlert(this, "A vendor is already registered with the given number." +
                " Please use a different number.", null);
    }

    private void onVendorIsNew() {
        queryFindVendor.removeEventListener(findVendorEventListener);
        Snackbar.make(btnAddVendor, "You're good to go!", Snackbar.LENGTH_SHORT).show();
        hideProgressDialog();
    }

    private boolean isAddVendorValid() {
        boolean isAddVendorValid = true;

        if (Utils.isInvalidString(etName.getText().toString())) {
            isAddVendorValid = false;
            etName.setError("Give valid name!");
        } else if (Utils.isInvalidString(etPhone.getText().toString())) {
            isAddVendorValid = false;
            etPhone.setError("Give valid phone!");
        } else if (Utils.isInvalidString(etCode.getText().toString())) {
            isAddVendorValid = false;
            etCode.setError("Give valid code!");
        } else if (Utils.isInvalidString(etPassword.getText().toString())) {
            isAddVendorValid = false;
            etPassword.setError("Give valid password!");
        }

        return isAddVendorValid;
    }

    private void addNewVendor() {
        VendorModel vendorModel = new VendorModel();

        vendorModel.setRegisteredByAdminUid(firebaseUser.getUid());
        vendorModel.setRegisteredByAdminName(firebaseUser.getDisplayName());
        vendorModel.setRegisteredByAdminPhone(firebaseUser.getPhoneNumber());
        vendorModel.setName(etName.getText().toString());
        vendorModel.setCode(etCode.getText().toString());
        vendorModel.setPhoneNumber(etPhone.getText().toString());
        vendorModel.setPassword(etPassword.getText().toString());

        vendorsReference.addValueEventListener(addVendorEventListener);
        vendorsReference.push().setValue(vendorModel);
    }

    @Override
    public void setUpData() {
        etAdminPhone.setText(firebaseUser.getDisplayName());
        etAdminPhone.setInputType(InputType.TYPE_NULL);
        etAdminName.setText(firebaseUser.getPhoneNumber());
        etAdminName.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void setUpRecycler() {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (vendorsReference != null)
            vendorsReference.removeEventListener(addVendorEventListener);
    }

    // todo : send push to phone number with code and password
}
