package com.faiteasytrack.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class OTPReceiver extends BroadcastReceiver {

    private static final String TAG = "OTPReceiver";

    private static SMSReceivedListener smsReceivedListener;

    public interface SMSReceivedListener {
        void onOTPReceived(String otp);
    }

    public static void bindListener(SMSReceivedListener otpReceivedListener) {
        smsReceivedListener = otpReceivedListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey("pdus")) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[0]);
            String sender = sms.getDisplayOriginatingAddress();
            if (sender.endsWith("59029410")) {
                String message = sms.getMessageBody();
                String otp = message.replaceAll("[^0-9]", "");
                if (smsReceivedListener != null)
                    smsReceivedListener.onOTPReceived(otp);
                Log.i(TAG, "onReceive: otp = " + otp);
            }
        }
    }
}
