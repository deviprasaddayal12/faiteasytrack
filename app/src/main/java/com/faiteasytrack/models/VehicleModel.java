package com.faiteasytrack.models;

import android.os.Parcel;
import android.os.Parcelable;

public class VehicleModel implements Parcelable {

    private String vehicleId;
    private String vehicleNumber;
    private String vehicleModel;
    private String vehicleType;
    private String vehicleDescription;

    private String driverId;
    private String driverName;

    private String registeredByVendorUid;
    private String registeredByVendorPhone;
    private String registeredByVendorName;

    private String routeName;
    private String routeId;

    private boolean isVehicleAssignedToRoute;

    public VehicleModel() {
    }

    protected VehicleModel(Parcel in) {
        vehicleId = in.readString();
        vehicleNumber = in.readString();
        vehicleModel = in.readString();
        vehicleType = in.readString();
        vehicleDescription = in.readString();
        driverId = in.readString();
        driverName = in.readString();
        registeredByVendorUid = in.readString();
        registeredByVendorPhone = in.readString();
        registeredByVendorName = in.readString();
        routeName = in.readString();
        routeId = in.readString();
        isVehicleAssignedToRoute = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vehicleId);
        dest.writeString(vehicleNumber);
        dest.writeString(vehicleModel);
        dest.writeString(vehicleType);
        dest.writeString(vehicleDescription);
        dest.writeString(driverId);
        dest.writeString(driverName);
        dest.writeString(registeredByVendorUid);
        dest.writeString(registeredByVendorPhone);
        dest.writeString(registeredByVendorName);
        dest.writeString(routeName);
        dest.writeString(routeId);
        dest.writeByte((byte) (isVehicleAssignedToRoute ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VehicleModel> CREATOR = new Creator<VehicleModel>() {
        @Override
        public VehicleModel createFromParcel(Parcel in) {
            return new VehicleModel(in);
        }

        @Override
        public VehicleModel[] newArray(int size) {
            return new VehicleModel[size];
        }
    };

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleDescription() {
        return vehicleDescription;
    }

    public void setVehicleDescription(String vehicleDescription) {
        this.vehicleDescription = vehicleDescription;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getRegisteredByVendorUid() {
        return registeredByVendorUid;
    }

    public void setRegisteredByVendorUid(String registeredByVendorUid) {
        this.registeredByVendorUid = registeredByVendorUid;
    }

    public String getRegisteredByVendorPhone() {
        return registeredByVendorPhone;
    }

    public void setRegisteredByVendorPhone(String registeredByVendorPhone) {
        this.registeredByVendorPhone = registeredByVendorPhone;
    }

    public String getRegisteredByVendorName() {
        return registeredByVendorName;
    }

    public void setRegisteredByVendorName(String registeredByVendorName) {
        this.registeredByVendorName = registeredByVendorName;
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

    public boolean isVehicleAssignedToRoute() {
        return isVehicleAssignedToRoute;
    }

    public void setVehicleAssignedToRoute(boolean vehicleAssignedToRoute) {
        isVehicleAssignedToRoute = vehicleAssignedToRoute;
    }
}
