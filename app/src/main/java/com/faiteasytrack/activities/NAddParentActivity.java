package com.faiteasytrack.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.faiteasytrack.R;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class NAddParentActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NAddParentActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parent);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void setUpActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void initUI() {

    }

    @Override
    public void setUpListeners() {

    }

    @Override
    public void setUpData() {

    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void updateInternetError(boolean isOnline) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
