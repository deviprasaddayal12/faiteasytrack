package com.faiteasytrack.helpers;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

import com.faiteasytrack.constants.Error;
import com.faiteasytrack.constants.Request;
import com.faiteasytrack.listeners.ContactsListener;
import com.faiteasytrack.listeners.UserListener;
import com.faiteasytrack.models.ContactModel;
import com.faiteasytrack.models.RequestStatusModel;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactsHelper implements ContactsListener {

    public static final String TAG = "ContactsHelper";

    private static ContactsHelper contactsHelper;
    private Context context;

    private ArrayList<OnContactsLoadListener> onContactsLoadListeners;
    private ArrayList<OnContactsSyncListener> onContactsSyncListeners;

    private HashMap<String, ArrayList<ContactModel>> uniqueContactMapping = new HashMap<>();

    private ArrayList<ContactModel> deviceContactModels = new ArrayList<>();
    private ArrayList<ContactModel> appContactModels = new ArrayList<>();

    private Handler handler = new Handler();

    public static synchronized ContactsHelper getInstance(Context context) {
        if (contactsHelper == null)
            contactsHelper = new ContactsHelper(context);

        return contactsHelper;
    }

    private ContactsHelper(Context context) {
        this.context = context.getApplicationContext();

        onContactsLoadListeners = new ArrayList<>();
        onContactsSyncListeners = new ArrayList<>();
    }

    public void addOnContactsLoadListener(OnContactsLoadListener onContactsLoadListener) {
        this.onContactsLoadListeners.add(onContactsLoadListener);
    }

    public void setOnContactsSyncListener(OnContactsSyncListener onContactsSyncListener) {
        this.onContactsSyncListeners.add(onContactsSyncListener);
    }

    public void getAllDeviceContactsAsync() {
        new DeviceContactsAsync().execute();
    }

    private class DeviceContactsAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getDeviceContacts();
            return null;
        }
    }

    private void getDeviceContacts() {
        Cursor contactsCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

        if (contactsCursor != null) {
            int totalContactsCount = contactsCursor.getCount(), progressCount = 0;

            if (totalContactsCount != 0) {
                for (OnContactsLoadListener onContactsLoadListener : onContactsLoadListeners)
                    onContactsLoadListener.onLoadingStarted();
                while (contactsCursor.moveToNext()) {

                    ArrayList<ContactModel> duplicateModels;
                    ArrayList<String> phoneList = new ArrayList<>();

                    String id = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));

                    String phoneNumber, lastPhoneAdded = "";

                    if (Integer.parseInt(contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor phoneCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        if (phoneCursor != null) {
                            while (phoneCursor.moveToNext()) {
                                phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                phoneNumber = phoneNumber.replace(" ", "");
                                phoneNumber = phoneNumber.replace("-", "");

                                if (phoneNumber.length() < 10 || phoneNumber.equals(lastPhoneAdded))
                                    continue;

                                lastPhoneAdded = phoneNumber;
                                int type = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                                if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                                    if (phoneList.size() != 0)
                                        phoneList.set(0, phoneNumber);
                                    else
                                        phoneList.add(phoneNumber);
                                }
                            }
                            phoneCursor.close();
                        }
                    }
                    if (phoneList.size() == 0)
                        continue;

                    ContactModel contactModel = new ContactModel();
                    contactModel.setHeader(false);
                    contactModel.setContactId(id);
                    contactModel.setContactName(name);
                    contactModel.setPhones(phoneList);
                    contactModel.setRequestStatusModel(getDefaultRequestModels());

                    if (uniqueContactMapping.containsKey(id)) {
                        duplicateModels = uniqueContactMapping.get(id);
                        if (duplicateModels != null) {
                            duplicateModels.add(contactModel);
                            uniqueContactMapping.put(id, duplicateModels);
                        }
                    } else {
                        duplicateModels = new ArrayList<>();
                        duplicateModels.add(contactModel);
                        deviceContactModels.add(contactModel);
                    }

                    ++progressCount;
                    if (progressCount % 20 == 0) {
                        if (progressCount / 20 == 1) {
                            for (OnContactsLoadListener onContactsLoadListener : onContactsLoadListeners)
                                onContactsLoadListener.onFirstPageLoaded(deviceContactModels);
                        } else {
                            for (OnContactsLoadListener onContactsLoadListener : onContactsLoadListeners)
                                onContactsLoadListener.onNextPageLoaded(new ArrayList<ContactModel>(deviceContactModels.subList(progressCount - 20, progressCount - 1)));
                        }
                    }
                }
                for (OnContactsLoadListener onContactsLoadListener : onContactsLoadListeners)
                    onContactsLoadListener.onLoadingFinished();
            }

            contactsCursor.close();
        }
    }

    private RequestStatusModel getDefaultRequestModels() {

        return Request.Status.getRequestStatusModel(-1);
    }
}
