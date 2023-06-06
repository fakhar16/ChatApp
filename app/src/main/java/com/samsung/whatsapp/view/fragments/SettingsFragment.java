package com.samsung.whatsapp.view.fragments;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.FragmentSettingsBinding;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.view.activities.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class SettingsFragment extends Fragment {
    FragmentSettingsBinding binding;
    private User currentUser;
    private String currentUserId;
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
        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

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

        binding.setProfileImage.setOnClickListener(view -> ((MainActivity)(requireContext())).showPhotoPreview(binding.setProfileImage));

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        RetrieveUserInfo();
    }

    private void updateProfileName () {
        if (Objects.requireNonNull(binding.setUserName.getText()).toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please write your user name...", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put(getString(R.string.NAME), binding.setUserName.getText().toString());

            userDatabaseReference.child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Error: " + Objects.requireNonNull(task.getException()), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateProfileStatus () {
        if (Objects.requireNonNull(binding.setProfileStatus.getText()).toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please write your user name...", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put(getString(R.string.STATUS), binding.setProfileStatus.getText().toString());

            userDatabaseReference.child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Error: " + Objects.requireNonNull(task.getException()), Toast.LENGTH_SHORT).show();
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
                        if ((snapshot.exists()) && (snapshot.hasChild(getString(R.string.IMAGE)))) {
                            Picasso.get().load(currentUser.getImage()).placeholder(R.drawable.profile_image).into(binding.setProfileImage);
                        }
                        if (snapshot.exists() && snapshot.hasChild(getString(R.string.STATUS))) {
                            binding.setProfileStatus.setText(currentUser.getStatus());
                        }
                        if ((snapshot.exists()) && (snapshot.hasChild(getString(R.string.NAME)))) {
                            binding.setUserName.setText(currentUser.getName());
                            binding.tvPhone.setText(currentUser.getPhone_number());
                        } else {
                            Toast.makeText(getActivity(), "Please set & update your profile information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}