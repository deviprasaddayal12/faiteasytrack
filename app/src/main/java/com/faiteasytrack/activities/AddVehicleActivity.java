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
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.faiteasytrack.R;
import com.faiteasytrack.firebase.FirebaseKeys;
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

public class AddVehicleActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = "NAddVehicleActivity";

    public static final int REQUEST_ADD_NEW_DRIVER = 2001;
    public static final int REQUEST_ADD_NEW_ROUTE = 2002;
    public static final int CALLED_FROM_ADD_VEHICLE = 2003;

    public static final String EXTRA_PARCELABLE = TAG + ".extra_parcelable";

    private ProgressDialog progressDialog;

    private TextInputLayout tilAssignedDriver, tilAssignedRoute;
    private TextInputEditText etAssignedDriver, etAssignedRoute, etVendorName, etVendorPhone,
            etVehicleNumber, etVehicleModel, etVehicleDescription;
    private MaterialButton btnAssignedDriver, btnAssignedRoute, btnAddVehicle;
    private Spinner spinnerVehicleType;

    private FirebaseUser firebaseUser;
    private DatabaseReference driversReference, vehiclesReference, routesReference;

    private String[] vehicleTypes;
    private String vehicleId, vehicleNumber;
    private boolean canChangeDriverCreds = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        driversReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.DRIVERS_DB);
        vehiclesReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.VEHICLE_DB);
        routesReference = FirebaseDatabase.getInstance().getReference().child(FirebaseKeys.ROUTES_DB);

        setContentView(R.layout.activity_add_vehicle);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_vehicle: {
                if (isAddVehicleValid())
                    addNewVehicle();
            }
            break;
            case R.id.btn_assign_driver: {
                lookForAvailableDriver();
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

        btnAddVehicle = findViewById(R.id.btn_add_vehicle);
        btnAssignedRoute = findViewById(R.id.btn_assign_route);
        btnAssignedDriver = findViewById(R.id.btn_assign_driver);

        tilAssignedRoute = findViewById(R.id.til_assigned_route);
        ViewUtils.hideViews(tilAssignedRoute);
        tilAssignedDriver = findViewById(R.id.til_assigned_driver);
        ViewUtils.hideViews(tilAssignedDriver);

        etAssignedRoute = findViewById(R.id.et_assigned_route);
        etAssignedRoute.setInputType(InputType.TYPE_NULL);
        etAssignedDriver = findViewById(R.id.et_assigned_vehicle);
        etAssignedDriver.setInputType(InputType.TYPE_NULL);

        etVendorName = findViewById(R.id.et_vendor_name);
        etVendorName.setInputType(InputType.TYPE_NULL);
        etVendorPhone = findViewById(R.id.et_vendor_phone);
        etVendorPhone.setInputType(InputType.TYPE_NULL);

        etVehicleNumber = findViewById(R.id.et_driver_name);
        etVehicleNumber.setFilters(Utils.getLengthFilter(10));
        etVehicleModel = findViewById(R.id.et_driver_phone);
        etVehicleDescription = findViewById(R.id.et_driver_code);
        etVehicleDescription.setFilters(Utils.getLengthFilter(501));

        vehicleTypes = getResources().getStringArray(R.array.vehicle_types);
        spinnerVehicleType = findViewById(R.id.spinner_vehicle_type);
        ArrayAdapter<String> adapterSpinnerVType = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, vehicleTypes);
        spinnerVehicleType.setAdapter(adapterSpinnerVType);
    }

    @Override
    public void setUpListeners() {
        btnAddVehicle.setOnClickListener(this);
        btnAssignedRoute.setOnClickListener(this);
        btnAssignedDriver.setOnClickListener(this);

        etVehicleNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10) {
                    showProgressDialog(null, "Looking for existing vehicle...");
                    checkIfVehicleExists();
                }
            }
        });
    }

    @Override
    public void setUpData() {
        etVendorName.setText(firebaseUser.getDisplayName());
        etVendorPhone.setText(firebaseUser.getPhoneNumber());
    }

    private void checkIfVehicleExists() {
        queryFindVehicle = vehiclesReference.orderByChild(FirebaseKeys.VEHICLE_NUMBER)
                .equalTo(etVehicleNumber.getText().toString());
        queryFindVehicle.addValueEventListener(findVehicleEventListener);
    }

    private Query queryFindVehicle;
    private ValueEventListener findVehicleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot querySnapshot) {
            Log.i(TAG, "findVehicleEventListener.onDataChange: " + querySnapshot);
            boolean isVehicleExists = false;

            for (DataSnapshot dataSnapshot : querySnapshot.getChildren()) {
                VehicleModel vehicleModel = dataSnapshot.getValue(VehicleModel.class);
                if (vehicleModel != null && vehicleModel.getVehicleNumber().equals(etVehicleNumber.getText().toString()))
                    isVehicleExists = true;
            }

            if (isVehicleExists)
                onVehicleExistsAlready();
            else
                onVehicleIsNew();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "findVehicleEventListener.onCancelled: " + databaseError.getMessage());
            hideProgressDialog();
            DialogUtils.showSorryAlert(AddVehicleActivity.this, databaseError.getMessage(), null);
        }
    };

    private void onVehicleExistsAlready() {
        // todo : check if vehicle is free or assigned
        queryFindVehicle.removeEventListener(findVehicleEventListener);
        hideProgressDialog();

        DialogUtils.showSorryAlert(this, "A vehicle is already registered with the given number." +
                " Please use a different vehicle.", new Runnable() {
            @Override
            public void run() {
                etVehicleNumber.requestFocus();
                etVehicleNumber.setText("");
            }
        });
    }

    private void onVehicleIsNew() {
        queryFindVehicle.removeEventListener(findVehicleEventListener);
        Snackbar.make(btnAddVehicle, "You're good to go!", Snackbar.LENGTH_SHORT).show();
        hideProgressDialog();

        btnAssignedDriver.setEnabled(true);
        ViewUtils.showViews(tilAssignedDriver);
        btnAssignedRoute.setEnabled(true);
        ViewUtils.showViews(tilAssignedRoute);
    }

    private ValueEventListener addVehicleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "addVehicleEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            DialogUtils.showCheersAlert(AddVehicleActivity.this, "Vehicle added successfully.",
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.i(TAG, "addVehicleEventListener.onCancelled: " + databaseError.getMessage());

            hideProgressDialog();
            DialogUtils.showSorryAlert(AddVehicleActivity.this, "" + databaseError.getMessage(),
                    new Runnable() {
                        @Override
                        public void run() {
                            onBackPressed();
                        }
                    });
        }
    };

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
                            vehicleNumber = etVehicleNumber.getText().toString();
                            if (Utils.isInvalidString(vehicleNumber)){
                                Snackbar.make(btnAddVehicle, "Please add vehicle number first!", Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            vehicleId = DateUtils.createDriverId();
                        }
                        Intent driverIntent = new Intent(AddVehicleActivity.this, AddDriverActivity.class);
                        driverIntent.putExtra(Constants.CALLED_FROM, CALLED_FROM_ADD_VEHICLE);
                        driverIntent.putExtra(Constants.CALLED_FROM_WITH_ID, vehicleId);
                        driverIntent.putExtra(Constants.CALLED_FROM_WITH_NAME, vehicleNumber);

                        startActivityForResult(driverIntent, REQUEST_ADD_NEW_DRIVER);
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
                            vehicleNumber = etVehicleNumber.getText().toString();
                            if (Utils.isInvalidString(vehicleNumber)){
                                Snackbar.make(btnAddVehicle, "Please add vehicle number first!", Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            vehicleId = DateUtils.createDriverId();
                        }
                        Intent routeIntent = new Intent(AddVehicleActivity.this, AddRouteActivity.class);
                        routeIntent.putExtra(Constants.CALLED_FROM, CALLED_FROM_ADD_VEHICLE);
                        routeIntent.putExtra(Constants.CALLED_FROM_WITH_ID, vehicleId);
                        routeIntent.putExtra(Constants.CALLED_FROM_WITH_NAME, vehicleNumber);
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
        if (requestCode == REQUEST_ADD_NEW_DRIVER) {
            if (resultCode == RESULT_OK && data != null) {
                selectedDriverModel = data.getParcelableExtra(EXTRA_PARCELABLE);
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

    private boolean isAddVehicleValid() {
        boolean isAddVehicleValid = true;

        if (Utils.isInvalidString(etVehicleNumber.getText().toString())) {
            isAddVehicleValid = false;
            etVehicleNumber.setError("Give valid name!");
        } else if (Utils.isInvalidString(etVehicleModel.getText().toString())) {
            isAddVehicleValid = false;
            etVehicleModel.setError("Give valid phone!");
        }/* else if (Utils.isInvalidString(etVehicleDescription.getText().toString())) {
            isAddVehicleValid = false;
            etVehicleDescription.setError("Give valid code!");
        }*/

        return isAddVehicleValid;
    }

    private void addNewVehicle() {
        VehicleModel vehicleModel = new VehicleModel();

        vehicleModel.setVehicleId(DateUtils.createVehicleId());
        vehicleModel.setVehicleNumber(etVehicleNumber.getText().toString());
        vehicleModel.setVehicleDescription(etVehicleDescription.getText().toString());
        vehicleModel.setVehicleModel(etVehicleModel.getText().toString());
        vehicleModel.setVehicleType(vehicleTypes[spinnerVehicleType.getSelectedItemPosition()]);

        vehicleModel.setRegisteredByVendorUid(firebaseUser.getUid());
        vehicleModel.setRegisteredByVendorName(firebaseUser.getDisplayName());
        vehicleModel.setRegisteredByVendorPhone(firebaseUser.getPhoneNumber());

        String routeId = "", routeName = "";
        if (selectedRouteModel != null){
            routeId = selectedRouteModel.getRouteId();
            routeName = selectedRouteModel.getRouteName();
        }
        vehicleModel.setRouteId(routeId);
        vehicleModel.setRouteName(routeName);

        String driverId = "", driverName = "";
        if (selectedRouteModel != null){
            driverId = selectedDriverModel.getDriverId();
            driverName = selectedDriverModel.getName();
        }
        vehicleModel.setDriverId(driverId);
        vehicleModel.setDriverName(driverName);

        vehicleModel.setVehicleAssignedToRoute(!(routeId.equals("") || routeName.equals("")));

        vehiclesReference.addValueEventListener(addVehicleEventListener);
        vehiclesReference.push().setValue(vehicleModel);
    }

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
    public void updateInternetStatus(boolean online) {

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

    // todo : send push to the driver(if assigned) with vehicle and route(if assigned) details
}
