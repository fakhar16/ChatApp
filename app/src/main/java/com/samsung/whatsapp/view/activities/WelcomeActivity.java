package com.samsung.whatsapp.view.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.samsung.whatsapp.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends BaseActivity {
    private static final String TAG = "ConsoleWelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWelcomeBinding binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initCheckPermissions();

        binding.btnAgreeAndContinue.setOnClickListener(view -> {
            Intent intent = new Intent(WelcomeActivity.this, PhoneLoginActivity.class);
            startActivity(intent);
        });
    }

    private void initCheckPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.USE_FULL_SCREEN_INTENT
        };

        if (!hasPermissions(WelcomeActivity.this, permissions)) {
            ActivityCompat.requestPermissions(WelcomeActivity.this, permissions, 1);
        }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission: permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < grantResults.length; i++ ) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, permissions[i] + " : Granted");
                } else {
                    Log.i(TAG, permissions[i] + " : Denied");
                }
            }
        }
    }
}