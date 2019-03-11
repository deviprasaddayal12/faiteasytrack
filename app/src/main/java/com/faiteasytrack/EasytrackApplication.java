package com.faiteasytrack;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;

import com.faiteasytrack.activities.NTrackingMapActivity;
import com.faiteasytrack.broadcasts.LocationChangeReceiver;
import com.faiteasytrack.services.LocationChangeService;
import com.faiteasytrack.utils.AppPermissions;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import co.uk.rushorm.android.AndroidInitializeConfig;
import co.uk.rushorm.core.RushCore;

public class EasytrackApplication extends Application {

    public static final String TAG = "EasytrackApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
    }
}
