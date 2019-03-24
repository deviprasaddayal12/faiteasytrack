package com.faiteasytrack.helpers;

import android.content.Context;

import com.faiteasytrack.constants.Error;
import com.faiteasytrack.listeners.ProfileListener;
import com.faiteasytrack.models.ProfileModel;
import com.faiteasytrack.utils.SharePreferences;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProfileHelper {

    private static final String PROFILE_DB = "profile";

    private DatabaseReference gProfileBaseDbRef, gProfileDbRef;

    private Context gContext;
    private ProfileListener profileListener;

    private String gUserUid;

    public ProfileHelper(Context gContext, ProfileListener profileListener) {
        this.gContext = gContext;
        this.profileListener = profileListener;

        gUserUid = SharePreferences.getUid(gContext);

        gProfileBaseDbRef = FirebaseDatabase.getInstance().getReference(PROFILE_DB);
        gProfileDbRef = gProfileBaseDbRef.child(gUserUid);
    }

    private ValueEventListener valueEventListener_getProfile = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ProfileModel profileModel = dataSnapshot.getValue(ProfileModel.class);

            if (profileModel != null)
                profileListener.onProfileRetrieveSuccess(profileModel);
            else
                profileListener.onProfileRetrieveFailure(Error.ErrorType.ERROR_NOT_DEFINED);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            profileListener.onProfileRetrieveFailure(Error.ErrorType.ERROR_NOT_DEFINED);
        }
    };

    private ValueEventListener valueEventListener_updateProfile = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ProfileModel profileModel = dataSnapshot.getValue(ProfileModel.class);

            if (profileModel != null)
                profileListener.onProfileUpdateSuccess(profileModel);
            else
                profileListener.onProfileUpdateFailure(Error.ErrorType.ERROR_NOT_DEFINED);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            profileListener.onProfileUpdateFailure(Error.ErrorType.ERROR_NOT_DEFINED);
        }
    };

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private DatabaseReference.CompletionListener completionListener = new DatabaseReference.CompletionListener() {
        @Override
        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
            profileListener.onTaskComplete();
        }
    };

    public void getProfileDetails() {
        gProfileDbRef.addValueEventListener(valueEventListener_getProfile);
    }

    public void getProfileDetailsForUid(String uid) {
        final DatabaseReference databaseReference = gProfileBaseDbRef.child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ProfileModel profileModel = dataSnapshot.getValue(ProfileModel.class);

                if (profileModel != null)
                    profileListener.onProfileRetrieveSuccess(profileModel);
                else
                    profileListener.onProfileRetrieveFailure(Error.ErrorType.ERROR_NOT_DEFINED);

                databaseReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                profileListener.onProfileRetrieveFailure(Error.ErrorType.ERROR_NOT_DEFINED);

                databaseReference.removeEventListener(this);
            }
        });
    }

    public void updateProfileDetails(ProfileModel profileModel) {
        profileModel.setAuthorizationId(gUserUid);
        gProfileDbRef.addValueEventListener(valueEventListener_updateProfile);
        gProfileDbRef.setValue(profileModel);
    }

    public void reset() {
        try {
            gProfileDbRef.removeEventListener(valueEventListener_getProfile);
            gProfileDbRef.removeValue(completionListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMyProfileDetails() {
        getProfileDetailsForUid(gUserUid);
    }
}
