package com.faiteasytrack.views;

import android.content.Context;
import android.util.AttributeSet;

import com.faiteasytrack.R;

import androidx.appcompat.widget.AppCompatButton;

public class EasytrackButton extends AppCompatButton {
    public EasytrackButton(Context context) {
        super(context);
        setBackgroundResource(R.drawable.bg_easytrack_progress_button);
        setTextColor(getResources().getColor(R.color.colorWhite));
    }

    public EasytrackButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.bg_easytrack_progress_button);
        setTextColor(getResources().getColor(R.color.colorWhite));
    }

    public EasytrackButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(R.drawable.bg_easytrack_progress_button);
        setTextColor(getResources().getColor(R.color.colorWhite));
    }
}
