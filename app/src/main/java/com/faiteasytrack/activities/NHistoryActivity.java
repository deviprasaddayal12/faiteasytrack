package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.faiteasytrack.R;
import com.faiteasytrack.adapters.FriendAdapter;
import com.faiteasytrack.adapters.HistoryAdapter;
import com.faiteasytrack.models.FriendModel;
import com.faiteasytrack.models.HistoryModel;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NHistoryActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NHistoryActivity";

    private View loader;
    private RelativeLayout layoutFoundNothing;

    private RecyclerView recyclerHistory;
    private ArrayList<HistoryModel> historyModels;
    private HistoryAdapter adapterHistory;

    private FloatingActionButton fabAddHistory;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add_history:{

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

        fabAddHistory = findViewById(R.id.fab_add_history);

        layoutFoundNothing = findViewById(R.id.layout_found_nothing);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void setUpListeners() {
        fabAddHistory.setOnClickListener(this);
    }

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {
        historyModels = new ArrayList<>();

        recyclerHistory = findViewById(R.id.recycler_historys);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));

        adapterHistory = new HistoryAdapter(this, historyModels, new HistoryAdapter.OnHistorySelectedListener() {
            @Override
            public void onHistorySelected(int position, HistoryModel driverModel) {

            }
        });
        recyclerHistory.setAdapter(adapterHistory);
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
