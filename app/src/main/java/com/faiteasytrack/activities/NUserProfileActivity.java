package com.faiteasytrack.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.Toast;

import com.faiteasytrack.BuildConfig;
import com.faiteasytrack.R;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.DateUtils;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.FileUtils;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

public class NUserProfileActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NUserProfileActivity";
    public static final int CALLED_FROM_PHONE_AUTH = 0;
    public static final int CALLED_FROM_NAVIGATION = 1;

    public static final int REQUEST_FOR_CAMERA = 201, REQUEST_FOR_GALLERY = 202, REQUEST_FOR_FILE_BROWSER = 203;

    private FloatingActionButton fabUploadPhoto;
    private TextInputEditText etUserName, etUserEmail, etUserPhone;

    private View loader;
    private MaterialButton btnEdit, btnSkip;

    private String name, email;
    private FirebaseUser firebaseUser;

    private boolean isCalledFromAuth;

    private Uri photoURI;
    private String imagePathForProfilePic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        isCalledFromAuth = getIntent().getIntExtra("CALLED_FROM",
                CALLED_FROM_NAVIGATION) == CALLED_FROM_PHONE_AUTH;
        setContentView(R.layout.activity_user_profile);
    }

    @Override
    public void setUpActionBar() {
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
    }

    @Override
    public void initUI() {
        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        loader = viewStubLoader.inflate();
        loader.setOnClickListener(this);

        ViewUtils.hideViews(loader);

        etUserName = findViewById(R.id.et_user_name);
        etUserEmail = findViewById(R.id.et_user_email);
        etUserPhone = findViewById(R.id.et_user_phone);

        fabUploadPhoto = findViewById(R.id.fab_upload_photo);

        btnEdit = findViewById(R.id.btn_edit);
        btnSkip = findViewById(R.id.btn_skip);

        if (!isCalledFromAuth)
            ViewUtils.hideViews(btnSkip);
        fabUploadPhoto.hide();
    }

    @Override
    public void setUpListeners() {
        btnEdit.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        fabUploadPhoto.setOnClickListener(this);
    }

    @Override
    public void setUpData() {
        if (firebaseUser.getPhoneNumber() != null)
            etUserPhone.setText(String.format(Locale.getDefault(), "%s", firebaseUser.getPhoneNumber()));
        if (firebaseUser.getDisplayName() != null)
            etUserName.setText(String.format(Locale.getDefault(), "%s", firebaseUser.getDisplayName()));
        if (firebaseUser.getEmail() != null)
            etUserEmail.setText(String.format(Locale.getDefault(), "%s", firebaseUser.getEmail()));
    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_edit) {
            etUserName.requestFocus();
            fabUploadPhoto.show();

        } else if (v.getId() == R.id.btn_skip) {
            startActivity(new Intent(this, NIAmActivity.class));
            finish();

        } else if (v.getId() == R.id.fab_upload_photo) {
            showFileSourcePickerDialog();
        }
    }

    private AlertDialog fileSourceChooserDialog;

    private void showFileSourcePickerDialog() {
        String[] sourceList = {"Camera", "Gallery", "File Manager", "Cancel"};
        fileSourceChooserDialog = DialogUtils.showFileSourceChooserDialog(this, sourceList,
                new DialogUtils.onListDialogClickListener() {
                    @Override
                    public void onItemSelected(int position, String itemTitle) {
                        fileSourceChooserDialog.dismiss();
                        switch (position) {
                            case 0:
                                requestCamera();
                                break;
                            case 1:
                                requestGallery();
                                break;
                            case 2:
                                requestFileBrowser();
                                break;
                            default:
                                break;
                        }
                    }
                });
        fileSourceChooserDialog.show();
    }

    private void requestCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (AppPermissions.checkCameraPermission(this))
                startCamera();
        } else
            startCamera();
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_FOR_CAMERA);
            }
        } else {
            Toast.makeText(this, "No application found to perform the action.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (AppPermissions.checkCameraPermission(this))
                startGallery();
        } else
            startGallery();
    }

    private void startGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_FOR_GALLERY);
        } else {
            Toast.makeText(this, "No application found to perform the action.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestFileBrowser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (AppPermissions.checkCameraPermission(this))
                startFileBrowser();
        } else
            startFileBrowser();
    }

    private void startFileBrowser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_FOR_FILE_BROWSER);
    }

    private File createImageFile() throws IOException {
        File storageFolder = getExternalFilesDir(Environment.DIRECTORY_DCIM);

        String name = firebaseUser.getDisplayName().split(" ")[0];
        String timeStamp = DateUtils.getImageTimeStamp();
        String prefix = String.format("%s_%s", name, timeStamp);

        File image = File.createTempFile(prefix, ".jpg", storageFolder);

        imagePathForProfilePic = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_FOR_FILE_BROWSER) {
            startFileBrowser();
        } else if (requestCode == REQUEST_FOR_GALLERY) {
            startGallery();
        } else if (requestCode == REQUEST_FOR_CAMERA) {
            startCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_FOR_CAMERA)
                setUpImageToImageView();

            else if (requestCode == REQUEST_FOR_GALLERY)
                attachFile(data);

            else if (requestCode == REQUEST_FOR_FILE_BROWSER)
                attachFile(data);
        }
    }

    private void attachFile(Intent data) {
        try {
            if (data.getData() != null) {
                String imagePath = FileUtils.getInternalStoragePath(this, data.getData());
                if (imagePath == null) {
                    if (FileUtils.isSDCardAvailable(this)) {
                        imagePath = FileUtils.getExternalStoragePath(this, data.getData());
                    }
                    if (imagePath == null) {
                        DialogUtils.showSorryAlert(this, "Unable to fetch file from SDCard." +
                                "\nMove the file into Device Memory and retry.", null);
                        return;
                    }
                }
                if (!imagePath.equals(imagePathForProfilePic)) {
                    setUpImageToImageView();

                } else {
                    Toast.makeText(this, "File already chosen.", Toast.LENGTH_SHORT).show();
                }
            }
//            else if (data.getClipData() != null) {
//                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
//                    String imagePath = FileUtils.getInternalStoragePath(this, data.getClipData().getItemAt(i).getUri());
//                    if (imagePath == null) {
//                        DialogUtils.showSorryAlert(this, "Unable to fetch file from SDCard." +
//                                "\nMove the file into Device Memory and retry.", null);
//                        return;
//                    }
//                    if (!imagePath.equals(imagePathForProfilePic)) {
//                        setUpImageToImageView();
//
//                    }
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpImageToImageView(){
//        ivImageView.setImageBitmap(BitmapFactory.decodeFile(
//                (FileUtils.getCompressedFile(this, imagePathForProfilePic)).getAbsolutePath()));
    }

    private boolean isNameUpdated = false, isEmailUpdated = false;

//    private void updateUserProfile() {
//        updateName();
//        updateEmail();
//        updateProfilePic();
//    }

    private void updateName() {
        if (/*!Pattern.compile("[a-zA-Z]").matcher(name).matches()*/ name.isEmpty()) {
            etUserName.setError("Invalid name!");
            etUserName.requestFocus();
            return;
        }
        if (firebaseUser.getDisplayName() != null && name.equals(firebaseUser.getDisplayName()))
            return;

        ViewUtils.showViews(loader);

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        builder.setDisplayName(name);

        firebaseUser.updateProfile(builder.build()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ViewUtils.hideViews(loader);
                ViewUtils.makeToast(NUserProfileActivity.this, "Your name updated successfully.");

                isNameUpdated = true;

                if (isEmailUpdated)
                    gotoDashboard();
            }
        });
    }

    private void gotoDashboard() {
        if (getIntent().getIntExtra("CALLED_FROM", CALLED_FROM_NAVIGATION) == CALLED_FROM_PHONE_AUTH)
            startActivity(new Intent(this, NDashboardActivity.class));
        else
            onBackPressed();
    }

    private void updateEmail() {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etUserEmail.setError("Invalid email!");
            etUserEmail.requestFocus();
            return;
        }
        if (firebaseUser.getEmail() != null && email.equals(firebaseUser.getEmail()))
            return;

        ViewUtils.showViews(loader);

        firebaseUser.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ViewUtils.hideViews(loader);
                ViewUtils.makeToast(NUserProfileActivity.this, "Your email updated successfully.");

                isEmailUpdated = true;

                if (isNameUpdated)
                    gotoDashboard();
            }
        });
    }

    private void updateProfilePic() {
        ViewUtils.showViews(loader);

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        Uri photoUri = new Uri.Builder().build();
        builder.setPhotoUri(photoUri);

        firebaseUser.updateProfile(builder.build()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                ViewUtils.hideViews(loader);
                ViewUtils.makeToast(NUserProfileActivity.this, "Your profile photo updated successfully.");
            }
        });
    }

    @Override
    public void updateInternetError(boolean isOnline) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
