package com.faiteasytrack.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.faiteasytrack.listeners.OnLocationChangedListener;
import com.faiteasytrack.services.LocationChangeService;

import java.util.ArrayList;

public class LocationChangeReceiver extends BroadcastReceiver {

    public static final String TAG = "LocationChangeReceiver";

    private ArrayList<OnLocationChangedListener> onLocationChangedListeners = new ArrayList<>();

    public LocationChangeReceiver(OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListeners.add(onLocationChangedListener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = intent.getParcelableExtra(LocationChangeService.EXTRA_LOCATION);
        updateAllListeners(location);
    }

    private void updateAllListeners(Location location) {
        for (int i = 0; i < onLocationChangedListeners.size(); i++){
            onLocationChangedListeners.get(i).onLocationChanged(location);
        }
    }
}
