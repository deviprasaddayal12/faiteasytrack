package com.faiteasytrack.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.firebase.FirebaseKeys;
import com.faiteasytrack.firebase.FirebaseUtils;
import com.faiteasytrack.fragments.IAmAccountsFragment;
import com.faiteasytrack.fragments.IAmReferralsFragment;
import com.faiteasytrack.fragments.IAmTypeFragment;
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.SharePreferences;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class IAmActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = IAmActivity.class.getCanonicalName();

    private ContentLoadingProgressBar pbLoader;
    private TextView tvName;
    private MaterialButton btnContinueAsGuest;

    private FragmentManager fragmentManager;
    private IAmTypeFragment typeFragment;
    private IAmAccountsFragment accountsFragment;
    private IAmReferralsFragment referralsFragment;

    private boolean isBusy = false;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference;

    private boolean userExists;
    private ArrayList<UserModel> userModels;

    private Query queryUserAccounts;
    private ValueEventListener listenerUserAccountsQuery = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "listenerUserAccountsQuery.onDataChange: " + dataSnapshot);

            userExists = dataSnapshot.exists();
            if (userExists) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    userModels.add(userModel);
                }
            }

            onUserChecked();
            queryUserAccounts.removeEventListener(listenerUserAccountsQuery);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "listenerUserAccountsQuery.onCancelled: " + databaseError.getMessage());

            hideProgressDialog();
            DialogUtils.showSorryAlert(IAmActivity.this, "" + databaseError.getMessage(), null);

            queryUserAccounts.removeEventListener(listenerUserAccountsQuery);
        }
    };

    private ValueEventListener addGuestEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "addGuestEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            DialogUtils.showCheersAlert(IAmActivity.this, "Your information was saved successfully.",
                    new Runnable() {
                        @Override
                        public void run() {
                            gotoDashboard();
                        }
                    });

            userReference.removeEventListener(addGuestEventListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "addGuestEventListener.onCancelled: " + databaseError.getMessage());

            userReference.removeEventListener(addGuestEventListener);
        }
    };

    private Runnable cancelRunnable = new Runnable() {
        @Override
        public void run() {
            onBackPressed();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseUtils.getUser();
        userReference = FirebaseUtils.getUserReference();

        setContentView(R.layout.activity_i_am);
    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void initUI() {
        pbLoader = findViewById(R.id.pb_loader);
        pbLoader.hide();

        btnContinueAsGuest = findViewById(R.id.btn_continue_as_guest);
        tvName = findViewById(R.id.tv_name);

        setUpFragments();
        checkUserExists();
    }

    private void setUpFragments() {
        fragmentManager = getSupportFragmentManager();

        typeFragment = new IAmTypeFragment();
        accountsFragment = new IAmAccountsFragment();
        referralsFragment = new IAmReferralsFragment();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fl_fragment_container, typeFragment, IAmTypeFragment.TAG);
        transaction.commit();
    }

    private void checkUserExists() {
        if (isBusy) return;

        showProgressDialog();
        userModels = new ArrayList<>();

        queryUserAccounts = userReference.orderByChild(FirebaseKeys.PHONE_NUMBER)
                .equalTo(firebaseUser.getPhoneNumber());
        queryUserAccounts.addValueEventListener(listenerUserAccountsQuery);
    }

    private void onUserChecked() {
        // todo switch to proper mode of as a guest
        btnContinueAsGuest.setEnabled(/*!userExists*/ true);
    }

    @Override
    public void setUpListeners() {
        btnContinueAsGuest.setOnClickListener(this);
    }

    @Override
    public void setUpData() {
        tvName.setText(String.format("Hello %s!", firebaseUser.getDisplayName().split(" ")[0]));
    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_continue_as_guest:{
                // todo add new guest user to firebase
                SharePreferences.saveUserModel(this, FirebaseUtils.getNewGuestModel());
                gotoDashboard();
            }
            break;
        }
    }

    public void switchFragments(boolean toLeft){
        Fragment fragment = toLeft ? accountsFragment : referralsFragment;
        String TAG = toLeft ? IAmAccountsFragment.TAG : IAmReferralsFragment.TAG;

        int pushEnter = toLeft ? R.anim.enter_from_left : R.anim.enter_from_right;
        int pushExit = toLeft ? R.anim.exit_to_right : R.anim.exit_to_left;
        int popEnter = toLeft ? R.anim.enter_from_right : R.anim.enter_from_left;
        int popExit = toLeft ? R.anim.exit_to_left : R.anim.exit_to_right;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(pushEnter, pushExit, popEnter, popExit);
        transaction.replace(R.id.fl_fragment_container, fragment);
        transaction.addToBackStack(TAG);
        transaction.commit();
    }

    private void gotoDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void showProgressDialog() {
        if (!pbLoader.isShown())
            pbLoader.show();

        isBusy = true;
    }

    private void hideProgressDialog() {
        if (pbLoader.isShown())
            pbLoader.hide();

        isBusy = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateInternetStatus(boolean online) {

    }
}
