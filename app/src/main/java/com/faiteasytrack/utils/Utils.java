package com.faiteasytrack.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.faiteasytrack.R;
import com.faiteasytrack.classess.ETLatLng;
import com.faiteasytrack.enums.Preferences;
import com.faiteasytrack.models.PreferenceModel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Utils {
    private static final String TAG = "Utils";

    public static long getMillisAtNow(){
        return Calendar.getInstance().getTimeInMillis();
    }

    public static void getAddressFromLocation(final double latitude, final double longitude,
                                              final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(
                            latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        Log.e(TAG, "run: " + address );
                        StringBuilder sb = new StringBuilder();
//                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
//                            sb.append(address.getAddressLine(i)).append("\n");
//                        }
//                        sb.append(address.getLocality()).append("\n");
//                        sb.append(address.getPostalCode()).append("\n");
//                        sb.append(address.getCountryName());
                        sb.append(address.getAddressLine(0));
                        result = sb.toString();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
//                        result = "Latitude: " + latitude + " Longitude: " + longitude +
//                                "\n\nAddress:\n" + result;
                        bundle.putString("address", result);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = "Latitude: " + latitude + " Longitude: " + longitude +
                                "\n Unable to get address for this lat-long.";
                        bundle.putString("address", result);
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }

    public static String getTimeInString(long millis){
        SimpleDateFormat format = new SimpleDateFormat("hh:mm aa, dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return format.format(calendar.getTime());
    }

    public static long getTimeInMillis(){
        return Calendar.getInstance().getTimeInMillis();
    }

    public static void hideSoftKeyboard(Context context){
        try {
            InputMethodManager imm =(InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = ((Activity) context).findViewById(android.R.id.content);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception error){
            error.printStackTrace();
        }
    }

    public static boolean isInvalidString(String toString) {
        return  (toString == null || toString.equals(""));
    }

    public static InputFilter[] getLengthFilter(int size) {
        return new InputFilter[]{new InputFilter.LengthFilter(size)};
    }

    public void showSoftKeyboard(Context context, View view){
        try {
            if(view.requestFocus()){
                InputMethodManager imm =(InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view,InputMethodManager.SHOW_IMPLICIT);
            }
        } catch (Exception error){
            error.printStackTrace();
        }
    }

    public static ColorStateList getColorStateList(Context context){
        Resources resources = context.getResources();
        int[][] states = new int[][] {{android.R.attr.state_activated},{-android.R.attr.state_activated}};
        int[] colors = new int[]{resources.getColor(R.color.colorAccent), resources.getColor(R.color.colorGradientEnd)};
        return new ColorStateList(states, colors);
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "" + location.getLatitude() + ", " + location.getLongitude() + " at " + location.getSpeed() +"m/s" +
                " with accuracy " + location.getAccuracy() + " mtrs";
    }

    public static String getLocationText(ETLatLng location) {
        return location == null ? "Unknown location" :
                "" + location.getLatitude() + ", " + location.getLongitude();
    }

    public static String getLocationTitle(Context context) {
        return String.format("%s", context.getString(R.string.location_updated));
    }

    public static ETLatLng getMyLatLng(Location location){
        return new ETLatLng(location.getLatitude(), location.getLongitude(), location.getTime(), location.getSpeed(), location.getAccuracy());
    }

    public static String getPhoneNumberWithoutCode(String phoneWithCode){
        return phoneWithCode.substring(3);
    }

    public static PreferenceModel getDefaultPreference(){
        PreferenceModel preferenceModel = new PreferenceModel();

        preferenceModel.setShareLocationTo(Preferences.ShareLocation.TO_ANYONE);
        preferenceModel.setDoShareLocation(true);

        return preferenceModel;
    }
}
