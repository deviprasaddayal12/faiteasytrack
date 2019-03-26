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

    private OnLocationChangedListener onLocationChangedListener;

    public boolean bindListener(OnLocationChangedListener locationChangedListener){
        onLocationChangedListener = locationChangedListener;
        return true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(LocationChangeService.ACTION_BROADCAST)){
            Location location = intent.getParcelableExtra(LocationChangeService.EXTRA_LOCATION);

            if (onLocationChangedListener != null && location != null)
                onLocationChangedListener.onLocationChanged(location);
        }
    }
}
