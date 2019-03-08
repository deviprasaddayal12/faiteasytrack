package com.faiteasytrack.helpers;

import android.content.Context;

import com.faiteasytrack.models.TripModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TracingHelper {

    public static void endMyTrip(Context context, String currentTripKey) {
        TripModel finishingTripModel = TripModel.getInstance(context);

        DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference("trips");

        DatabaseReference currentTripRef = tripReference.child(finishingTripModel.getUserId()).child(currentTripKey);
        currentTripRef.child("tripOngoing").setValue(finishingTripModel.isTripOngoing());
        currentTripRef.child("tripDest").setValue(finishingTripModel.getTripDest());
        currentTripRef.child("tripEndTime").setValue(finishingTripModel.getTripEndTime());

        TripModel.getInstance(context).reset();
    }
}
