package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;

import com.faiteasytrack.R;
import com.faiteasytrack.adapters.FriendAdapter;
import com.faiteasytrack.constants.Error;
import com.faiteasytrack.helpers.FriendHelper;
import com.faiteasytrack.listeners.FriendListener;
import com.faiteasytrack.models.FriendModel;
import com.faiteasytrack.utils.Constants;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class OFriendsActivity extends BaseActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener, FriendListener {

    public static final String TAG = "OFriendsActivity";

    private View loader;

    private FriendHelper friendHelper;

    private RecyclerView recyclerFriends;
    private ArrayList<FriendModel> friendModels;
    private FriendAdapter adapterFriends;

    private RelativeLayout layoutFoundNothing;

    private boolean isContactsCallBusy = false;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_add_friend && !isContactsCallBusy) {
            isContactsCallBusy = true;
            startActivityForResult(new Intent(this, OContactsActivity.class), Constants.INTENT_LAUNCH_CODES.START_CONTACTS_TO_REQUEST_NEW);
        }
    }

    @Override
    public void setUpActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void initUI() {
        layoutFoundNothing = findViewById(R.id.layout_found_nothing);

        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        loader = viewStubLoader.inflate();
        loader.setOnClickListener(this);

        ViewUtils.hideViews(loader);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Looking for requests");
        progressDialog.setMessage("Please be patient while we try to your requests....");
    }

    @Override
    public void setUpListeners() {
        findViewById(R.id.fab_add_friend).setOnClickListener(this);
    }

    @Override
    public void setUpData() {
//        showViews(loader);
        progressDialog.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }, 7000);

        friendHelper = new FriendHelper(this, this);
        friendHelper.getAllFriends();
    }

    @Override
    public void setUpRecycler() {
        friendModels = new ArrayList<>();
        checkEmptyList();

        recyclerFriends = findViewById(R.id.recycler_friends);
        recyclerFriends.setLayoutManager(new LinearLayoutManager(this));

        adapterFriends = new FriendAdapter(this, friendModels, new FriendAdapter.OnFriendSelectedListener() {
            @Override
            public void onFriendSelected(int position, FriendModel friendModel) {
                Intent backIntent = new Intent();
                backIntent.putExtra(Constants.INTENT_EXTRA_KEYS.SELECTED_FRIEND_MODEL_TO_TRACK, friendModel);
                setResult(Constants.INTENT_LAUNCH_CODES.START_FRIENDS_ACTIVITY_FOR_TRACKING, backIntent);
                finish();
            }
        });

        recyclerFriends.setAdapter(adapterFriends);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        isContactsCallBusy = false;
    }

    private Handler handler = new Handler();

    @Override
    public void onAllFriendsFetched(ArrayList<FriendModel> friendModels) {

        this.friendModels.clear();
        this.friendModels.addAll(friendModels);
        adapterFriends.notifyDataSetChanged();

//        hideViews(loader);
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        checkEmptyList();
    }

    @Override
    public void onNewFriendAdded(FriendModel friendModel, ArrayList<FriendModel> friendModels) {
        this.friendModels.add(friendModel);
        adapterFriends.notifyDataSetChanged();

        checkEmptyList();
    }

    @Override
    public void onFriendAddFailed(Error.ErrorType errorTypeMessage) {
        ViewUtils.makeToast(this, errorTypeMessage.toString());
        checkEmptyList();
    }

    private void checkEmptyList() {
        if (friendModels.size() == 0)
            ViewUtils.showViews(layoutFoundNothing);
        else
            ViewUtils.hideViews(layoutFoundNothing);
    }

    @Override
    public void updateInternetStatus(boolean online) {

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
