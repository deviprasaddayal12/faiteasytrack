package com.faiteasytrack.managers;

import android.content.Context;

import com.faiteasytrack.enums.Error;
import com.faiteasytrack.helpers.RequestHelper;
import com.faiteasytrack.helpers.TrackingHelper;
import com.faiteasytrack.listeners.RequestListener;
import com.faiteasytrack.listeners.TrackingListener;
import com.faiteasytrack.classess.ETLatLng;
import com.faiteasytrack.models.FriendModel;
import com.faiteasytrack.models.RequestModel;
import com.faiteasytrack.models.TripModel;

import java.util.ArrayList;

public class DashboardManager {

    public static final String TAG = "DashboardManager";

    public interface DashboardListener{
        void onNewRequestReceived();

        void onNewTripStarted(TripModel newTripModel);

        void onTripFetchSuccess(TripModel lastTripModel, boolean isOngoing);

        void onNewLatLngReceived(TripModel currentTripModel, ETLatLng newLatLng);

        void onTripFetchFailed(String error);

        void onTripFinished();
    }

    private DashboardListener dashboardListener;
    private Context gContext;

    public DashboardManager(Context gContext, DashboardListener dashboardListener) {
        this.gContext = gContext;
        this.dashboardListener = dashboardListener;
        initInteractors();
    }

    private RequestHelper requestHelper;

    private TrackingHelper trackingHelper;

    private void initInteractors(){
        requestHelper = new RequestHelper(gContext, requestListener);
//        requestHelper.initRequestDatabases();
//        requestHelper.countNewRequest();

        trackingHelper = new TrackingHelper(gContext, trackingListener);
    }

    public void startTracking(FriendModel friendModel){
        trackingHelper.startTracking(friendModel);
    }

    public void stopTracking(){
        trackingHelper.stopTracking();
    }

    public void refreshTracking(FriendModel friendModel){
        trackingHelper.refreshTrackingToNewTrip(friendModel);
    }

    private TrackingListener trackingListener = new TrackingListener() {
        @Override
        public void onNewTripStarted(TripModel newTripModel) {
            dashboardListener.onNewTripStarted(newTripModel);
        }

        @Override
        public void onLastTripFetchSuccess(TripModel lastTripModel, boolean isOngoing) {
            dashboardListener.onTripFetchSuccess(lastTripModel, isOngoing);
        }

        @Override
        public void onLastTripFetchFailure(Error.ErrorType errorType) {
            dashboardListener.onTripFetchFailed(errorType.toString());
        }

        @Override
        public void onNewLocationReceived(TripModel tripModel, ETLatLng newLatLng) {
            dashboardListener.onNewLatLngReceived(tripModel, newLatLng);
        }

        @Override
        public void onTripFinished(TripModel tripModel) {
            dashboardListener.onTripFinished();
        }
    };

    private RequestListener requestListener = new RequestListener() {
        @Override
        public void onNewRequestReceived(RequestModel requestModel) {

        }

        @Override
        public void onRequestSendSuccess(RequestModel requestModel) {

        }

        @Override
        public void onRequestSendFailed(RequestModel requestModel, Error.ErrorType errorTypeMessage) {

        }

        @Override
        public void onRequestSending(RequestModel requestModel) {

        }

        @Override
        public void onRequestStatusUpdated(RequestModel requestModel) {

        }

        @Override
        public void onCreteRequestFailed(Error.ErrorType s) {

        }

        @Override
        public void onNewRequestsCounted(int count) {

        }

        @Override
        public void onRequestsFetchFailed(Error.ErrorType message) {

        }

        @Override
        public void onAllReceivedRequestsFetched(ArrayList<RequestModel> requestModels) {

        }

        @Override
        public void onAllSentRequestsFetched(ArrayList<RequestModel> requestModels) {

        }

        @Override
        public void onStatusUpdateFailed(Error.ErrorType message) {

        }
    };
}
