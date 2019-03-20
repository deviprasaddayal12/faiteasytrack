package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.faiteasytrack.R;
import com.faiteasytrack.enums.Preferences;
import com.faiteasytrack.models.PreferenceModel;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.button.MaterialButton;

import androidx.appcompat.widget.Toolbar;

public class NSettingsActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NSettingsActivity";

    private MaterialButton btnChangePassword, btnShareLocationTo;
    private Switch swtShareLocation;
    private RadioGroup rgShareLocationTo;
    private ProgressDialog progressDialog;

    private PreferenceModel lastPreferenceModel, changedPreferenceModel;
    private int shareLocationTo;
    private boolean doShareLocationChanged = false, doShareLocationToChanged = false, isChangedPreferencesSaved = false;

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

        btnShareLocationTo = findViewById(R.id.btn_share_location_to);
        btnChangePassword = findViewById(R.id.btn_change_password);
        swtShareLocation = findViewById(R.id.swt_share_location);
        rgShareLocationTo = findViewById(R.id.rg_location_pref);
    }

    @Override
    public void setUpListeners() {
        btnShareLocationTo.setOnClickListener(this);
        btnChangePassword.setOnClickListener(this);

        swtShareLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.getId() == R.id.swt_share_location) {
                    onShareLocationPreferenceChanged(b);
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
                    }
                });
    }

    @Override
    public void setUpData() {
        changedPreferenceModel = new PreferenceModel();
        shareLocationTo = lastPreferenceModel.getShareLocationTo();

        swtShareLocation.setChecked(lastPreferenceModel.isDoShareLocation());
        switch (shareLocationTo){
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
            case R.id.btn_share_location_to: {
                onShareLocationToPreferenceChanged();
            }
            break;
            case R.id.btn_change_password: {
                onChangePasswordRequested();
            }
            break;
        }
    }

    private void onShareLocationPreferenceChanged(boolean b) {
        if (lastPreferenceModel.isDoShareLocation() == b) {
            doShareLocationChanged = false;
        } else {
            doShareLocationChanged = true;
            changedPreferenceModel.setDoShareLocation(b);
            ViewUtils.makeToast(this, "Preferences updated.");
        }
    }

    private void onShareLocationToPreferenceChanged() {
        if (lastPreferenceModel.getShareLocationTo() == shareLocationTo) {
            doShareLocationToChanged = false;
        } else {
            doShareLocationToChanged = true;
            changedPreferenceModel.setShareLocationTo(shareLocationTo);
            ViewUtils.makeToast(this, "Preferences updated.");
        }
    }

    private void onChangePasswordRequested() {
        // todo : implement change password request
    }

    @Override
    public void onBackPressed() {
        if ((doShareLocationChanged || doShareLocationToChanged) && !isChangedPreferencesSaved) {
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
        SharePreferences.savePreferenceModel(this, changedPreferenceModel);
        // todo : upload preferences to firebase database

        isChangedPreferencesSaved = true;
        listener.onPreferencesSaveSuccess();
    }

    @Override
    public void updateInternetError(boolean isOnline) {

    }

    private interface OnPreferencesSaveListener {
        void onPreferencesSaveSuccess();
    }
}
