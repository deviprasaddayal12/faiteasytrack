package com.faiteasytrack.helpers;

import android.content.Context;

import com.faiteasytrack.exceptions.EasyTrackDatabaseError;
import com.faiteasytrack.exceptions.UserModelNotFound;
import com.faiteasytrack.exceptions.UserNotFoundException;
import com.faiteasytrack.classess.ETLatLng;
import com.faiteasytrack.models.ProfileModel;
import com.faiteasytrack.models.TripModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";

    public interface Constants {
        int STATE_INITIALIZED = 1;
        int STATE_CODE_SENT = 2;
        int STATE_VERIFY_FAILED = 3;;
        int STATE_VERIFY_TIMEOUT = 7;
    }

    public static void initialize(Context context) {

    }



    public interface UserProfileUpdateListener {
        void onUserProfileUpdateSucceed(DatabaseReference databaseReference, String message);

        void onUserProfileUpdateFailed(DatabaseError databaseError);
    }

    public interface LoginVerifyListener {
        void onUserLoginVerified(boolean validCredentials, String message, Throwable t);

        void onUserVerificationFailed(Throwable t);
    }

//    public interface OnThisUserFoundListener {
//        void onUserFound(ProfileModel userModel);
//
//        void inFailureOccured(DatabaseError databaseError);
//    }

//    public interface FetchAllUsersListener {
//        void onUsersListFetchingSuccess(ArrayList<String> strings);
//
//        void onUsersListFetchingFailure(DatabaseError databaseError);
//    }

    public interface FetchAllFriendsListener {
        void onFriendsListFetchingSuccess(ArrayList<ProfileModel> arrayList);

        void onFriendsListFetchingFailure(DatabaseError databaseError);
    }

//    public interface TrackFriendLocationListener {
//        void onFriendLocationFetchingSuccess(DataSnapshot dataSnapshot);
//
//        void onFriendLocationFetchingFailure(DatabaseError databaseError);
//    }

//    public interface ShareMyLocationListener {
//        void onMyLocationUploadSuccess(DatabaseReference databaseReference, String message);
//
//        void onMyLocationUploadFailure(DatabaseError databaseError);
//    }

//    public static boolean isUserAuthorized() {
//        return FirebaseAuth.getInstance().getCurrentUser() != null;
//    }

    public static String getUserPhone() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException("ProfileModel not found. Authorization Required.");
        } else {
            return currentUser.getPhoneNumber();
        }
    }

    public static void updateUserDetails(ProfileModel profileModel, final UserProfileUpdateListener signUpUpdateListener) throws UserNotFoundException {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException("ProfileModel not found. Authorization Required.");
        } else {
            String authorizationId = currentUser.getUid();
            profileModel.setAuthorizationId(authorizationId);
            final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users");
            userReference.child(authorizationId).setValue(profileModel, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError == null)
                        signUpUpdateListener.onUserProfileUpdateSucceed(databaseReference, "Cheers! Your profile details updated successfully.");
                    else
                        signUpUpdateListener.onUserProfileUpdateFailed(databaseError);
                }
            });
        }
    }

    public static void setUserRegisteredAs(final int registrationType, final UserProfileUpdateListener profileUpdateListener) throws UserNotFoundException {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException("ProfileModel not found. Authorization Required.");
        } else {
            String authorizationId = currentUser.getUid();
            final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users");
            HashMap<String, Object> userRegistrationUpdates = new HashMap<>();
//            userRegistrationUpdates.put(com.easytrack.utils.Constants.USER.REGISTRATION_TYPE, registrationType);
            userReference.child(authorizationId).updateChildren(userRegistrationUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if (databaseError == null) {
//                        profileUpdateListener.onUserProfileUpdateSucceed(databaseReference, "Cheers! You have been registered as "
//                                + com.easytrack.utils.Constants.getRegistrationType(registrationType));
                    } else
                        profileUpdateListener.onUserProfileUpdateFailed(databaseError);
                }
            });
        }
    }

    public static void verifyUserLogIn(FirebaseUser user, final LoginVerifyListener loginVerifyListener) throws UserNotFoundException {
        if (user == null) {
            throw new UserNotFoundException("ProfileModel not found. Authorization Required.");
        } else {
            String authorizationId = user.getUid();
            final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users");
            userReference.child(authorizationId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        loginVerifyListener.onUserLoginVerified(true, "You're welcome to EasyTrack.", null);
                    } else {
                        loginVerifyListener.onUserVerificationFailed(new UserModelNotFound("User with given credentials doesn't exist! Try Signing In."));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    throw new EasyTrackDatabaseError(databaseError.getMessage());
                }
            });
        }
    }

//    public static void getThisUser(final OnThisUserFoundListener listener) throws UserModelNotFound {
//        final DatabaseReference userReference = FirebaseHelper.getCurrentUserDatabase();
//        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                listener.onUserFound(dataSnapshot.getValue(ProfileModel.class));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                listener.inFailureOccured(databaseError);
//            }
//        });
//    }

    public static DatabaseReference getCurrentUserDatabase() throws UserModelNotFound {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException("ProfileModel not found. Authorization Required.");
        } else {
            String authorizationId = currentUser.getUid();
            DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users");
            return userReference.child(authorizationId);
        }
    }

    public static void getAllFriends(final ProfileModel myModel, final FetchAllFriendsListener listener) throws UserModelNotFound {
        final DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users");
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<ProfileModel> profileModels = new ArrayList<>();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ProfileModel profileModel = dataSnapshot1.getValue(ProfileModel.class);
                    if (myModel == null)
                        throw new UserModelNotFound("Unable to fetch details.");
                    if (profileModel != null) {
                        if (!profileModel.getAuthorizationId().equals(myModel.getAuthorizationId()))
                            profileModels.add(profileModel);
                    }
                }
                listener.onFriendsListFetchingSuccess(profileModels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFriendsListFetchingFailure(databaseError);
            }
        });
    }

    public static String createMyTrip(Context context) {
        TripModel myTripModel = TripModel.getInstance(context);
        DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference("trips").child(myTripModel.getUserId()).push();
        tripReference.setValue(myTripModel);
        return tripReference.getKey();
    }

    public static void uploadMyLocationToServer(Context context, String currentTripKey, ETLatLng ETLatLng) {
        FirebaseDatabase.getInstance().getReference("trips")
                .child(TripModel.getInstance(context).getUserId()).child(currentTripKey)
                .child("wayPoints").push().setValue(ETLatLng);
    }

    public static void endMyTrip(Context context, String currentTripKey) {
        TripModel finishingTripModel = TripModel.getInstance(context);

        DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference("trips");

        DatabaseReference currentTripRef = tripReference.child(finishingTripModel.getUserId()).child(currentTripKey);
        currentTripRef.child("tripOngoing").setValue(finishingTripModel.isTripOngoing());
        currentTripRef.child("tripDest").setValue(finishingTripModel.getTripDest());
        currentTripRef.child("tripEndTime").setValue(finishingTripModel.getTripEndTime());

        TripModel.getInstance(context).reset();
    }

//    public static void fetchFriendLocationFromServer(ProfileModel friend, final TrackFriendLocationListener listener) {
//        final DatabaseReference tripReference = FirebaseDatabase.getInstance().getReference("trips");
//        tripReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                listener.onFriendLocationFetchingSuccess(dataSnapshot);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                listener.onFriendLocationFetchingFailure(databaseError);
//            }
//        });
//    }
}
