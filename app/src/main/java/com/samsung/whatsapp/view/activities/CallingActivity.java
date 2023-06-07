package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.videoUserDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.INCOMING_CALL_NOTIFICATION_ID;
import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.utils.Utils.currentUser;

import androidx.core.app.NotificationManagerCompat;

import android.content.Intent;
import android.os.Bundle;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivityCallingBinding;
import com.samsung.whatsapp.webrtc.CallActivity;
import com.squareup.picasso.Picasso;


public class CallingActivity extends BaseActivity {
    ActivityCallingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupProfileInfo();
        handleButtonClicks();
    }

    private void setupProfileInfo() {
        binding.profileName.setText(getIntent().getStringExtra(context.getString(R.string.NAME)));
        Picasso.get().load(getIntent().getStringExtra(context.getString(R.string.IMAGE))).placeholder(R.drawable.profile_image).into(binding.profileImage);
    }

    private void handleButtonClicks() {
        binding.btnReject.setOnClickListener(view -> {
            videoUserDatabaseReference.child(getIntent().getStringExtra(context.getString(R.string.FRIEND_USER_NAME))).setValue(null);
            NotificationManagerCompat.from(getApplicationContext()).cancel(INCOMING_CALL_NOTIFICATION_ID);
            finish();
        });

        binding.btnAccept.setOnClickListener(view -> {
            Intent intent = new Intent(CallingActivity.this, CallActivity.class);
            intent.putExtra(context.getString(R.string.CALL_ACCEPTED), true);
            intent.putExtra(context.getString(R.string.CALLER), currentUser.getUid());
            startActivity(intent);

            finish();
        });
    }
}