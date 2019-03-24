package com.faiteasytrack.helpers;

import android.content.Context;
import android.util.Log;

import com.faiteasytrack.constants.Error;
import com.faiteasytrack.constants.Request;
import com.faiteasytrack.listeners.RequestListener;
import com.faiteasytrack.models.ProfileModel;
import com.faiteasytrack.models.RequestModel;
import com.faiteasytrack.models.RequestStatusModel;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.utils.Constants;
import com.faiteasytrack.utils.SharePreferences;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RequestHelper {

    public static final String TAG = "RequestHelper";

    private DatabaseReference gDbRefBaseRequestsSend, gDbRefBaseRequestsRcvd;

    private Context gContext;
    private RequestListener requestListener;

    private String uid;

    public RequestHelper(Context gContext, RequestListener requestListener) {
        this.gContext = gContext;
        this.requestListener = requestListener;

        this.uid = SharePreferences.getUid(gContext);

        gDbRefBaseRequestsSend = FirebaseDatabase.getInstance().getReference()
                .child(Constants.DATABASE_KEYS.kREQUESTS).child(Constants.DATABASE_KEYS.kSEND);
        gDbRefBaseRequestsRcvd = FirebaseDatabase.getInstance().getReference()
                .child(Constants.DATABASE_KEYS.kREQUESTS).child(Constants.DATABASE_KEYS.kRECEIVED);
    }

    private ChildEventListener childEventListener_sendRequests = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            requestListener.onNewRequestReceived(null);
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

    private ChildEventListener childEventListener_receiveRequests = new ChildEventListener() {
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

    public void initRequestDatabases() {
//        gDbRefBaseRequestsSend.child(uid).addChildEventListener(childEventListener_sendRequests);
        gDbRefBaseRequestsRcvd.child(uid).addChildEventListener(childEventListener_receiveRequests);
    }

    public void countNewRequest(){
        gDbRefBaseRequestsRcvd.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "countNewRequest:onDataChange" + dataSnapshot);
                int count = 0;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    RequestModel requestModel = dataSnapshot1.getValue(RequestModel.class);
                    if (requestModel!= null && requestModel.getRequestStatusModel().getStatus() == Request.REQUEST_SENT)
                        count++;
                }
                requestListener.onNewRequestsCounted(count);

                gDbRefBaseRequestsRcvd.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                gDbRefBaseRequestsRcvd.removeEventListener(this);
            }
        });
    }

    public void fetchAllReceivedRequests(){
        gDbRefBaseRequestsRcvd.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "fetchAllReceivedRequests:onDataChange" + dataSnapshot);
                ArrayList<RequestModel> requestModels = new ArrayList<>();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    RequestModel requestModel = dataSnapshot1.getValue(RequestModel.class);
                    requestModels.add(requestModel);
                }
                requestListener.onAllReceivedRequestsFetched(requestModels);

                gDbRefBaseRequestsRcvd.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                requestListener.onRequestsFetchFailed(Error.ErrorType.ERROR_NOT_DEFINED);

                gDbRefBaseRequestsRcvd.removeEventListener(this);
            }
        });
    }

    public void onUpdateReceivedRequest(RequestModel requestModel) {
        updateRequestToFriendSentDb(requestModel);
        updateRequestToMyReceivedDb(requestModel);
    }

    private void updateRequestToFriendSentDb(final RequestModel requestModel) {

        final DatabaseReference myRefInFriendSentDb = gDbRefBaseRequestsSend.child(requestModel.getFromId()).child(requestModel.getToId());
        myRefInFriendSentDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "updateRequestToFriendSentDb:onDataChange" + dataSnapshot);
                if (!dataSnapshot.exists())

//                else
                    requestListener.onStatusUpdateFailed(Error.ErrorType.ERROR_NOT_DEFINED);

                myRefInFriendSentDb.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                requestListener.onStatusUpdateFailed(Error.ErrorType.ERROR_NOT_DEFINED);

                myRefInFriendSentDb.removeEventListener(this);
            }
        });
        myRefInFriendSentDb.setValue(requestModel);
    }

    private void updateRequestToMyReceivedDb(final RequestModel requestModel) {
        final DatabaseReference frndRefInMyRcvdDb = gDbRefBaseRequestsRcvd.child(requestModel.getToId()).child(requestModel.getFromId());

        frndRefInMyRcvdDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "updateRequestToMyReceivedDb:onDataChange" + dataSnapshot);
                RequestModel requestModel = dataSnapshot.getValue(RequestModel.class);

                if (requestModel != null)
                    requestListener.onRequestStatusUpdated(requestModel);
                else
                    requestListener.onStatusUpdateFailed(Error.ErrorType.ERROR_NOT_DEFINED);

                frndRefInMyRcvdDb.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                requestListener.onStatusUpdateFailed(Error.ErrorType.ERROR_NOT_DEFINED);

                frndRefInMyRcvdDb.removeEventListener(this);
            }
        });
        frndRefInMyRcvdDb.setValue(requestModel);
    }

    public void createNewRequest(String friendUserId, final ProfileModel profileModel) {

        final RequestModel requestModel = createRequestModel(friendUserId, profileModel);

        // upload the same request to two different databases
        //1.to own send

        //2.to others received
        gDbRefBaseRequestsSend.child(uid).child(profileModel.getAuthorizationId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "createNewRequest:gDbRefBaseRequestsSend:onDataChange: " + dataSnapshot);
                if (dataSnapshot.exists()){
                    requestListener.onRequestSendFailed(requestModel, Error.ErrorType.ERROR_NOT_DEFINED);
                } else {
                    gDbRefBaseRequestsRcvd.child(profileModel.getAuthorizationId()).child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.e(TAG, "createNewRequest:gDbRefBaseRequestsRcvd:onDataChange: " + dataSnapshot);
                            if (dataSnapshot.exists()){
                                requestListener.onRequestSendFailed(requestModel, Error.ErrorType.ERROR_NOT_DEFINED);
                            } else {
                                updateRequestToFriendReceivedDb(requestModel);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateRequestToFriendReceivedDb(final RequestModel requestModel) {
        final DatabaseReference myRefInFriendReceivedDb = gDbRefBaseRequestsRcvd.child(requestModel.getToId()).child(uid);
        myRefInFriendReceivedDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "updateRequestToFriendReceivedDb:onDataChange: " + dataSnapshot);
                if (!dataSnapshot.exists()) {
                    requestListener.onRequestSendFailed(requestModel, Error.ErrorType.ERROR_NOT_DEFINED);
                } else {
                    updateRequestToMySentDb(requestModel);
                }
                myRefInFriendReceivedDb.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                RequestStatusModel requestStatusModel =
                        Request.Status.getRequestStatusModel(Request.REQUEST_SEND_FAILED);

                requestModel.setRequestStatusModel(requestStatusModel);
                updateRequestToMySentDb(requestModel);
                requestListener.onRequestSendFailed(requestModel, Error.ErrorType.ERROR_NOT_DEFINED);

                myRefInFriendReceivedDb.removeEventListener(this);
            }
        });

        RequestStatusModel requestStatusModel =
                Request.Status.getRequestStatusModel(Request.REQUEST_SENT);
        requestModel.setRequestStatusModel(requestStatusModel);
        myRefInFriendReceivedDb.setValue(requestModel);
    }

    private void updateRequestToMySentDb(final RequestModel requestModel) {
        final DatabaseReference frndRefInMySentDb = gDbRefBaseRequestsSend.child(uid).child(requestModel.getToId());

        frndRefInMySentDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "updateRequestToMySentDb:onDataChange: " + dataSnapshot);
                if (!dataSnapshot.exists()) {
                    requestListener.onRequestSendFailed(requestModel, Error.ErrorType.ERROR_NOT_DEFINED);
                } else
                    requestListener.onRequestSendSuccess(requestModel);
                frndRefInMySentDb.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                requestListener.onRequestSendFailed(requestModel, databaseError.getMessage());
                // todo : request to friend is success while in own db is a failure
                frndRefInMySentDb.removeEventListener(this);
            }
        });
        frndRefInMySentDb.setValue(requestModel);
    }

    private RequestModel createRequestModel(String friendUserId, ProfileModel profileModel) {
        RequestModel requestModel = new RequestModel();
        requestModel.setFromId(profileModel.getAuthorizationId());
        requestModel.setToId(friendUserId);
        requestModel.setHeader(false);
        requestModel.setPriority(100);
        requestModel.setMessage("Accept my request.");
        requestModel.setRequestedAtMillis(new Date().getTime());

        requestModel.setRequesteeName(profileModel.getAlias());
        requestModel.setRequesteePhone(profileModel.getPhones().get(0));
        requestModel.setRequesteeProfilePicUrl(profileModel.getProfilePhotoUrl());

        RequestStatusModel requestStatusModel =
                Request.Status.getRequestStatusModel(Request.REQUEST_CREATED);

        requestModel.setRequestStatusModel(requestStatusModel);
        requestListener.onRequestStatusUpdated(requestModel);

        return requestModel;
    }

    public UserModel getFriendModel(DataSnapshot dataSnapshot) {
        UserModel userModel = null;
        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
            userModel = dataSnapshot1.getValue(UserModel.class);
        }
        return userModel;
    }

    public void fetchAllSentRequest(){
        gDbRefBaseRequestsSend.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange: fetchAllSentRequest" + dataSnapshot);
                ArrayList<RequestModel> requestModels = new ArrayList<>();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    RequestModel requestModel = dataSnapshot1.getValue(RequestModel.class);
                    requestModels.add(requestModel);
                }
                requestListener.onAllSentRequestsFetched(requestModels);

                gDbRefBaseRequestsSend.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void removeChildListeners() {
        gDbRefBaseRequestsRcvd.child(uid).removeEventListener(childEventListener_receiveRequests);
        gDbRefBaseRequestsSend.child(uid).removeEventListener(childEventListener_sendRequests);
    }
}
