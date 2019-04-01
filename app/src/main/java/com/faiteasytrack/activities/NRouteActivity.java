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
import com.faiteasytrack.adapters.RouteAdapter;
import com.faiteasytrack.models.DriverModel;
import com.faiteasytrack.models.RouteModel;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NRouteActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NRouteActivity";

    private View loader;
    private RelativeLayout layoutFoundNothing;

    private RecyclerView recyclerRoutes;
    private ArrayList<RouteModel> routeModels;
    private RouteAdapter adapterRoutes;

    private FloatingActionButton fabAddRoute;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add_route:{
                startActivity(new Intent(this, NAddRouteActivity.class));
            }
            break;
        }
    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void initUI() {
        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        loader = viewStubLoader.inflate();
        loader.setOnClickListener(this);

        ViewUtils.hideViews(loader);

        fabAddRoute = findViewById(R.id.fab_add_route);

        layoutFoundNothing = findViewById(R.id.layout_found_nothing);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void setUpListeners() {
        fabAddRoute.setOnClickListener(this);
    }

    @Override
    public void setUpData() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void setUpRecycler() {
        routeModels = new ArrayList<>();

        recyclerRoutes = findViewById(R.id.recycler_routes);
        recyclerRoutes.setLayoutManager(new LinearLayoutManager(this));

        adapterRoutes = new RouteAdapter(this, routeModels, new RouteAdapter.OnRouteSelectedListener() {
            @Override
            public void onRouteSelected(int position, RouteModel routeModel) {

            }
        });
        recyclerRoutes.setAdapter(adapterRoutes);
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
