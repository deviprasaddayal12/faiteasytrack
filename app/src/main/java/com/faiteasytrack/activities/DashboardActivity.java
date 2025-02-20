package com.faiteasytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.faiteasytrack.R;
import com.faiteasytrack.adapters.DashboardAdapter;
import com.faiteasytrack.constants.Preferences;
import com.faiteasytrack.constants.User;
import com.faiteasytrack.listeners.OnStatisticsFetchListener;
import com.faiteasytrack.managers.DashboardManager;
import com.faiteasytrack.models.DashboardModel;
import com.faiteasytrack.models.PreferenceModel;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.SharePreferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {

    public static final String TAG = DashboardActivity.class.getCanonicalName();
    public static final int REQUEST_FOR_TRACKING = 1;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton fabLetsTrack;

    private RecyclerView recyclerDashboard;
    private DashboardAdapter adapterDashboard;
    private ArrayList<DashboardModel> dashboardModels;

    private FirebaseUser firebaseUser;
    private StorageReference profilePhotosReference;
    private Handler handlerDashboardViewUpdates;

    private UserModel userModel;
    private PreferenceModel preferenceModel;

    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.SimpleDrawerListener() {
        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);

            drawerClosedRunnable = null;
            if (!isNavViewInstantiated)
                updateUserDetailsInNavView(false);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);

            if (drawerClosedRunnable != null)
                handlerDashboardViewUpdates.postDelayed(drawerClosedRunnable, NAV_DRAWER_CLOSE_WAIT_TIME);
        }
    };

    private Runnable drawerClosedRunnable;

    private Snackbar snackbarIndefinite;
    private boolean isSnackbarIndefiniteBusy = false;
    private Runnable runnableSnackbarIndefinite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profilePhotosReference = FirebaseStorage.getInstance()
                .getReference("images/profilePhotos").child(firebaseUser.getUid());
        userModel = SharePreferences.getUserModel(this);
        preferenceModel = SharePreferences.getPreferenceModel(this);
        handlerDashboardViewUpdates = new Handler();

        setContentView(R.layout.activity_dashboard);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.navigation_header: {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivityForResult(new Intent(this, UserProfileActivity.class),
                        AppPermissions.REQUEST_FOR_UPDATE_USER_DETAILS);
            }
            break;
            case R.id.fab_lets_track: {
                startActivityForResult(new Intent(this, MapActivity.class), REQUEST_FOR_TRACKING);
            }
            break;
        }
    }

    @Override
    public void setUpActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void initUI() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        fabLetsTrack = findViewById(R.id.fab_lets_track);
    }

    @Override
    public void setUpListeners() {
        drawerLayout.addDrawerListener(drawerListener);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getHeaderView(0).setOnClickListener(this);
        fabLetsTrack.setOnClickListener(this);
    }

    @Override
    public void setUpData() {
        setUpNavigationMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!isNavViewInstantiated)
                updateUserDetailsInNavView(false);
            handlerDashboardViewUpdates.post(new Runnable() {
                @Override
                public void run() {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpNavigationMenu() {
//        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.colorStatusBar));
        UserModel userModel = SharePreferences.getUserModel(this);
        if (userModel == null)
            return;
        switch (userModel.getI_am()) {
            case User.TYPE_ADMIN:
                handlerDashboardViewUpdates.post(new Runnable() {
                    @Override
                    public void run() {
                        navigationView.inflateMenu(R.menu.menu_nav_admin);
                    }
                });
                break;
            case User.TYPE_VENDOR:
                handlerDashboardViewUpdates.post(new Runnable() {
                    @Override
                    public void run() {
                        navigationView.inflateMenu(R.menu.menu_nav_vendor);
                    }
                });
                break;
            case User.TYPE_DRIVER:
                handlerDashboardViewUpdates.post(new Runnable() {
                    @Override
                    public void run() {
                        navigationView.inflateMenu(R.menu.menu_nav_driver);
                    }
                });
                break;
            case User.TYPE_PARENT:
                handlerDashboardViewUpdates.post(new Runnable() {
                    @Override
                    public void run() {
                        navigationView.inflateMenu(R.menu.menu_nav_parent);
                    }
                });
                break;
            default:
                handlerDashboardViewUpdates.post(new Runnable() {
                    @Override
                    public void run() {
                        navigationView.inflateMenu(R.menu.menu_nav_guest);
                    }
                });
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AppPermissions.REQUEST_FOR_UPDATE_USER_DETAILS) {
            if (resultCode == RESULT_OK) {
                Glide.get(this).clearDiskCache();
                updateUserDetailsInNavView(true);
            }
        }
    }

    private boolean isNavViewInstantiated = false;

    private void updateUserDetailsInNavView(boolean refresh) {
        View navHeader = navigationView.getHeaderView(0);

        CircularImageView userProfilePic = navHeader.findViewById(R.id.nav_header_icon);
        setUpImageToNavIcon(userProfilePic, refresh);

        final TextView userName = navHeader.findViewById(R.id.nav_header_title);
        final TextView userContactInfo = navHeader.findViewById(R.id.nav_header_subtitle);

        try {
            final String name = firebaseUser.getDisplayName();
            final String phoneNumber = firebaseUser.getPhoneNumber();
            final String email = firebaseUser.getEmail();

            handlerDashboardViewUpdates.post(new Runnable() {
                @Override
                public void run() {
                    if (name == null || name.isEmpty())
                        userName.setHint("Add your name");
                    else {
                        userName.setText(name);
                    }
                }
            });
            handlerDashboardViewUpdates.post(new Runnable() {
                @Override
                public void run() {
                    if (phoneNumber != null)
                        userContactInfo.setText(phoneNumber);
                    else if (email != null)
                        userContactInfo.setText(email);
                }
            });
            isNavViewInstantiated = true;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void setUpImageToNavIcon(CircularImageView civProfilePic, boolean refresh) {
        PreferenceModel preferenceModel = SharePreferences.getPreferenceModel(this);
        if (preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.LOCAL) {
            try {
                civProfilePic.setImageURI(firebaseUser.getPhotoUrl());
            } catch (Exception e) {
                e.printStackTrace();
//                civProfilePic.setImageResource(R.drawable.user_1);
            }

        } else if (preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.CLOUD) {
            try {
                Glide.with(this).load(profilePhotosReference)
                        .into(civProfilePic);
            } catch (Exception e) {
                e.printStackTrace();
//                civProfilePic.setImageResource(R.drawable.user_1);
            }
        }
    }

    private static final long NAV_DRAWER_CLOSE_WAIT_TIME = 300;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.nav_dashboard: {

            }
            return true;
            case R.id.nav_admins: {
                drawerClosedRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DashboardActivity.this, AdminActivity.class));
                    }
                };
            }
            return true;
            case R.id.nav_vendors: {
                drawerClosedRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DashboardActivity.this, VendorActivity.class));
                    }
                };
            }
            return true;
            case R.id.nav_vehicles: {
                drawerClosedRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DashboardActivity.this, VehicleActivity.class));
                    }
                };
            }
            return true;
            case R.id.nav_drivers: {
                drawerClosedRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DashboardActivity.this, DriverActivity.class));
                    }
                };
            }
            return true;
            case R.id.nav_routes: {
                drawerClosedRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DashboardActivity.this, RouteActivity.class));
                    }
                };
            }
            return true;
            case R.id.nav_contacts: {
                drawerClosedRunnable = new Runnable() {
                    @Override
                    public void run() {
//                        startActivity(new Intent(DashboardActivity.this, RouteActivity.class));
                        if (!isSnackbarIndefiniteBusy) {
                            runnableSnackbarIndefinite = getRunnableSnackbarIndefinite();
                            snackbarIndefinite = getSnackbarIndefinite("Implementing soon...", "Dismiss");
                            snackbarIndefinite.show();
                        }
                    }
                };
            }
            return true;
            case R.id.nav_share: {
                drawerClosedRunnable = new Runnable() {
                    @Override
                    public void run() {
//                        startActivity(new Intent(DashboardActivity.this, RouteActivity.class));
                        if (!isSnackbarIndefiniteBusy) {
                            runnableSnackbarIndefinite = getRunnableSnackbarIndefinite();
                            snackbarIndefinite = getSnackbarIndefinite("Implementing soon...", "Dismiss");
                            snackbarIndefinite.show();
                        }
                    }
                };
            }
            return true;
            case R.id.nav_feedback: {
                drawerClosedRunnable = new Runnable() {
                    @Override
                    public void run() {
//                        startActivity(new Intent(DashboardActivity.this, RouteActivity.class));
                        if (!isSnackbarIndefiniteBusy) {
                            runnableSnackbarIndefinite = getRunnableSnackbarIndefinite();
                            snackbarIndefinite = getSnackbarIndefinite("Implementing soon...", "Dismiss");
                            snackbarIndefinite.show();
                        }
                    }
                };
            }
            return true;
            case R.id.nav_account: {
                drawerClosedRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DashboardActivity.this, IAmActivity.class));
                    }
                };
            }
            return true;
            case R.id.nav_settings: {
                drawerClosedRunnable = new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
                    }
                };
            }
            return true;
            case R.id.nav_logout: {
                onLogoutRequested();
            }
            return true;
            default:
                return false;
        }
    }

    private void onLogoutRequested() {
        DialogUtils.showLogoutDialog(this,
                new Runnable() {
                    @Override
                    public void run() {
                        FirebaseAuth.getInstance().signOut();
                        SharePreferences.userLoggedOut(DashboardActivity.this);
                        startActivity(new Intent(DashboardActivity.this, PhoneAuthActivity.class));
                        finish();
                    }
                }, null);
    }

    @Override
    public void setUpRecycler() {
        dashboardModels = new ArrayList<>();

        recyclerDashboard = findViewById(R.id.recycler_dashboard);
        recyclerDashboard.setLayoutManager(new LinearLayoutManager(this));

        adapterDashboard = new DashboardAdapter(this, dashboardModels, new DashboardAdapter.OnDashboardSelectedListener() {
            @Override
            public void onDashboardItemSelected(int position, DashboardModel dashboardModel) {

            }
        });

        recyclerDashboard.setAdapter(adapterDashboard);

        DashboardManager dashboardManager = DashboardManager.getInstance(this);
        dashboardManager.setStatisticsFetchListener(new OnStatisticsFetchListener() {
            @Override
            public void onFetchingStarted(DashboardModel dashboardModel) {
                dashboardModels.add(dashboardModel);
                adapterDashboard.notifyDataSetChanged();
            }

            @Override
            public void onFetchComplete(DashboardModel dashboardModel) {
                dashboardModels.remove(0);
                dashboardModels.add(dashboardModel);
                handlerDashboardViewUpdates.post(new Runnable() {
                    @Override
                    public void run() {
                        adapterDashboard.notifyDataSetChanged();
                    }
                });
            }
        });
        dashboardManager.getStatisticsReport();
    }

    private Runnable getRunnableSnackbarIndefinite(){
        return new Runnable() {
            @Override
            public void run() {
                isSnackbarIndefiniteBusy = false;
                snackbarIndefinite.dismiss();
            }
        };
    }

    private Snackbar getSnackbarIndefinite(@NonNull String message, @Nullable String action){
        snackbarIndefinite = null;

        Snackbar snackbar = Snackbar.make(fabLetsTrack, message, Snackbar.LENGTH_INDEFINITE);
        if (action != null){
            snackbar.setAction(action, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runnableSnackbarIndefinite.run();
                }
            });
        }

        isSnackbarIndefiniteBusy = true;
        return snackbar;
    }

    @Override
    public void updateInternetStatus(boolean online) {

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else
            super.onBackPressed();
    }
}
