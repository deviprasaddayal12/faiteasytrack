package com.faiteasytrack.managers;

import android.content.Context;
import android.os.Handler;

import com.faiteasytrack.enums.Error;
import com.faiteasytrack.helpers.ContactsHelper;
import com.faiteasytrack.listeners.ContactsListener;
import com.faiteasytrack.models.ContactModel;

import java.util.ArrayList;

public class ContactsManager implements ContactsListener.OnContactsLoadListener,
        ContactsListener.OnContactsSearchListener, ContactsListener.OnContactsSyncListener {

    public interface OnContactsListener {
        void onLoadingStarted();

        void onFirstPageLoaded(ArrayList<ContactModel> firstPageOfContacts);

        void onNextPageLoaded(ArrayList<ContactModel> nextPageOfContacts);

        void onContactSynced(ContactModel contactModel);

        void onSearchContactsFound(ArrayList<ContactModel> listOfSearchedContacts);

        void onSearchReset(ArrayList<ContactModel> listOfAllContacts);
    }

    private int ITEMS_PER_PAGE = 20;

    private static ContactsManager contactsManager;

    private Context context;
    private OnContactsListener onContactsListener;
    private ContactsHelper contactsHelper;
    private ArrayList<ContactModel> appContactModels, contactModels;

    private Handler handler;

    public static synchronized ContactsManager getInstance(Context context){
        if (contactsManager == null) {
            contactsManager = new ContactsManager(context);
        }

        return contactsManager;
    }

    public ContactsManager(Context context) {
        this.context = context.getApplicationContext();
        this.onContactsListener = (OnContactsListener) context;

        init();
        addListeners();
    }

    private void init() {
        handler = new Handler();
        appContactModels = new ArrayList<>();
        contactModels = new ArrayList<>();
        contactsHelper = ContactsHelper.getInstance(context);
    }

    private void addListeners() {
        contactsHelper.addOnContactsLoadListener(this);
//        contactsHelper.setOnContactsSearchListener(this);
        contactsHelper.setOnContactsSyncListener(this);
    }

    public void onContactSynced(ContactModel contactModel){}

    public void startLoading() {
        contactsHelper.getAllDeviceContactsAsync();
    }

    public void searchForContacts(String searchKey) {
        ArrayList<ContactModel> searchedModels = new ArrayList<>();

        for (ContactModel contactModel : contactModels) {
            if (contactModel.isHeader())
                continue;
            String name = contactModel.getContactName().toLowerCase();
            String number = contactModel.getPhones().get(0);
            if (name.contains(searchKey.toLowerCase()) || number.contains(searchKey.toLowerCase()))
                searchedModels.add(contactModel);
        }

        if (onContactsListener != null)
            onContactsListener.onSearchContactsFound(searchedModels);
//        if (searchedModels.size() != 0) {
//            onContactsSearchListener.onSearchSuccess(searchedModels);
//        } else {
//            onContactsSearchListener.onSearchFailure(ContactsListener.MODELS_FOR_UNIQUE_KEY_NOT_FOUND, "No contacts found for the key.");
//        }
    }

    public void resetSearch() {
        if (onContactsListener != null)
            onContactsListener.onSearchReset(contactModels);
//        onContactsSearchListener.onSearchReset(deviceContactModels);
    }

    @Override
    public void onLoadingStarted() {
        // Called from async thread
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (onContactsListener != null)
                    onContactsListener.onLoadingStarted();
            }
        });
    }

    @Override
    public void onFirstPageLoaded(final ArrayList<ContactModel> firstPageOfContacts) {
        // Called from async thread
        handler.post(new Runnable() {
            @Override
            public void run() {
                contactModels.addAll(firstPageOfContacts);
                if (onContactsListener != null)
                    onContactsListener.onFirstPageLoaded(firstPageOfContacts);
            }
        });
    }

    @Override
    public void onNextPageLoaded(final ArrayList<ContactModel> nextPageOfContacts) {
        // Called from async thread
        handler.post(new Runnable() {
            @Override
            public void run() {
                contactModels.addAll(nextPageOfContacts);
                if (onContactsListener != null)
                    onContactsListener.onNextPageLoaded(nextPageOfContacts);
            }
        });
    }

    @Override
    public void onLoadingFinished() {
        // Called from async thread
        handler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onSearchSuccess(ArrayList<ContactModel> listOfSearchedContacts) {
        if (onContactsListener != null)
            onContactsListener.onSearchContactsFound(listOfSearchedContacts);
    }

    @Override
    public void onSearchFailure(Error.ErrorStatus errorStatus) {

    }

    @Override
    public void onSearchReset(ArrayList<ContactModel> listOfAllContacts) {
        if (onContactsListener != null)
            onContactsListener.onSearchReset(listOfAllContacts);
    }

    @Override
    public void onFoundOnServer(ContactModel contactModel, boolean userExists) {
        if (onContactsListener != null)
            onContactsListener.onContactSynced(contactModel);

        onContactSynced(contactModel);
    }

    @Override
    public void onNotFoundOnServer(Error.ErrorStatus errorStatus) {

    }
}
