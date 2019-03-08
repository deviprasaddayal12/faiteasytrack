package com.faiteasytrack.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.faiteasytrack.R;

public class ViewUtils {

    public static void showViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.VISIBLE);
        }
    }

    public static void hideViews(View... views) {
        for (View v : views) {
            v.setVisibility(View.GONE);
        }
    }

    public static void enableViews(View... views) {
        for (View v : views) {
            v.setEnabled(true);
        }
    }

    public static void disableViews(View... views) {
        for (View v : views) {
            v.setEnabled(false);
        }
    }

    public static void makeToast(Context context, String message) {
        makeToast(context, message, Toast.LENGTH_SHORT);
    }

    public static void makeToast(Context context, String message, int duration) {
        Toast toast = new Toast(context.getApplicationContext());
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.view_toast, null);
        ((TextView) view.findViewById(R.id.tv_toast)).setText(message);
        toast.setView(view);
        toast.setDuration(duration);
        int margin = context.getResources().getDimensionPixelSize(R.dimen.margin_96);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL, 0, margin);
        toast.show();
    }
}
