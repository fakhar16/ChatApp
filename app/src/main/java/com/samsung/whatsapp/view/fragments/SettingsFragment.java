package com.samsung.whatsapp.view.fragments;

import static com.samsung.whatsapp.utils.Utils.currentUser;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.FragmentSettingsBinding;
import com.samsung.whatsapp.view.activities.SettingsActivity;
import com.samsung.whatsapp.view.activities.StarMessageActivity;
import com.squareup.picasso.Picasso;

public class SettingsFragment extends Fragment {
    FragmentSettingsBinding binding;
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        handleItemsClick();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupProfileInfo();
    }

    private void handleItemsClick() {
        binding.profileInfo.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });

        binding.starMessages.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), StarMessageActivity.class);
            startActivity(intent);
        });
    }

    private void setupProfileInfo() {
        binding.userName.setText(currentUser.getName());
        if (currentUser.getStatus() != null && !currentUser.getStatus().isEmpty()) {
            binding.userStatus.setText(currentUser.getStatus());
        }
        if (currentUser.getImage() != null && !currentUser.getImage().isEmpty()) {
            Picasso.get().load(currentUser.getImage()).placeholder(R.drawable.profile_image).into(binding.userImage);
        }
    }
}