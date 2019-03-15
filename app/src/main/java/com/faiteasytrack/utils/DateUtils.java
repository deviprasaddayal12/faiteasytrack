package com.faiteasytrack.utils;

import com.faiteasytrack.enums.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public static String generateCode(int type){
        switch (type){
            case User.TYPE_ADMIN:
                return String.format(Locale.getDefault(), "%d", User.TYPE_ADMIN);
                default:
                    return "";
        }
    }

    public static String getDateInString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        return simpleDateFormat.format(date);
    }

    public static String getTimeInString(Date date) {
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());

        return simpleTimeFormat.format(date);
    }

    public static String getNowDate(){
        return getDateInString(new Date());
    }

    public static String getNowTime(){
        return getTimeInString(new Date());
    }

    public static long getNowInLong(){
        return new Date().getTime();
    }

    public static String getImageTimeStamp() {
        String date = getNowDate();
        date = date.replace("/", "");
        String time = getNowTime();
        time = time.replace(":", "");

        return String.format("%s_%s", date, time);
    }
}
