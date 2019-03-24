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
import com.faiteasytrack.adapters.DriverAdapter;
import com.faiteasytrack.constants.FirebaseKeys;
import com.faiteasytrack.models.DriverModel;
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

public class NDriverActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NDriverActivity";

    private View loader;
    private RelativeLayout layoutFoundNothing;

    private RecyclerView recyclerDrivers;
    private ArrayList<DriverModel> driverModels;
    private DriverAdapter adapterDrivers;

    private FloatingActionButton fabAddDriver;

    private ProgressDialog progressDialog;

    private FirebaseUser firebaseUser;
    private DatabaseReference driversReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        driversReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.DRIVERS_DB);

        setContentView(R.layout.activity_driver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add_driver:{
                startActivity(new Intent(this, NAddDriverActivity.class));
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

        fabAddDriver = findViewById(R.id.fab_add_driver);

        layoutFoundNothing = findViewById(R.id.layout_found_nothing);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void setUpListeners() {
        fabAddDriver.setOnClickListener(this);
        driversReference.addChildEventListener(driverChangesEventListener);
    }

    @Override
    public void setUpData() {
        fetchAllDriversRegisteredWithThisVendor();
    }

    @Override
    public void setUpRecycler() {
        driverModels = new ArrayList<>();

        recyclerDrivers = findViewById(R.id.recycler_drivers);
        recyclerDrivers.setLayoutManager(new LinearLayoutManager(this));

        adapterDrivers = new DriverAdapter(this, driverModels, new DriverAdapter.OnDriverSelectedListener() {
            @Override
            public void onDriverSelected(int position, DriverModel driverModel) {

            }
        });
        recyclerDrivers.setAdapter(adapterDrivers);
    }

    private void fetchAllDriversRegisteredWithThisVendor() {
        queryFetchDrivers = driversReference.orderByChild(FirebaseKeys.REGISTERED_BY_VENDOR_UID)
                .equalTo(firebaseUser.getUid());

        queryFetchDrivers.addValueEventListener(fetchDriversEventListener);
    }

    private Query queryFetchDrivers;

    private ValueEventListener fetchDriversEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot querySnapshot) {
            Log.i(TAG, "fetchDriversEventListener.onDataChange: " + querySnapshot);

            driverModels.clear();
            for (DataSnapshot dataSnapshot : querySnapshot.getChildren()) {
                DriverModel driverModel = dataSnapshot.getValue(DriverModel.class);

                driverModels.add(driverModel);
            }
            adapterDrivers.notifyDataSetChanged();

            if (driverModels.size() == 0)
                ViewUtils.showViews(layoutFoundNothing);
            else
                ViewUtils.hideViews(layoutFoundNothing);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "fetchDriversEventListener.onCancelled: " + databaseError.getMessage());

            DialogUtils.showSorryAlert(NDriverActivity.this, databaseError.getMessage(), null);
        }
    };

    private ChildEventListener driverChangesEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.i(TAG, "driverChangesEventListener.onChildAdded: " + dataSnapshot);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.i(TAG, "driverChangesEventListener.onChildChanged: " + dataSnapshot);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "driverChangesEventListener.onChildRemoved: " + dataSnapshot);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.i(TAG, "driverChangesEventListener.onChildMoved: " + dataSnapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "driverChangesEventListener.onCancelled: " + databaseError.getMessage());
        }
    };

    @Override
    public void updateInternetError(boolean isOnline) {

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        driversReference.removeEventListener(driverChangesEventListener);
        queryFetchDrivers.removeEventListener(fetchDriversEventListener);
    }
}
