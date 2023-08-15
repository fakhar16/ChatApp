package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.presenceDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.TAG;
import static com.samsung.whatsapp.utils.Utils.currentUser;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivityMainBinding;
import com.samsung.whatsapp.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        CheckIfUserIsLogined();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_navigation);
        NavigationUI.setupWithNavController(binding.navView, navController);

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
                Log.i(TAG, "acquireFcmRegistrationToken: task failed");
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

    private void SendUserToWelcomeActivity() {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SetupProfileActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        settingsIntent.putExtra(getString(R.string.PHONE_NUMBER), getIntent().getStringExtra(getString(R.string.PHONE_NUMBER)));
        startActivity(settingsIntent);
    }
}