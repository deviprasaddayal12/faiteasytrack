package com.faiteasytrack.activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.adapters.ContactsAdapter;
import com.faiteasytrack.managers.ContactsManager;
import com.faiteasytrack.models.ContactModel;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class ContactsActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        ContactsManager.OnContactsListener {

    public static final String TAG = "NContactsActivity";

    private View loader;
    private EditText etSearchContact;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerContacts;
    private ContactsAdapter adapterContacts;
    private ArrayList<ContactModel> contactModels;

    private boolean isSearchActive = false;
    private ContactsManager contactsManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void initUI() {
        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        loader = viewStubLoader.inflate();
        loader.setOnClickListener(this);

        ViewUtils.hideViews(loader);

        etSearchContact = findViewById(R.id.et_search_contact);
        swipeRefreshLayout = ((SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout));
    }

    @Override
    public void setUpListeners() {
        etSearchContact.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                searchContact(etSearchContact.getText().toString());
                return false;
            }
        });

        etSearchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ViewUtils.showViews(loader);
                if (s.length() == 0)
                    resetSearch();
                else
                    searchContact(s.toString());
            }
        });

        findViewById(R.id.iv_back).setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {
        contactModels = new ArrayList<>();
        ArrayList<ContactModel> savedContacts = SharePreferences.getAppContacts(this);
        if (savedContacts != null && savedContacts.size() > 0)
            contactModels.addAll(savedContacts);

        recyclerContacts = findViewById(R.id.recycler_contacts);
        adapterContacts = new ContactsAdapter(this, contactModels, new ContactsAdapter.OnContactSelectedListener() {
            @Override
            public void onContactSelected(int position, ContactModel contactModel) {
                if (contactModel.isHeader())
                    return;

                String phone = contactModel.getPhones().get(0);
                if (phone.length() > 10)
                    phone = phone.substring(phone.length() - 10);
                ViewUtils.makeToast(ContactsActivity.this, "Requesting...");
            }
        });
        recyclerContacts.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayout.VERTICAL);
        recyclerContacts.addItemDecoration(dividerItemDecoration);
        recyclerContacts.setAdapter(adapterContacts);

        contactsManager = new  ContactsManager(this );
        contactsManager.startLoading();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back)
            onBackPressed();
    }

    @Override
    public void onBackPressed() {
        etSearchContact.setText("");
        etSearchContact.clearFocus();

        Utils.hideSoftKeyboard(this);

        if (isSearchActive)
            resetSearch();
        else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    private void searchContact(String s) {
        if (!isSearchActive)
            isSearchActive = true;
        contactsManager.searchForContacts(s);
    }

    private void resetSearch() {
        if (isSearchActive)
            isSearchActive = false;
        contactsManager.resetSearch();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == AppPermissions.ACCESS_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                contactsManager.startLoading();
            }
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        contactModels.clear();
        contactsManager.startLoading();
    }

    @Override
    public void updateInternetStatus(boolean online) {

    }

    @Override
    public void onLoadingStarted() {

    }

    @Override
    public void onFirstPageLoaded(ArrayList<ContactModel> firstPageOfContacts) {
        swipeRefreshLayout.setRefreshing(false);
        contactModels.addAll(firstPageOfContacts);
        adapterContacts.notifyDataSetChanged();
    }

    @Override
    public void onNextPageLoaded(ArrayList<ContactModel> nextPageOfContacts) {
        contactModels.addAll(nextPageOfContacts);
        adapterContacts.notifyDataSetChanged();
    }

    @Override
    public void onContactSynced(ContactModel contactModel) {

    }

    @Override
    public void onSearchContactsFound(ArrayList<ContactModel> listOfSearchedContacts) {
        ViewUtils.hideViews(loader);
        contactModels.clear();
        contactModels.addAll(listOfSearchedContacts);
        adapterContacts.notifyDataSetChanged();
    }

    @Override
    public void onSearchReset(ArrayList<ContactModel> listOfAllContacts) {
        ViewUtils.hideViews(loader);
        contactModels.clear();
        contactModels.addAll(listOfAllContacts);
        adapterContacts.notifyDataSetChanged();
    }
}
