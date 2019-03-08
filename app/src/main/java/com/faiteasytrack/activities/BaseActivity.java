package com.faiteasytrack.activities;

import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.text.InputType;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.faiteasytrack.R;
import com.faiteasytrack.broadcasts.NetworkStateReceiver;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.Constants;
import com.faiteasytrack.utils.Utils;
import com.google.android.material.snackbar.Snackbar;

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

        if (AppPermissions.checkNetworkStateReceivePermission(this))
            registerNetworkStateBroadcast();
    }

    public abstract void setUpActionBar();

    public abstract void initUI();

    public abstract void setUpListeners();

    public abstract void setUpData();

    public abstract void setUpRecycler();

    public abstract void updateInternetError(boolean isOnline);

    private void registerNetworkStateBroadcast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkStateReceiver stateReceiver = new NetworkStateReceiver();
            registerReceiver(stateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        NetworkStateReceiver.bindListener(new NetworkStateReceiver.NetworkStateReceivedListener() {
            @Override
            public void onStateReceived(NetworkInfo.State state) {

            }

            @Override
            public void onStateChanged(boolean isOnlineNow) {
                updateInternetError(isOnlineNow);
            }
        });
    }
}
