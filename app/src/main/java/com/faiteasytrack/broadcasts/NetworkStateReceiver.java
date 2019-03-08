package com.faiteasytrack.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkStateReceiver";

    private static NetworkStateReceivedListener stateReceivedListener;

    public interface NetworkStateReceivedListener {
        void onStateReceived(NetworkInfo.State state);

        void onStateChanged(boolean isOnlineNow);
    }

    public static void bindListener(NetworkStateReceivedListener otpReceivedListener) {
        stateReceivedListener = otpReceivedListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                Log.d(TAG, "onReceive: networkInfo : " + networkInfo);
                if (networkInfo != null) {
                    if (stateReceivedListener != null)
                        stateReceivedListener.onStateChanged(networkInfo.isConnected() && networkInfo.isAvailable());
                } else {
                    if (stateReceivedListener != null)
                        stateReceivedListener.onStateChanged(false);
                }
            }
        }
    }
}
