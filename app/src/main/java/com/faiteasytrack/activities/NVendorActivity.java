package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.faiteasytrack.R;
import com.faiteasytrack.adapters.VendorAdapter;
import com.faiteasytrack.enums.FirebaseKeys;
import com.faiteasytrack.models.VendorModel;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NVendorActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NVendorActivity";

    private View loader;
    private RelativeLayout layoutFoundNothing;

    private RecyclerView recyclerVendors;
    private ArrayList<VendorModel> vendorModels;
    private VendorAdapter adapterVendors;

    private FloatingActionButton fabAddVendor;

    private ProgressDialog progressDialog;

    private FirebaseUser firebaseUser;
    private DatabaseReference vendorsReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        vendorsReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.VENDORS_DB);
        setContentView(R.layout.activity_vendors);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_vendor: {
                startActivity(new Intent(this, NAddVendorActivity.class));
            }
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
        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        loader = viewStubLoader.inflate();
        loader.setOnClickListener(this);

        ViewUtils.hideViews(loader);

        fabAddVendor = findViewById(R.id.fab_add_vendor);

        layoutFoundNothing = findViewById(R.id.layout_found_nothing);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void setUpListeners() {
        fabAddVendor.setOnClickListener(this);
        vendorsReference.addChildEventListener(vendorChangesEventListener);
    }

    @Override
    public void setUpData() {
        fetchAllVendorsRegisteredWithThisAdmin();
    }

    @Override
    public void setUpRecycler() {
        vendorModels = new ArrayList<>();

        recyclerVendors = findViewById(R.id.recycler_vendors);
        recyclerVendors.setLayoutManager(new LinearLayoutManager(this));

        adapterVendors = new VendorAdapter(this, vendorModels, new VendorAdapter.OnVendorSelectedListener() {
            @Override
            public void onVendorSelected(int position, VendorModel vendorModel) {

            }
        });
        recyclerVendors.setAdapter(adapterVendors);
    }

    private void fetchAllVendorsRegisteredWithThisAdmin() {
        queryFetchVendors = vendorsReference.orderByChild(FirebaseKeys.REGISTERED_BY_ADMIN_UID)
                .equalTo(firebaseUser.getUid());

        queryFetchVendors.addValueEventListener(fetchVendorsEventListener);
    }

    private Query queryFetchVendors;

    private ValueEventListener fetchVendorsEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot querySnapshot) {
            Log.i(TAG, "fetchVendorsEventListener.onDataChange: " + querySnapshot);

            vendorModels.clear();
            for (DataSnapshot dataSnapshot : querySnapshot.getChildren()) {
                VendorModel vendorModel = dataSnapshot.getValue(VendorModel.class);

                vendorModels.add(vendorModel);
            }
            adapterVendors.notifyDataSetChanged();

            if (vendorModels.size() == 0)
                ViewUtils.showViews(layoutFoundNothing);
            else
                ViewUtils.hideViews(layoutFoundNothing);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "fetchVendorsEventListener.onCancelled: " + databaseError.getMessage());

            DialogUtils.showSorryAlert(NVendorActivity.this, databaseError.getMessage(), null);
        }
    };

    private ChildEventListener vendorChangesEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.i(TAG, "vendorChangesEventListener.onChildAdded: " + dataSnapshot);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.i(TAG, "vendorChangesEventListener.onChildChanged: " + dataSnapshot);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "vendorChangesEventListener.onChildRemoved: " + dataSnapshot);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.i(TAG, "vendorChangesEventListener.onChildMoved: " + dataSnapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "vendorChangesEventListener.onCancelled: " + databaseError.getMessage());
        }
    };

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

        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        vendorsReference.removeEventListener(vendorChangesEventListener);
        queryFetchVendors.removeEventListener(fetchVendorsEventListener);
    }
}
