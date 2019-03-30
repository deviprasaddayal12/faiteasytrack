package com.faiteasytrack.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;

import com.faiteasytrack.R;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AppPermissions {
    public static final String TAG = AppPermissions.class.getSimpleName();

    public static final int ACCESS_LOCATION = 0;
    public static final int ACCESS_INTERNET = 1;
    public static final int ACCESS_READ_MESSAGE = 2;
    public static final int ACCESS_NETWORK_STATE = 3;
    public static final int ACCESS_READ_CONTACTS = 4;
    public static final int ACCESS_CAMERA = 5;
    public static final int ACCESS_GALLERY = 6;
    public static final int ACCESS_FILE_BROWSER = 7;
    public static final int REQUEST_FOR_UPDATE_USER_DETAILS = 8;

    public static final String[] permissionLocation = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    public static final String[] permissionInternet = new String[]{Manifest.permission.INTERNET};
    public static final String[] permissionReadMessage = new String[]{Manifest.permission.READ_SMS};
    public static final String[] permissionNetworkState = new String[]{Manifest.permission.ACCESS_NETWORK_STATE};
    public static final String[] permissionReadContact = new String[]{Manifest.permission.READ_CONTACTS};
    public static final String[] permissionCamera = new String[]{Manifest.permission.CAMERA};
    public static final String[] permissionGallery = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    public static final String[] permissionFileBrowser = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static boolean checkReadContactsPermission(final Activity context, boolean showDialog) {
        if (ContextCompat.checkSelfPermission(context, permissionReadContact[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (showDialog) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionReadContact[0])) {
                    new AlertDialog.Builder(context)
                            .setTitle("Read Contact Access Needed")
                            .setMessage("Easytrack needs to read the contacts of your phonebook.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(context, permissionReadContact, ACCESS_READ_CONTACTS);
                                }
                            }).create().show();
                } else {
                    ActivityCompat.requestPermissions(context, permissionReadContact, ACCESS_READ_CONTACTS);
                }
            }
            return false;
        } else
            return true;
    }

    public static boolean checkNetworkStateReceivePermission(final Activity context, boolean showDialog) {
        if (ContextCompat.checkSelfPermission(context, permissionNetworkState[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (showDialog) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionNetworkState[0])) {
                    new AlertDialog.Builder(context)
                            .setTitle("Network State Access Needed")
                            .setMessage("Easytrack needs to access the states of network.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(context, permissionNetworkState, ACCESS_NETWORK_STATE);
                                }
                            }).create().show();
                } else {
                    ActivityCompat.requestPermissions(context, permissionNetworkState, ACCESS_NETWORK_STATE);
                }
            }
            return false;
        } else
            return true;
    }

    public static boolean checkMessagePermission(final Activity context, boolean showDialog) {
        if (ContextCompat.checkSelfPermission(context, permissionReadMessage[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (showDialog) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionReadMessage[0])) {
                    new AlertDialog.Builder(context)
                            .setTitle("Read Message Permission Needed")
                            .setMessage("Easytrack needs to detect and read OTP sent while verification, automatically.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(context, permissionReadMessage, ACCESS_READ_MESSAGE);
                                }
                            }).create().show();
                } else {
                    ActivityCompat.requestPermissions(context, permissionReadMessage, ACCESS_READ_MESSAGE);
                }
            }
            return false;
        } else
            return true;
    }

    public static boolean checkInternetPermission(final Activity context, boolean showDialog) {
        if (ContextCompat.checkSelfPermission(context, permissionInternet[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (showDialog) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionInternet[0])) {
                    new AlertDialog.Builder(context)
                            .setTitle("Data Use Needed")
                            .setMessage("Easytrack will use data to communicate with server.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(context, permissionInternet, ACCESS_INTERNET);
                                }
                            }).create().show();
                } else {
                    ActivityCompat.requestPermissions(context, permissionInternet, ACCESS_INTERNET);
                }
            }
            return false;
        } else
            return true;
    }

    public static boolean checkLocationPermission(final Activity context, boolean showDialog) {
        if (ContextCompat.checkSelfPermission(context, permissionLocation[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (showDialog) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionLocation[0])) {
                    new AlertDialog.Builder(context)
                            .setTitle("Location Permission Needed")
                            .setMessage("Easytrack needs to access your location.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(context, permissionLocation, ACCESS_LOCATION);
                                }
                            }).create().show();
                } else {
                    ActivityCompat.requestPermissions(context, permissionLocation, ACCESS_LOCATION);
                }
            }
            return false;
        } else
            return true;
    }

    public static boolean checkCameraPermission(final Activity context, boolean showDialog) {
        Log.i(TAG, "checkCameraPermission: ");
        if (ContextCompat.checkSelfPermission(context, permissionCamera[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (showDialog) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionCamera[0])) {
                    new AlertDialog.Builder(context)
                            .setTitle("Camera Permission Needed")
                            .setMessage("Easytrack needs to access your camera.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(context, permissionCamera, ACCESS_CAMERA);
                                }
                            }).create().show();
                } else {
                    ActivityCompat.requestPermissions(context, permissionCamera, ACCESS_CAMERA);
                }
            }
            return false;
        } else
            return true;
    }

    public static boolean checkGalleryPermission(final Activity context, boolean showDialog) {
        if (ContextCompat.checkSelfPermission(context, permissionGallery[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (showDialog) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionGallery[0])) {
                    new AlertDialog.Builder(context)
                            .setTitle("Gallery Permission Needed")
                            .setMessage("Easytrack needs to access your gallery.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(context, permissionGallery, ACCESS_GALLERY);
                                }
                            }).create().show();
                } else {
                    ActivityCompat.requestPermissions(context, permissionGallery, ACCESS_GALLERY);
                }
            }
            return false;
        } else
            return true;
    }

    public static boolean checkFileBrowserPermission(final Activity context, boolean showDialog) {
        if (ContextCompat.checkSelfPermission(context, permissionFileBrowser[0])
                != PackageManager.PERMISSION_GRANTED) {
            if (showDialog){
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionFileBrowser[0])) {
                    new AlertDialog.Builder(context)
                            .setTitle("File Browser Permission Needed")
                            .setMessage("Easytrack needs to access your file browser.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(context, permissionFileBrowser, ACCESS_FILE_BROWSER);
                                }
                            }).create().show();
                } else {
                    ActivityCompat.requestPermissions(context, permissionFileBrowser, ACCESS_FILE_BROWSER);
                }
            }
            return false;
        } else
            return true;
    }

    public static void showAllowPermissionDialog(Context context, final String[] permissions,
                                                 final OnPermissionChangeListener listener) {
        showAllowPermissionDialog(context, "If permissions denied, you might not be able " +
                "to enjoy many features.", permissions, listener);
    }

    public static void showAllowPermissionDialog(Context context, String message, final String[] permissions,
                                                 final OnPermissionChangeListener listener) {
        try {
            if (context != null) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context, R.style.AlertWarning);
                dialog.setTitle("Alas!");
                dialog.setMessage(message);
                dialog.setCancelable(false);
                dialog.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (listener != null)
                            listener.onAllowPermission(permissions);
                    }
                });
                dialog.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (listener != null)
                            listener.onPermissionDenied();
                    }
                });
                dialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnPermissionChangeListener{
        void onAllowPermission(String[] permissions);

        void onPermissionDenied();
    }

    private boolean isLocationAccessPermitted = false;
    private boolean isInternetAccessPermitted = false;
    private boolean isMessageReadPermitted = false;
    private boolean isContactsReadPermitted = false;
    private boolean isCameraAccessPermitted = false;
    private boolean isGalleryAccessPermitted = false;
    private boolean isFileAccessPermitted = false;

    public AppPermissions() {
    }

    public boolean isLocationAccessPermitted() {
        return isLocationAccessPermitted;
    }

    public void setLocationAccessPermitted(boolean locationAccessPermitted) {
        isLocationAccessPermitted = locationAccessPermitted;
    }

    public boolean isInternetAccessPermitted() {
        return isInternetAccessPermitted;
    }

    public void setInternetAccessPermitted(boolean internetAccessPermitted) {
        isInternetAccessPermitted = internetAccessPermitted;
    }

    public boolean isMessageReadPermitted() {
        return isMessageReadPermitted;
    }

    public void setMessageReadPermitted(boolean messageReadPermitted) {
        isMessageReadPermitted = messageReadPermitted;
    }

    public boolean isContactsReadPermitted() {
        return isContactsReadPermitted;
    }

    public void setContactsReadPermitted(boolean contactsReadPermitted) {
        isContactsReadPermitted = contactsReadPermitted;
    }

    public boolean isCameraAccessPermitted() {
        return isCameraAccessPermitted;
    }

    public void setCameraAccessPermitted(boolean cameraAccessPermitted) {
        isCameraAccessPermitted = cameraAccessPermitted;
    }

    public boolean isGalleryAccessPermitted() {
        return isGalleryAccessPermitted;
    }

    public void setGalleryAccessPermitted(boolean galleryAccessPermitted) {
        isGalleryAccessPermitted = galleryAccessPermitted;
    }

    public boolean isFileAccessPermitted() {
        return isFileAccessPermitted;
    }

    public void setFileAccessPermitted(boolean fileAccessPermitted) {
        isFileAccessPermitted = fileAccessPermitted;
    }
}
