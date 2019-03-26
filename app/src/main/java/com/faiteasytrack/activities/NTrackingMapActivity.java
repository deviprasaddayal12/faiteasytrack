package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.helpers.FirebaseHelper;
import com.faiteasytrack.customclasses.ETLatLng;
import com.faiteasytrack.models.FriendModel;
import com.faiteasytrack.models.TripModel;
import com.faiteasytrack.services.LocationChangeService;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.Constants;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NTrackingMapActivity extends BaseActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnCameraIdleListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "NMapActivity";

    private ProgressDialog progressDialog;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View loader;
    private TextView tvNetworkInfo;
    private SupportMapFragment mapFragment;

    private FloatingActionButton fabCenterLocation, fabTraceLocation, fabTrackLocation;

    private CoordinatorLayout clPositionAddressLayout;
    private TextView tvTripInfoName, tvTripInfoLocation, tvTripInfoTime, tvTrackState;
    private ImageView ivTripInfoUser, ivNavigation, ivNotifications;

    private static final int REQUEST_TURN_ON_GPS = 568;
    private static final float ZOOM = 18.0f;
    private static final float TILT = 60.0f;

    private GoogleMap googleMap;

    private MarkerOptions myMarkerOptions;
    private Location myLastLocation;
    private LatLng myLastLatLng;

    private boolean isOnline = false, isTracing = false, isTracking = false,
            isMapReady = false, isServiceReady = false,
            isStartedFromNotification = false;

    private String currentTripKey;

    private Handler handler;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private LocationChangeReceiver locationChangeReceiver = null;

    // A reference to the service used to get location updates.
    private LocationChangeService locationChangeService = null;

    // Tracks the bound state of the service.
    private boolean isServiceBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationChangeService.LocalBinder binder = (LocationChangeService.LocalBinder) service;
            locationChangeService = binder.getService();

            if (checkGPSEnabled())
                locationChangeService.requestLocationUpdates();

            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationChangeService = null;
            isServiceBound = false;
        }
    };

    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        handler = new Handler();
        isStartedFromNotification = getIntent().getBooleanExtra(LocationChangeService.EXTRA_STARTED_FROM_NOTIFICATION, false);
        locationChangeReceiver = new LocationChangeReceiver();

        setContentView(R.layout.z_activity_dashboard_user);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    private void onMyDetailsNotFound() {
        FirebaseAuth.getInstance().signOut();
        Log.e(TAG, "onMyDetailsNotFound: ");

        DialogUtils.showSorryAlert(this, "We couldn't recognize you." +
                "\nNo worries! Just SignIn again,\n and, Voila! You're in.", new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(NTrackingMapActivity.this, OPhoneAuthActivity.class));
                        finish();
                    }
                });
            }
        });
    }

    private Toolbar toolbar;
    @Override
    public void initUI() {
        progressDialog = new ProgressDialog(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);

        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        loader = viewStubLoader.inflate();
        loader.setOnClickListener(this);
        ViewUtils.showViews(loader);

        tvNetworkInfo = findViewById(R.id.tv_network_info);

        navigationView = findViewById(R.id.nav_view);
        ivNavigation = findViewById(R.id.iv_navigation);
        ivNotifications = findViewById(R.id.iv_notifications);
        fabTraceLocation = findViewById(R.id.fab_share_my_location);
        fabTrackLocation = findViewById(R.id.fab_track_friend_location);
        fabCenterLocation = findViewById(R.id.fab_my_current_location);

        clPositionAddressLayout = findViewById(R.id.layout_trip_info);
        tvTripInfoName = findViewById(R.id.tv_name);
        tvTripInfoLocation = findViewById(R.id.tv_location);
        tvTripInfoTime = findViewById(R.id.tv_time);
        ivTripInfoUser = findViewById(R.id.iv_friend_pic);
        tvTrackState = findViewById(R.id.tv_track_state);

        isOnline = checkInternetEnabled();
        if (!isOnline)
            updateInternetError(false);

    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void setUpListeners() {
        toolbar.setOnClickListener(this);
        drawerLayout.addDrawerListener(drawerListener);

        fabTraceLocation.setOnClickListener(this);
        fabTrackLocation.setOnClickListener(this);
        fabCenterLocation.setOnClickListener(this);
        ivNavigation.setOnClickListener(this);
        ivNotifications.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getHeaderView(0).setOnClickListener(this);

        findViewById(R.id.fab_close).setOnClickListener(this);
    }

    @Override
    public void setUpData() {

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == AppPermissions.ACCESS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (AppPermissions.checkLocationPermission(this, false)) {
                    if (checkGPSEnabled() && locationChangeService != null) {
                        locationChangeService.requestLocationUpdates();
                    }
                }
            }
        }
    }

    //    private DashboardManager.DashboardListener dashboardListener = new DashboardManager.DashboardListener() {
//    @Override
//    public void onNewRequestReceived() {
//
//    }
//
//    @Override
//    public void onNewTripStarted(TripModel newTripModel) {
//
//    }
//
//    @Override
//    public void onTripFetchSuccess(TripModel lastTripModel, boolean isOngoing) {
//        Log.e(TAG, "onTripFetchSuccess: " + lastTripModel + isOngoing);
//        ViewUtils.hideViews(loader);
//        createTrackingLine(lastTripModel);
//    }
//
//    @Override
//    public void onNewLatLngReceived(TripModel currentTripModel, ETLatLng newLatLng) {
//        appendTrackingLine(newLatLng);
//    }
//
//    @Override
//    public void onTripFetchFailed(String error) {
//
//    }
//
//    @Override
//    public void onTripFinished() {
//
//    }
//    };

    private PolylineOptions polylineOptions;

    private void createTrackingLine(TripModel tripModel) {
        isTracking = true;

        polylineOptions = new PolylineOptions();
        polylineOptions.color(getResources().getColor(R.color.colorPrimary));
        polylineOptions.jointType(JointType.ROUND);
        polylineOptions.width(3);
        polylineOptions.startCap(new RoundCap());
        polylineOptions.endCap(new RoundCap());

        LatLng lastLatLng = null;

        for (ETLatLng etLatLng : tripModel.getWayPoints()) {
            lastLatLng = etLatLng.getLatLng();
            polylineOptions.add(lastLatLng);

            Log.e(TAG, "createTrackingLine: " + etLatLng.getLatitude() + etLatLng.getLongitude()
                    + etLatLng.getAccuracy() + etLatLng.getSpeed() + etLatLng.getMillis());
        }

        googleMap.clear();
        googleMap.addPolyline(polylineOptions);
        if (lastLatLng != null) {
            googleMap.addMarker(new MarkerOptions().position(lastLatLng));

            animateCamera(lastLatLng);
        }
    }

    private void appendTrackingLine(ETLatLng newLatLng) {
        polylineOptions.add(newLatLng.getLatLng());

        googleMap.clear();
        googleMap.addPolyline(polylineOptions);
        googleMap.addMarker(new MarkerOptions().position(newLatLng.getLatLng()));

        animateCamera(newLatLng.getLatLng());
    }

    private void animateCamera(LatLng latLng){
        CameraPosition cameraPosition = new CameraPosition.Builder().zoom(ZOOM).target(latLng).tilt(TILT).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

//    private DashboardManager dashboardManager = new DashboardManager(this, this);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_TURN_ON_GPS) {
            if (resultCode == RESULT_OK) {
                if (AppPermissions.checkLocationPermission(this, false)) {
                    if (checkGPSEnabled() && locationChangeService != null) {
                        locationChangeService.requestLocationUpdates();
                    }
                }
            }

        } else if (requestCode == Constants.INTENT_LAUNCH_CODES.START_FRIENDS_ACTIVITY_FOR_TRACKING) {
            if (data != null) {
                ViewUtils.showViews(loader);
                FriendModel friendModel = data.getParcelableExtra(Constants.INTENT_EXTRA_KEYS.SELECTED_FRIEND_MODEL_TO_TRACK);
//                dashboardManager.startTracking(friendModel);
            }
        }
    }

    @Override
    public void setUpRecycler() {

    }

    private void updateUserDetailsInNavView() {
        resetNavigationSelection();
        View navHeader = navigationView.getHeaderView(0);
//        ImageView userProfilePic = navHeader.findViewById(R.id.nav_header_icon);
        TextView userName = navHeader.findViewById(R.id.nav_header_title);
        TextView userContactInfo = navHeader.findViewById(R.id.nav_header_subtitle);
        try {
            userName.setText(firebaseUser.getDisplayName());
            String phoneNumber = firebaseUser.getPhoneNumber();
            String email = firebaseUser.getEmail();

            /*if (phoneNumber != null && email != null)
                userContactInfo.setText(String.format("%s\n%s", phoneNumber, email));
            else */if (phoneNumber != null)
                userContactInfo.setText(phoneNumber);
            else if (email != null)
                userContactInfo.setText(email);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        ViewUtils.hideViews(loader);
        isMapReady = true;
        if (AppPermissions.checkLocationPermission(this, true) && !isStartedFromNotification) {
            if (checkGPSEnabled() && locationChangeService != null) {
                locationChangeService.requestLocationUpdates();
            }
        }
    }

    @Override
    public void onCameraIdle() {
        if (myLastLatLng == null)
            return;

        googleMap.clear();
        animateCamera(myLastLatLng);

        ViewUtils.showViews(clPositionAddressLayout);
        showMyTraces();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (SharePreferences.appWasInBackground(this)) {
                if (SharePreferences.isTracingOngoing(this)) {
                    startTracing(true);
                } else if (SharePreferences.ongoingTripFinishedWhileInBackground(this)) {
                    isTracing = false;
                    fabTrackLocation.show();
                    ongoingTripFinishedWhileInBackground();
                }
                SharePreferences.setAppWasInBackground(this, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        bindService(new Intent(this, LocationChangeService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(locationChangeReceiver, new IntentFilter(LocationChangeService.ACTION_BROADCAST));
    }

    private void ongoingTripFinishedWhileInBackground() {
        TripModel lastTrip = SharePreferences.getTripModelFinishedFromBackground(this);
        DialogUtils.showDoYouKnowAlert(this, "You have successfully completed your last trip! " +
                firebaseUser.getDisplayName() + "! We're waiting for your next.", new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setTitle("Updating Trip!");
                        progressDialog.setMessage("Please wait while we update last trip info....");
                        if (!progressDialog.isShowing())
                            progressDialog.show();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    }
                                });
                            }
                        }, 2000);
                        SharePreferences.setOngoingTripFinishedWhileInBackground(NTrackingMapActivity.this, false);
                        SharePreferences.removeTripModelFinishedFromBackground(NTrackingMapActivity.this);
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationChangeReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (isServiceBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(serviceConnection);
            isServiceBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        try {
            SharePreferences.setAppWasInBackground(this, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isFinishing()) {
            if (!SharePreferences.isTracingOngoing(this))
                SharePreferences.setRequestingLocationUpdates(this, false);
        }
        super.onStop();
    }

    /**
     * Receiver for broadcasts sent by {@link LocationChangeService}.
     */
    private class LocationChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationChangeService.EXTRA_LOCATION);
            Log.e(TAG, "onReceive: " + location);
            if (location != null) {
                if (!isServiceReady) {
                    isServiceReady = true;
                    onMapReady(googleMap);
                }
                myLastLocation = location;
                showMyTraces();
            }
        }
    }

    private void startTracing(boolean isOngoingTripRestoring) {
        ViewUtils.showViews(clPositionAddressLayout);
        isTracing = true;
        fabTrackLocation.hide();

        if (!isOngoingTripRestoring) {
            TripModel currentTripModel = TripModel.getInstance(this);

            currentTripModel.setUserId(firebaseUser.getUid());
            currentTripModel.setTripStartTime(new Date().getTime());
            currentTripModel.setTripSource(Utils.getMyLatLng(myLastLocation));
            currentTripModel.setTripOngoing(true);

            currentTripKey = FirebaseHelper.createMyTrip(this);

            try {
                SharePreferences.setOngoingTripExists(this, true);
                SharePreferences.saveKeyForLastUnfinishedTrip(this, currentTripKey);
                SharePreferences.saveLastUnfinishedTripModel(this, currentTripModel);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ViewUtils.makeToast(this, "Your are being traced....");
        } else {
            currentTripKey = SharePreferences.getKeyForLastUnfinishedTrip(this);
            showMyTraces();
        }
    }

    private ETLatLng getMyLatLng(Location location) {
        return new ETLatLng(location.getLatitude(), location.getLongitude(), location.getTime(), location.getSpeed(), location.getAccuracy());
    }

    private void stopTracing() {

        TripModel.getInstance(this).setTripEndTime(new Date().getTime());
        TripModel.getInstance(this).setTripDest(getMyLatLng(myLastLocation));
        TripModel.getInstance(this).setTripOngoing(false);

        FirebaseHelper.endMyTrip(this, currentTripKey);

        try {
            SharePreferences.setOngoingTripExists(this, false);
            SharePreferences.removeKeyForLastUnfinishedTrip(this);
            SharePreferences.removeLastUnfinishedTripModel(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ViewUtils.makeToast(this, "Tracing has been stopped....");

        fabTrackLocation.show();

        isTracing = false;
        ViewUtils.hideViews(clPositionAddressLayout);
        onCameraIdle();
    }

    private void showMyTraces() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (myLastLocation == null)
                    return;

                LatLng latLng = new LatLng(myLastLocation.getLatitude(), myLastLocation.getLongitude());
                myLastLatLng = latLng;

                // If user is tracking a friend, then user's last location will be updated to location value,
                // while it won't be updated in ui
                if (isTracking)
                    return;

                Utils.getAddressFromLocation(latLng.latitude, latLng.longitude, NTrackingMapActivity.this, geocoderHandler);
                tvTripInfoName.setText(firebaseUser.getDisplayName());
                tvTripInfoTime.setText(Utils.getTimeInString(myLastLocation.getTime()));

                animateCamera(latLng);
                googleMap.clear();

                if (isTracing) {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.geodesic(true);
                    polylineOptions.jointType(JointType.ROUND);
                    polylineOptions.startCap(new RoundCap());
                    polylineOptions.endCap(new RoundCap());
                    polylineOptions.color(getResources().getColor(R.color.colorPrimary));

                    for (ETLatLng ETLatLng : TripModel.getInstance(NTrackingMapActivity.this).getWayPoints())
                        polylineOptions.add(ETLatLng.getLatLng());

                    googleMap.addPolyline(polylineOptions);
                }

                if (myMarkerOptions == null) {
                    myMarkerOptions = new MarkerOptions();
                    myMarkerOptions.position(latLng);
                    myMarkerOptions.title("You are here");
                    googleMap.addMarker(myMarkerOptions);
                } else {
                    myMarkerOptions.position(latLng);
                    googleMap.addMarker(myMarkerOptions);
                }
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    private void stopTracking() {
        ViewUtils.hideViews(clPositionAddressLayout);

        googleMap.clear();
        isTracking = false;

        fabTraceLocation.show();
        ViewUtils.hideViews(tvTrackState);
        showMyTraces();
    }

    private void showAllFriends() {
        ViewUtils.hideViews(loader);
        startActivityForResult(new Intent(this, NFriendsActivity.class), Constants.INTENT_LAUNCH_CODES.START_FRIENDS_ACTIVITY_FOR_TRACKING);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_navigation: {
                updateUserDetailsInNavView();
                drawerLayout.openDrawer(GravityCompat.START);
            }
            break;

            case R.id.iv_notifications: {
                startActivity(new Intent(this, NRequestsActivity.class));
            }
            break;

            case R.id.fab_my_current_location: {
                if (isEveryThingReady() && !isTracking)
                    onCameraIdle();
            }
            break;

            case R.id.fab_share_my_location: {
                if (!isEveryThingReady())
                    return;

                if (isTracking) {
                    ViewUtils.makeToast(this, "You can't trace yourself while tracking friend!");
                    return;
                }

                if (isTracing) {
                    stopTracing();
                } else {
                    startTracing(false);
                }
            }
            break;

            case R.id.fab_track_friend_location: {
                if (!isEveryThingReady())
                    return;

                if (isTracking) {
                    stopTracking();
                } else {
                    ViewUtils.showViews(loader);
                    showAllFriends();
                }
            }
            break;

            case R.id.navigation_header: {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, NUserProfileActivity.class));
            }
            break;

            case R.id.fab_close: {
                if (isTracking)
                    stopTracking();
                else if (isTracing)
                    stopTracing();
                else
                    ViewUtils.hideViews(clPositionAddressLayout);
            }
            break;
        }
    }

    private boolean isEveryThingReady() {
        if (!isMapReady)
            ViewUtils.makeToast(this, "Please wait until map is ready!");
        else if (!isOnline)
            ViewUtils.makeToast(this, "Please wait while network connection is restored!");
        else if (!isServiceReady)
            ViewUtils.makeToast(this, "Please wait while location service prepared!");
        return isMapReady && isOnline && isServiceReady;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
//            case R.id.nav_map: {
//
//            }
//            return true;
//            case R.id.nav_history: {
//                startActivity(new Intent(this, NHistoryActivity.class));
//            }
//            return true;
//            case R.id.nav_requests: {
//                startActivity(new Intent(this, NRequestsActivity.class));
//            }
//            return true;
//            case R.id.nav_friends: {
//                startActivity(new Intent(this, NFriendsActivity.class));
//            }
//            return true;
//            case R.id.nav_contacts: {
//                startActivity(new Intent(this, NContactsActivity.class));
//            }
//            return true;
            case R.id.nav_settings: {
                startActivity(new Intent(this, NSettingsActivity.class));
            }
            return true;
            case R.id.nav_logout: {
                FirebaseAuth.getInstance().signOut();
                SharePreferences.removeUserModel(this);
                startActivity(new Intent(this, OPhoneAuthActivity.class));
                finish();
            }
            return true;
            default:
                return false;
        }
    }

    public void resetNavigationSelection() {
        if (navigationView == null)
            return;
        if (navigationView.getCheckedItem() != null)
            navigationView.getCheckedItem().setChecked(false);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean checkInternetEnabled() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected() && networkInfo.isAvailable();
    }

    private boolean checkGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            displayEnableGPSDialog();
            return false;
        } else
            return true;
    }

    private void displayEnableGPSDialog() {
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
                Log.d(TAG, "LocationSettingsResponse.onFailure: ");

                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(NTrackingMapActivity.this, REQUEST_TURN_ON_GPS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
        resultTask.addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.d(TAG, "LocationSettingsResponse.onCanceled: ");

//                makeSnackbar(fabTrackLocation, "GPS Off!", Snackbar.LENGTH_INDEFINITE, "TURN ON", new Runnable() {
//                    @Override
//                    public void run() {
//                        displayEnableGPSDialog();
//                    }
//                });
            }
        });
        resultTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "LocationSettingsResponse.onSuccess: ");

                if (AppPermissions.checkLocationPermission(NTrackingMapActivity.this, false)) {
                    if (checkGPSEnabled()) {
                        locationChangeService.requestLocationUpdates();
                    }
                }
            }
        });
    }

    @Override
    public void updateInternetError(boolean isOnlineNow) {
        String message = isOnlineNow ? "Cheers! We are back." : "Sorry! Could not connect to internet.";
        int backgroundColor = isOnlineNow ? getResources().getColor(android.R.color.holo_green_dark)
                : getResources().getColor(android.R.color.holo_red_dark);

        tvNetworkInfo.setText(message);
        tvNetworkInfo.setBackgroundColor(backgroundColor);

        if (tvNetworkInfo.getVisibility() == View.GONE)
            ViewUtils.showViews(tvNetworkInfo);

        if (isOnlineNow) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ViewUtils.hideViews(tvNetworkInfo);
                }
            }, 2000);
        }
    }

    private void updateNotificationCount(int count) {
        if (count == 0) {
            ((TextView) findViewById(R.id.tv_notifications)).setText("");
        } else {
            ((TextView) findViewById(R.id.tv_notifications)).setText("" + count);
        }
    }

    private GeocoderHandler geocoderHandler = new GeocoderHandler();

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            tvTripInfoLocation.setText(locationAddress);
        }
    }

    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {
            if (newState == DrawerLayout.STATE_IDLE)
                updateUserDetailsInNavView();
        }
    };
}
