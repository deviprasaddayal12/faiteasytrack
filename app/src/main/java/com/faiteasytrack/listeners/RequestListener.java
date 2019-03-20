package com.faiteasytrack.listeners;

import com.faiteasytrack.enums.Error;
import com.faiteasytrack.models.RequestModel;

import java.util.ArrayList;

public interface RequestListener {

    void onNewRequestReceived(RequestModel requestModel);

    void onRequestSendSuccess(RequestModel requestModel);

    void onRequestSendFailed(RequestModel requestModel, Error.ErrorType errorType);

    void onRequestSending(RequestModel requestModel);

    void onRequestStatusUpdated(RequestModel requestModel);

    void onCreteRequestFailed(Error.ErrorType errorType);

    void onNewRequestsCounted(int count);

    void onRequestsFetchFailed(Error.ErrorType errorType);

    void onAllReceivedRequestsFetched(ArrayList<RequestModel> requestModels);

    void onAllSentRequestsFetched(ArrayList<RequestModel> requestModels);

    void onStatusUpdateFailed(Error.ErrorType errorType);
}
