package com.faiteasytrack.utils;

import com.faiteasytrack.models.ContactModel;
import com.faiteasytrack.models.RequestStatusModel;

import java.util.ArrayList;

public class Constants {
    public static final String TAG = Constants.class.getCanonicalName();

    public static final String CALLED_FROM = TAG + ".called_from";
    public static final String CALLED_FROM_WITH_ID = TAG + ".called_from_with_id";
    public static final String CALLED_FROM_WITH_NAME = TAG + ".called_from_with_name";

    public static final int MAX_LENGTH_NAME = 30;
    public static final int MAX_LENGTH_PHONE = 10;
    public static final int MAX_LENGTH_CODE = 10;
    public static final int MAX_LENGTH_PASSWORD = 10;

    public static class Intent {
        public static class Keys {
            public static final String EXTRA_FRIEND = "extra_friend";
        }

        public static class Codes {
            public static final int START_FRIENDS_ACTIVITY_FOR_TRACKING = 201;
            public static final int START_CONTACTS_TO_REQUEST_NEW = 202;
        }
    }

    public static class Database{
        public static class Keys {
            // root databases
            public static final String kADMINS = "admins";

            public static final String kVENDORS = "vendors";
            public static final String kDRIVERS = "drivers";
            public static final String kVEHICLES = "vehicles";

            public static final String kUSERS = "users";
            public static final String kUSER_PROFILES = "user_profiles";
            public static final String kFRIENDS = "friends";

            public static final String kPREFERENCES = "preferences";

            public static final String kTRIP_PROFILES = "trip_profiles";
            public static final String kTRIP_HISTORIES = "trip_histories";
            public static final String kTRIP_CURRENTS = "trip_currents";

            public static final String kREQUESTS = "requests";
            public static final String kNOTIFICATIONS = "notifications";
            public static final String kRESPONSES = "responses";

            // users/(push_key)/
            public static final String kAUTH_ID = "authorizationId";
            public static final String kREGISTER_AS = "register_as";
            public static final String kUSERNAME = "username";
            public static final String kPASSWORDS = "password";
            public static final String kPERMISSIONS = "permissions";
            public static final String kIS_USER_PROFILE_EXISTS = "isUserProfileExists";

            // profiles/(uid)/[](push_key)/
            public static final String kUPDATED_AT_MILLIS = "updated_at_millis";
            public static final String kPHONES = "phones";
            public static final String kEMAILS = "emails";
            public static final String kNAMES = "names";
            public static final String kALIASES = "aliases";
            public static final String kPROFILE_PICS = "profile_pics";

            // friends/(uid)/[](push_key)
            public static final String kFRIEND_ID = "friend_id";
            public static final String kF_SHIP_STATUS = "f_ship_status";
            public static final String kHAS_REQUESTED_TO_ME = "has_requested_to_me";
            public static final String kREQUEST_ID = "request_id";
            public static final String kBECAME_FRIEND_AT_MILLIS = "became_friends_at_millis";

            // requests(uid)/(push_key)[]
            public static final String kREQUEST_PUSH = "request_push";
            public static final String kREQUESTED_FROM_UID = "requested_from_uid";
            public static final String kREQUESTED_BY_NAME = "requested_by_name";
            public static final String kREQUESTED_AT_MILLIS = "requested_at_millis";

            // responses(uid)/(push_key)[]
            public static final String kRESPONSE_AT_MILLIS = "response_at_millis";
            public static final String kRES_TO_REQUEST_PUSH = "res_to_request_push";
            public static final String kIS_ACCEPTED = "is_accepted";
            public static final String kIS_BLOCKED = "is_blocked";

            //notification/(type...)
            public static final String kUNSEEN_NOTIFICATIONS = "unseen_notifications";
            public static final String kSEEN_NOTIFICATIONS = "seen_notifications";
            // notifications(uid)/type/(push_key)[]
            public static final String kNOTIFICATION_TYPE = "notification_type";
            public static final String kSEND = "send";
            public static final String kRECEIVED = "received";

            // preferences/(uid)/(push_key)
        }
    }

    public static class LOG {
        public static final int d = 0;
        public static final int e = 1;
        public static final int i = 2;
        public static final int v = 3;
        public static final int w = 4;
    }
}
