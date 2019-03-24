package com.faiteasytrack.helpers;

import android.content.Context;
import android.util.Log;

import com.faiteasytrack.constants.Error;
import com.faiteasytrack.listeners.TrackingListener;
import com.faiteasytrack.customclasses.ETLatLng;
import com.faiteasytrack.models.FriendModel;
import com.faiteasytrack.models.TripModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TrackingHelper {

    public static final String TAG = "TrackingHelper";

    private Context gContext;
    private DatabaseReference gTripsBaseDb;

    private TrackingListener trackingListener;

    private DatabaseReference friendTripRef, currentTripRef, currentTripWayPointsRef;

    public TrackingHelper(Context gContext, TrackingListener trackingListener) {
        this.gContext = gContext;
        this.trackingListener = trackingListener;

        gTripsBaseDb = FirebaseDatabase.getInstance().getReference().child("trips");
    }

    private ValueEventListener valueEventListener_forLastTripInDbRef = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.e(TAG, "valueEventListener_forLastTripInDbRef.onDataChange: " + dataSnapshot);

            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                TripModel lastTripModel = dataSnapshot1.getValue(TripModel.class);

                for (DataSnapshot dataSnapshot2 : dataSnapshot1.child("wayPoints").getChildren()){
                    ETLatLng latLng = dataSnapshot2.getValue(ETLatLng.class);
                    lastTripModel.setWayPoints(latLng);
                }

                if (lastTripModel != null) {
                    startListeningToLocationChanges();
                    trackingListener.onLastTripFetchSuccess(lastTripModel, lastTripModel.isTripOngoing());
                } else
                    trackingListener.onLastTripFetchFailure(Error.ErrorType.ERROR_NOT_DEFINED);

                break;
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "valueEventListener_forLastTripInDbRef.onCancelled: " + databaseError.getMessage());

            trackingListener.onLastTripFetchFailure(Error.ErrorType.ERROR_NOT_DEFINED);
        }
    };

    private ChildEventListener childEventListener_forNewTripsStarted = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousKey) {
            Log.e(TAG, "childEventListener_forNewTripsStarted.onChildAdded: " + dataSnapshot);

            TripModel tripModel = dataSnapshot.getValue(TripModel.class);
            trackingListener.onNewTripStarted(tripModel);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private ChildEventListener childEventListener_forLastLatLngUpdated = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.e(TAG, "childEventListener_forLastLatLngUpdated.onDataChange: " + dataSnapshot);

            ETLatLng newLatLng = dataSnapshot.getValue(ETLatLng.class);
            trackingListener.onNewLocationReceived(null, newLatLng);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public void refreshTrackingToNewTrip(FriendModel friendModel) {
        stopTracking(); // stop tracking the old trip

        startTracking(friendModel); // start tracking the new one
    }

    public void startTracking(FriendModel friendModel) {
        friendTripRef = gTripsBaseDb.child(friendModel.getF_uid());
        startListeningToNewTripsCreated();

        Query friendTripQuery = friendTripRef.orderByKey().limitToLast(1);

        currentTripRef = friendTripQuery.getRef();
        currentTripRef.addValueEventListener(valueEventListener_forLastTripInDbRef);
    }

    private void startListeningToNewTripsCreated() {

        friendTripRef.addChildEventListener(childEventListener_forNewTripsStarted);
    }

    private void stopListeningToNewTripsCreated() {

        friendTripRef.removeEventListener(childEventListener_forNewTripsStarted);
    }

    private void startListeningToLocationChanges() {
        currentTripWayPointsRef = currentTripRef.child("wayPoints");

        currentTripWayPointsRef.addChildEventListener(childEventListener_forLastLatLngUpdated);
    }

    private void stopListeningToLocationChanges() {

        currentTripWayPointsRef.removeEventListener(childEventListener_forLastLatLngUpdated);
    }

    public void stopTracking() {

        stopListeningToLocationChanges();
        stopListeningToNewTripsCreated();
        friendTripRef.removeEventListener(valueEventListener_forLastTripInDbRef);
    }
}
