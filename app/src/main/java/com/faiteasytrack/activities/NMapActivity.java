package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.services.LocationChangeService;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NMapActivity extends BaseActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        View.OnClickListener {

    private static final String TAG = "NMapActivity";
    private static final int REQUEST_TURN_ON_GPS = 568;

    private ProgressDialog progressDialog;
    private GoogleMap googleMap;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
//    private View loader;
    private TextView tvNetworkInfo;
    private SupportMapFragment mapFragment;

    private FloatingActionButton fabCenterLocation, fabTraceLocation, fabTrackLocation;

    private CoordinatorLayout clPositionAddressLayout;
    private TextView tvTripInfoName, tvTripInfoLocation, tvTripInfoTime, tvTrackState;
    private ImageView ivTripInfoUser, ivNavigation, ivNotifications;

    private boolean isOnline = false, isTracking = false, isMapReady = false, isServiceReady = false, isStartedFromNotification = false;
    private String currentTripKey;
    private Handler handler;

    private FirebaseUser firebaseUser;
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

    @Override
    public void initUI() {
        progressDialog = new ProgressDialog(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);

//        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
//        loader = viewStubLoader.inflate();
//        loader.setOnClickListener(this);
//        ViewUtils.showViews(loader);

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
        if (requestCode == AppPermissions.REQUESTS.ACCESS_LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (AppPermissions.checkLocationPermission(this)) {
                    if (checkGPSEnabled() && locationChangeService != null) {
                        locationChangeService.requestLocationUpdates();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_TURN_ON_GPS) {
            if (resultCode == RESULT_OK) {
                if (AppPermissions.checkLocationPermission(this)) {
                    if (checkGPSEnabled() && locationChangeService != null) {
                        locationChangeService.requestLocationUpdates();
                    }
                }
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

            if (phoneNumber != null)
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
//        ViewUtils.hideViews(loader);
        isMapReady = true;
        if (AppPermissions.checkLocationPermission(this) && !isStartedFromNotification) {
            if (checkGPSEnabled() && locationChangeService != null) {
                locationChangeService.requestLocationUpdates();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        bindService(new Intent(this, LocationChangeService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationChangeReceiver, new IntentFilter(LocationChangeService.ACTION_BROADCAST));
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
//        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
        if (isFinishing()) {
            if (!SharePreferences.isTracingOngoing(this))
                SharePreferences.setRequestingLocationUpdates(this, false);
        }
        super.onStop();
    }

    private class LocationChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationChangeService.EXTRA_LOCATION);
            if (location != null) {
                if (!isServiceReady) {
                    isServiceReady = true;
                    onMapReady(googleMap);
                }
            }
        }
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
            case R.id.fab_share_my_location: {
                if (!isEveryThingReady())
                    return;

                if (isTracking) {
                    ViewUtils.makeToast(this, "You can't trace yourself while tracking friend!");
                    return;
                }
            }
            break;
            case R.id.fab_track_friend_location: {
                if (!isEveryThingReady())
                    return;
            }
            break;
            case R.id.navigation_header: {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, NUserProfileActivity.class));
            }
            break;
            case R.id.fab_close: {

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
                startActivity(new Intent(this, NPhoneAuthActivity.class));
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
                        resolvable.startResolutionForResult(NMapActivity.this, REQUEST_TURN_ON_GPS);
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
            }
        });
        resultTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "LocationSettingsResponse.onSuccess: ");

                if (AppPermissions.checkLocationPermission(NMapActivity.this)) {
                    if (checkGPSEnabled()) {
                        locationChangeService.requestLocationUpdates();
                    }
                }
            }
        });
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
}
