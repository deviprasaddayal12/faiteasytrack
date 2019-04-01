package com.faiteasytrack.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.faiteasytrack.listeners.OnLocationChangedListener;
import com.faiteasytrack.observables.ObservableBoolean;
import com.faiteasytrack.services.LocationChangeService;

public class LocationChangeReceiver extends BroadcastReceiver {
    public static final String TAG = "LocationChangeReceiver";

    private OnLocationChangedListener onLocationChangedListener;
    private ObservableBoolean.Position obsPosition;

    public boolean bindListener(OnLocationChangedListener locationChangedListener){
        onLocationChangedListener = locationChangedListener;
        return true;
    }

    public void setLocationObservable(ObservableBoolean.Position obsPosition) {
        this.obsPosition = obsPosition;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(LocationChangeService.ACTION_BROADCAST)){
            Location location = intent.getParcelableExtra(LocationChangeService.EXTRA_LOCATION);
            Log.i(TAG, "onReceive: " + location);

            if (onLocationChangedListener != null && location != null)
                onLocationChangedListener.onLocationChanged(location);
        }
    }
}
