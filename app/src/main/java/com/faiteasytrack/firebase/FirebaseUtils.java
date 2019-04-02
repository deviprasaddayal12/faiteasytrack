package com.faiteasytrack.firebase;

import com.faiteasytrack.utils.FileUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtils {

    public static final String TAG = FirebaseUtils.class.getSimpleName();

    public static FirebaseUser getUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static DatabaseReference getUserReference(){
        return FirebaseDatabase.getInstance().getReference().child("users");
    }

    public static DatabaseReference getAdminReference(){
        return FirebaseDatabase.getInstance().getReference().child("admins");
    }

    public static DatabaseReference getVendorReference(){
        return FirebaseDatabase.getInstance().getReference().child("vendors");
    }

    public static DatabaseReference getDriverReference(){
        return FirebaseDatabase.getInstance().getReference().child("drivers");
    }

    public static DatabaseReference getRouteReference(){
        return FirebaseDatabase.getInstance().getReference().child("routes");
    }

    public static StorageReference getProfilePhotoReference(){
        return getProfilePhotoReference(FileUtils.RESOLUTION_HIGH);
    }

    public static StorageReference getProfilePhotoReference(int resolution){
        String location = "images/profilePhotos";
        if (resolution == FileUtils.RESOLUTION_SMALL)
            location = location + "/small";
        else if (resolution == FileUtils.RESOLUTION_MEDIUM)
            location = location + "/medium";

        return FirebaseStorage.getInstance().getReference(location)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }
}
