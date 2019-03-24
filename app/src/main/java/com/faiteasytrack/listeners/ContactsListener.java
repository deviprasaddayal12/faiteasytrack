package com.faiteasytrack.listeners;

import com.faiteasytrack.constants.Error;
import com.faiteasytrack.constants.Request;
import com.faiteasytrack.models.ContactModel;

import java.util.ArrayList;

public interface ContactsListener {

    int MAPPING_FOR_UNIQUE_KEY_NOT_FOUND = 0;
    int MODELS_FOR_UNIQUE_KEY_NOT_FOUND = 1;

    interface OnContactsLoadListener {
        void onLoadingStarted();

        void onFirstPageLoaded(ArrayList<ContactModel> firstPageOfContacts);

        void onNextPageLoaded(ArrayList<ContactModel> nextPageOfContacts);

        void onLoadingFinished();
    }

    interface OnContactsGetListener{
        void onContactsRetrieved(ArrayList<ContactModel> savedContactModels);

        void onContactsNotFound(Error.ErrorType errorType);
    }

    interface OnContactsSearchListener {
        void onSearchSuccess(ArrayList<ContactModel> listOfSearchedContacts);

        void onSearchFailure(Error.ErrorType errorType);

        void onSearchReset(ArrayList<ContactModel> listOfAllContacts);
    }

    interface OnContactsSyncListener {
        void onFoundOnServer(ContactModel contactModel, boolean userExists);

        void onNotFoundOnServer(Error.ErrorType errorType);
    }

    interface OnSyncWithMeListener{
        void onSyncedWithMe(ContactModel contactModel, Request.Status status);

        void onSyncingFailed(Error.ErrorType errorType);
    }
}
