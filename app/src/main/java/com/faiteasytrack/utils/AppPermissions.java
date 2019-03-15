package com.faiteasytrack.utils;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AppPermissions {

    public interface REQUESTS {
        int ACCESS_LOCATION_REQUEST = 0;
        int ACCESS_INTERNET_REQUEST = 1;
        int ACCESS_MESSAGE_READ_REQUEST = 2;
        int ACCESS_NETWORK_STATE_REQUEST = 3;
        int ACCESS_READ_CONTACTS = 4;
        int ACCESS_CAMERA_REQUEST = 5;
        int ACCESS_GALLERY_REQUEST = 6;
        int ACCESS_FILE_BROWSER_REQUEST = 7;
    }

    public static boolean checkReadConactsPermission(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_CONTACTS)) {
                new AlertDialog.Builder(context)
                        .setTitle("Read Contact Access Needed")
                        .setMessage("Easytrack needs to read the contacts of your phonebook.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_CONTACTS}, REQUESTS.ACCESS_READ_CONTACTS);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_CONTACTS}, REQUESTS.ACCESS_READ_CONTACTS);
            }
            return false;
        } else
            return true;
    }

    public static boolean checkNetworkStateReceivePermission(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                new AlertDialog.Builder(context)
                        .setTitle("Network State Access Needed")
                        .setMessage("Easytrack needs to access the states of network.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, REQUESTS.ACCESS_NETWORK_STATE_REQUEST);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, REQUESTS.ACCESS_NETWORK_STATE_REQUEST);
            }
            return false;
        } else
            return true;
    }

    public static boolean checkMessagePermission(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.INTERNET)) {
                new AlertDialog.Builder(context)
                        .setTitle("Read Message Permission Needed")
                        .setMessage("Easytrack needs to detect and read OTP sent while verification, automatically.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.INTERNET}, AppPermissions.REQUESTS.ACCESS_MESSAGE_READ_REQUEST);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.INTERNET}, AppPermissions.REQUESTS.ACCESS_MESSAGE_READ_REQUEST);
            }
            return false;
        } else
            return true;
    }

    public static boolean checkInternetPermission(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.INTERNET)) {
                new AlertDialog.Builder(context)
                        .setTitle("Data Use Needed")
                        .setMessage("Easytrack will use data to communicate with server.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.INTERNET}, AppPermissions.REQUESTS.ACCESS_INTERNET_REQUEST);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.INTERNET}, AppPermissions.REQUESTS.ACCESS_INTERNET_REQUEST);
            }
            return false;
        } else
            return true;
    }

    public static boolean checkLocationPermission(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(context)
                        .setTitle("Location Permission Needed")
                        .setMessage("Easytrack needs to access your location.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, AppPermissions.REQUESTS.ACCESS_LOCATION_REQUEST);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, AppPermissions.REQUESTS.ACCESS_LOCATION_REQUEST);
            }
            return false;
        } else
            return true;
    }

    public static boolean checkCameraPermission(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(context)
                        .setTitle("Camera Permission Needed")
                        .setMessage("Easytrack needs to access your camera.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, REQUESTS.ACCESS_CAMERA_REQUEST);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, REQUESTS.ACCESS_CAMERA_REQUEST);
            }
            return false;
        } else
            return true;
    }

    public static boolean checkGalleryPermission(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(context)
                        .setTitle("Gallery Permission Needed")
                        .setMessage("Easytrack needs to access your gallery.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        REQUESTS.ACCESS_GALLERY_REQUEST);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUESTS.ACCESS_GALLERY_REQUEST);
            }
            return false;
        } else
            return true;
    }

    public static boolean checkFileBrowserPermission(final Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(context)
                        .setTitle("File Browser Permission Needed")
                        .setMessage("Easytrack needs to access your file browser.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUESTS.ACCESS_FILE_BROWSER_REQUEST);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUESTS.ACCESS_FILE_BROWSER_REQUEST);
            }
            return false;
        } else
            return true;
    }
}
