package com.faiteasytrack.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.faiteasytrack.listeners.OnNetworkStateChangeListener;

public class NetworkStatusReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkStatusReceiver.class.getSimpleName();

    private OnNetworkStateChangeListener onNetworkStateChangeListener;

    public void bindListener(OnNetworkStateChangeListener onNetworkStateChangeListener) {
        this.onNetworkStateChangeListener = onNetworkStateChangeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                Log.d(TAG, "onReceive: " + networkInfo);
                if (networkInfo != null) {
                    if (onNetworkStateChangeListener != null)
                        onNetworkStateChangeListener.onStateChanged(networkInfo.isConnected() && networkInfo.isAvailable());
                }
            }
        }
    }
}
