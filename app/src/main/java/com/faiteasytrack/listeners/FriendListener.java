package com.faiteasytrack.listeners;

import com.faiteasytrack.enums.Error;
import com.faiteasytrack.models.FriendModel;

import java.util.ArrayList;

public interface FriendListener {

    void onAllFriendsFetched(ArrayList<FriendModel> friendModels);

    void onNewFriendAdded(FriendModel friendModel, ArrayList<FriendModel> friendModels);

    void onFriendAddFailed(Error.ErrorType errorType);
}
