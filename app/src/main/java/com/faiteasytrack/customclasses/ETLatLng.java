package com.faiteasytrack.customclasses;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class ETLatLng implements Parcelable {

    private double latitude;
    private double longitude;
    private long millis;
    private float speed;
    private float accuracy;

    public ETLatLng() {

    }

    public ETLatLng(double latitude, double longitude, long millis, float speed, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.millis = millis;
        this.speed = speed;
        this.accuracy = accuracy;
    }

    protected ETLatLng(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        millis = in.readLong();
        speed = in.readFloat();
        accuracy = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeLong(millis);
        dest.writeFloat(speed);
        dest.writeFloat(accuracy);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ETLatLng> CREATOR = new Creator<ETLatLng>() {
        @Override
        public ETLatLng createFromParcel(Parcel in) {
            return new ETLatLng(in);
        }

        @Override
        public ETLatLng[] newArray(int size) {
            return new ETLatLng[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLatLng(){
        return new LatLng(this.latitude, this.longitude);
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
