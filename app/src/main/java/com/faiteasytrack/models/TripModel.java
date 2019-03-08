package com.faiteasytrack.models;

import android.content.Context;

import com.faiteasytrack.classess.ETLatLng;
import com.faiteasytrack.utils.SharePreferences;

import java.util.ArrayList;

public class TripModel {
    private static TripModel tripModel = null;

    private String userId;
    private boolean isTripOngoing;
    private long tripStartTime;
    private long tripEndTime;
    private ETLatLng tripSource;
    private ETLatLng tripDest;
    private ArrayList<ETLatLng> wayPoints = new ArrayList<>();

//    private ArrayList<MyLocation> wayPoints = new ArrayList<>();

    private TripModel() {
    }

    public static TripModel getInstance(Context context) {
        if (tripModel == null) {
            if (SharePreferences.isTracingOngoing(context)) {
                tripModel = SharePreferences.getLastUnfinishedTripModel(context);
            }
            if (tripModel == null)
                tripModel = new TripModel();
        }
        return tripModel;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isTripOngoing() {
        return isTripOngoing;
    }

    public void setTripOngoing(boolean tripOngoing) {
        isTripOngoing = tripOngoing;
    }

    public long getTripStartTime() {
        return tripStartTime;
    }

    public void setTripStartTime(long tripStartTime) {
        this.tripStartTime = tripStartTime;
    }

    public long getTripEndTime() {
        return tripEndTime;
    }

    public void setTripEndTime(long tripEndTime) {
        this.tripEndTime = tripEndTime;
    }

    public ETLatLng getTripSource() {
        return tripSource;
    }

    public void setTripSource(ETLatLng tripSource) {
        this.tripSource = tripSource;
    }

    public ETLatLng getTripDest() {
        return tripDest;
    }

    public void setTripDest(ETLatLng tripDest) {
        this.tripDest = tripDest;
    }

    public ArrayList<ETLatLng> getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(ETLatLng wayPoint) {
        this.wayPoints.add(wayPoint);
    }

    public void reset() {
        wayPoints.clear();
        tripModel = null;
    }
}
