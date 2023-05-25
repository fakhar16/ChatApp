package com.samsung.whatsapp.view.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivityPhoneLoginBinding;

import java.util.Objects;

public class PhoneLoginActivity extends BaseActivity {
    private ActivityPhoneLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.btnContinue.setOnClickListener(view -> {
            if (Objects.requireNonNull(binding.phoneNumberInput.getText()).toString().isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, OTPActivity.class);
                intent.putExtra(getString(R.string.PHONE_NUMBER), binding.phoneNumberInput.getText().toString());
                startActivity(intent);
            }
        });
    }
}