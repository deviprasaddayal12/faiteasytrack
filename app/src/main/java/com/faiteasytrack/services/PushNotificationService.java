package com.faiteasytrack.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {

    public static final String TAG = "PushNotificationService";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        Log.i(TAG, "onNewToken: " + s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.i(TAG, "onMessageReceived: " + remoteMessage);
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);

        Log.i(TAG, "onMessageSent: " + s);
    }
}
