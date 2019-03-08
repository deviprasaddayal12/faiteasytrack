package com.faiteasytrack.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.faiteasytrack.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EasytrackProgressButton extends FrameLayout {

    public interface OnClickListener {
        void onProcessStart(Runnable runnableStart);

        void onProcessSuccess(Runnable runnableSuccess);

        void onProcessFailed(Runnable runnableFailure);
    }

    private View viewRoot;
    private ProgressBar progressBar;
    private TextView textView;

    private String text;
    private float textSize;

    public EasytrackProgressButton(@NonNull Context context) {
        super(context);

        init(context);
    }

    public EasytrackProgressButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
        styleProgressButton(context, attrs);
    }

    private void init(@NonNull Context context) {
        viewRoot = inflate(context, R.layout.view_progress_button, this);
        textView = viewRoot.findViewById(R.id.textView);
        progressBar = viewRoot.findViewById(R.id.progressBar);

        setBackgroundResource(R.drawable.background_fill_easytrack_button);
        hide(progressBar);
    }

    private void styleProgressButton(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EasytrackProgressButton);

        text = typedArray.getString(R.styleable.EasytrackProgressButton_android_text);
        textSize = typedArray.getDimension(R.styleable.EasytrackProgressButton_android_textSize, 14);

        textView.setTextSize((int) textSize, TypedValue.COMPLEX_UNIT_SP);
        textView.setText(text);

        typedArray.recycle();
    }

    private void hide(View... views) {
        for (View view : views)
            view.setVisibility(GONE);
    }

    private void show(View... views) {
        for (View view : views)
            view.setVisibility(VISIBLE);
    }
}
