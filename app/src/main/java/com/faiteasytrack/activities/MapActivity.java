package com.faiteasytrack.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;

import com.faiteasytrack.R;
import com.faiteasytrack.broadcasts.GpsStatusReceiver;
import com.faiteasytrack.broadcasts.LocationChangeReceiver;
import com.faiteasytrack.listeners.OnLocationChangedListener;
import com.faiteasytrack.listeners.OnMapStateChangeListener;
import com.faiteasytrack.managers.MapManager;
import com.faiteasytrack.observables.ObservableBoolean;
import com.faiteasytrack.services.LocationChangeService;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.SharePreferences;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Observable;
import java.util.Observer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MapActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener,
        OnLocationChangedListener {

    private static final String TAG = MapActivity.class.getSimpleName();
    private static final int REQUEST_TURN_ON_GPS = 568;

    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private FloatingActionButton fabCurrentLocation;
    private ContentLoadingProgressBar pbPositionLoader;

    private boolean isStartedFromNotification = false;
    private Handler handlerMap;

    private FirebaseUser firebaseUser;

    private MapManager mapManager;

    private ObservableBoolean.Permission obsPermission;
    private ObservableBoolean.Gps obsGps;
    private ObservableBoolean.Internet obsInternet;
    private ObservableBoolean.Map obsMap;
    private ObservableBoolean.Service obsService;
    private ObservableBoolean.Position obsPosition;

    private Observer observerLocationUpdate = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            if (obsService.isBound()) {
                checkGpsEnabled();
            }

            if (obsMap.isReady() && !MapManager.isReady) {
                mapManager = new MapManager(MapActivity.this, handlerMap, googleMap);
                mapManager.setOnMapStateChangeListener(onMapStateChangeListener);
            }

            if (obsPosition.isReceived() && !fabCurrentLocation.isShown())
                fabCurrentLocation.show();
        }
    };

    private boolean isPermissionDialogShown = false;
    private Observer observerPermission = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            if (!obsPermission.isGranted()) {
                if (!isPermissionDialogShown) {
                    isPermissionDialogShown = true;
                    AppPermissions.checkLocationPermission(MapActivity.this, true);
                } else {
                    AppPermissions.showAllowPermissionDialog(MapActivity.this,
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
        }
    };

    private Observer observerGps = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            if (obsGps.isOn()) {
                if (gpsSnackBar.isShown())
                    gpsSnackBar.dismiss();
                if (obsPosition.isReceived()) {
                    if (!fabCurrentLocation.isShown())
                        fabCurrentLocation.show();
                }
            } else {
                if (fabCurrentLocation.isShown())
                    fabCurrentLocation.hide();
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
            locationChangeService.requestLocationUpdates();

            obsService.setBound(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationChangeService = null;
            obsService.setBound(false);
        }
    };

    private OnMapStateChangeListener onMapStateChangeListener =
            new OnMapStateChangeListener() {
                @Override
                public void onIdealStateLost(int mode) {
                    fabCurrentLocation.hide();
                }

                @Override
                public void onIdealStateRestored(int mode) {
                    fabCurrentLocation.show();
                }
            };

    private GpsStatusReceiver gpsStatusReceiver;
    private Snackbar gpsSnackBar;

    private void initGpsSnackBar() {
        gpsSnackBar = Snackbar.make(fabCurrentLocation, "GPS off.", Snackbar.LENGTH_INDEFINITE)
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

        handlerMap = new Handler();
        locationChangeReceiver = new LocationChangeReceiver();
        gpsStatusReceiver = new GpsStatusReceiver();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        isStartedFromNotification = getIntent().getBooleanExtra
                (LocationChangeService.EXTRA_STARTED_FROM_NOTIFICATION, false);

        initObservables();

        obsPermission.setGranted(AppPermissions.checkLocationPermission(this, false));
        setContentView(R.layout.activity_maps);
    }

    private void initObservables() {
        obsInternet = new ObservableBoolean.Internet();
//        obsInternet.addObserver(observerInternet);

        obsMap = new ObservableBoolean.Map();
        obsMap.addObserver(observerLocationUpdate);

        obsService = new ObservableBoolean.Service();
        obsService.addObserver(observerLocationUpdate);

        obsPosition = new ObservableBoolean.Position();
        obsPosition.addObserver(observerLocationUpdate);

        obsPermission = new ObservableBoolean.Permission();
        obsPermission.addObserver(observerPermission);

        obsGps = new ObservableBoolean.Gps();
        obsGps.addObserver(observerGps);
    }

    private void deinitObservables() {
//        obsInternet.deleteObserver(observerInternet);
        obsMap.deleteObserver(observerLocationUpdate);
        obsService.deleteObserver(observerLocationUpdate);
        obsPosition.deleteObserver(observerLocationUpdate);
        obsPermission.deleteObserver(observerPermission);
        obsGps.deleteObserver(observerGps);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharePreferences.setRequestingLocationUpdates(this, true);
        bindService(new Intent(this, LocationChangeService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter locationFilter = new IntentFilter(LocationChangeService.ACTION_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationChangeReceiver, locationFilter);

        IntentFilter gpsFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(gpsStatusReceiver, gpsFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(gpsStatusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationChangeReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (!SharePreferences.isTracingOngoing(this) && !SharePreferences.isTrackingOngoing(this))
            SharePreferences.setRequestingLocationUpdates(this, false);
        if (obsService.isBound()) {
            unbindService(serviceConnection);
            obsService.setBound(false);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        deinitObservables();
        super.onDestroy();
    }

    @Override
    public void initUI() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fabCurrentLocation = findViewById(R.id.fab_current_location);
        pbPositionLoader = findViewById(R.id.pb_position_loader);

        fabCurrentLocation.hide();
        initGpsSnackBar();
    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void setUpListeners() {
        if (mapFragment != null) mapFragment.getMapAsync(this);

        locationChangeReceiver.bindListener(this);
        gpsStatusReceiver.setGpsObservable(obsGps);
        fabCurrentLocation.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        obsMap.setReady(true);

        UiSettings uiSettings = this.googleMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
    }

    @Override
    public void onLocationChanged(Location location) {
        obsPosition.setReceived(true);
        if (MapManager.isReady)
            mapManager.onLocationReceived(location);

        if (pbPositionLoader.isShown())
            pbPositionLoader.hide();
        if (!fabCurrentLocation.isShown())
            fabCurrentLocation.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_current_location: {
                if (MapManager.isReady)
                    mapManager.gotoCurrentLocation();
            }
            break;
        }
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
                        resolvable.startResolutionForResult(MapActivity.this, REQUEST_TURN_ON_GPS);
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

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void updateInternetStatus(boolean online) {
        obsInternet.setOnline(online);
    }
}
