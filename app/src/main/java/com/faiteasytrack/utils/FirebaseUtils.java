package com.faiteasytrack.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtils {

    public static final String TAG = FirebaseUtils.class.getSimpleName();

    public static StorageReference getProfilePhotoReference(){
        return getProfilePhotoReference(FileUtils.RESOLUTION_HIGH);
    }

    public static StorageReference getProfilePhotoReference(int resolution){
        String location = "images/profilePhotos";
        if (resolution == FileUtils.RESOLUTION_SMALL)
            location = location + "/small";
        else if (resolution == FileUtils.RESOLUTION_MEDIUM)
            location = location + "medium";

        return FirebaseStorage.getInstance().getReference(location)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }
}
