package com.faiteasytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.enums.User;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.utils.SharePreferences;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class NDashboardActivity extends BaseActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, View.OnClickListener {

    public static final String TAG = "NDashboardActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private GoogleMap googleMap;

    private Toolbar toolbar;
    private ImageView ivNavigation, ivNotifications;

    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        setContentView(R.layout.activity_dashboard);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_navigation: {
                updateUserDetailsInNavView();
                drawerLayout.openDrawer(GravityCompat.START);
            }
            break;
            case R.id.iv_notifications: {
                startActivity(new Intent(this, NRequestsActivity.class));
            }
            break;
            case R.id.navigation_header: {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, NUserProfileActivity.class));
            }
            break;
        }
    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void initUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        ivNavigation = findViewById(R.id.iv_navigation);
        ivNotifications = findViewById(R.id.iv_notifications);
        navigationView = findViewById(R.id.nav_view);
    }

    @Override
    public void setUpListeners() {
        drawerLayout.addDrawerListener(drawerListener);
        toolbar.setOnClickListener(this);
        ivNavigation.setOnClickListener(this);
        ivNotifications.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getHeaderView(0).setOnClickListener(this);
    }

    @Override
    public void setUpData() {
        setUpNavigationMenu();
    }

    private void setUpNavigationMenu() {
        UserModel userModel = SharePreferences.getUserModel(this);
        if (userModel == null)
            return;
        switch (userModel.getI_am()){
            case User.TYPE_ADMIN:
                navigationView.inflateMenu(R.menu.menu_nav_admin);
                break;
            case User.TYPE_VENDOR:
                navigationView.inflateMenu(R.menu.menu_nav_vendor);
                break;
            case User.TYPE_DRIVER:
                navigationView.inflateMenu(R.menu.menu_nav_driver);
                break;
            default:
                navigationView.inflateMenu(R.menu.menu_nav_parent);
                break;
        }

        updateUserDetailsInNavView();
    }

    private void updateUserDetailsInNavView() {
        resetNavigationSelection();
        View navHeader = navigationView.getHeaderView(0);
//        ImageView userProfilePic = navHeader.findViewById(R.id.nav_header_icon);
        TextView userName = navHeader.findViewById(R.id.nav_header_title);
        TextView userContactInfo = navHeader.findViewById(R.id.nav_header_subtitle);
        try {
            userName.setText(firebaseUser.getDisplayName());
            String phoneNumber = firebaseUser.getPhoneNumber();
            String email = firebaseUser.getEmail();

            /*if (phoneNumber != null && email != null)
                userContactInfo.setText(String.format("%s\n%s", phoneNumber, email));
            else */
            if (phoneNumber != null)
                userContactInfo.setText(phoneNumber);
            else if (email != null)
                userContactInfo.setText(email);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.nav_dashboard: {
//                startActivity(new Intent(this, NDashboardActivity.class));
            }
            return true;
//            case R.id.nav_map: {
//                startActivity(new Intent(this, NMapActivity.class));
//            }
//            return true;
            case R.id.nav_admins: {
                startActivity(new Intent(this, NAdminActivity.class));
            }
            return true;
            case R.id.nav_vendors: {
                startActivity(new Intent(this, NVendorActivity.class));
            }
            return true;
            case R.id.nav_vehicles: {
                startActivity(new Intent(this, NVehicleActivity.class));
            }
            return true;
            case R.id.nav_drivers: {
                startActivity(new Intent(this, NDriverActivity.class));
            }
            return true;
            case R.id.nav_routes: {
                startActivity(new Intent(this, NRouteActivity.class));
            }
            return true;
//            case R.id.nav_history: {
//                startActivity(new Intent(this, NHistoryActivity.class));
//            }
//            return true;
//            case R.id.nav_requests: {
//                startActivity(new Intent(this, NRequestsActivity.class));
//            }
//            return true;
//            case R.id.nav_friends: {
//                startActivity(new Intent(this, NFriendsActivity.class));
//            }
//            return true;
//            case R.id.nav_contacts: {
//                startActivity(new Intent(this, NContactsActivity.class));
//            }
//            return true;
            case R.id.nav_settings: {
                startActivity(new Intent(this, NSettingsActivity.class));
            }
            return true;
            case R.id.nav_logout: {
                FirebaseAuth.getInstance().signOut();
                SharePreferences.userLoggedOut(this);
                startActivity(new Intent(this, NPhoneAuthActivity.class));
                finish();
            }
            return true;
            default:
                return false;
        }
    }

    public void resetNavigationSelection() {
        if (navigationView == null)
            return;
        if (navigationView.getCheckedItem() != null)
            navigationView.getCheckedItem().setChecked(false);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void updateInternetError(boolean isOnline) {

    }

    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {
            if (newState == DrawerLayout.STATE_IDLE)
                updateUserDetailsInNavView();
        }
    };
}
