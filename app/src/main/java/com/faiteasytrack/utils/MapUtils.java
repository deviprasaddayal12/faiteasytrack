package com.faiteasytrack.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class MapUtils {
    public static final String TAG = MapUtils.class.getSimpleName();

    public static String getAccuracyText(float accuracy){
        return String.format(Locale.getDefault(), "Your location is accurate to %d meters", (int) accuracy);
    }

    public static CircleOptions createAccuracyCircle(Activity activity, LatLng latLng, float accuracy){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(accuracy);
        circleOptions.strokeColor(activity.getResources().getColor(R.color.colorAccent));
        circleOptions.strokeWidth(2);
        circleOptions.fillColor(activity.getResources().getColor(R.color.colorMapOverLayAccent));

        return circleOptions;
    }

    public static MarkerOptions createPositionMarker(Activity activity, LatLng latLng, String title){
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        PreferenceModel preferenceModel = SharePreferences.getPreferenceModel(activity);
//        boolean isCloud = preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.CLOUD;

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);

        try {
            View markerIconView = activity.getLayoutInflater().inflate(R.layout.icon_map_marker, null);
//            CircularImageView profilePicView = markerIconView.findViewById(R.id.iv_marker_image);
//            if (isCloud){
//                StorageReference profilePicReference = FirebaseUtils.getProfilePhotoReference();
//                profilePicView.setImageResource(R.drawable.user_1);
//            } else {
//                Bitmap bitmapThumbnail = FileUtils.getThumbnail(activity, firebaseUser.getPhotoUrl());
//                profilePicView.setImageBitmap(bitmapThumbnail);
//            }
            ((TextView) markerIconView.findViewById(R.id.tv_marker_title)).setText(title);
            markerOptions.icon(getMarkerFromView(markerIconView));

        } catch (Exception e) {
            e.printStackTrace();
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_foreground));
        }

        return markerOptions;
    }

    public static BitmapDescriptor getMarkerFromView(View view){
        if (view.getMeasuredHeight() <= 0) {
            view.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
        return null;
    }

    public static boolean areBoundsTooSmall(LatLngBounds bounds, float minDistanceInMeter){
        float[] result = new float[1];
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude, result);
        return result[0] < minDistanceInMeter;
    }
}
