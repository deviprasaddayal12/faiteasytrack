package com.faiteasytrack.firebase;

import com.faiteasytrack.constants.User;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.utils.FileUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtils {

    public static final String TAG = FirebaseUtils.class.getSimpleName();

    private static FirebaseUser firebaseUser = null;

    public static synchronized void initialise(){
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public static FirebaseUser getUser(){
        return firebaseUser;
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
                .child(firebaseUser.getUid());
    }

    public static UserModel getNewGuestModel(){
        UserModel userModel = new UserModel();

        userModel.setUid(firebaseUser.getUid());
        userModel.setPhoneNumber(firebaseUser.getPhoneNumber());
        userModel.setName(firebaseUser.getDisplayName());

        userModel.setCode("");
        userModel.setPassword("");
        userModel.setI_am(User.TYPE_GUEST);

        return userModel;
    }

    public static void reset(){
        firebaseUser = null;
    }
}
