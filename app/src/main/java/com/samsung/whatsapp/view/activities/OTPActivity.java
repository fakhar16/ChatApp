package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivityOtpBinding;
import com.samsung.whatsapp.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OTPActivity extends BaseActivity {
    private ActivityOtpBinding binding;
    private FirebaseAuth mAuth;
    private String mVerificationId;

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Utils.dismissLoadingBar(OTPActivity.this, binding.progressbar.getRoot());
            SendUserToPhoneActivity();
            Toast.makeText(OTPActivity.this, "Invalid phone number, Try again...", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            mVerificationId = verificationId;
            Utils.dismissLoadingBar(OTPActivity.this, binding.progressbar.getRoot());
            Toast.makeText(OTPActivity.this, "Verification code has been sent", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnVerify.setOnClickListener(view -> {
            String verificationCode = Objects.requireNonNull(binding.tvCode.getText()).toString();
            if (TextUtils.isEmpty(verificationCode)) {
                Toast.makeText(OTPActivity.this, "Please enter verification code", Toast.LENGTH_SHORT).show();
            } else {
                binding.progressbar.dialogTitle.setText(getString(R.string.VERIFICATION_CODE_TITLE));
                binding.progressbar.dialogDescription.setText(getString(R.string.VERIFICATION_CODE_DESCRIPTION));
                Utils.showLoadingBar(OTPActivity.this, binding.progressbar.getRoot());

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                signInWithPhoneAuthCredential(credential);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        String phoneNumber = getIntent().getStringExtra(getString(R.string.PHONE_NUMBER));
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(OTPActivity.this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        binding.progressbar.dialogTitle.setText(getString(R.string.PHONE_VERIFICATION_TITLE));
        binding.progressbar.dialogDescription.setText(getString(R.string.PHONE_VERIFICATION_DESCRIPTION));
        Utils.showLoadingBar(OTPActivity.this, binding.progressbar.getRoot());
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Utils.dismissLoadingBar(OTPActivity.this, binding.progressbar.getRoot());
                        SendUserToMainActivity(Objects.requireNonNull(task.getResult().getUser()).getPhoneNumber());
                    } else {
                        Utils.dismissLoadingBar(OTPActivity.this, binding.progressbar.getRoot());
                        String message = Objects.requireNonNull(task.getException()).toString();
                        Toast.makeText(OTPActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updatePhoneNumberInDB(String phone) {
        Map<String, Object> map = new HashMap<>();
        map.put(getString(R.string.UID), FirebaseAuth.getInstance().getUid());
        map.put(getString(R.string.PHONE_NUMBER), phone);

        userDatabaseReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .updateChildren(map);
    }

    private void SendUserToMainActivity(String phone) {
        Intent mainIntent = new Intent(OTPActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        updatePhoneNumberInDB(phone);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToPhoneActivity() {
        Intent intent = new Intent(OTPActivity.this, PhoneLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}