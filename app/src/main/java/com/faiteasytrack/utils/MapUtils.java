package com.faiteasytrack.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapUtils {
    public static final String TAG = MapUtils.class.getSimpleName();

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
