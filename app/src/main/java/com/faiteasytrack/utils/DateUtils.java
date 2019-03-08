package com.faiteasytrack.utils;

import java.util.Date;

public class DateUtils {

    public static final String TAG = "DateUtils";

    public static String createDriverId(){
        long millisNow = new Date().getTime();

        return String.format("DRI%s", millisNow);
    }

    public static String createRouteId(){
        long millisNow = new Date().getTime();

        return String.format("RUT%s", millisNow);
    }

    public static String createVehicleId(){
        long millisNow = new Date().getTime();

        return String.format("VEH%s", millisNow);
    }
}
