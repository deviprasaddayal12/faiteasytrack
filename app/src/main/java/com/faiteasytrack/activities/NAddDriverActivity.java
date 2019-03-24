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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.faiteasytrack.R;
import com.faiteasytrack.constants.FirebaseKeys;
import com.faiteasytrack.models.DriverModel;
import com.faiteasytrack.models.RouteModel;
import com.faiteasytrack.models.VehicleModel;
import com.faiteasytrack.utils.Constants;
import com.faiteasytrack.utils.DateUtils;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.button.MaterialButton;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

public class NAddDriverActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NAddDriverActivity";

    public static final int REQUEST_ADD_NEW_VEHICLE = 1001;
    public static final int REQUEST_ADD_NEW_ROUTE = 1002;
    public static final int CALLED_FROM_ADD_DRIVER = 1003;

    public static final String EXTRA_PARCELABLE = TAG + ".extra_parcelable";

    private ProgressDialog progressDialog;

    private TextInputLayout tilAssignedVehicle, tilAssignedRoute;
    private TextInputEditText etAssignedVehicle, etAssignedRoute, etVendorName, etVendorPhone,
            etDriverName, etDriverPhone, etDriverCode, etDriverPassword;
    private MaterialButton btnAssignedVehicle, btnAssignedRoute, btnAddDriver;

    private FirebaseUser firebaseUser;
    private DatabaseReference driversReference, vehiclesReference, routesReference;

    private String driverId, driverName;
    private boolean canChangeDriverCreds = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        driversReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.DRIVERS_DB);
        vehiclesReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.VEHICLE_DB);
        routesReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.ROUTES_DB);

        setContentView(R.layout.activity_add_driver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_driver: {
                if (isAddDriverValid())
                    addNewDriver();
            }
            break;
            case R.id.btn_assign_vehicle: {
                lookForAvailableVehicles();
            }
            break;
            case R.id.btn_assign_route: {
                lookForAvailableRoutes();
            }
            break;
        }
    }

    @Override
    public void setUpActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void initUI() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);

        btnAddDriver = findViewById(R.id.btn_add_driver);
        btnAssignedRoute = findViewById(R.id.btn_assign_route);
        btnAssignedVehicle = findViewById(R.id.btn_assign_vehicle);

        tilAssignedRoute = findViewById(R.id.til_assigned_route);
        ViewUtils.hideViews(tilAssignedRoute);
        tilAssignedVehicle = findViewById(R.id.til_assigned_vehicle);
        ViewUtils.hideViews(tilAssignedVehicle);

        etAssignedRoute = findViewById(R.id.et_assigned_route);
        etAssignedRoute.setInputType(InputType.TYPE_NULL);
        etAssignedVehicle = findViewById(R.id.et_assigned_vehicle);
        etAssignedVehicle.setInputType(InputType.TYPE_NULL);

        etVendorName = findViewById(R.id.et_vendor_name);
        etVendorName.setInputType(InputType.TYPE_NULL);
        etVendorPhone = findViewById(R.id.et_vendor_phone);
        etVendorPhone.setInputType(InputType.TYPE_NULL);

        etDriverName = findViewById(R.id.et_driver_name);
        etDriverPhone = findViewById(R.id.et_driver_phone);
        etDriverPhone.setFilters(Utils.getLengthFilter(10));
        etDriverCode = findViewById(R.id.et_driver_code);
        etDriverCode.setFilters(Utils.getLengthFilter(10));
        etDriverPassword = findViewById(R.id.et_driver_password);
    }

    @Override
    public void setUpListeners() {
        btnAddDriver.setOnClickListener(this);
        btnAssignedRoute.setOnClickListener(this);
        btnAssignedVehicle.setOnClickListener(this);

        etDriverPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    showProgressDialog(null, "Looking for existing driver...");
                    checkIfDriverExists();
                }
            }
        });
    }

    @Override
    public void setUpData() {
        etVendorName.setText(firebaseUser.getDisplayName());
        etVendorPhone.setText(firebaseUser.getPhoneNumber());
    }

    private void checkIfDriverExists() {
        queryFindDriver = driversReference.orderByChild(FirebaseKeys.PHONE_NUMBER)
                .equalTo(etDriverPhone.getText().toString());
        queryFindDriver.addValueEventListener(findDriverEventListener);
    }

    private Query queryFindDriver;
    private ValueEventListener findDriverEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot querySnapshot) {
            Log.i(TAG, "findDriverEventListener.onDataChange: " + querySnapshot);
            boolean isDriverExists = false;

            for (DataSnapshot dataSnapshot : querySnapshot.getChildren()) {
                DriverModel driverModel = dataSnapshot.getValue(DriverModel.class);
                if (driverModel != null && driverModel.getPhoneNumber().equals(etDriverPhone.getText().toString()))
                    isDriverExists = true;
            }

            if (isDriverExists)
                onDriverExistsAlready();
            else
                onDriverIsNew();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "findDriverEventListener.onCancelled: " + databaseError.getMessage());
            hideProgressDialog();
            DialogUtils.showSorryAlert(NAddDriverActivity.this, databaseError.getMessage(), null);
        }
    };

    private void onDriverExistsAlready() {
        queryFindDriver.removeEventListener(findDriverEventListener);
        hideProgressDialog();

        DialogUtils.showSorryAlert(this, "A driver is already registered with the given number." +
                " Please use a different number.", new Runnable() {
            @Override
            public void run() {
                etDriverPhone.requestFocus();
                etDriverPhone.setText("");
            }
        });
    }

    private void onDriverIsNew() {
        queryFindDriver.removeEventListener(findDriverEventListener);
        Snackbar.make(btnAddDriver, "You're good to go!", Snackbar.LENGTH_SHORT).show();
        hideProgressDialog();

        btnAssignedVehicle.setEnabled(true);
        ViewUtils.showViews(tilAssignedVehicle);
        btnAssignedRoute.setEnabled(true);
        ViewUtils.showViews(tilAssignedRoute);
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
                            driverName = etDriverName.getText().toString();
                            if (Utils.isInvalidString(driverName)){
                                Snackbar.make(btnAddDriver, "Please add driver name first!", Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            driverId = DateUtils.createDriverId();
                        }
                        Intent vehicleIntent = new Intent(NAddDriverActivity.this, NAddVehicleActivity.class);
                        vehicleIntent.putExtra(Constants.CALLED_FROM, CALLED_FROM_ADD_DRIVER);
                        vehicleIntent.putExtra(Constants.CALLED_FROM_WITH_ID, driverId);
                        vehicleIntent.putExtra(Constants.CALLED_FROM_WITH_NAME, driverName);
                        startActivityForResult(vehicleIntent, REQUEST_ADD_NEW_VEHICLE);
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {

                    }
                });
    }

    private void lookForAvailableRoutes() {
        queryUnassignedRoutes = routesReference.orderByChild(FirebaseKeys.IS_ROUTE_IN_RUNNING).equalTo(false);
        queryUnassignedRoutes.addValueEventListener(findUnassignedRoutesEventListener);
    }

    ArrayList<RouteModel> routeModels = new ArrayList<>();
    private Query queryUnassignedRoutes;
    private ValueEventListener findUnassignedRoutesEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "findUnassignedRoutesEventListener.onDataChange: " + dataSnapshot);

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                routeModels.add(snapshot.getValue(RouteModel.class));
            }

            if (routeModels.size() > 0)
                showRoutesDialog();
            else
                askWhetherToCreateNewRoute();
            queryUnassignedRoutes.removeEventListener(findUnassignedRoutesEventListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "findUnassignedRoutesEventListener.onCancelled: " + databaseError.getMessage());

            queryUnassignedRoutes.removeEventListener(findUnassignedRoutesEventListener);
        }
    };

    private void askWhetherToCreateNewRoute() {
        DialogUtils.showSorryAlert(this, "Seems as if all routes are occupied." +
                        " Do you wish to add a new route?", "Yes, Let's Add", "No, Not Now",
                new Runnable() {
                    @Override
                    public void run() {
                        if (canChangeDriverCreds){
                            driverName = etDriverName.getText().toString();
                            if (Utils.isInvalidString(driverName)){
                                Snackbar.make(btnAddDriver, "Please add driver name first!", Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            driverId = DateUtils.createDriverId();
                        }
                        Intent routeIntent = new Intent(NAddDriverActivity.this, NAddRouteActivity.class);
                        routeIntent.putExtra(Constants.CALLED_FROM, CALLED_FROM_ADD_DRIVER);
                        routeIntent.putExtra(Constants.CALLED_FROM_WITH_ID, driverId);
                        routeIntent.putExtra(Constants.CALLED_FROM_WITH_NAME, driverName);
                        startActivityForResult(routeIntent, REQUEST_ADD_NEW_ROUTE);
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
        } else if (requestCode == REQUEST_ADD_NEW_ROUTE) {
            if (resultCode == RESULT_OK && data != null) {
                selectedRouteModel = data.getParcelableExtra(EXTRA_PARCELABLE);
                canChangeDriverCreds = false;
            }
        }
    }

    private RouteModel selectedRouteModel;

    private void showRoutesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setCancelable(true);
        builder.setTitle("Select a route:");
        String[] vehicles = new String[routeModels.size()];
        for (int i = 0; i < routeModels.size(); i++) {
            RouteModel routeModel = routeModels.get(i);
            vehicles[i] = routeModel.getRouteName();
        }
        builder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRouteModel = routeModels.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRouteModel = null;
            }
        });
        builder.setSingleChoiceItems(vehicles, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
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

    private boolean isAddDriverValid() {
        boolean isAddDriverValid = true;

        if (Utils.isInvalidString(etDriverName.getText().toString())) {
            isAddDriverValid = false;
            etDriverName.setError("Give valid name!");
        } else if (Utils.isInvalidString(etDriverPhone.getText().toString())) {
            isAddDriverValid = false;
            etDriverPhone.setError("Give valid phone!");
        } else if (Utils.isInvalidString(etDriverCode.getText().toString())) {
            isAddDriverValid = false;
            etDriverCode.setError("Give valid code!");
        } else if (Utils.isInvalidString(etDriverPassword.getText().toString())) {
            isAddDriverValid = false;
            etDriverPassword.setError("Give valid password!");
        }

        return isAddDriverValid;
    }

    private void addNewDriver() {
        DriverModel driverModel = new DriverModel();

        if (canChangeDriverCreds) {
            driverId = (DateUtils.createDriverId());
            driverName = (etDriverName.getText().toString());
        }
        driverModel.setDriverId(driverId);
        driverModel.setName(driverName);
        driverModel.setPhoneNumber(etDriverPhone.getText().toString());
        driverModel.setCode(etDriverCode.getText().toString());
        driverModel.setPassword(etDriverPassword.getText().toString());

        driverModel.setRegisteredByVendorUid(firebaseUser.getUid());
        driverModel.setRegisteredByVendorName(firebaseUser.getDisplayName());
        driverModel.setRegisteredByVendorPhone(firebaseUser.getPhoneNumber());

        String routeId = "", routeName = "";
        if (selectedRouteModel != null){
            routeId = selectedRouteModel.getRouteId();
            routeName = selectedRouteModel.getRouteName();
        }
        driverModel.setRouteId(routeId);
        driverModel.setRouteName(routeName);

        String vehicleId = "", vehicleNumber = "";
        if (selectedRouteModel != null){
            vehicleId = selectedVehicleModel.getVehicleId();
            vehicleNumber = selectedVehicleModel.getVehicleNumber();
        }
        driverModel.setVehicleId(vehicleId);
        driverModel.setVehicleNumber(vehicleNumber);

        driverModel.setDriverAssignedToVehicle(!(vehicleId.equals("") || vehicleNumber.equals("")));

        driversReference.addValueEventListener(addDriverEventListener);
        driversReference.push().setValue(driverModel);
    }

    private ValueEventListener addDriverEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "addDriverEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            DialogUtils.showCheersAlert(NAddDriverActivity.this, "Driver added successfully.",
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "addDriverEventListener.onCancelled: " + databaseError.getMessage());

            hideProgressDialog();
            DialogUtils.showSorryAlert(NAddDriverActivity.this, "" + databaseError.getMessage(),
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
        }
    };

    @Override
    public void setUpRecycler() {

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // todo : send push to the phone number with code and password with vehicle(if assigned) and route(if assigned) details
}
