package com.faiteasytrack.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.adapters.WayPointAdapter;
import com.faiteasytrack.classess.ETLatLng;
import com.faiteasytrack.enums.FirebaseKeys;
import com.faiteasytrack.models.DriverModel;
import com.faiteasytrack.models.RouteModel;
import com.faiteasytrack.models.VehicleModel;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.Constants;
import com.faiteasytrack.utils.DateUtils;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

public class NAddRouteActivity extends BaseActivity implements View.OnClickListener, OnMapReadyCallback {

    public static final String TAG = "NAddRouteActivity";

    public static final int REQUEST_ADD_NEW_VEHICLE = 3001;
    public static final int REQUEST_ADD_NEW_DRIVER = 3002;
    public static final int CALLED_FROM_ADD_ROUTE = 3003;

    public static final String EXTRA_PARCELABLE = TAG + ".extra_parcelable";

    private ProgressDialog progressDialog;

    private SupportMapFragment mapFragment;

//    private TextInputLayout tilAssignedVehicle, tilAssignedDriver, tilWayPoint;
//    private TextInputEditText etAssignedVehicle, etAssignedDriver,
//            etRouteName, etRouteSource, etRouteDest, etRouteDesc, etWayPoint;
//    private TextView tvVendorName, tvVendorPhone;
//    private MaterialButton btnAssignedVehicle, btnAssignedDriver, btnAddRoute;
//    private FloatingActionButton fabAddWayPoint;

    private RecyclerView recyclerWayPoint;
    private ArrayList<ETLatLng> etLatLngs;
    private WayPointAdapter adapterWayPoints;

    private FirebaseUser firebaseUser;
    private DatabaseReference driversReference, vehiclesReference, routesReference;

    private String routeId, routeName;
    private boolean canChangeDriverCreds = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        driversReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.DRIVERS_DB);
        vehiclesReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.VEHICLE_DB);
        routesReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.ROUTES_DB);

        setContentView(R.layout.activity_add_route_new);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.btn_add_route: {
//                if (isAddRouteValid())
//                    addNewRoute();
//            }
//            break;
//            case R.id.btn_assign_vehicle: {
//                lookForAvailableVehicles();
//            }
//            break;
//            case R.id.btn_assign_driver: {
//                lookForAvailableDriver();
//            }
//            break;
//            case R.id.fab_add_route_waypoint: {
//                ViewUtils.showViews(tilWayPoint);
//            }
//            break;
        }
    }

    @Override
    public void setUpActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void initUI() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);

//        btnAddRoute = findViewById(R.id.btn_add_route);
//        btnAssignedDriver = findViewById(R.id.btn_assign_driver);
//        btnAssignedVehicle = findViewById(R.id.btn_assign_vehicle);
//
//        tilAssignedDriver = findViewById(R.id.til_assigned_driver);
//        ViewUtils.hideViews(tilAssignedDriver);
//        tilAssignedVehicle = findViewById(R.id.til_assigned_vehicle);
//        ViewUtils.hideViews(tilAssignedVehicle);
//
//        etAssignedDriver = findViewById(R.id.et_assigned_driver);
//        etAssignedDriver.setInputType(InputType.TYPE_NULL);
//        etAssignedVehicle = findViewById(R.id.et_assigned_vehicle);
//        etAssignedVehicle.setInputType(InputType.TYPE_NULL);
//
//        tvVendorName = findViewById(R.id.tv_vendor_name);
//        tvVendorPhone = findViewById(R.id.tv_vendor_phone);

//        etRouteName = findViewById(R.id.et_route_name);
//        etRouteSource = findViewById(R.id.et_route_source);
//        etRouteDest = findViewById(R.id.et_route_destination);
//        etRouteDesc = findViewById(R.id.et_route_description);
//
//        tilWayPoint = findViewById(R.id.til_waypoint);
//        etWayPoint = findViewById(R.id.et_route_waypoint);
//        ViewUtils.showViews(tilWayPoint);
//        fabAddWayPoint = findViewById(R.id.fab_add_route_waypoint);
//
//        etWayPoint.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                etLatLngs.add(new ETLatLng(0d, 0d, 0, 0f, 0f));
//                adapterWayPoints.notifyDataSetChanged();
//                ViewUtils.hideViews(tilAssignedVehicle);
//                return true;
//            }
//        });
    }

    @Override
    public void setUpListeners() {
        mapFragment.getMapAsync(this);

//        btnAssignedDriver.setOnClickListener(this);
//        btnAssignedVehicle.setOnClickListener(this);
//        btnAddRoute.setOnClickListener(this);
//
//        etRouteName.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (s.length() == 10) {
//                    showProgressDialog(null, "Looking for existing route...");
//                    checkIfRouteExists();
//                }
//            }
//        });
    }

    @Override
    public void setUpData() {
//        tvVendorName.setText(firebaseUser.getDisplayName());
//        tvVendorPhone.setText(firebaseUser.getPhoneNumber());
    }

    @Override
    public void setUpRecycler() {
//        etLatLngs = new ArrayList<>();
//
//        recyclerWayPoint = findViewById(R.id.recycler_route_waypoints);
//        adapterWayPoints = new WayPointAdapter(this, etLatLngs, new WayPointAdapter.OnWayPointSelectedListener() {
//            @Override
//            public void onWayPointSelected(int position, ETLatLng etLatLng) {
//
//            }
//        });
//        recyclerWayPoint.setAdapter(adapterWayPoints);
    }

    private void checkIfRouteExists() {
//        queryFindRoute = routesReference.orderByChild(FirebaseKeys.ROUTE_NAME)
//                .equalTo(etRouteName.getText().toString());
//        queryFindRoute.addValueEventListener(findRouteEventListener);
    }

    private Query queryFindRoute;
    private ValueEventListener findRouteEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot querySnapshot) {
            Log.i(TAG, "findRouteEventListener.onDataChange: " + querySnapshot);
            boolean isRouteExists = false;

            for (DataSnapshot dataSnapshot : querySnapshot.getChildren()) {
                RouteModel routeModel = dataSnapshot.getValue(RouteModel.class);
//                if (routeModel != null && routeModel.getRouteName().equals(etRouteName.getText().toString()))
//                    isRouteExists = true;
            }

            if (isRouteExists)
                onRouteExistsAlready();
            else
                onRouteIsNew();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "findRouteEventListener.onCancelled: " + databaseError.getMessage());
            hideProgressDialog();
            DialogUtils.showSorryAlert(NAddRouteActivity.this, databaseError.getMessage(), null);
        }
    };

    private void onRouteExistsAlready() {
        queryFindRoute.removeEventListener(findRouteEventListener);
        hideProgressDialog();

        DialogUtils.showSorryAlert(this, "A route is already registered with the given name." +
                " Please use a different name.", new Runnable() {
            @Override
            public void run() {
//                etRouteName.requestFocus();
//                etRouteName.setText("");
            }
        });
    }

    private void onRouteIsNew() {
        queryFindRoute.removeEventListener(findRouteEventListener);
//        Snackbar.make(btnAddRoute, "You're good to go!", Snackbar.LENGTH_SHORT).show();
        hideProgressDialog();

//        btnAssignedDriver.setEnabled(true);
//        btnAssignedVehicle.setEnabled(true);
    }

    private void lookForAvailableDriver() {
        queryUnassignedDrivers = driversReference.orderByChild(FirebaseKeys.IS_DRIVER_ASSIGNED_TO_VEHICLE).equalTo(false);
        queryUnassignedDrivers.addValueEventListener(findUnassignedDriversEventListener);
    }

    ArrayList<DriverModel> driverModels = new ArrayList<>();
    private Query queryUnassignedDrivers;
    private ValueEventListener findUnassignedDriversEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "findUnassignedDriversEventListener.onDataChange: " + dataSnapshot);

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                driverModels.add(snapshot.getValue(DriverModel.class));
            }

            if (driverModels.size() > 0)
                showDriversDialog();
            else
                askWhetherToAddNewDriver();
            queryUnassignedDrivers.removeEventListener(findUnassignedDriversEventListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "findUnassignedDriversEventListener.onCancelled: " + databaseError.getMessage());

            queryUnassignedDrivers.removeEventListener(findUnassignedDriversEventListener);
        }
    };

    private void askWhetherToAddNewDriver() {
        DialogUtils.showSorryAlert(this, "Seems as if all drivers are occupied." +
                        " Do you wish to add a new driver?", "Yes, Let's Add", "No, Not Now",
                new Runnable() {
                    @Override
                    public void run() {
                        if (canChangeDriverCreds){
//                            routeName = etRouteName.getText().toString();
//                            if (Utils.isInvalidString(routeName)){
//                                Snackbar.make(btnAddRoute, "Please add route name first!", Snackbar.LENGTH_LONG).show();
//                                return;
//                            }
//                            routeId = DateUtils.createRouteId();
                        }
                        Intent driverIntent = new Intent(NAddRouteActivity.this, NAddDriverActivity.class);
                        driverIntent.putExtra(Constants.CALLED_FROM, CALLED_FROM_ADD_ROUTE);
                        driverIntent.putExtra(Constants.CALLED_FROM_WITH_ID, routeId);
                        driverIntent.putExtra(Constants.CALLED_FROM_WITH_NAME, routeName);

                        startActivityForResult(driverIntent, REQUEST_ADD_NEW_DRIVER);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {

                    }
                });
    }

    private void lookForAvailableVehicles() {
        queryUnassignedVehicles = vehiclesReference.orderByChild(FirebaseKeys.IS_VEHICLE_ASSIGNED_TO_ROUTE).equalTo(false);
        queryUnassignedVehicles.addValueEventListener(findUnassignedVehiclesEventListener);
    }

    ArrayList<VehicleModel> vehicleModels = new ArrayList<>();
    private Query queryUnassignedVehicles;
    private ValueEventListener findUnassignedVehiclesEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "findUnassignedVehiclesEventListener.onDataChange: " + dataSnapshot);

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                vehicleModels.add(snapshot.getValue(VehicleModel.class));
            }

            if (vehicleModels.size() > 0)
                showVehiclesDialog();
            else
                askWhetherToAddNewVehicle();
            queryUnassignedVehicles.removeEventListener(findUnassignedVehiclesEventListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "findUnassignedVehiclesEventListener.onCancelled: " + databaseError.getMessage());

            queryUnassignedVehicles.removeEventListener(findUnassignedVehiclesEventListener);
        }
    };

    private void askWhetherToAddNewVehicle() {
        DialogUtils.showSorryAlert(this, "Seems as if all vehicles are occupied." +
                        " Do you wish to add a new vehicle?", "Yes, Let's Add", "No, Not Now",
                new Runnable() {
                    @Override
                    public void run() {
                        if (canChangeDriverCreds){
//                            routeName = etRouteName.getText().toString();
//                            if (Utils.isInvalidString(routeName)){
//                                Snackbar.make(btnAddRoute, "Please add route name first!", Snackbar.LENGTH_LONG).show();
//                                return;
//                            }
//                            routeId = DateUtils.createRouteId();
                        }
                        Intent vehicleIntent = new Intent(NAddRouteActivity.this, NAddVehicleActivity.class);
                        vehicleIntent.putExtra(Constants.CALLED_FROM, CALLED_FROM_ADD_ROUTE);
                        vehicleIntent.putExtra(Constants.CALLED_FROM_WITH_ID, routeId);
                        vehicleIntent.putExtra(Constants.CALLED_FROM_WITH_NAME, routeName);
                        startActivityForResult(vehicleIntent, REQUEST_ADD_NEW_VEHICLE);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ADD_NEW_VEHICLE) {
            if (resultCode == RESULT_OK && data != null) {
                selectedVehicleModel = data.getParcelableExtra(EXTRA_PARCELABLE);
                canChangeDriverCreds = false;
            }
        } else if (requestCode == REQUEST_ADD_NEW_DRIVER) {
            if (resultCode == RESULT_OK && data != null) {
                selectedDriverModel = data.getParcelableExtra(EXTRA_PARCELABLE);
                canChangeDriverCreds = false;
            }
        }
    }

    private VehicleModel selectedVehicleModel;

    private void showVehiclesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setCancelable(true);
        builder.setTitle("Select a vehicle:");
        String[] vehicles = new String[vehicleModels.size()];
        for (int i = 0; i < vehicleModels.size(); i++) {
            VehicleModel vehicleModel = vehicleModels.get(i);
            vehicles[i] = vehicleModel.getVehicleNumber();
        }
        builder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedVehicleModel = vehicleModels.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedVehicleModel = null;
            }
        });
        builder.setSingleChoiceItems(vehicles, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private DriverModel selectedDriverModel;

    private void showDriversDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setCancelable(true);
        builder.setTitle("Select a vehicle:");
        String[] vehicles = new String[driverModels.size()];
        for (int i = 0; i < driverModels.size(); i++) {
            DriverModel driverModel = driverModels.get(i);
            vehicles[i] = driverModel.getName();
        }
        builder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDriverModel = driverModels.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDriverModel = null;
            }
        });
        builder.setSingleChoiceItems(vehicles, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private boolean isAddRouteValid() {
        boolean isAddRouteValid = true;

//        if (Utils.isInvalidString(etRouteName.getText().toString())) {
//            isAddRouteValid = false;
//            etRouteName.setError("Give valid name!");
//        } else if (Utils.isInvalidString(etRouteSource.getText().toString())) {
//            isAddRouteValid = false;
//            etRouteSource.setError("Give valid source!");
//        } else if (Utils.isInvalidString(etRouteDest.getText().toString())) {
//            isAddRouteValid = false;
//            etRouteDest.setError("Give valid destination!");
//        }

        return isAddRouteValid;
    }

    private void addNewRoute() {
        RouteModel routeModel = new RouteModel();

        if (canChangeDriverCreds) {
//            routeId = (DateUtils.createRouteId());
//            routeName = (etRouteName.getText().toString());
        }
        routeModel.setRouteId(routeId);
        routeModel.setRouteName(routeName);
        routeModel.setSource(new ETLatLng());
        routeModel.setDestination(new ETLatLng());
        routeModel.setWayPoints(new LinkedList<ETLatLng>());

        routeModel.setRegisteredByVendorUid(firebaseUser.getUid());
        routeModel.setRegisteredByVendorName(firebaseUser.getDisplayName());
        routeModel.setRegisteredByVendorPhone(firebaseUser.getPhoneNumber());

        String driverId = "", driverName = "";
        if (selectedDriverModel != null){
            driverId = selectedDriverModel.getDriverId();
            driverName = selectedDriverModel.getName();
        }
        routeModel.setDriverId(driverId);
        routeModel.setDriverName(driverName);

        String vehicleId = "", vehicleNumber = "";
        if (selectedVehicleModel != null){
            vehicleId = selectedVehicleModel.getVehicleId();
            vehicleNumber = selectedVehicleModel.getVehicleNumber();
        }
        routeModel.setVehicleId(vehicleId);
        routeModel.setVehicleNumber(vehicleNumber);

        routeModel.setRouteInRunning(!(vehicleId.equals("") || vehicleNumber.equals("")));

        routesReference.addValueEventListener(addRouteEventListener);
        routesReference.push().setValue(routeModel);
    }

    private ValueEventListener addRouteEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "addRouteEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            DialogUtils.showCheersAlert(NAddRouteActivity.this, "Route added successfully.",
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "addRouteEventListener.onCancelled: " + databaseError.getMessage());

            hideProgressDialog();
            DialogUtils.showSorryAlert(NAddRouteActivity.this, "" + databaseError.getMessage(),
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
        }
    };

    @Override
    public void updateInternetError(boolean isOnline) {

    }

    private void showProgressDialog(String title, String message) {
        if (title != null)
            progressDialog.setTitle(title);
        if (message != null)
            progressDialog.setMessage(message);

        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void findLatLngFromPlacesAPI(){

    }

    // todo : send push to the driver(if assigned) with vehicle(if assigned) and route details
}
