package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userProfilesImagesReference;
import static com.samsung.whatsapp.utils.Utils.currentUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivitySettingsBinding;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolBar();
        handleItemClicks();
        updateProfileNameLimitTextOnFocus();
    }

    private void updateProfileNameLimitTextOnFocus() {
        binding.setUserName.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                int remainingLimit = 25 - Objects.requireNonNull(binding.setUserName.getText()).length();
                binding.profileNameLimiter.setVisibility(View.VISIBLE);
                binding.profileNameLimiter.setText(String.valueOf(remainingLimit));
            } else {
                binding.profileNameLimiter.setVisibility(View.GONE);
            }
        });

        binding.setUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int remainingLimit = 25 - editable.length();
                binding.profileNameLimiter.setText(String.valueOf(remainingLimit));
            }
        });
    }

    private void handleItemClicks() {
        binding.setUserName.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                updateProfileName();
            }
            return false;
        });

        binding.setProfileStatus.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                updateProfileStatus();
            }
            return false;
        });

        binding.editProfileImage.setOnClickListener(view -> Crop.pickImage(SettingsActivity.this));
        binding.setProfileImage.setOnClickListener(view -> WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(binding.setProfileImage, binding.expandedImage.cardView, binding.expandedImage.image, binding.toolBar.getRoot().getRootView(), currentUser.getImage()));
    }

    private void initToolBar() {
        setSupportActionBar(binding.toolBar.mainAppBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        setupProfileInfo();
    }
    private void updateProfileName () {
        if (Objects.requireNonNull(binding.setUserName.getText()).toString().isEmpty()) {
            Toast.makeText(SettingsActivity.this, "Please write your user name...", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put(getString(R.string.NAME), binding.setUserName.getText().toString());

            userDatabaseReference.child(currentUser.getUid()).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Error: " + Objects.requireNonNull(task.getException()), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateProfileStatus () {
        if (Objects.requireNonNull(binding.setProfileStatus.getText()).toString().isEmpty()) {
            Toast.makeText(SettingsActivity.this, "Please write your user name...", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put(getString(R.string.STATUS), binding.setProfileStatus.getText().toString());

            userDatabaseReference.child(currentUser.getUid()).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Error: " + Objects.requireNonNull(task.getException()), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setupProfileInfo() {
        if (!currentUser.getImage().isEmpty()) {
            Picasso.get().load(currentUser.getImage()).placeholder(R.drawable.profile_image).into(binding.setProfileImage);
        }
        if (currentUser.getStatus() != null && !currentUser.getStatus().isEmpty()) {
            binding.setProfileStatus.setText(currentUser.getStatus());
        }
        if (currentUser.getImage() != null && !currentUser.getName().isEmpty()) {
            binding.setUserName.setText(currentUser.getName());
            binding.tvPhone.setText(currentUser.getPhone_number());
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), getString(R.string.CROPPED)));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Uri resultUri = Crop.getOutput(result);
            SaveUserProfileImageToFireBaseStorage(resultUri);

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void SaveUserProfileImageToFireBaseStorage(Uri resultUri) {
        StorageReference filePath = userProfilesImagesReference.child(currentUser.getUid() + ".jpg");
        UploadTask uploadTask = filePath.putFile(resultUri);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                filePath.getDownloadUrl().addOnSuccessListener(uri -> userDatabaseReference.child(currentUser.getUid()).child(getString(R.string.IMAGE))
                        .setValue(uri.toString())
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Utils.dismissLoadingBar(SettingsActivity.this, binding.progressbar.getRoot());
                            } else {
                                Utils.dismissLoadingBar(SettingsActivity.this, binding.progressbar.getRoot());
                                String message = Objects.requireNonNull(task1.getException()).toString();
                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(Objects.requireNonNull(data).getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            binding.progressbar.dialogTitle.setText(getString(R.string.UPDATE_PROFILE_IMAGE_TITLE));
            binding.progressbar.dialogDescription.setText(getString(R.string.UPDATE_PROFILE_IMAGE_DESCRIPTION));
            Utils.showLoadingBar(SettingsActivity.this, binding.progressbar.getRoot());
            handleCrop(resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (binding.expandedImage.cardView.getVisibility() == View.VISIBLE) {
            WhatsappLikeProfilePicPreview.Companion.dismissPhotoPreview();
        } else {
            finish();
        }
    }
}