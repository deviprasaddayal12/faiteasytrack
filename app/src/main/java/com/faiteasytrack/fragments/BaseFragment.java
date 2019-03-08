package com.faiteasytrack.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI(view);
        setUpListeners(view);
        setUpData(view);
        setUpRecycler(view);
    }

    public abstract void initUI(View view);

    public abstract void setUpListeners(View view);

    public abstract void setUpData(View view);

    public abstract void setUpRecycler(View view);
}
