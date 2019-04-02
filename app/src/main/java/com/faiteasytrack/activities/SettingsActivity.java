package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import com.faiteasytrack.R;
import com.faiteasytrack.constants.Preferences;
import com.faiteasytrack.models.PreferenceModel;
import com.faiteasytrack.utils.SharePreferences;
import com.google.android.material.button.MaterialButton;

import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NSettingsActivity";

    private MaterialButton btnChangePassword;
    private Switch swtShareLocation, swtShareProfilePhoto;
    private RadioGroup rgShareLocationTo;

    private Spinner spinStorageProfilePhoto;
    private ArrayAdapter<String> adapterStorageProfilePhoto;
    private final String[] storages = {"Local", "Cloud"};

    private ProgressDialog progressDialog;

    private PreferenceModel lastPreferenceModel, changedPreferenceModel;

    private int shareLocationTo, storageProfilePhoto;
    private boolean isShareLocationChanged = false,
            isSharePhotoChanged = false,
            isPhotoStorageChanged = false,
            isShareLocationToChanged = false,
            isChangedPreferencesSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lastPreferenceModel = SharePreferences.getPreferenceModel(this);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public void setUpActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void initUI() {
        progressDialog = new ProgressDialog(this);

        swtShareLocation = findViewById(R.id.swt_share_location);
        rgShareLocationTo = findViewById(R.id.rg_location_pref);

        swtShareProfilePhoto = findViewById(R.id.swt_share_photo);
        spinStorageProfilePhoto = findViewById(R.id.spinner_storage_profilePhoto);

        btnChangePassword = findViewById(R.id.btn_change_password);
    }

    @Override
    public void setUpListeners() {
        btnChangePassword.setOnClickListener(this);

        swtShareLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.getId() == R.id.swt_share_location) {
                    onShareLocationPreferenceChanged(b);
                }
            }
        });
        swtShareProfilePhoto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.getId() == R.id.swt_share_photo) {
                    onSharePhotoPreferenceChanged(b);
                }
            }
        });

        rgShareLocationTo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_pref_friends_only:
                        shareLocationTo = Preferences.ShareLocation.TO_FRIENDS;
                        break;
                    case R.id.rb_pref_noone:
                        shareLocationTo = Preferences.ShareLocation.TO_NO_ONE;
                        break;
                    case R.id.rb_pref_anyone:
                        shareLocationTo = Preferences.ShareLocation.TO_ANYONE;
                        break;
                }
                onShareLocationToPreferenceChanged();
            }
        });
    }

    @Override
    public void setUpData() {
        changedPreferenceModel = new PreferenceModel();

        shareLocationTo = lastPreferenceModel.getShareLocationTo();
        swtShareLocation.setChecked(lastPreferenceModel.isDoShareLocation());

        switch (shareLocationTo) {
            case Preferences.ShareLocation.TO_FRIENDS:
                rgShareLocationTo.check(R.id.rb_pref_friends_only);
                break;
            case Preferences.ShareLocation.TO_NO_ONE:
                rgShareLocationTo.check(R.id.rb_pref_noone);
                break;
            case Preferences.ShareLocation.TO_ANYONE:
                rgShareLocationTo.check(R.id.rb_pref_anyone);
                break;
        }
    }

    @Override
    public void setUpRecycler() {
        adapterStorageProfilePhoto = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, storages);
        spinStorageProfilePhoto.setAdapter(adapterStorageProfilePhoto);

        spinStorageProfilePhoto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                storageProfilePhoto = i + 1;
                onPhotoStoragePreferenceChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        swtShareProfilePhoto.setChecked(lastPreferenceModel.isDoShareProfilePicture());
        storageProfilePhoto = lastPreferenceModel.getStorageForProfilePhoto();
        spinStorageProfilePhoto.setSelection(storageProfilePhoto - 1, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change_password: {
                onChangePasswordRequested();
            }
            break;
        }
    }

    private void onShareLocationPreferenceChanged(boolean b) {
        isShareLocationChanged = lastPreferenceModel.isDoShareLocation() != b;
    }

    private void onShareLocationToPreferenceChanged() {
        isShareLocationToChanged = lastPreferenceModel.getShareLocationTo() != shareLocationTo;
    }

    private void onSharePhotoPreferenceChanged(boolean b) {
        isSharePhotoChanged = lastPreferenceModel.isDoShareProfilePicture() != b;
    }

    private void onPhotoStoragePreferenceChanged() {
        isPhotoStorageChanged = lastPreferenceModel.getStorageForProfilePhoto() != storageProfilePhoto;
    }

    private void onChangePasswordRequested() {
        // todo : implement change password request
    }

    private boolean isProfileUpdated(){
        return isShareLocationChanged || isShareLocationToChanged
                || isSharePhotoChanged || isPhotoStorageChanged;
    }

    @Override
    public void onBackPressed() {
        if (isProfileUpdated() && !isChangedPreferencesSaved) {
            progressDialog.setMessage("Saving your preferences...");
            progressDialog.show();

            saveChangedPreferences(new OnPreferencesSaveListener() {
                @Override
                public void onPreferencesSaveSuccess() {
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    onBackPressed();
                }
            });
        } else
            super.onBackPressed();
    }

    private void saveChangedPreferences(OnPreferencesSaveListener listener) {
        changedPreferenceModel.setDoShareProfilePicture(swtShareProfilePhoto.isChecked());
        changedPreferenceModel.setStorageForProfilePhoto(storageProfilePhoto);
        changedPreferenceModel.setDoShareLocation(swtShareLocation.isChecked());
        changedPreferenceModel.setShareLocationTo(shareLocationTo);

        SharePreferences.savePreferenceModel(this, changedPreferenceModel);
        // todo : upload preferences to firebase database

        isChangedPreferencesSaved = true;
        listener.onPreferencesSaveSuccess();
    }

    @Override
    public void updateInternetStatus(boolean online) {

    }

    private interface OnPreferencesSaveListener {
        void onPreferencesSaveSuccess();
    }
}
