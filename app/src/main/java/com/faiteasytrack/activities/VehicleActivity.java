package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.faiteasytrack.R;
import com.faiteasytrack.adapters.VehicleAdapter;
import com.faiteasytrack.models.VehicleModel;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class VehicleActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NVehicleActivity";

    private View loader;
    private RelativeLayout layoutFoundNothing;

    private RecyclerView recyclerVehicles;
    private ArrayList<VehicleModel> vehicleModels;
    private VehicleAdapter adapterVehicles;

    private FloatingActionButton fabAddVehicle;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add_vehicle:{
                startActivity(new Intent(this, AddVehicleActivity.class));
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

        fabAddVehicle = findViewById(R.id.fab_add_vehicle);

        layoutFoundNothing = findViewById(R.id.layout_found_nothing);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void setUpListeners() {
        fabAddVehicle.setOnClickListener(this);
    }

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {
        vehicleModels = new ArrayList<>();

        recyclerVehicles = findViewById(R.id.recycler_vehicles);
        recyclerVehicles.setLayoutManager(new LinearLayoutManager(this));

        adapterVehicles = new VehicleAdapter(this, vehicleModels, new VehicleAdapter.OnVehicleSelectedListener() {
            @Override
            public void onVehicleSelected(int position, VehicleModel vehicleModel) {

            }
        });
        recyclerVehicles.setAdapter(adapterVehicles);
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
}
