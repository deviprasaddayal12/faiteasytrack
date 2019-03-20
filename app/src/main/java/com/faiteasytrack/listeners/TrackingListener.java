package com.faiteasytrack.listeners;

import com.faiteasytrack.enums.Error;
import com.faiteasytrack.classess.ETLatLng;
import com.faiteasytrack.models.TripModel;

public interface TrackingListener {
    void onNewTripStarted(TripModel newTripModel);

    void onLastTripFetchSuccess(TripModel lastTripModel, boolean isOngoing);

    void onLastTripFetchFailure(Error.ErrorType errorType);

    void onNewLocationReceived(TripModel tripModel, ETLatLng newLatLng);

    void onTripFinished(TripModel tripModel);
}
