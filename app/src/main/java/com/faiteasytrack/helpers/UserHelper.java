package com.faiteasytrack.helpers;

import android.content.Context;
import android.util.Log;

import com.faiteasytrack.enums.Error;
import com.faiteasytrack.listeners.UserListener;
import com.faiteasytrack.models.ContactModel;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.utils.Constants;
import com.faiteasytrack.utils.SharePreferences;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserHelper {
    public static final String TAG = UserHelper.class.getSimpleName();

    private DatabaseReference databaseThisUser, databaseUsers;

    private Context gContext;
    private UserListener userListener;

    private UserListener.onFindUserListener onFindUserListener;
    private ArrayList<UserListener.onFindUserListener> onFindUserListeners = new ArrayList<>();

    private String u_idThisUser;

    public UserHelper(Context gContext, UserListener userListener) {
        this.gContext = gContext;
        this.userListener = userListener;

        init();
    }

    public UserHelper(Context gContext) {
        this.gContext = gContext;

        init();
    }

    private void init(){
        this.u_idThisUser = SharePreferences.getUid(gContext);

        databaseUsers = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_KEYS.kUSERS);
        databaseThisUser = databaseUsers.child(u_idThisUser);
    }

    public void addOnFindUserListener(UserListener.onFindUserListener onFindUserListener) {
        this.onFindUserListeners.add(onFindUserListener);
    }

    public void setOnFindUserListener(UserListener.onFindUserListener onFindUserListener) {
        this.onFindUserListener = onFindUserListener;
    }

    public void checkUserExists(@NonNull FirebaseUser user){
        databaseThisUser.addListenerForSingleValueEvent(valueEventListener_checkUser);
    }

    public void checkUserExists(@NonNull ContactModel contactModel){
        Query userQuery = databaseUsers.orderByKey().equalTo(contactModel.getPhones().get(0));

        userQuery.addListenerForSingleValueEvent(valueEventListener_checkUser);
    }

    public void getUserDetails(){
        databaseThisUser.addValueEventListener(valueEventListener_getUser);
    }

    public void getUserDetailsFromSnapshot(DataSnapshot dataSnapshot){

    }

    public void updateUserDetails(UserModel userModel){
        databaseThisUser.addValueEventListener(valueEventListener_updateUser);
        databaseThisUser.setValue(userModel);
    }

    public void setUserProfileIsUpdated(){
//        databaseThisUser.child(FirebaseHelper.DATABASE_KEYS.kIS_USER_PROFILE_EXISTS).addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                userListener.onChildUpdated(dataSnapshot, s);
//                databaseThisUser.child(FirebaseHelper.DATABASE_KEYS.kIS_USER_PROFILE_EXISTS).removeEventListener(this);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                userListener.onChildUpdated(dataSnapshot, s);
//                databaseThisUser.child(FirebaseHelper.DATABASE_KEYS.kIS_USER_PROFILE_EXISTS).removeEventListener(this);
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                userListener.onChildUpdated(dataSnapshot, s);
//                databaseThisUser.child(FirebaseHelper.DATABASE_KEYS.kIS_USER_PROFILE_EXISTS).removeEventListener(this);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

//        Map<String, Object> map = new HashMap<>();
//        map.put(FirebaseHelper.DATABASE_KEYS.kIS_USER_PROFILE_EXISTS, true);

//        databaseThisUser.child(FirebaseHelper.DATABASE_KEYS.kIS_USER_PROFILE_EXISTS).updateChildren(map);
    }

    public void reset(){
        try {
            databaseThisUser.removeEventListener(valueEventListener_checkUser);
            databaseThisUser.removeEventListener(valueEventListener_getUser);
            databaseThisUser.removeValue();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void checkUserWithNumberExists(String s) {
        Query query = databaseUsers.orderByChild("username").equalTo(s);
        query.addListenerForSingleValueEvent(valueEventListener_checkUserWithNumber);
    }

    private ValueEventListener valueEventListener_getUser = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            UserModel userModel = dataSnapshot.getValue(UserModel.class);

            if (userModel != null)
                userListener.onUserRetrieveSuccess(userModel);
            else
                userListener.onUserRetrieveFailure(Error.ErrorType.ERROR_NOT_DEFINED);

            databaseThisUser.removeEventListener(valueEventListener_getUser);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            userListener.onUserRetrieveFailure(Error.ErrorType.ERROR_NOT_DEFINED);
            databaseThisUser.removeEventListener(valueEventListener_getUser);
        }
    };

    private ValueEventListener valueEventListener_checkUser = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.e(TAG, "onDataChange: " + dataSnapshot);
            if (dataSnapshot.exists()) {
                onFindUserListener.onUserFound(true, dataSnapshot.getValue().toString());
            } else
                onFindUserListener.onUserNotFound(Error.ErrorType.CONTACT_NOT_REGISTERED);

//            userListener.onCheckedUser(dataSnapshot.exists());
            databaseThisUser.removeEventListener(valueEventListener_checkUser);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            onFindUserListener.onUserNotFound(Error.ErrorType.OPERATION_WAS_CANCELLED);

//            userListener.onUserRetrieveFailure(databaseError.getMessage());
            databaseThisUser.removeEventListener(valueEventListener_checkUser);
        }
    };

    private ValueEventListener valueEventListener_checkUserWithNumber = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            userListener.onUserWithNumberFound(dataSnapshot.exists(), dataSnapshot);
            databaseThisUser.removeEventListener(valueEventListener_checkUserWithNumber);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            userListener.onUserRetrieveFailure(Error.ErrorType.ERROR_NOT_DEFINED);
            databaseThisUser.removeEventListener(valueEventListener_checkUserWithNumber);
        }
    };

    private ValueEventListener valueEventListener_updateUser = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            UserModel userModel = dataSnapshot.getValue(UserModel.class);

            if (userModel != null)
                userListener.onUserUpdateSuccess(userModel);
            else
                userListener.onUserUpdateFailure(Error.ErrorType.ERROR_NOT_DEFINED);

            databaseThisUser.removeEventListener(valueEventListener_updateUser);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            userListener.onUserUpdateFailure(Error.ErrorType.ERROR_NOT_DEFINED);
            databaseThisUser.removeEventListener(valueEventListener_updateUser);
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
            userListener.onTaskComplete();
        }
    };
}
