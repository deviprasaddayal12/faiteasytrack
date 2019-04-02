package com.faiteasytrack.models;

import android.os.Parcel;
import android.os.Parcelable;

public class DriverModel extends BaseAddendModel implements Parcelable {

    private String driverId;
    private String vehicleId;
    private String vehicleNumber;
    private String routeId;
    private String routeName;
    private boolean isDriverAssignedToVehicle;

    public DriverModel() {
    }

    protected DriverModel(Parcel in) {
        driverId = in.readString();
        vehicleId = in.readString();
        vehicleNumber = in.readString();
        routeId = in.readString();
        routeName = in.readString();
        isDriverAssignedToVehicle = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(driverId);
        dest.writeString(vehicleId);
        dest.writeString(vehicleNumber);
        dest.writeString(routeId);
        dest.writeString(routeName);
        dest.writeByte((byte) (isDriverAssignedToVehicle ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DriverModel> CREATOR = new Creator<DriverModel>() {
        @Override
        public DriverModel createFromParcel(Parcel in) {
            return new DriverModel(in);
        }

        @Override
        public DriverModel[] newArray(int size) {
            return new DriverModel[size];
        }
    };

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public boolean isDriverAssignedToVehicle() {
        return isDriverAssignedToVehicle;
    }

    public void setDriverAssignedToVehicle(boolean driverAssignedToVehicle) {
        isDriverAssignedToVehicle = driverAssignedToVehicle;
    }
}
