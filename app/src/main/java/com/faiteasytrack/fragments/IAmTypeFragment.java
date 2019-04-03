package com.faiteasytrack.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faiteasytrack.R;
import com.faiteasytrack.activities.IAmActivity;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IAmTypeFragment extends BaseFragment implements View.OnClickListener {

    public static final String TAG = IAmTypeFragment.class.getSimpleName();

    private MaterialButton btnLook4Accounts, btnLook4Referrals;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_i_am_type, container, false);
    }

    @Override
    public void initUI(View view) {
        btnLook4Accounts = view.findViewById(R.id.btn_look_for_accounts);
        btnLook4Referrals = view.findViewById(R.id.btn_look_for_referrals);
    }

    @Override
    public void setUpListeners(View view) {
        btnLook4Accounts.setOnClickListener(this);
        btnLook4Referrals.setOnClickListener(this);
    }

    @Override
    public void setUpData(View view) {

    }

    @Override
    public void setUpRecycler(View view) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_look_for_accounts:{
                if (getActivity() != null)
                    ((IAmActivity) getActivity()).switchFragments(true);
            }
            break;
            case R.id.btn_look_for_referrals:{
                if (getActivity() != null)
                    ((IAmActivity) getActivity()).switchFragments(false);
            }
            break;
        }
    }
}
