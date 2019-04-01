package com.faiteasytrack.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import com.faiteasytrack.listeners.OnGpsStateChangeListener;
import com.faiteasytrack.observables.ObservableBoolean;

public class GpsStatusReceiver extends BroadcastReceiver {
    public static final String TAG = GpsStatusReceiver.class.getSimpleName();

    private OnGpsStateChangeListener onGpsStateChangeListener;
    private ObservableBoolean.Gps gps;

    public boolean bindListener(OnGpsStateChangeListener onGpsStateChangeListener) {
        this.onGpsStateChangeListener = onGpsStateChangeListener;
        return true;
    }

    public void setGpsObservable(ObservableBoolean.Gps gps) {
        this.gps = gps;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isEnabled = false;
            if (locationManager != null)
                isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (onGpsStateChangeListener != null)
                onGpsStateChangeListener.onGpsStateChanged(isEnabled);
            else if (gps != null)
                gps.setOn(isEnabled);
        }
    }
}
