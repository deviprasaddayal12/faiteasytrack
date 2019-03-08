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
import com.faiteasytrack.adapters.AdminAdapter;
import com.faiteasytrack.adapters.VendorAdapter;
import com.faiteasytrack.enums.FirebaseKeys;
import com.faiteasytrack.models.AdminModel;
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

public class NAdminActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NAdminActivity";

    private View loader;
    private RelativeLayout layoutFoundNothing;

    private RecyclerView recyclerAdmins;
    private ArrayList<AdminModel> adminModels;
    private AdminAdapter adapterAdmins;

    private FloatingActionButton fabAddAdmin;

    private ProgressDialog progressDialog;

    private FirebaseUser firebaseUser;
    private DatabaseReference adminsReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        adminsReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.ADMINS_DB);

        setContentView(R.layout.activity_admins);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_admin: {
                startActivity(new Intent(this, NAddAdminActivity.class));
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

        fabAddAdmin = findViewById(R.id.fab_add_admin);

        layoutFoundNothing = findViewById(R.id.layout_found_nothing);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void setUpListeners() {
        fabAddAdmin.setOnClickListener(this);
        adminsReference.addChildEventListener(adminChangesEventListener);
    }

    @Override
    public void setUpData() {
        fetchAllVendorsRegisteredWithThisAdmin();
    }

    @Override
    public void setUpRecycler() {
        adminModels = new ArrayList<>();

        recyclerAdmins = findViewById(R.id.recycler_admins);
        recyclerAdmins.setLayoutManager(new LinearLayoutManager(this));

        adapterAdmins = new AdminAdapter(this, adminModels, new AdminAdapter.OnAdminSelectedListener() {
            @Override
            public void onAdminSelected(int position, AdminModel adminModel) {

            }
        });
        recyclerAdmins.setAdapter(adapterAdmins);
    }

    private void fetchAllVendorsRegisteredWithThisAdmin() {
        queryFetchAdmins = adminsReference.orderByChild(FirebaseKeys.REGISTERED_BY_ADMIN_UID)
                .equalTo(firebaseUser.getUid());

        queryFetchAdmins.addValueEventListener(fetchAdminsEventListener);
    }

    private Query queryFetchAdmins;

    private ValueEventListener fetchAdminsEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot querySnapshot) {
            Log.i(TAG, "fetchAdminsEventListener.onDataChange: " + querySnapshot);

            adminModels.clear();
            for (DataSnapshot dataSnapshot : querySnapshot.getChildren()) {
                AdminModel adminModel = dataSnapshot.getValue(AdminModel.class);

                adminModels.add(adminModel);
            }
            adapterAdmins.notifyDataSetChanged();

            if (adminModels.size() == 0)
                ViewUtils.showViews(layoutFoundNothing);
            else
                ViewUtils.hideViews(layoutFoundNothing);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "fetchAdminsEventListener.onCancelled: " + databaseError.getMessage());

            DialogUtils.showSorryAlert(NAdminActivity.this, databaseError.getMessage(), null);
        }
    };

    private ChildEventListener adminChangesEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.i(TAG, "adminChangesEventListener.onChildAdded: " + dataSnapshot);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.i(TAG, "adminChangesEventListener.onChildChanged: " + dataSnapshot);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "adminChangesEventListener.onChildRemoved: " + dataSnapshot);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.i(TAG, "adminChangesEventListener.onChildMoved: " + dataSnapshot);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "adminChangesEventListener.onCancelled: " + databaseError.getMessage());
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

        adminsReference.removeEventListener(adminChangesEventListener);
        queryFetchAdmins.removeEventListener(fetchAdminsEventListener);
    }
}
