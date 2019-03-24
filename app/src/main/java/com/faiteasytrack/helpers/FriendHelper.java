package com.faiteasytrack.helpers;

import android.content.Context;
import android.util.Log;

import com.faiteasytrack.constants.Error;
import com.faiteasytrack.listeners.FriendListener;
import com.faiteasytrack.models.FriendModel;
import com.faiteasytrack.models.ProfileModel;
import com.faiteasytrack.models.RequestModel;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.Utils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FriendHelper {

    public static final String TAG = "FriendHelper";

    public static final String FRIEND_DB = "friends";

//    private static FriendHelper friendInteractor;

    private Context gContext;
    private FriendListener friendListener;

    private String uid;

    private DatabaseReference gDbRefFriends, gDbRefBaseFriends;
    private ArrayList<FriendModel> friendModels = new ArrayList<>();

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.e(TAG, "onChildAdded: " + dataSnapshot);
            FriendModel friendModel = dataSnapshot.getValue(FriendModel.class);

            if (friendModel != null) {
                friendModels.add(friendModel);
                friendListener.onNewFriendAdded(friendModel, friendModels);
            } else
                friendListener.onFriendAddFailed(Error.ErrorType.ERROR_NOT_DEFINED);
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
            friendListener.onFriendAddFailed(Error.ErrorType.ERROR_NOT_DEFINED);
        }
    };

    private ValueEventListener valueEventListener_allFriends = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<FriendModel> friendModels = new ArrayList<>();

            for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()){
                FriendModel friendModel = friendSnapshot.getValue(FriendModel.class);

                if (friendModel != null)
                    friendModels.add(friendModel);
            }

            friendListener.onAllFriendsFetched(friendModels);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

//    public static synchronized FriendHelper getInstance(Context context, FriendListener friendListener){
//        if (friendInteractor == null)
//            friendInteractor = new FriendHelper(context, friendListener);
//        return friendInteractor;
//    }

    public FriendHelper(Context gContext, FriendListener friendListener) {
        this.gContext = gContext;
        this.friendListener = friendListener;

        uid = SharePreferences.getUid(gContext);

        gDbRefBaseFriends = FirebaseDatabase.getInstance().getReference().child(FRIEND_DB);
        gDbRefFriends = gDbRefBaseFriends.child(uid);
    }

    public void getAllFriends(){
        gDbRefFriends.addChildEventListener(childEventListener);
        gDbRefFriends.addValueEventListener(valueEventListener_allFriends);
    }

    private void addNewFriend(FriendModel friendModel){
        gDbRefFriends.addChildEventListener(childEventListener);

        gDbRefFriends.push().setValue(friendModel);
    }

    public void addNewFriends(final RequestModel requestModel){
//        addAsMyFriend(requestModel);
//        addMeAsFriend(requestModel);
        gDbRefBaseFriends.child(requestModel.getToId()).orderByChild("f_uid").equalTo(requestModel.getFromId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange: " + dataSnapshot);
                if (!dataSnapshot.exists()){
                    addMeAsFriend(requestModel);
                } else {
                    friendListener.onFriendAddFailed(Error.ErrorType.ERROR_NOT_DEFINED);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        gDbRefBaseFriends.child(requestModel.getFromId()).orderByChild("f_uid").equalTo(requestModel.getToId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange: " + dataSnapshot);
                if (!dataSnapshot.exists()){
                    addAsMyFriend(requestModel);
                } else {
                    friendListener.onFriendAddFailed(Error.ErrorType.ERROR_NOT_DEFINED);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addMeAsFriend(RequestModel requestModel){
        ProfileModel profileModel = SharePreferences.getProfileModel(gContext);

        FriendModel friendModel = new FriendModel();
        friendModel.setHeader(false);
        friendModel.setFriendsAtMillis(Utils.getTimeInMillis());
        friendModel.setF_uid(requestModel.getToId());
        friendModel.setName(profileModel.getName());
        friendModel.setPhone(profileModel.getPhones().get(0));
        friendModel.setProfilePicUrl(profileModel.getProfilePhotoUrl());

        // add it friends database
        gDbRefBaseFriends.child(requestModel.getFromId()).push().setValue(friendModel);
    }

    private void addAsMyFriend(RequestModel requestModel){
        FriendModel friendModel = new FriendModel();
        friendModel.setHeader(false);
        friendModel.setFriendsAtMillis(Utils.getTimeInMillis());
        friendModel.setF_uid(requestModel.getFromId());
        friendModel.setName(requestModel.getRequesteeName());
        friendModel.setPhone(requestModel.getRequesteePhone());
        friendModel.setProfilePicUrl(requestModel.getRequesteeProfilePicUrl());

        // add it friends database
        gDbRefBaseFriends.child(requestModel.getToId()).push().setValue(friendModel);
    }

    public void reset(){
        try {
            gDbRefFriends.removeEventListener(valueEventListener_allFriends);
            gDbRefFriends.removeEventListener(childEventListener);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
