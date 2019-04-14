package com.faiteasytrack;

import android.app.Application;

import com.faiteasytrack.firebase.FirebaseUtils;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class ETApplication extends Application {

    public static final String TAG = "EasytrackApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        FirebaseUtils.reset();
    }
}
