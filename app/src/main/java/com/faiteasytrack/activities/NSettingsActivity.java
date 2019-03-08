package com.faiteasytrack.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import com.faiteasytrack.R;
import com.faiteasytrack.enums.Preferences;
import com.faiteasytrack.utils.ViewUtils;
import com.faiteasytrack.views.EasytrackButton;
import com.google.android.material.button.MaterialButton;

import androidx.appcompat.widget.Toolbar;

public class NSettingsActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NSettingsActivity";

    private MaterialButton btnSaveLocationPref, btnRegisterAsVendor;

    private int gLocationSharePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    public void setUpActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void initUI() {
        btnRegisterAsVendor = findViewById(R.id.btn_register_as_vendor);
        btnSaveLocationPref = findViewById(R.id.btn_save_location_pref);
    }

    @Override
    public void setUpListeners() {
        btnRegisterAsVendor.setOnClickListener(this);
        btnSaveLocationPref.setOnClickListener(this);

        ((RadioGroup) findViewById(R.id.rg_location_pref))
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_pref_friends_only:
                        gLocationSharePref = Preferences.ShareLocation.TO_FRIENDS;
                        break;
                    case R.id.rb_pref_noone:
                        gLocationSharePref = Preferences.ShareLocation.TO_NO_ONE;
                        break;
                        default:
                            gLocationSharePref = Preferences.ShareLocation.TO_ANYONE;
                            break;
                }
            }
        });
    }

    @Override
    public void setUpData() {
        gLocationSharePref = Preferences.ShareLocation.TO_ANYONE;
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
        switch (v.getId()){
            case R.id.btn_save_location_pref:{
                ViewUtils.makeToast(this, "Selection is : " + gLocationSharePref);
            }
            break;
            case R.id.btn_register_as_vendor:{
                ViewUtils.makeToast(this, "Change my password");
            }
            break;
        }
    }

    @Override
    public void updateInternetError(boolean isOnline) {

    }
}
