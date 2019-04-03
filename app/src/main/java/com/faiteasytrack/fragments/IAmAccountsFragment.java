package com.faiteasytrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faiteasytrack.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IAmAccountsFragment extends BaseFragment {

    public static final String TAG = IAmAccountsFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_i_am_accounts, container, false);
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
}
