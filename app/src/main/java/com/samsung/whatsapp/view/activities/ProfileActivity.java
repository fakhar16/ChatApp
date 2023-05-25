package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;

import androidx.annotation.NonNull;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivityProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfileActivity extends BaseActivity {
    private String receiverUserId;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InitializeFields();
        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        userDatabaseReference.child(receiverUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild(getString(R.string.IMAGE))) {
                            String userImage = Objects.requireNonNull(snapshot.child(getString(R.string.IMAGE)).getValue()).toString();
                            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(binding.visitProfileImage);
                        }

                        String userName = Objects.requireNonNull(snapshot.child(getString(R.string.NAME)).getValue()).toString();
                        String userStatus = Objects.requireNonNull(snapshot.child(getString(R.string.STATUS)).getValue()).toString();

                        binding.visitUserName.setText(userName);
                        binding.visitProfileStatus.setText(userStatus);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void InitializeFields() {
        receiverUserId = getIntent().getExtras().getString(getString(R.string.VISIT_USER_ID));
    }
}