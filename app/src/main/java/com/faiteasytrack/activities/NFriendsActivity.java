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
import com.faiteasytrack.adapters.FriendAdapter;
import com.faiteasytrack.helpers.FriendHelper;
import com.faiteasytrack.models.DriverModel;
import com.faiteasytrack.models.FriendModel;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NFriendsActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NFriendsActivity";

    private View loader;
    private RelativeLayout layoutFoundNothing;

    private RecyclerView recyclerFriends;
    private ArrayList<FriendModel> friendModels;
    private FriendAdapter adapterFriends;

    private FloatingActionButton fabAddFriend;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab_add_friend:{

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

        fabAddFriend = findViewById(R.id.fab_add_friend);

        layoutFoundNothing = findViewById(R.id.layout_found_nothing);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void setUpListeners() {
        fabAddFriend.setOnClickListener(this);
    }

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {
        friendModels = new ArrayList<>();

        recyclerFriends = findViewById(R.id.recycler_friends);
        recyclerFriends.setLayoutManager(new LinearLayoutManager(this));

        adapterFriends = new FriendAdapter(this, friendModels, new FriendAdapter.OnFriendSelectedListener() {
            @Override
            public void onFriendSelected(int position, FriendModel driverModel) {

            }
        });
        recyclerFriends.setAdapter(adapterFriends);
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
