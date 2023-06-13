package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userProfilesImagesReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivitySetupProfileBinding;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.utils.Utils;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class SetupProfileActivity extends BaseActivity {
    private String currentUserId;
    private ActivitySetupProfileBinding binding;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentUserId = FirebaseAuth.getInstance().getUid();

        InitializeFields();
        retrieveUserImage();
        handleItemsClick();
    }

    private void handleItemsClick() {
        binding.editProfileImage.setOnClickListener(view -> Crop.pickImage(SetupProfileActivity.this));
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

    private void retrieveUserImage() {
        userDatabaseReference.child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUser = snapshot.getValue(User.class);
                        if (snapshot.hasChild(getString(R.string.IMAGE))) {
                            Picasso.get().load(currentUser.getImage()).placeholder(R.drawable.profile_image).into(binding.setProfileImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
                filePath.getDownloadUrl().addOnSuccessListener(uri -> userDatabaseReference.child(currentUserId).child(getString(R.string.IMAGE))
                        .setValue(uri.toString())
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Utils.dismissLoadingBar(SetupProfileActivity.this, binding.progressbar.getRoot());
                            } else {
                                Utils.dismissLoadingBar(SetupProfileActivity.this, binding.progressbar.getRoot());
                                String message = Objects.requireNonNull(task1.getException()).toString();
                                Toast.makeText(SetupProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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
            Utils.showLoadingBar(SetupProfileActivity.this, binding.progressbar.getRoot());
            handleCrop(resultCode, data);
        }
    }

    private void InitializeFields() {
        setSupportActionBar(binding.settingsToolbar.mainAppBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");
    }

    private void updateProfileName() {
        String setUserName = Objects.requireNonNull(binding.setUserName.getText()).toString();
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put(getString(R.string.NAME), setUserName);

            userDatabaseReference.child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            SendUserToMainActivity();
                            Toast.makeText(SetupProfileActivity.this, "Profile Updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = Objects.requireNonNull(task.getException()).toString();
                            Toast.makeText(SetupProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupProfileActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        menu.findItem(R.id.done).setEnabled(false);

        binding.setUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() != 0) {
                    if (!menu.findItem(R.id.done).isEnabled()) {
                        menu.findItem(R.id.done).setEnabled(true);
                    }
                } else {
                    menu.findItem(R.id.done).setEnabled(false);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.done) {
            updateProfileName();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}