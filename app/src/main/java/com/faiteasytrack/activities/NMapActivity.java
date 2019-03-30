package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.broadcasts.LocationChangeReceiver;
import com.faiteasytrack.listeners.OnLocationChangedListener;
import com.faiteasytrack.services.LocationChangeService;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.FileUtils;
import com.faiteasytrack.utils.MapUtils;
import com.faiteasytrack.utils.SharePreferences;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.android.SphericalUtil;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NMapActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "NMapActivity";
    private static final int REQUEST_TURN_ON_GPS = 568;

    private ProgressDialog mapPreparingDialog;

    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private FloatingActionButton fabMyLocation;

    private boolean isStartedFromNotification = false;
    private Handler handlerMapUiUpdates;

    private FirebaseUser firebaseUser;

    private LocationChangeReceiver locationChangeReceiver = null;
    private LocationChangeService locationChangeService = null;
    private boolean isServiceBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationChangeService.LocalBinder binder = (LocationChangeService.LocalBinder) service;
            locationChangeService = binder.getService();

            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationChangeService = null;
            isServiceBound = false;
        }
    };

    private boolean isMapReady = false, isLocationPermitted = false, isGPSEnabled = false, isFirstLocationReceived = false;

    private boolean isMapPreparingDialogRunning = false;
    private Runnable runnableShowMapPreparingDialog = new Runnable() {
        @Override
        public void run() {
            mapPreparingDialog = new ProgressDialog(NMapActivity.this);
            mapPreparingDialog.setTitle("Preparing map");
            mapPreparingDialog.setMessage("Just a while....be patient.");
            mapPreparingDialog.setCancelable(false);

            mapPreparingDialog.show();
            isMapPreparingDialogRunning = true;
        }
    };

    private Runnable runnableDismissMapPreparingDialog = new Runnable() {
        @Override
        public void run() {
            if (mapPreparingDialog.isShowing()) {
                mapPreparingDialog.dismiss();
                isMapPreparingDialogRunning = false;
            }
        }
    };

    private Snackbar gpsSnackBar;
    private void initGpsSnackBar(){
        gpsSnackBar = Snackbar.make(fabMyLocation, "GPS off.", Snackbar.LENGTH_INDEFINITE)
                .setAction("Turn On", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEnableGpsDialog();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handlerMapUiUpdates = new Handler();
        locationChangeReceiver = new LocationChangeReceiver();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        isStartedFromNotification = getIntent().getBooleanExtra
                (LocationChangeService.EXTRA_STARTED_FROM_NOTIFICATION, false);

        setContentView(R.layout.activity_maps);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, LocationChangeService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(LocationChangeService.ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationChangeReceiver, intentFilter);

        if (!AppPermissions.checkLocationPermission(this, false))
            AppPermissions.checkLocationPermission(this, true);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationChangeReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (!SharePreferences.isTracingOngoing(this) && !SharePreferences.isTrackingOngoing(this))
            SharePreferences.setRequestingLocationUpdates(this, false);
        super.onDestroy();
    }

    @Override
    public void initUI() {
        fabMyLocation = findViewById(R.id.fab_my_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        initGpsSnackBar();
    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void setUpListeners() {
        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        locationChangeReceiver.bindListener(new OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                myLastLocation = location;
                onNewLocationReceived();
            }
        });

        fabMyLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_my_location: {
                gotoMyCurrentLocation();
            }
            break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions == AppPermissions.permissionLocation) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkGpsEnabled();
            } else {
                AppPermissions.showAllowPermissionDialog(this,
                        "We cannot proceed further with location permissions.",
                        permissions,
                        new AppPermissions.OnPermissionChangeListener() {
                            @Override
                            public void onAllowPermission(String[] permissions) {

                            }

                            @Override
                            public void onPermissionDenied() {
                                finish();
                            }
                        });
            }
        }
    }

    private void checkGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showEnableGpsDialog();
        }
    }

    private void showEnableGpsDialog() {
        if (locationChangeService == null)
            return;

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationChangeService.mLocationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> resultTask = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        resultTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(NMapActivity.this, REQUEST_TURN_ON_GPS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        sendEx.printStackTrace();
                    }
                }
            }
        });
        resultTask.addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                if (!gpsSnackBar.isShown())
                    gpsSnackBar.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TURN_ON_GPS) {
                if (gpsSnackBar.isShown())
                    gpsSnackBar.dismiss();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void gotoMyCurrentLocation() {
        shouldRecenter = true;
        updateNonTracingCurrentPosition(myLastLocation);
    }

    private Location myLastLocation;

    private void onNewLocationReceived() {
        updateNonTracingCurrentPosition(myLastLocation);
    }

    private static final int ZOOM_LEVEL = 18;
    private Marker markerIndicatingPosition; // user's own location
    private Circle circleIndicatingAccuracy; // user's own accuracy
    private boolean shouldRecenter = true;

    private void updateNonTracingCurrentPosition(Location location) {
        if (googleMap == null)
            return;

        final LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        float accuracy = location.getAccuracy();

        if (circleIndicatingAccuracy == null) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(currentLatLng);
            circleOptions.radius(accuracy);
            circleOptions.strokeColor(getResources().getColor(R.color.colorAccent));
            circleOptions.strokeWidth(2);
            circleOptions.fillColor(getResources().getColor(R.color.colorMapOverLayAccent));

            circleIndicatingAccuracy = googleMap.addCircle(circleOptions);
        }
        if (markerIndicatingPosition == null) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLatLng);

            try {
                View markerIconView = getLayoutInflater().inflate(R.layout.layout_map_marker, null);

                Bitmap thumbnail = FileUtils.getThumbnail(this, firebaseUser.getPhotoUrl());
                ((CircularImageView) markerIconView.findViewById(R.id.iv_marker_image)).setImageBitmap(thumbnail);
                ((TextView) markerIconView.findViewById(R.id.tv_marker_title)).setText(firebaseUser.getDisplayName());

                markerOptions.icon(MapUtils.getMarkerFromView(markerIconView));
            } catch (Exception e) {
                Log.e(TAG, "exception: markerIconView = " + e.getMessage());
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground));
            }

            markerIndicatingPosition = googleMap.addMarker(markerOptions);
        }

        ((TextView) findViewById(R.id.tv_my_location_accuracy)).setText(
                String.format(Locale.getDefault(), "Your location is accurate to %d meters", (int) accuracy));

        markerIndicatingPosition.setPosition(currentLatLng);
        circleIndicatingAccuracy.setCenter(currentLatLng);
        circleIndicatingAccuracy.setRadius(accuracy);

        if (shouldRecenter)
            addLatLngBoundsToCircleAndAnimate();
    }

    private void addLatLngBoundsToCircleAndAnimate() {
        final LatLng centre = circleIndicatingAccuracy.getCenter();
        double radius = circleIndicatingAccuracy.getRadius();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        LatLng targetNorthEast = SphericalUtil.computeOffset(centre, radius * Math.sqrt(2), 45);
        LatLng targetSouthWest = SphericalUtil.computeOffset(centre, radius * Math.sqrt(2), 225);

        builder.include(targetNorthEast);
        builder.include(targetSouthWest);

        final LatLngBounds latLngBounds = builder.build();

        handlerMapUiUpdates.post(new Runnable() {
            @Override
            public void run() {
                if (MapUtils.areBoundsTooSmall(latLngBounds, 400)) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), ZOOM_LEVEL));
                } else {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, ZOOM_LEVEL));
                }

                shouldRecenter = false;
            }
        });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void updateInternetError(boolean isOnlineNow) {

    }
}
