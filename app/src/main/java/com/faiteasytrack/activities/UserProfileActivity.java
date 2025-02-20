package com.faiteasytrack.activities;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.faiteasytrack.BuildConfig;
import com.faiteasytrack.R;
import com.faiteasytrack.constants.Preferences;
import com.faiteasytrack.models.PreferenceModel;
import com.faiteasytrack.utils.AppPermissions;
import com.faiteasytrack.utils.Constants;
import com.faiteasytrack.utils.DateUtils;
import com.faiteasytrack.utils.DialogUtils;
import com.faiteasytrack.utils.FileUtils;
import com.faiteasytrack.firebase.FirebaseUtils;
import com.faiteasytrack.utils.SharePreferences;
import com.faiteasytrack.utils.Utils;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
import androidx.core.widget.ContentLoadingProgressBar;

public class UserProfileActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "NUserProfileActivity";
    public static final int CALLED_FROM_PHONE_AUTH = 0;
    public static final int CALLED_FROM_NAVIGATION = 1;

    public static final int REQUEST_FOR_CAMERA = 201, REQUEST_FOR_GALLERY = 202,
            REQUEST_FOR_FILE_BROWSER = 203, REQUEST_FOR_CROP = 204;

    private FloatingActionButton fabUploadPhoto;
    private TextInputEditText etUserName, etUserEmail, etUserPhone;
    private CircularImageView civProfilePic;
    private MaterialButton btnSkip;

    private View detailsUploader;
    private ContentLoadingProgressBar pbPhotoLoader, pbPhotoUploader;
    private Snackbar snackBarInfo;

    private FirebaseUser firebaseUser;
    private StorageReference profilePhotosReference;

    private PreferenceModel preferenceModel;

    private boolean isCalledFromAuth, isNameSetAuto = true,
            isProfileUpdated = false, isUploadingPic = false, isUpdatingName = false, isUpdatingEmail = false;

    private Uri photoURI;
    private String imagePathForCamera;

    private TextWatcher nameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isNameSetAuto) {
                isNameSetAuto = false;
                return;
            }

            if (s.length() > 0) {
                snackBarInfo.dismiss();
                btnSkip.setText(getString(R.string.done_continue));
                btnSkip.setEnabled(true);
            } else {
                showAddNamePrompt();
            }
        }
    };

    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profilePhotosReference = FirebaseUtils.getProfilePhotoReference();
        preferenceModel = SharePreferences.getPreferenceModel(this);

        isCalledFromAuth = getIntent().getIntExtra("CALLED_FROM",
                CALLED_FROM_NAVIGATION) == CALLED_FROM_PHONE_AUTH;

        setContentView(R.layout.activity_user_profile);
    }

    @Override
    public void setUpActionBar() {

    }

    @Override
    public void initUI() {
        pbPhotoLoader = findViewById(R.id.pb_photo_loader);
        pbPhotoUploader = findViewById(R.id.pd_photo_uploader);
        pbPhotoUploader.hide();

        ViewStub viewStubLoader = findViewById(R.id.vs_loader);
        detailsUploader = viewStubLoader.inflate();
        detailsUploader.setOnClickListener(this);

        ViewUtils.hideViews(detailsUploader);

        civProfilePic = findViewById(R.id.civ_profile_pic);

        etUserName = findViewById(R.id.et_name);
        etUserName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constants.MAX_LENGTH_NAME)});
        etUserEmail = findViewById(R.id.et_email);
        etUserPhone = findViewById(R.id.et_phone);

        fabUploadPhoto = findViewById(R.id.fab_upload_photo);
        btnSkip = findViewById(R.id.btn_skip);

        if (!isCalledFromAuth) ViewUtils.hideViews(btnSkip);

        snackBarInfo = Snackbar.make(etUserPhone, "", Snackbar.LENGTH_SHORT);
    }

    @Override
    public void setUpListeners() {
        btnSkip.setOnClickListener(this);
        fabUploadPhoto.setOnClickListener(this);

        etUserName.addTextChangedListener(nameTextWatcher);
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

        if (etUserName.getText().toString().isEmpty() && isCalledFromAuth) {
            showAddNamePrompt();
        }

        setUpImageToImageView();
    }

    private void showAddNamePrompt() {
        snackBarInfo.setText("Add name to continue");
        snackBarInfo.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackBarInfo.show();
        btnSkip.setText(getString(R.string.skip));
        btnSkip.setEnabled(false);
    }

    private void setUpImageToImageView() {
        if (preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.LOCAL) {
            try {
                civProfilePic.setImageURI(firebaseUser.getPhotoUrl());
                pbPhotoLoader.hide();
            } catch (Exception e) {
                e.printStackTrace();
                pbPhotoLoader.hide();
            }

        } else if (preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.CLOUD) {
            try {
                Glide.with(this)
                        .load(profilePhotosReference)
                        .thumbnail(FileUtils.THUMBNAIL_MULTIPLIER)
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                pbPhotoLoader.hide();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                pbPhotoLoader.hide();
                                return false;
                            }
                        })
                        .into(civProfilePic);
            } catch (Exception e) {
                e.printStackTrace();
                pbPhotoLoader.hide();
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
        String[] sourceList = {"Camera", "Gallery", "File Manager"};
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
                        }
                    }
                });
        fileSourceChooserDialog.show();
    }

    private void requestCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (AppPermissions.checkCameraPermission(this, true))
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
            if (AppPermissions.checkGalleryPermission(this, true))
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
            if (AppPermissions.checkFileBrowserPermission(this, true))
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

        imagePathForCamera = image.getAbsolutePath();
        return image;
    }

    private void cropImageShot() {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");

            cropIntent.setDataAndType(Uri.parse(imagePathForCamera), "image/*");
            cropIntent.putExtra("crop", true);
            cropIntent.putExtra("return-data", true);
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(cropIntent, REQUEST_FOR_CROP);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            String errorMessage = "Whoops - your device doesn't support the cropping!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == AppPermissions.ACCESS_CAMERA) {
                startCamera();
            } else if (requestCode == AppPermissions.ACCESS_GALLERY) {
                startGallery();
            } else if (requestCode == AppPermissions.ACCESS_FILE_BROWSER) {
                startFileBrowser();
            }
        } else {
            Log.i(TAG, "onRequestPermissionsResult: " + permissions[0] + " permission denied.");
            AppPermissions.showAllowPermissionDialog(this, permissions,
                    new AppPermissions.OnPermissionChangeListener() {
                        @Override
                        public void onAllowPermission(String[] permissions) {

                        }

                        @Override
                        public void onPermissionDenied() {

                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_FOR_CAMERA) {
                cropImageShot();
            } else if (requestCode == REQUEST_FOR_GALLERY) {
                attachFile(data);
            } else if (requestCode == REQUEST_FOR_FILE_BROWSER) {
                takePersistablePermissions(data);
                attachFile(data);
            } else if (requestCode == REQUEST_FOR_CROP) {
                if (data != null) {
                    photoURI = data.getData();
                    uploadProfilePhoto();
                }
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
                if (!imagePath.equals(imagePathForCamera)) {
                    imagePathForCamera = imagePath;
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
        if (name.isEmpty()) {
            etUserName.setError("Invalid name!");
            etUserName.requestFocus();
            return;
        }
        if (firebaseUser.getDisplayName() != null && name.equals(firebaseUser.getDisplayName()))
            return;

        etUserName.clearFocus();
        Utils.hideSoftKeyboard(this);
        ViewUtils.showViews(detailsUploader);
        isUpdatingName = true;

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        builder.setDisplayName(name);

        firebaseUser.updateProfile(builder.build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        isProfileUpdated = true;
                        ViewUtils.hideViews(detailsUploader);
                        isUpdatingName = false;
                        updateDoneInfoToUser("Your name updated successfully.", false);

                        if (isCalledFromAuth) {
                            btnSkip.setEnabled(true);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        etUserName.setText(Utils.getValidString(firebaseUser.getDisplayName()));
                        ViewUtils.hideViews(detailsUploader);
                        isUpdatingName = false;
                        updateRetryInfoToUser(e.getMessage(), new Runnable() {
                            @Override
                            public void run() {
                                updateName(etUserName.getText().toString());
                            }
                        });
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
        ViewUtils.showViews(detailsUploader);
        isUpdatingEmail = true;

        firebaseUser.updateEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        isProfileUpdated = true;
                        ViewUtils.hideViews(detailsUploader);
                        isUpdatingEmail = false;
                        updateDoneInfoToUser("Your email updated successfully.", false);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        etUserEmail.setText(Utils.getValidString(firebaseUser.getEmail()));
                        ViewUtils.hideViews(detailsUploader);
                        isUpdatingEmail = false;
                        updateRetryInfoToUser(e.getMessage(), new Runnable() {
                            @Override
                            public void run() {
                                updateEmail(etUserEmail.getText().toString());
                            }
                        });
                    }
                });
    }

    private void uploadProfilePhoto() {
        if (preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.LOCAL)
            uploadImageToLocalStorage();
        else if (preferenceModel.getStorageForProfilePhoto() == Preferences.Storage.CLOUD)
            uploadImageToCloudStorage();
    }

    private void uploadImageToLocalStorage() {
        isUploadingPic = true;

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
        builder.setPhotoUri(photoURI);

        firebaseUser.updateProfile(builder.build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        isProfileUpdated = true;
                        isUploadingPic = false;
                        updateDoneInfoToUser("Your profile photo updated successfully.", false);

                        setUpImageToImageView();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isUploadingPic = false;
                        updateRetryInfoToUser(e.getMessage(), new Runnable() {
                            @Override
                            public void run() {
                                uploadImageToLocalStorage();
                            }
                        });
                    }
                });
    }

    private void uploadImageToCloudStorage() {
        pbPhotoUploader.show();
        pbPhotoLoader.show();

        profilePhotosReference.putFile(photoURI)
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        String message = "Photo upload cancelled!";
                        updateDoneInfoToUser(message, false);
                    }
                })

                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        String message = String.format(Locale.getDefault(), "Uploaded %.3f%%", progress);
                        updateDoneInfoToUser(message, true);
                        pbPhotoUploader.setProgress((int) progress);
                    }
                })

                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pbPhotoUploader.hide();
                        String message = "Photo uploaded successfully.";
                        updateDoneInfoToUser(message, false);

                        updateDisplayImage();
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pbPhotoUploader.hide();
                        String message = "Photo uploading failed.\n" + e.getMessage();
                        updateRetryInfoToUser(message, new Runnable() {
                            @Override
                            public void run() {
                                uploadImageToCloudStorage();
                            }
                        });
                    }
                });
    }

    private void updateDisplayImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Glide.get(UserProfileActivity.this).clearDiskCache();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setUpImageToImageView();
                    }
                }, 5000);
            }
        }).start();
    }

    private void updateDoneInfoToUser(String message, boolean indefinite) {
        snackBarInfo.setText(message);
        if (indefinite)
            snackBarInfo.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        else
            snackBarInfo.setDuration(BaseTransientBottomBar.LENGTH_SHORT);
        snackBarInfo.show();
    }

    private void updateRetryInfoToUser(String message, final Runnable retryRunnable) {
        snackBarInfo.setText(message);
        snackBarInfo.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackBarInfo.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (retryRunnable != null) retryRunnable.run();
            }
        });
        snackBarInfo.show();
    }

    @Override
    public void updateInternetStatus(boolean online) {

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
                startActivity(new Intent(this, IAmActivity.class));
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
