package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userProfilesImagesReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivitySettingsBinding;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends BaseActivity {
    private String currentUserId;
    private ActivitySettingsBinding binding;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        InitializeFields();
        RetrieveUserInfo();

        binding.updateSettingsButton.setOnClickListener(view -> UpdateSettings());
        binding.editProfileImage.setOnClickListener(view -> Crop.pickImage(SettingsActivity.this));
        binding.setProfileImage.setOnClickListener(view -> WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(binding.setProfileImage, binding.expandedImageCardview, binding.expandedImage, binding.container, currentUser.getImage()));
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
        StorageReference filePath = userProfilesImagesReference.child(currentUserId + ".jpg");
        UploadTask uploadTask = filePath.putFile(resultUri);

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                filePath.getDownloadUrl().addOnSuccessListener(uri -> userDatabaseReference.child(currentUserId).child(getString(R.string.IMAGE))
                        .setValue(uri.toString())
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Utils.dismissLoadingBar(SettingsActivity.this, binding.progressbar.getRoot());
                                Toast.makeText(SettingsActivity.this, "Image saved in database successfully", Toast.LENGTH_SHORT).show();
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

    private void InitializeFields() {
        setSupportActionBar(binding.settingsToolbar.mainAppBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");
        binding.tvPhone.setText(getIntent().getStringExtra(getString(R.string.PHONE_NUMBER)));
    }

    private void UpdateSettings() {
        String setUserName = Objects.requireNonNull(binding.setUserName.getText()).toString();
        String setUserStatus = Objects.requireNonNull(binding.setProfileStatus.getText()).toString();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please write your user name first...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(setUserStatus)) {
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put(getString(R.string.UID), currentUserId);
            profileMap.put(getString(R.string.PHONE_NUMBER), getIntent().getStringExtra(getString(R.string.PHONE_NUMBER)));
            profileMap.put(getString(R.string.NAME), setUserName);
            profileMap.put(getString(R.string.STATUS), setUserStatus);

            userDatabaseReference.child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SendUserToMainActivity();
                            Toast.makeText(SettingsActivity.this, "Profile Updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = Objects.requireNonNull(task.getException()).toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void RetrieveUserInfo() {
        userDatabaseReference.child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUser = snapshot.getValue(User.class);
                        if ((snapshot.exists()) && (snapshot.hasChild(getString(R.string.NAME))) && (snapshot.hasChild(getString(R.string.IMAGE)))) {
                            String retrieveProfileImage = Objects.requireNonNull(snapshot.child(getString(R.string.IMAGE)).getValue()).toString();
                            Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(binding.setProfileImage);
                        }
                        if ((snapshot.exists()) && (snapshot.hasChild(getString(R.string.NAME)))) {
                            String retrieveUserName = Objects.requireNonNull(snapshot.child(getString(R.string.NAME)).getValue()).toString();
                            String retrieveUserStatus = Objects.requireNonNull(snapshot.child(getString(R.string.STATUS)).getValue()).toString();
                            String retrieveUserPhone = Objects.requireNonNull(snapshot.child(getString(R.string.PHONE_NUMBER)).getValue()).toString();

                            binding.setUserName.setText(retrieveUserName);
                            binding.setProfileStatus.setText(retrieveUserStatus);
                            binding.tvPhone.setText(retrieveUserPhone);

                        } else {
                            Toast.makeText(SettingsActivity.this, "Please set & update your profile information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (binding.expandedImageCardview.getVisibility() == View.VISIBLE) {
            WhatsappLikeProfilePicPreview.Companion.dismissPhotoPreview();
        } else {
            finish();
        }
    }
}