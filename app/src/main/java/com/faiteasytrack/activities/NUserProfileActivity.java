package com.faiteasytrack.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.faiteasytrack.BuildConfig;
import com.faiteasytrack.R;
import com.faiteasytrack.constants.Preferences;
import com.faiteasytrack.models.PreferenceModel;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.DateUtils;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.FileUtils;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

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
    private EditText etUserName, etUserEmail, etUserPhone;
    private CircularImageView civProfilePic;

    private View loader;
    private MaterialButton btnSkip;

    private FirebaseUser firebaseUser;
    private StorageReference profilePhotosReference;

    private PreferenceModel preferenceModel;

    private boolean isCalledFromAuth, isUploadingPic = false, isUpdatingName = false, isUpdatingEmail = false;
    private boolean isProfileUpdated = false;

    private Uri photoURI;
    private String imagePathForProfilePic;

    private Handler handlerProfileViewLoad;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profilePhotosReference = FirebaseStorage.getInstance()
                .getReference("images/profilePhotos").child(firebaseUser.getUid());
        preferenceModel = SharePreferences.getPreferenceModel(this);

        isCalledFromAuth = getIntent().getIntExtra("CALLED_FROM",
                CALLED_FROM_NAVIGATION) == CALLED_FROM_PHONE_AUTH;

        handlerProfileViewLoad = new Handler();

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

        civProfilePic = findViewById(R.id.civ_profile_pic);

        etUserName = findViewById(R.id.et_name);
        etUserEmail = findViewById(R.id.et_email);
        etUserPhone = findViewById(R.id.et_phone);

        fabUploadPhoto = findViewById(R.id.fab_upload_photo);

        btnSkip = findViewById(R.id.btn_skip);

        if (!isCalledFromAuth)
            ViewUtils.hideViews(btnSkip);
    }

    @Override
    public void setUpListeners() {
        btnSkip.setOnClickListener(this);
        fabUploadPhoto.setOnClickListener(this);

        etUserName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String nameWritten = etUserName.getText().toString();
                if (!nameWritten.isEmpty()) {
                    if (!nameWritten.equals(firebaseUser.getDisplayName())) {
                        updateName(nameWritten);
                        return true;
                    }
                }
                return false;
            }
        });
        etUserEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String emailWritten = etUserEmail.getText().toString();
                if (!emailWritten.isEmpty()) {
                    if (!emailWritten.equals(firebaseUser.getEmail())) {
                        updateEmail(emailWritten);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void setUpData() {
        etUserName.setText(Utils.getValidString(firebaseUser.getDisplayName()));
        etUserEmail.setText(Utils.getValidString(firebaseUser.getEmail()));
        etUserPhone.setText(Utils.getValidString(firebaseUser.getPhoneNumber()));
        etUserPhone.setInputType(InputType.TYPE_NULL);

        setUpImageToImageView();
    }

    private void setUpImageToImageView() {
        if (preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.LOCAL) {
            try{
                civProfilePic.setImageURI(firebaseUser.getPhotoUrl());
            } catch (Exception e){
                civProfilePic.setImageResource(R.drawable.user_1);
            }

        } else if (preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.CLOUD) {
            try{
                Glide.with(this).load(profilePhotosReference)
                        .into(civProfilePic).onLoadFailed(getDrawable(R.drawable.user_1));
            } catch (Exception e){
                civProfilePic.setImageResource(R.drawable.user_1);
            }
        }
    }

    @Override
    public void setUpRecycler() {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_skip) {
            onBackPressed();

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
                photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID
                        + ".provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_FOR_CAMERA);
            }
        } else {
            Toast.makeText(this, "No application found to perform the action.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (AppPermissions.checkGalleryPermission(this))
                startGallery();
        } else
            startGallery();
    }

    private void startGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_FOR_GALLERY);
        } else {
            Toast.makeText(this, "No application found to perform the action.", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestFileBrowser() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (AppPermissions.checkFileBrowserPermission(this))
                startFileBrowser();
        } else
            startFileBrowser();
    }

    private void startFileBrowser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_FOR_FILE_BROWSER);
    }

    private File createImageFile() throws IOException {
        File storageFolder = getExternalFilesDir(Environment.DIRECTORY_DCIM);

        String name = "PROFILE_IMG_";
        String timeStamp = DateUtils.getImageTimeStamp();
        String prefix = String.format("%s_%s", name, timeStamp);

        File image = File.createTempFile(prefix, ".jpg", storageFolder);

        imagePathForProfilePic = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: ");

        if (grantResults.length > 0) {
            if (requestCode == REQUEST_FOR_FILE_BROWSER) {
                startFileBrowser();
            } else if (requestCode == REQUEST_FOR_GALLERY) {
                startGallery();
            } else if (requestCode == REQUEST_FOR_CAMERA) {
                startCamera();
            }
        } else
            ViewUtils.makeToast(this, "Please grant permissions to enjoy more features.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_FOR_CAMERA) {
                uploadProfilePhoto();
            } else if (requestCode == REQUEST_FOR_GALLERY) {
                attachFile(data);
            } else if (requestCode == REQUEST_FOR_FILE_BROWSER) {
                takePersistablePermissions(data);
                attachFile(data);
            }
        }
    }

    private void takePersistablePermissions(Intent intent) {
        final int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (intent.getData() != null)
            getContentResolver().takePersistableUriPermission(intent.getData(), takeFlags);
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
                    imagePathForProfilePic = imagePath;
                    photoURI = data.getData();
                    uploadProfilePhoto();

                } else {
                    Toast.makeText(this, "File already chosen.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateName(String name) {
        if (/*!Pattern.compile("[a-zA-Z]").matcher(name).matches()*/ name.isEmpty()) {
            etUserName.setError("Invalid name!");
            etUserName.requestFocus();
            return;
        }
        if (firebaseUser.getDisplayName() != null && name.equals(firebaseUser.getDisplayName()))
            return;

        etUserName.clearFocus();
        Utils.hideSoftKeyboard(this);
        ViewUtils.showViews(loader);
        isUpdatingName = true;

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        builder.setDisplayName(name);

        firebaseUser.updateProfile(builder.build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        isProfileUpdated = true;
                        ViewUtils.hideViews(loader);
                        isUpdatingName = false;
                        ViewUtils.makeToast(NUserProfileActivity.this,
                                "Your name updated successfully.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        etUserName.setText(Utils.getValidString(firebaseUser.getDisplayName()));
                        ViewUtils.hideViews(loader);
                        isUpdatingName = false;
                        DialogUtils.showSorryAlert(NUserProfileActivity.this, ""
                                + e.getMessage(), null);
                    }
                });
    }

    private void updateEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etUserEmail.setError("Invalid email!");
            etUserEmail.requestFocus();
            return;
        }
        if (firebaseUser.getEmail() != null && email.equals(firebaseUser.getEmail()))
            return;

        etUserEmail.clearFocus();
        Utils.hideSoftKeyboard(this);
        ViewUtils.showViews(loader);
        isUpdatingEmail = true;

        firebaseUser.updateEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        isProfileUpdated = true;
                        ViewUtils.hideViews(loader);
                        isUpdatingEmail = false;
                        ViewUtils.makeToast(NUserProfileActivity.this,
                                "Your email updated successfully.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        etUserEmail.setText(Utils.getValidString(firebaseUser.getEmail()));
                        ViewUtils.hideViews(loader);
                        isUpdatingEmail = false;
                        DialogUtils.showSorryAlert(NUserProfileActivity.this, ""
                                + e.getMessage(), null);
                    }
                });
    }

    private void uploadProfilePhoto(){
        if (preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.LOCAL)
            uploadImageToLocalStorage();
        else if (preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.CLOUD)
            uploadImageToCloudStorage();
    }

    private void uploadImageToLocalStorage() {
        ViewUtils.showViews(loader);
        isUploadingPic = true;

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        builder.setPhotoUri(photoURI);

        firebaseUser.updateProfile(builder.build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        isProfileUpdated = true;
                        ViewUtils.hideViews(loader);
                        isUploadingPic = false;
                        ViewUtils.makeToast(NUserProfileActivity.this,
                                "Your profile photo updated successfully.");

                        setUpImageToImageView();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ViewUtils.hideViews(loader);
                        isUploadingPic = false;
                        DialogUtils.showSorryAlert(NUserProfileActivity.this, ""
                                + e.getMessage(), null);
                    }
                });
    }

    private void uploadImageToCloudStorage() {
        final ProgressDialog uploadProgress = new ProgressDialog(this);
        uploadProgress.setCancelable(false);
        uploadProgress.setTitle("Uploading profile photo");
        uploadProgress.setMessage("Uploaded 0%");
        uploadProgress.show();

//        ViewUtils.showViews(loader);
//        isUploadingPic = true;

        profilePhotosReference.putFile(photoURI)
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Snackbar snackbar = Snackbar.make(fabUploadPhoto,
                                "Photo upload cancelled!", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        uploadProgress.setMessage(String.format(Locale.getDefault(), "Uploaded %.2f%%", progress));
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploadProgress.setMessage("Photo uploaded successfully.");
                        uploadProgress.setCancelable(true);
                        setUpImageToImageView();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadProgress.setMessage("Photo uploading failed.\n" + e.getMessage());
                        uploadProgress.setCancelable(true);
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

    @Override
    public void onBackPressed() {
        if (isUpdatingName || isUploadingPic || isUpdatingEmail) {
            ViewUtils.makeToast(this, "Please wait while changes are updated...");
        } else {
            if (getIntent().getIntExtra("CALLED_FROM", CALLED_FROM_NAVIGATION) == CALLED_FROM_PHONE_AUTH) {
                startActivity(new Intent(this, NIAmActivity.class));
                finish();
            } else {
                if (isProfileUpdated)
                    setResult(RESULT_OK);
                else
                    setResult(RESULT_CANCELED);
                super.onBackPressed();
            }
        }
    }
}
