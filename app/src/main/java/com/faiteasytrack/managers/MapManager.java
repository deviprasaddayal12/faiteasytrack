package com.faiteasytrack.managers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.listeners.OnMapStateChangeListener;
import com.faiteasytrack.observables.ObservableBoolean;
import com.faiteasytrack.utils.FileUtils;
import com.faiteasytrack.utils.MapUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.android.SphericalUtil;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

public class MapManager {

    public static final String TAG = MapManager.class.getSimpleName();

    private static final int ZOOM_LEVEL = 18;
    private static final int MIN_DISTANCE = 400;

    public static boolean isReady = false;

    private Activity activity;
    private Handler handler;
    private FirebaseUser firebaseUser;

    private OnMapStateChangeListener onMapStateChangeListener;

    private GoogleMap googleMap;

    // Following are used for non-tracing or non-tracking mode
    private Circle circleAccuracy;
    private Marker markerPosition;
    private boolean shouldRecenter;
    private Location locationLastRcvd;

    private ObservableBoolean.Tracking obsTracking;
    private ObservableBoolean.Tracing obsTracing;

    private Observer tracingObserver = new Observer() {
        @Override
        public void update(Observable observable, Object o) {

        }
    };

    private Observer trackingObserver = new Observer() {
        @Override
        public void update(Observable observable, Object o) {

        }
    };

    public MapManager(Activity activity, Handler handler, GoogleMap googleMap) {
        this.activity = activity;
        this.handler = handler;
        this.googleMap = googleMap;

        initialise();
        isReady = true;
    }

    private void initialise(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        shouldRecenter = true;

        obsTracing = new ObservableBoolean.Tracing(false);
        obsTracing.addObserver(tracingObserver);

        obsTracking = new ObservableBoolean.Tracking(false);
        obsTracking.addObserver(trackingObserver);
    }

    public void setOnMapStateChangeListener(OnMapStateChangeListener onMapStateChangeListener) {
        this.onMapStateChangeListener = onMapStateChangeListener;
    }

    public void onLocationReceived(Location location){
        locationLastRcvd = location;

        if (!obsTracing.isTracing() && !obsTracking.isTracking())
            updatePosition();
    }

    public void onGpsStatusChanged(boolean available){

    }

    public void gotoCurrentLocation() {
        shouldRecenter = true;
        updatePosition();
    }

    private void updatePosition() {
        LatLng currentLatLng = new LatLng(locationLastRcvd.getLatitude(), locationLastRcvd.getLongitude());
        float accuracy = locationLastRcvd.getAccuracy();

        if (circleAccuracy == null)
            circleAccuracy = googleMap.addCircle(MapUtils.createAccuracyCircle(activity, currentLatLng, accuracy));

        if (markerPosition == null)
            markerPosition = googleMap.addMarker(MapUtils.createPositionMarker(activity, currentLatLng, "You"));

        ((TextView) activity.findViewById(R.id.tv_my_location_accuracy)).setText(MapUtils.getAccuracyText(accuracy));

        markerPosition.setPosition(currentLatLng);
        circleAccuracy.setCenter(currentLatLng);
        circleAccuracy.setRadius(accuracy);

        if (shouldRecenter) addLatLngBoundsToCircleAndAnimate();
    }

    private void addLatLngBoundsToCircleAndAnimate() {
        LatLng centre = circleAccuracy.getCenter();
        double radius = circleAccuracy.getRadius();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        LatLng targetNorthEast = SphericalUtil.computeOffset(centre, radius * Math.sqrt(2), 45);
        LatLng targetSouthWest = SphericalUtil.computeOffset(centre, radius * Math.sqrt(2), 225);

        builder.include(targetNorthEast);
        builder.include(targetSouthWest);

        final LatLngBounds latLngBounds = builder.build();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (MapUtils.areBoundsTooSmall(latLngBounds, MIN_DISTANCE))
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), ZOOM_LEVEL));
                else
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, ZOOM_LEVEL));

                shouldRecenter = false;
            }
        });
    }
}
