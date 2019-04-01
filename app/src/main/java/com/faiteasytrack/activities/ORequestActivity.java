package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;

import com.faiteasytrack.R;
import com.faiteasytrack.adapters.RequestAdapter;
import com.faiteasytrack.constants.Error;
import com.faiteasytrack.constants.Request;
import com.faiteasytrack.helpers.FriendHelper;
import com.faiteasytrack.listeners.FriendListener;
import com.faiteasytrack.helpers.RequestHelper;
import com.faiteasytrack.listeners.RequestListener;
import com.faiteasytrack.models.FriendModel;
import com.faiteasytrack.models.RequestModel;
import com.faiteasytrack.models.RequestStatusModel;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ORequestActivity extends BaseActivity implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener, RequestListener, FriendListener {

    public static final String TAG = "ORequestActivity";

    private View loader;
    private RequestHelper requestHelper;
    private FriendHelper friendHelper;

    private RecyclerView recyclerRequests;
    private ArrayList<RequestModel> requestModels;
    private RequestAdapter adapterRequests;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
    }

    @Override
    public void onClick(View v) {

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
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Looking for requests");
        progressDialog.setMessage("Please be patient while we try to your requests....");
    }

    @Override
    public void setUpListeners() {

    }

    @Override
    public void setUpData() {
        progressDialog.show();

        requestHelper = new RequestHelper(this, this);
        requestHelper.fetchAllReceivedRequests();
        requestHelper.initRequestDatabases();

        friendHelper = new FriendHelper(this, this);
    }

    @Override
    public void setUpRecycler() {
        requestModels = new ArrayList<>();

//        showViews(loader);

        recyclerRequests = findViewById(R.id.recycler_requests);
        adapterRequests = new RequestAdapter(this, requestModels, new RequestAdapter.OnRequestSelectedListener() {
            @Override
            public void onRequestSelected(int position, RequestModel requestModel) {

            }

            @Override
            public void onRequestUpdated(int position, RequestModel requestModel, boolean isAccepted) {
                if (isAccepted) {
                    RequestStatusModel requestStatusModel =
                            Request.Status.getRequestStatusModel(Request.REQUEST_ACCEPTED);
                    requestStatusModel.setUpdatedAtMillis(new Date().getTime());
                    requestModel.setRequestStatusModel(requestStatusModel);

                    requestHelper.onUpdateReceivedRequest(requestModel);
                }
            }
        });

        recyclerRequests.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayout.VERTICAL);
        recyclerRequests.addItemDecoration(dividerItemDecoration);
        recyclerRequests.setAdapter(adapterRequests);
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
    public void onNewRequestReceived(RequestModel requestModel) {
        requestModels.add(requestModel);
        adapterRequests.notifyDataSetChanged();
    }

    @Override
    public void onRequestSendSuccess(RequestModel requestModel) {

    }

    @Override
    public void onRequestsFetchFailed(Error.ErrorType errorType) {

    }

    @Override
    public void onRequestSending(RequestModel requestModel) {

    }

    @Override
    public void onRequestStatusUpdated(RequestModel requestModel) {
        // todo add friends
        friendHelper.addNewFriends(requestModel);
    }

    @Override
    public void onCreteRequestFailed(Error.ErrorType errorType) {

    }

    @Override
    public void onNewRequestsCounted(int count) {

    }

    @Override
    public void onRequestSendFailed(RequestModel requestModel, Error.ErrorType errorType) {

    }

    @Override
    public void onAllReceivedRequestsFetched(ArrayList<RequestModel> requestModels) {
        this.requestModels.clear();
        this.requestModels.addAll(requestModels);
        adapterRequests.notifyDataSetChanged();

        if (progressDialog.isShowing())
            progressDialog.dismiss();
//        hideViews(loader);
    }

    @Override
    public void onAllSentRequestsFetched(ArrayList<RequestModel> requestModels) {

    }

    @Override
    public void onStatusUpdateFailed(Error.ErrorType errorType) {

    }

    @Override
    protected void onDestroy() {
        if (isFinishing())
            requestHelper.removeChildListeners();
        super.onDestroy();
    }

    @Override
    public void onAllFriendsFetched(ArrayList<FriendModel> friendModels) {

    }

    @Override
    public void onNewFriendAdded(FriendModel friendModel, ArrayList<FriendModel> friendModels) {

    }

    @Override
    public void onFriendAddFailed(Error.ErrorType errorType) {

    }

    @Override
    public void updateInternetStatus(boolean online) {

    }
}
