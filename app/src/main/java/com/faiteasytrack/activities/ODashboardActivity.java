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
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.faiteasytrack.R;
import com.faiteasytrack.broadcasts.NetworkStateReceiver;
import com.faiteasytrack.enums.Error;
import com.faiteasytrack.helpers.FirebaseHelper;
import com.faiteasytrack.helpers.RequestHelper;
import com.faiteasytrack.listeners.RequestListener;
import com.faiteasytrack.exceptions.UserModelNotFound;
import com.faiteasytrack.classess.ETLatLng;
import com.faiteasytrack.models.FriendModel;
import com.faiteasytrack.models.ProfileModel;
import com.faiteasytrack.models.RequestModel;
import com.faiteasytrack.models.TripModel;
import com.faiteasytrack.models.UserModel;
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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ODashboardActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnCameraIdleListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "ODashboardActivity";

    private ProgressDialog progressDialog;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View loader;
    private TextView tvNetworkInfo;
    private SupportMapFragment mapFragment;

    private FloatingActionButton fabCenterLocation, fabTraceLocation, fabTrackLocation;

    private FrameLayout llLayoutTripInfo;
    private TextView tvTripInfoName, tvTripInfoLocation, tvTripInfoTime, tvTrackState;
    private ImageView ivTripInfoUser, fabNavigation, fabUsersList;

    private static final int REQUEST_TURN_ON_GPS = 568;
    private static final float ZOOM_LEVEL = 18.0f;

    private GoogleMap mMap;
    private MarkerOptions myMarkerOptions;

    private Location lastLocation;
    private LatLng myLastNonTracingLatLng;

    private UserModel userModel;
    private ProfileModel profileModel;

    private Query friendTripQuery;
    private ValueEventListener friendTripEventListener;

    private boolean isOnline = false, isTracing = false, isTracking = false,
    /*isTracingLoaderShowing = false,*/ isTrackingLoaderShowing = false,
            isMapReady = false, isServiceReady = false, isFriendListRequesting = false, isStartedFromNotification = false;

    private String currentTripKey;

    private Handler uiThreadHandler;

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyLocationReceiver myLocationReceiver;

    // A reference to the service used to get location updates.
    private LocationChangeService locationService = null;

    // Tracks the bound state of the service.
    private boolean isServiceBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationChangeService.LocalBinder binder = (LocationChangeService.LocalBinder) service;
            locationService = binder.getService();
            if (isMapReady && checkGPSEnabled())
                locationService.requestLocationUpdates();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService = null;
            isServiceBound = false;
        }
    };

    private RequestHelper requestHelper;

    private Handler mapHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            profileModel = SharePreferences.getProfileModel(this);
            userModel = SharePreferences.getUserModel(this);
            if (userModel != null) {
                uiThreadHandler = new Handler();
                isStartedFromNotification = getIntent().getBooleanExtra(LocationChangeService.EXTRA_STARTED_FROM_NOTIFICATION, false);

                setContentView(R.layout.z_activity_dashboard_user);
                mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                if (mapFragment != null)
                    mapFragment.getMapAsync(this);

                SharePreferences.setRequestingLocationUpdates(this, true);
                myLocationReceiver = new MyLocationReceiver();
            } else
                onMyDetailsNotFound();

        } catch (UserModelNotFound e) {

            onMyDetailsNotFound();
        }
    }

    private void onMyDetailsNotFound() {
        FirebaseAuth.getInstance().signOut();
        Log.e(TAG, "onMyDetailsNotFound: ");
        DialogUtils.showSorryAlert(this, "We couldn't recognize you." +
                "\nNo worries! Just enter your number for verification,\n and, Voila! You're in.", new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(ODashboardActivity.this, OPhoneAuthActivity.class));
                        finish();
                    }
                });
            }
        });
    }

    private void registerNetworkStateBroadcast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkStateReceiver stateReceiver = new NetworkStateReceiver();
            registerReceiver(stateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        NetworkStateReceiver.bindListener(new NetworkStateReceiver.NetworkStateReceivedListener() {
            @Override
            public void onStateReceived(NetworkInfo.State state) {

            }

            @Override
            public void onStateChanged(boolean isOnlineNow) {
                isOnline = isOnlineNow;
                updateInternetError(isOnlineNow);
            }
        });
    }

    @Override
    public void initUI() {
        progressDialog = new ProgressDialog(this);
        drawerLayout = findViewById(R.id.drawer_layout);

        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        loader = viewStubLoader.inflate();
        loader.setOnClickListener(this);
        ViewUtils.showViews(loader);

        tvNetworkInfo = findViewById(R.id.tv_network_info);

        navigationView = findViewById(R.id.nav_view);
        fabNavigation = findViewById(R.id.iv_navigation);
        fabUsersList = findViewById(R.id.iv_notifications);
        fabTraceLocation = findViewById(R.id.fab_share_my_location);
        fabTrackLocation = findViewById(R.id.fab_track_friend_location);
        fabCenterLocation = findViewById(R.id.fab_my_current_location);

        llLayoutTripInfo = findViewById(R.id.layout_trip_info);
        tvTripInfoName = findViewById(R.id.tv_name);
        tvTripInfoLocation = findViewById(R.id.tv_location);
        tvTripInfoTime = findViewById(R.id.tv_time);
        ivTripInfoUser = findViewById(R.id.iv_friend_pic);
        tvTrackState = findViewById(R.id.tv_track_state);

        isOnline = checkInternetEnabled();
        if (!isOnline)
            updateInternetError(false);

        requestHelper = new RequestHelper(this, requestListener);
        requestHelper.initRequestDatabases();
        requestHelper.countNewRequest();
    }

    @Override
    public void setUpActionBar() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
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
        });
    }

    @Override
    public void setUpListeners() {
        fabTraceLocation.setOnClickListener(this);
        fabTrackLocation.setOnClickListener(this);
        fabCenterLocation.setOnClickListener(this);
        fabNavigation.setOnClickListener(this);
        fabUsersList.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getHeaderView(0).setOnClickListener(this);

        findViewById(R.id.fab_close).setOnClickListener(this);
    }

    @Override
    public void setUpData() {
        if (AppPermissions.checkNetworkStateReceivePermission(this))
            registerNetworkStateBroadcast();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == AppPermissions.REQUESTS.ACCESS_LOCATION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (AppPermissions.checkLocationPermission(this)) {
                    if (checkGPSEnabled() && locationService != null) {
                        locationService.requestLocationUpdates();
                    }
                }
            }
        } else if (requestCode == AppPermissions.REQUESTS.ACCESS_NETWORK_STATE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerNetworkStateBroadcast();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_TURN_ON_GPS) {
            if (resultCode == RESULT_OK) {
                if (AppPermissions.checkLocationPermission(this)) {
                    if (checkGPSEnabled() && locationService != null) {
                        locationService.requestLocationUpdates();
                    }
                }
            }
        } else if (requestCode == Constants.INTENT_LAUNCH_CODES.START_FRIENDS_ACTIVITY_FOR_TRACKING) {
            isFriendListRequesting = false;

            if (data != null) {
                ViewUtils.showViews(loader);
                FriendModel friendModel = data.getParcelableExtra(Constants.INTENT_EXTRA_KEYS.SELECTED_FRIEND_MODEL_TO_TRACK);
                if (friendModel == null) {
                    ViewUtils.makeToast(this, "Unable to fetch friend. Please try again.");
                } else {
                    getFriendsWayPoints(friendModel);
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
            userName.setText(profileModel.getName());
            userContactInfo.setText(String.format("%s\n%s", profileModel.getPhones().get(0), profileModel.getEmails().get(0)));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        ViewUtils.hideViews(loader);
        isMapReady = true;
        if (AppPermissions.checkLocationPermission(this) && !isStartedFromNotification) {
            if (checkGPSEnabled() && locationService != null) {
                locationService.requestLocationUpdates();
            } /*else {
                mapHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onMapReady(mMap);
                    }
                }, 3000);
            }*/
        }
    }

    @Override
    public void onCameraIdle() {
        if (myLastNonTracingLatLng == null)
            return;

        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLastNonTracingLatLng, ZOOM_LEVEL));

        ViewUtils.showViews(llLayoutTripInfo);
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
        bindService(new Intent(this, LocationChangeService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(myLocationReceiver, new IntentFilter(LocationChangeService.ACTION_BROADCAST));
    }

    private void ongoingTripFinishedWhileInBackground() {
        TripModel lastTrip = SharePreferences.getTripModelFinishedFromBackground(this);
        DialogUtils.showDoYouKnowAlert(this, "You have successfully completed your last trip! " +
                profileModel.getName() + "! We're waiting for your next.", new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setTitle("Updating Trip!");
                        progressDialog.setMessage("Please wait while we update last trip info....");
                        if (!progressDialog.isShowing())
                            progressDialog.show();
                        uiThreadHandler.postDelayed(new Runnable() {
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
                        SharePreferences.setOngoingTripFinishedWhileInBackground(ODashboardActivity.this, false);
                        SharePreferences.removeTripModelFinishedFromBackground(ODashboardActivity.this);
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myLocationReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (isServiceBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
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
    private class MyLocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationChangeService.EXTRA_LOCATION);
            Log.e(TAG, "onReceive: " + location);
            if (location != null) {
                if (!isServiceReady) {
                    isServiceReady = true;
                    onMapReady(mMap);
                }
                lastLocation = location;
                showMyTraces();
            }
        }
    }

    private void startTracing(boolean isOngoingTripRestoring) {
        ViewUtils.showViews(llLayoutTripInfo);
        isTracing = true;
        fabTrackLocation.hide();

        if (!isOngoingTripRestoring) {
            TripModel currentTripModel = TripModel.getInstance(this);

            currentTripModel.setUserId(userModel.getUid());
            currentTripModel.setTripStartTime(new Date().getTime());
            currentTripModel.setTripSource(Utils.getMyLatLng(lastLocation));
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

    private ETLatLng getMyLatLng(Location location){
        return new ETLatLng(location.getLatitude(), location.getLongitude(), location.getTime(), location.getSpeed(), location.getAccuracy());
    }

    private void stopTracing() {

        TripModel.getInstance(this).setTripEndTime(new Date().getTime());
        TripModel.getInstance(this).setTripDest(getMyLatLng(lastLocation));
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
        ViewUtils.hideViews(llLayoutTripInfo);
        onCameraIdle();
    }

    private GeocoderHandler geocoderHandler = new GeocoderHandler();

    private int getMyColor() {
        return getResources().getColor(R.color.colorPrimaryDark);
    }

    private Handler handler = new Handler();

    private void showMyTraces() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (lastLocation == null)
                    return;
                LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                myLastNonTracingLatLng = latLng;

                // If user is tracking a friend, then user's last location will be updated to location value,
                // while it won't be updated in ui
                if (isTracking)
                    return;

                Utils.getAddressFromLocation(latLng.latitude, latLng.longitude, ODashboardActivity.this, geocoderHandler);
                tvTripInfoName.setText(profileModel.getName());
                tvTripInfoTime.setText(Utils.getTimeInString(lastLocation.getTime()));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
                mMap.clear();
                if (isTracing) {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.geodesic(true);
                    polylineOptions.jointType(JointType.ROUND);
                    polylineOptions.startCap(new RoundCap());
                    polylineOptions.endCap(new RoundCap());
                    polylineOptions.color(getMyColor());

                    for (ETLatLng ETLatLng : TripModel.getInstance(ODashboardActivity.this).getWayPoints())
                        polylineOptions.add(ETLatLng.getLatLng());

                    mMap.addPolyline(polylineOptions);
                }
                if (myMarkerOptions == null) {
                    myMarkerOptions = new MarkerOptions();
                    myMarkerOptions.position(latLng);
                    myMarkerOptions.title("You are here");
                    mMap.addMarker(myMarkerOptions);
                } else {
                    myMarkerOptions.position(latLng);
                    mMap.addMarker(myMarkerOptions);
                }
//         else {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));
//            if (defaultCircleOptions == null) {
//                defaultCircleOptions = new CircleOptions();
//                defaultCircleOptions.center(latLng);
//                defaultCircleOptions.radius(4.0d);
//                defaultCircleOptions.fillColor(getMyColor());
//                defaultCircleOptions.strokeColor(Color.WHITE);
//                defaultCircleOptions.strokeWidth(1.0f);
//                mMap.addCircle(defaultCircleOptions);
//            } else {
//                defaultCircleOptions.center(latLng);
//                mMap.addCircle(defaultCircleOptions);
//            }
//        }
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    private void startTracking(FriendModel friendModel, TripModel tripModel, ArrayList<ETLatLng> wayPoints) {
        ViewUtils.showViews(llLayoutTripInfo);

        fabTraceLocation.hide();
        isTracking = true;
        traceFriendsLocation(friendModel, tripModel, wayPoints);
    }

    private void stopTracking() {
        ViewUtils.hideViews(llLayoutTripInfo);

        mMap.clear();
        isTracking = false;
        friendTripQuery.removeEventListener(friendTripEventListener);
        friendTripEventListener = null;
        friendTripQuery = null;
        fabTraceLocation.show();

        ViewUtils.hideViews(tvTrackState);
        showMyTraces();
    }

    private void showAllFriends() {
        if (isFriendListRequesting) {
            ViewUtils.makeToast(this, "Please wait while we prepare list of your friends!", Toast.LENGTH_LONG);
            return;
        }
        isFriendListRequesting = true;

        ViewUtils.hideViews(loader);
        startActivityForResult(new Intent(this, OFriendsActivity.class), Constants.INTENT_LAUNCH_CODES.START_FRIENDS_ACTIVITY_FOR_TRACKING);
    }

    private void getFriendsWayPoints(final FriendModel friendModel) {
        friendTripQuery = FirebaseDatabase.getInstance().getReference("trips")
                .child(friendModel.getF_uid()).limitToLast(1);
        friendTripEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange: dataSnapshot = " + dataSnapshot);

                ArrayList<ETLatLng> latLngs = new ArrayList<>();
                DataSnapshot dataSnapshot1 = null;
                TripModel tripModel = null;

                for (DataSnapshot lastTripChild : dataSnapshot.getChildren()) {
                    Log.e(TAG, "onDataChange: "+ lastTripChild );
                    dataSnapshot1 = lastTripChild;
                    tripModel = lastTripChild.getValue(TripModel.class);
                    break;
                }

                if (tripModel != null) {

                    DataSnapshot wayPointsChild = dataSnapshot1.child("wayPoints");
                    for (DataSnapshot wayPoints : wayPointsChild.getChildren()) {
//                        MyLocation location = wayPoints.getValue(MyLocation.class);
                        ETLatLng ETLatLng = wayPoints.getValue(ETLatLng.class);
                        if (ETLatLng != null) {
                            latLngs.add(ETLatLng);
                        }
                    }

                    startTracking(friendModel, tripModel, latLngs);
                    ViewUtils.hideViews(loader);
                } else {

                    ViewUtils.hideViews(loader);
                    stopTracking();
                    DialogUtils.showSorryAlert(getThisContext(), "No active trips found for "
                            + friendModel.getName() /*+ "\nWould you like to know his/her last wherebe?"*/, new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                ViewUtils.hideViews(loader);
                stopTracking();
                ViewUtils.makeToast(ODashboardActivity.this, databaseError.getMessage());
            }
        };
        friendTripQuery.addValueEventListener(friendTripEventListener);
    }

    private Context getThisContext() {
        return ODashboardActivity.this;
    }

    private void traceFriendsLocation(final FriendModel friendModel, final TripModel tripModel, final ArrayList<ETLatLng> wayPoints) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mMap.clear();

                PolylineOptions polylineOptions = new PolylineOptions()
                        .geodesic(true)
                        .jointType(JointType.ROUND)
                        .color(getMyColor())
                        .startCap(new RoundCap())
                        .endCap(new RoundCap());

                for (ETLatLng location : wayPoints) {
                    polylineOptions.add(location.getLatLng());
                }

                mMap.addPolyline(polylineOptions);

                ETLatLng frndLastLocation;
                if (wayPoints.size() == 0){
                    frndLastLocation = tripModel.getTripSource();
                } else {
                    frndLastLocation = wayPoints.get(wayPoints.size() - 1);
                }

                LatLng latLng = /*getLatLng*/(frndLastLocation.getLatLng());
                Utils.getAddressFromLocation(latLng.latitude, latLng.longitude, ODashboardActivity.this, geocoderHandler);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL));

                ViewUtils.showViews(tvTrackState);
                if (tripModel.isTripOngoing()){
                    tvTrackState.setText("Ongoing!");
                    tvTrackState.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    tvTrackState.setText("Finished!");
                    tvTrackState.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }

                tvTripInfoName.setText(friendModel.getName());
                tvTripInfoTime.setText(Utils.getTimeInString(frndLastLocation.getMillis()));

                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(friendModel.getName() + " is here");
                mMap.addMarker(markerOptions);
            }
        });
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
                startActivity(new Intent(this, ORequestActivity.class));
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

                if (isFriendListRequesting || isTracking) {
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
                Snackbar.make((View) v.getParent(), "UserProfile Activity will be implemented soon.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            break;
            case R.id.fab_close: {
                if (isTracking)
                    stopTracking();
                else if (isTracing)
                    stopTracing();
                else
                    ViewUtils.hideViews(llLayoutTripInfo);
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
//            case R.id.nav_friends: {
//                startActivity(new Intent(this, OFriendsActivity.class));
//            }
//            return true;
//            case R.id.nav_contacts: {
//                startActivity(new Intent(this, OContactsActivity.class));
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
        if (locationService == null)
            return;
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationService.mLocationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> resultTask = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        resultTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(ODashboardActivity.this, REQUEST_TURN_ON_GPS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
        resultTask.addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
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
                LocationSettingsStates locationSettingsStates = locationSettingsResponse.getLocationSettingsStates();
//                if (locationSettingsStates.isGpsPresent())
//                    ViewUtils.makeToast("Using GPS to give best results.");
//                else if (locationSettingsStates.isNetworkLocationPresent())
//                    ViewUtils.makeToast("Using Network Locations.");
//                else if (locationSettingsStates.isLocationPresent())
//                    ViewUtils.makeToast("Using Location");

                if (AppPermissions.checkLocationPermission(ODashboardActivity.this)) {
                    if (checkGPSEnabled()) {
                        locationService.requestLocationUpdates();
                    }
                }
            }
        });
    }

    public void updateInternetError(boolean isOnlineNow) {
        String message = isOnlineNow ? "Cheers! We are back." : "Sorry! Could not connect to internet.";
        int backgroundColor = isOnlineNow ? Color.GREEN : Color.RED;
        tvNetworkInfo.setText(message);
        tvNetworkInfo.setBackgroundColor(backgroundColor);
        if (tvNetworkInfo.getVisibility() == View.GONE)
            ViewUtils.showViews(tvNetworkInfo);
        if (isOnlineNow) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewUtils.hideViews(tvNetworkInfo);
                        }
                    });
                }
            }, 2000);
        }
    }

    private RequestListener requestListener = new RequestListener() {
        @Override
        public void onNewRequestReceived(RequestModel requestModel) {
            requestHelper.countNewRequest();
        }

        @Override
        public void onRequestSendSuccess(RequestModel requestModel) {

        }

        @Override
        public void onRequestSendFailed(RequestModel requestModel, Error.ErrorType errorTypeMessage) {

        }

        @Override
        public void onRequestSending(RequestModel requestModel) {

        }

        @Override
        public void onRequestStatusUpdated(RequestModel requestModel) {

        }

        @Override
        public void onCreteRequestFailed(Error.ErrorType s) {

        }

        @Override
        public void onNewRequestsCounted(int count) {
            updateNotificationCount(count);
        }

        @Override
        public void onRequestsFetchFailed(Error.ErrorType message) {

        }

        @Override
        public void onAllReceivedRequestsFetched(ArrayList<RequestModel> requestModels) {

        }

        @Override
        public void onAllSentRequestsFetched(ArrayList<RequestModel> requestModels) {

        }

        @Override
        public void onStatusUpdateFailed(Error.ErrorType message) {

        }
    };

    private void updateNotificationCount(int count) {
        if (count == 0) {
            ((TextView) findViewById(R.id.tv_notifications)).setText("");
        } else {
            ((TextView) findViewById(R.id.tv_notifications)).setText("" + count);
        }
    }

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

}
