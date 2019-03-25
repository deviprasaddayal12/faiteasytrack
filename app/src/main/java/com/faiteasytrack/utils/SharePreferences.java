package com.faiteasytrack.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.faiteasytrack.constants.Preferences;
import com.faiteasytrack.exceptions.UserModelNotFound;
import com.faiteasytrack.models.ContactModel;
import com.faiteasytrack.models.PreferenceModel;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.models.TripModel;
import com.faiteasytrack.models.ProfileModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SharePreferences {

    public static final String TAG = "SharePreferences";

    public interface KEYS {
        String EASY_TRACK_PREFS = "easy_track_prefs";
        String UID = "uid";

        String IS_FIRST_LAUNCH = "is_first_launch";
        String IS_USER_LOGGED_IN = "is_user_logged_in";
        String IS_TRACKING_ONGOING = "is_tracking_ongoing";
        String IS_TRACING_ONGOING = "is_tracing_ongoing";
        String HAS_SAVED_APP_CONTACTS = "has_saved_app_contacts";
        String APP_WAS_IN_BACKGROUND = "app_was_in_background";

        String REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";

        String ONGOING_TRIP_FINISHED_IN_BACKGROUND = "ongoing_trip_finished_in_background";
        String FINISHED_TRIP_MODEL_FROM_BACKGROUND = "finished_trip_model_from_background";
        String KEY_FOR_LAST_UNFINISHED_TRIP = "key_for_last_unfinished_trip";

        String UNFINISHED_TRIP_MODEL = "unfinished_trip_model";
        String APP_CONTACT_MODELS = "app_contact_models";

        String USER_MODEL = "user_model";
        String PROFILE_MODEL = "profile_model";
        String PREFERENCE_MODEL = "preference_model";
    }

    public interface DefValues {
        String STRING_NOT_FOUND = "Value not found.";
        int INT_NOT_FOUND = -1;
        boolean BOOLEAN_NOT_FOUND = false;
    }

    private static void saveString(Context context, String key, String value) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putString(key, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveBoolean(Context context, String key, boolean value) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putBoolean(key, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveInt(Context context, String key, int value) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putInt(key, value).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getString(Context context, String key) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPreferences.getString(key, DefValues.STRING_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static int getInt(Context context, String key) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPreferences.getInt(key, DefValues.INT_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return DefValues.INT_NOT_FOUND;
        }
    }

    private static boolean getBoolean(Context context, String key) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            return sharedPreferences.getBoolean(key, DefValues.BOOLEAN_NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return DefValues.BOOLEAN_NOT_FOUND;
        }
    }

    private static void removeValue(Context context, String key) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().remove(key).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void userLoggedOut(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
    }

    public static boolean isFirstLaunch(Context context) {
        // default boolean is false, hence ! is used
        return !getBoolean(context, SharePreferences.KEYS.IS_FIRST_LAUNCH);
    }

    public static void setLaunchedAlready(Context context) {
        saveBoolean(context, SharePreferences.KEYS.IS_FIRST_LAUNCH, true);
    }

    public static boolean isUserLoggedIn(Context context){
        return getBoolean(context, KEYS.IS_USER_LOGGED_IN) && getUserModel(context) != null;
    }

    /**
     * Saves current user details in ProfileModel.
     * @param context Context to this callback.
     * @param profileModel The model to be saved.
     */
    public static void saveProfileModel(Context context, ProfileModel profileModel) {
        Type type = new TypeToken<ProfileModel>() {}.getType();
        saveString(context, KEYS.PROFILE_MODEL, new Gson().toJson(profileModel, type));
    }

    /**
     * Returns the saved ProfileModel.
     * @param context Context to this callback.
     * @return The saved ProfileModel.
     * @throws UserModelNotFound If exception occurs, then throws UserModelNotFound exception.
     */
    public static ProfileModel getProfileModel(Context context) throws UserModelNotFound {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<ProfileModel>() {}.getType();
            return gson.fromJson(getString(context, KEYS.PROFILE_MODEL), type);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UserModelNotFound("User model not found. Login required.");
        }
    }

    /**
     * Removes current user model, if saved.
     * @param context Context to this callback.
     * @throws UserModelNotFound If exception occurs, then throws UserModelNotFound exception.
     */
    public static void removeProfileModel(Context context) throws UserModelNotFound {
        removeValue(context, KEYS.PROFILE_MODEL);
    }

    /**
     * Returns true if requesting location updates, otherwise returns false.
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return getBoolean(context, KEYS.REQUESTING_LOCATION_UPDATES);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        saveBoolean(context, KEYS.REQUESTING_LOCATION_UPDATES, requestingLocationUpdates);
    }

    /**
     * Stores the boolean indicating current state of application.
     * @param context         Context of call to this callback.
     * @param wasInBackground The application's state.
     */
    public static void setAppWasInBackground(Context context, boolean wasInBackground) {
        saveBoolean(context, KEYS.APP_WAS_IN_BACKGROUND, wasInBackground);
    }

    /**
     * Returns true, if application's previous state was in background, previously.
     * @param context Context of call to this callback.
     * @return Last state of application.
     */
    public static boolean appWasInBackground(Context context) {
        return getBoolean(context, KEYS.APP_WAS_IN_BACKGROUND);
    }

    // Following two callbacks are used when application is brought to foreground .

    /**
     * Returns true, if an active trip exists.
     * @param context Context to this callback.
     * @return The boolean indicating state of ongoing trip.
     */
    public static boolean isTracingOngoing(Context context) {
        return getBoolean(context, KEYS.IS_TRACING_ONGOING);
    }

    /**
     * This stores a boolean indicating whether a trip is ongoing.
     * @param context             Context to this callback.
     * @param existsAnOngoingTrip boolean storing the value.
     */
    public static void setOngoingTripExists(Context context, boolean existsAnOngoingTrip) {
        saveBoolean(context, KEYS.IS_TRACING_ONGOING, existsAnOngoingTrip);
    }

    /**
     * This stores a boolean indicating an ongoing trip was finished while app was in background, or killed;
     * @param context Context to this callback.
     * @param finishedInBackground boolean storing the value.
     */
    public static void setOngoingTripFinishedWhileInBackground(Context context, boolean finishedInBackground) {
        saveBoolean(context, KEYS.ONGOING_TRIP_FINISHED_IN_BACKGROUND, finishedInBackground);
    }

    /**
     * Returns true, if an active trip was finished while application was in background or killed.
     * @param context Context to this callback.
     * @return The boolean indicating state of ongoing trip.
     */
    public static boolean ongoingTripFinishedWhileInBackground(Context context) {
        return getBoolean(context, KEYS.ONGOING_TRIP_FINISHED_IN_BACKGROUND);
    }

    /**
     * This returns the last unfinished tripModel, if exists, when user renters the foreground,
     * from the ServiceNotification or relaunches app.
     * @param context Context of this callback, ODashboardActivity, most probably.
     * @return The unfinished trip model.
     */
    public static TripModel getLastUnfinishedTripModel(Context context) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<TripModel>() {}.getType();
            return gson.fromJson(getString(context, KEYS.UNFINISHED_TRIP_MODEL), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This callback is invoked to removeChildListeners the unfinished trip model, if exists, upon successful, finishing.
     * @param context Context of this callback.
     */
    public static void removeLastUnfinishedTripModel(Context context) {
        try {
            removeValue(context, KEYS.UNFINISHED_TRIP_MODEL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This callback saves any trip, to ensure the trip details to be saved, for any unpredictable exceptions.
     * @param context   Context of this callback.
     * @param tripModel The trip Model to be saved.
     */
    public static void saveLastUnfinishedTripModel(Context context, TripModel tripModel) {
        try {
            Type type = new TypeToken<TripModel>() {}.getType();
            saveString(context, KEYS.UNFINISHED_TRIP_MODEL, new Gson().toJson(tripModel, type));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the key for last unfinished trip.
     * @param context Context of this callback.
     * @return Returns the key, if exists.
     */
    public static String getKeyForLastUnfinishedTrip(Context context) {
        return getString(context, KEYS.KEY_FOR_LAST_UNFINISHED_TRIP);
    }

    /**
     * Saves the key for ongoing trip for further use.
     * @param context Context of this callback.
     * @param key     The key for ongoing trip.
     */
    public static void saveKeyForLastUnfinishedTrip(Context context, String key) {
        saveString(context, KEYS.KEY_FOR_LAST_UNFINISHED_TRIP, key);
    }

    /**
     * Removes key for last trip upon successful trip completion, if exists any.
     * @param context Context to this callback.
     */
    public static void removeKeyForLastUnfinishedTrip(Context context) {
        removeValue(context, KEYS.KEY_FOR_LAST_UNFINISHED_TRIP);
    }

    /**
     * This returns the tripModel, finished from background, if exists, to notify user..
     * @param context Context of this callback, ODashboardActivity, most probably.
     * @return The unfinished trip model.
     */
    public static TripModel getTripModelFinishedFromBackground(Context context) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<TripModel>() {}.getType();
            return gson.fromJson(getString(context, KEYS.FINISHED_TRIP_MODEL_FROM_BACKGROUND), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This callback is invoked to removeChildListeners the trip model, finished from background, if exists.
     * @param context Context of this callback.
     */
    public static void removeTripModelFinishedFromBackground(Context context) {
        try {
            removeValue(context, KEYS.FINISHED_TRIP_MODEL_FROM_BACKGROUND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This callback saves any trip, which was finished from background, for any unpredictable exceptions.
     * @param context   Context of this callback.
     * @param tripModel The trip Model to be saved.
     */
    public static void saveTripModelFinishedFromBackground(Context context, TripModel tripModel) {
        try {
            Type type = new TypeToken<TripModel>() {}.getType();
            saveString(context, KEYS.FINISHED_TRIP_MODEL_FROM_BACKGROUND, new Gson().toJson(tripModel, type));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static UserModel getUserModel(Context context){
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<UserModel>() {}.getType();
            return gson.fromJson(getString(context, KEYS.USER_MODEL), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveUserModel(Context context, UserModel userModel) {
        try {
            Type type = new TypeToken<UserModel>() {}.getType();
            String value = new Gson().toJson(userModel, type);
            Log.i(TAG, "saveUserModel: " + value);
            saveString(context, KEYS.USER_MODEL, value);
            saveBoolean(context, KEYS.IS_USER_LOGGED_IN, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeUserModel(Context context){
        removeValue(context, KEYS.USER_MODEL);
        saveBoolean(context, KEYS.IS_USER_LOGGED_IN, false);
    }

    public static PreferenceModel getPreferenceModel(Context context){
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<PreferenceModel>() {}.getType();
            return gson.fromJson(getString(context, KEYS.PREFERENCE_MODEL), type);
        } catch (Exception e) {
            e.printStackTrace();
            return new PreferenceModel(false, Preferences.ShareLocation.TO_ANYONE,
                    true, Preferences.Storage.CLOUD);
        }
    }

    public static void savePreferenceModel(Context context, PreferenceModel preferenceModel){
        try {
            Type type = new TypeToken<PreferenceModel>() {}.getType();
            saveString(context, KEYS.PREFERENCE_MODEL, new Gson().toJson(preferenceModel, type));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removePreferenceModel(Context context){
        removeValue(context, KEYS.PREFERENCE_MODEL);
    }

    public static boolean isTrackingOngoing(Context context){
        return getBoolean(context, KEYS.IS_TRACKING_ONGOING);
    }

    public static void setTrackingOngoing(Context context, boolean tracking){
        saveBoolean(context, KEYS.IS_TRACKING_ONGOING, tracking);
    }

    public static void removeTrackingOngoing(Context context){
        removeValue(context, KEYS.IS_TRACKING_ONGOING);
    }

    public static String getUid(Context context){
        return getString(context, KEYS.UID);
    }

    public static void saveUid(Context context, String uid){
        Log.e(TAG, "saveUid:" + uid);
        saveString(context, KEYS.UID, uid);
    }

    public static void saveAppContacts(Context context, ArrayList<ContactModel> contactModels) {
        try {
            removeAppContacts(context);
            Type type = new TypeToken<ArrayList<ContactModel>>() {}.getType();
            saveString(context, KEYS.APP_CONTACT_MODELS, new Gson().toJson(contactModels, type));
            saveBoolean(context, KEYS.HAS_SAVED_APP_CONTACTS, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<ContactModel> getAppContacts(Context context){
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<ContactModel>>() {}.getType();
            return gson.fromJson(getString(context, KEYS.APP_CONTACT_MODELS), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void removeAppContacts(Context context){
        removeValue(context, KEYS.APP_CONTACT_MODELS);
    }

    public static boolean hasSavedAppContacts(Context context){
        return getBoolean(context, KEYS.HAS_SAVED_APP_CONTACTS);
    }
}
