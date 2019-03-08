package com.faiteasytrack.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.faiteasytrack.classess.ETLatLng;

import java.util.List;

public class RouteModel implements Parcelable {

    private String routeId;
    private String routeName;
    private List<ETLatLng> wayPoints;
    private ETLatLng source;
    private ETLatLng destination;
    private String vehicleId;
    private String vehicleNumber;
    private String driverId;
    private String driverName;
    private String registeredByVendorUid;
    private String registeredByVendorName;
    private String registeredByVendorPhone;
    private boolean isRouteInRunning;

    public RouteModel() {
    }

    protected RouteModel(Parcel in) {
        routeId = in.readString();
        routeName = in.readString();
        wayPoints = in.createTypedArrayList(ETLatLng.CREATOR);
        source = in.readParcelable(ETLatLng.class.getClassLoader());
        destination = in.readParcelable(ETLatLng.class.getClassLoader());
        vehicleId = in.readString();
        vehicleNumber = in.readString();
        driverId = in.readString();
        driverName = in.readString();
        registeredByVendorUid = in.readString();
        registeredByVendorName = in.readString();
        registeredByVendorPhone = in.readString();
        isRouteInRunning = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(routeId);
        dest.writeString(routeName);
        dest.writeTypedList(wayPoints);
        dest.writeParcelable(source, flags);
        dest.writeParcelable(destination, flags);
        dest.writeString(vehicleId);
        dest.writeString(vehicleNumber);
        dest.writeString(driverId);
        dest.writeString(driverName);
        dest.writeString(registeredByVendorUid);
        dest.writeString(registeredByVendorName);
        dest.writeString(registeredByVendorPhone);
        dest.writeByte((byte) (isRouteInRunning ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RouteModel> CREATOR = new Creator<RouteModel>() {
        @Override
        public RouteModel createFromParcel(Parcel in) {
            return new RouteModel(in);
        }

        @Override
        public RouteModel[] newArray(int size) {
            return new RouteModel[size];
        }
    };

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public List<ETLatLng> getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(List<ETLatLng> wayPoints) {
        this.wayPoints = wayPoints;
    }

    public ETLatLng getSource() {
        return source;
    }

    public void setSource(ETLatLng source) {
        this.source = source;
    }

    public ETLatLng getDestination() {
        return destination;
    }

    public void setDestination(ETLatLng destination) {
        this.destination = destination;
    }

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

    public String getRegisteredByVendorName() {
        return registeredByVendorName;
    }

    public void setRegisteredByVendorName(String registeredByVendorName) {
        this.registeredByVendorName = registeredByVendorName;
    }

    public String getRegisteredByVendorPhone() {
        return registeredByVendorPhone;
    }

    public void setRegisteredByVendorPhone(String registeredByVendorPhone) {
        this.registeredByVendorPhone = registeredByVendorPhone;
    }

    public boolean isRouteInRunning() {
        return isRouteInRunning;
    }

    public void setRouteInRunning(boolean routeInRunning) {
        isRouteInRunning = routeInRunning;
    }
}
