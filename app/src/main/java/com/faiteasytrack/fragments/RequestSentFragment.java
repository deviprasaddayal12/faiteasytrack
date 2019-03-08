package com.faiteasytrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faiteasytrack.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RequestSentFragment extends BaseFragment implements View.OnClickListener {

    public static final String TAG = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_splash, container, false);
    }

    @Override
    public void initUI(View view) {

    }

    @Override
    public void setUpListeners(View view) {

    }

    @Override
    public void setUpData(View view) {

    }

    @Override
    public void setUpRecycler(View view) {

    }

    @Override
    public void onClick(View v) {

    }
}
