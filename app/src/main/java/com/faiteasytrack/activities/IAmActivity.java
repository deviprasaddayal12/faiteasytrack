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
import com.faiteasytrack.models.UserModel;
import com.faiteasytrack.utils.DialogUtils;
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

public class IAmActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = IAmActivity.class.getSimpleName();

    private ContentLoadingProgressBar pbLoader;
    private boolean isBusy = false;

    private TextView tvName;

    private FirebaseUser firebaseUser;
    private DatabaseReference userReference;

    private boolean isUserRegistered;
    private ArrayList<UserModel> userModels;

    private Query queryUserAccounts;
    private ValueEventListener listenerUserAccountsQuery = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "listenerUserAccountsQuery.onDataChange: " + dataSnapshot);

            isUserRegistered = dataSnapshot.exists();
            if (isUserRegistered) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    userModels.add(userModel);
                }
                userExists();
            } else {
                userDoesNotExist();
            }

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
        checkUserExists();
    }

    @Override
    public void setUpActionBar() {

    }

    private void checkUserExists() {
        if (isBusy) return;

        showProgressDialog();
        userModels = new ArrayList<>();

        queryUserAccounts = userReference.orderByChild(FirebaseKeys.PHONE_NUMBER)
                .equalTo(firebaseUser.getPhoneNumber());
        queryUserAccounts.addValueEventListener(listenerUserAccountsQuery);
    }

    private void userExists() {

    }

    @Override
    public void initUI() {
        pbLoader = findViewById(R.id.pb_loader);
        pbLoader.hide();

        tvName = findViewById(R.id.tv_name);
    }

    @Override
    public void setUpListeners() {

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

    }

    private ValueEventListener addNewUserEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Log.i(TAG, "addNewUserEventListener.onDataChange: " + dataSnapshot);

            hideProgressDialog();
            DialogUtils.showCheersAlert(IAmActivity.this, "Your information was saved successfully.",
                    new Runnable() {
                        @Override
                        public void run() {
                            gotoDashboard();
                        }
                    });

            userReference.removeEventListener(addNewUserEventListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "addNewUserEventListener.onCancelled: " + databaseError.getMessage());

            userReference.removeEventListener(addNewUserEventListener);
        }
    };

    private void userDoesNotExist(){

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

    public interface OnCredentialsVerificationCompleteListener{
        void onCredentialsVerified(boolean isSuccess);
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
