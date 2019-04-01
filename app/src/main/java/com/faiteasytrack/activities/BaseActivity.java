package com.faiteasytrack.activities;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        initUI();
        setUpActionBar();
        setUpListeners();
        setUpRecycler();
        setUpData();
    }

    public abstract void setUpActionBar();

    public abstract void initUI();

    public abstract void setUpListeners();

    public abstract void setUpData();

    public abstract void setUpRecycler();

    public abstract void updateInternetStatus(boolean online);
}
