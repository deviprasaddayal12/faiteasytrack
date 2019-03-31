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
import com.faiteasytrack.observables.ObservableBoolean;
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
import java.util.Observable;
import java.util.Observer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NMapActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "NMapActivity";
    private static final int REQUEST_TURN_ON_GPS = 568;

    private ProgressDialog mapPreparingDialog;

    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private FloatingActionButton fabCurrentLocation;

    private boolean isStartedFromNotification = false;
    private Handler handlerMapUiUpdates;

    private FirebaseUser firebaseUser;

    private ObservableBoolean.Permission obsPermission;
    private ObservableBoolean.Gps obsGps;
    private ObservableBoolean.Internet obsInternet;
    private ObservableBoolean.Map obsMap;
    private ObservableBoolean.Service obsService;
    private ObservableBoolean.Location obsLocation;

    private Observer positionUpdateObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            if (obsService.isBound()) {
                Log.i(TAG, "positionUpdateObserver.update: obsService " + obsService.isBound());
                checkGpsEnabled();
            }

            if (obsMap.isReady()){
                Log.i(TAG, "positionUpdateObserver.update: obsMap " + obsMap.isReady());
            }

            if (obsLocation.isReceived()) {
                Log.i(TAG, "positionUpdateObserver.update: obsLocation " + obsLocation.isReceived());
                if (obsMap.isReady())
                    onNewLocationReceived();
            }
        }
    };

    private boolean isPermissionDialogShown = false;
    private Observer permissionObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Log.i(TAG, "permissionObserver.update: " + obsPermission.isGranted());
            if (!obsPermission.isGranted() && !isPermissionDialogShown) {
                isPermissionDialogShown = true;
                AppPermissions.checkLocationPermission(NMapActivity.this, true);

            } else {
                AppPermissions.showAllowPermissionDialog(NMapActivity.this,
                        "We cannot proceed further with obsLocation permissions.",
                        AppPermissions.permissionLocation,
                        new AppPermissions.OnPermissionChangeListener() {
                            @Override
                            public void onAllowPermission(String[] permissions) {
                                // todo : launch settings intent
                            }

                            @Override
                            public void onPermissionDenied() {
                                finish();
                            }
                        });
            }
        }
    };

    private Observer gpsObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Log.i(TAG, "gpsObserver.update: " + obsGps.isOn());
            if (obsGps.isOn()) {
                if (gpsSnackBar.isShown())
                    gpsSnackBar.dismiss();
            } else {
                if (!gpsSnackBar.isShown())
                    gpsSnackBar.show();
            }
        }
    };

    private LocationChangeReceiver locationChangeReceiver = null;
    private LocationChangeService locationChangeService = null;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationChangeService.LocalBinder binder = (LocationChangeService.LocalBinder) service;
            locationChangeService = binder.getService();

            obsService.setBound(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationChangeService = null;
            obsService.setBound(false);
        }
    };

    private Snackbar gpsSnackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handlerMapUiUpdates = new Handler();
        locationChangeReceiver = new LocationChangeReceiver();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        isStartedFromNotification = getIntent()
                .getBooleanExtra(LocationChangeService.EXTRA_STARTED_FROM_NOTIFICATION, false);

        initObservables();

        obsPermission.setGranted(AppPermissions.checkLocationPermission(this, false));
        setContentView(R.layout.activity_maps);
    }

    private void initObservables() {
        obsInternet = new ObservableBoolean.Internet();
        obsInternet.addObserver(positionUpdateObserver);

        obsMap = new ObservableBoolean.Map();
        obsMap.addObserver(positionUpdateObserver);

        obsService = new ObservableBoolean.Service();
        obsService.addObserver(positionUpdateObserver);

        obsLocation = new ObservableBoolean.Location();
        obsLocation.addObserver(positionUpdateObserver);

        obsPermission = new ObservableBoolean.Permission();
        obsPermission.addObserver(permissionObserver);

        obsGps = new ObservableBoolean.Gps();
        obsGps.addObserver(gpsObserver);
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
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationChangeReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (obsService.isBound()) {
            unbindService(serviceConnection);
            obsService.setBound(false);
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
        fabCurrentLocation = findViewById(R.id.fab_my_location);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        gpsSnackBar = Snackbar.make(fabCurrentLocation, "GPS off.", Snackbar.LENGTH_INDEFINITE)
                .setAction("Turn On", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEnableGpsDialog();
                    }
                });
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
                locationLastRcvd = location;
                obsLocation.setReceived(true);
            }
        });

        fabCurrentLocation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_my_location: {
                gotoCurrentLocation();
            }
            break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        obsMap.setReady(true);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions == AppPermissions.permissionLocation) {
            obsPermission.setGranted(grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    private void checkGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showEnableGpsDialog();
        }
    }

    private void showEnableGpsDialog() {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_TURN_ON_GPS) {
            obsGps.setOn(resultCode == RESULT_OK);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void gotoCurrentLocation() {
        shouldRecenter = true;
        updateNonTracingCurrentPosition(locationLastRcvd);
    }

    private Location locationLastRcvd;

    private void onNewLocationReceived() {
        updateNonTracingCurrentPosition(locationLastRcvd);
    }

    private static final int ZOOM_LEVEL = 18;
    private Marker markerIndicatingPosition; // user's own obsLocation
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
                String.format(Locale.getDefault(), "Your obsLocation is accurate to %d meters", (int) accuracy));

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
        obsInternet.setOnline(isOnlineNow);
    }
}
