package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.presenceDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.TabAccessorAdapter;
import com.samsung.whatsapp.databinding.ActivityMainBinding;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "consoleMainActivity";
    public static User currentUser = null;
    private final String[] fragmentLabels = {"Chats", "Status", "Settings"};

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        CheckIfUserIsLogined();

        setSupportActionBar(binding.mainPageToolbar.mainAppBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));

        TabAccessorAdapter mTabAccessorAdapter = new TabAccessorAdapter(getSupportFragmentManager(), getLifecycle());
        binding.mainTabsPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.mainTabsPager.setAdapter(mTabAccessorAdapter);

        new TabLayoutMediator(binding.mainTabs, binding.mainTabsPager,
                (tab, position) -> tab.setText(fragmentLabels[position])
                ).attach();

        if (FirebaseAuth.getInstance().getUid() != null) {
            acquireFcmRegistrationToken();
            setupCurrentUser();
        }
    }

    private void setupCurrentUser() {
        userDatabaseReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            currentUser = snapshot.getValue(User.class);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void acquireFcmRegistrationToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Map<String, Object> map = new HashMap<>();
                map.put(getString(R.string.TOKEN), token);
                userDatabaseReference
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                        .updateChildren(map);
            } else {
                Log.wtf(TAG, "acquireFcmRegistrationToken: task failed");
            }
        });
    }

    private void CheckIfUserIsLogined() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            SendUserToWelcomeActivity();
        } else {
            VerifyUserExistence();
        }
    }

    private void VerifyUserExistence() {
        String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        userDatabaseReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.child(getString(R.string.NAME)).exists()) {
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenceDatabaseReference
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenceDatabaseReference
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .setValue("Offline");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_option) {
            mAuth.signOut();
            SendUserToPhoneLoginActivity();
        }
        return true;
    }

    private void SendUserToWelcomeActivity() {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void SendUserToPhoneLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, PhoneLoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SetupProfileActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        settingsIntent.putExtra(getString(R.string.PHONE_NUMBER), getIntent().getStringExtra(getString(R.string.PHONE_NUMBER)));
        startActivity(settingsIntent);
    }

    public void showPhotoPreview(View thumbView) {
        binding.appBarLayout.setVisibility(View.GONE);
        WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(thumbView, binding.expandedImageCardView, binding.expandedImage, binding.getRoot(), currentUser.getImage());
    }

    @Override
    public void onBackPressed() {
        if (binding.expandedImageCardView.getVisibility() == View.VISIBLE) {
            WhatsappLikeProfilePicPreview.Companion.dismissPhotoPreview();
            binding.appBarLayout.setVisibility(View.VISIBLE);
        }  else {
            finish();
        }
    }
}