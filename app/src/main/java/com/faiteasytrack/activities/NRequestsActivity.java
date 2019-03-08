package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.faiteasytrack.R;
import com.faiteasytrack.adapters.DriverAdapter;
import com.faiteasytrack.adapters.RequestAdapter;
import com.faiteasytrack.models.DriverModel;
import com.faiteasytrack.models.RequestModel;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NRequestsActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NRequestsActivity";

    private View loader;
    private RelativeLayout layoutFoundNothing;

    private RecyclerView recyclerRequests;
    private ArrayList<RequestModel> requestModels;
    private RequestAdapter adapterRequest;

    private FloatingActionButton fabAddRequest;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add_request:{

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

        fabAddRequest = findViewById(R.id.fab_add_request);

        layoutFoundNothing = findViewById(R.id.layout_found_nothing);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void setUpListeners() {
        fabAddRequest.setOnClickListener(this);
    }

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {
        requestModels = new ArrayList<>();

        recyclerRequests = findViewById(R.id.recycler_requests);
        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));

        adapterRequest = new RequestAdapter(this, requestModels, new RequestAdapter.OnRequestSelectedListener() {
            @Override
            public void onRequestSelected(int position, RequestModel requestModel) {

            }

            @Override
            public void onRequestUpdated(int position, RequestModel requestModel, boolean isAccepted) {

            }
        });
        recyclerRequests.setAdapter(adapterRequest);
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
