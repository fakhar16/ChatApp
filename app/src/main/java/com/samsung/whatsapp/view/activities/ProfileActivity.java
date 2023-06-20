package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.TYPE_VIDEO_CALL;
import static com.samsung.whatsapp.utils.Utils.currentUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivityProfileBinding;
import com.samsung.whatsapp.fcm.FCMNotificationSender;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.model.Notification;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.repository.MessageRepositoryImpl;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;
import com.samsung.whatsapp.webrtc.CallActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private User receiver;
    String receiverId;
    ArrayList<Message> starMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiverId = getIntent().getStringExtra(getString(R.string.RECEIVER_ID));
        starMessages = MessageRepositoryImpl.getInstance().getStarredMessagesMatchingReceiver().getValue();

        loadUserInfo();
        initToolBar();
        handleItemsClick();

    }

    private void handleItemsClick() {
        binding.userImage.setOnClickListener(view -> showImagePreview(binding.userImage, receiver.getImage()));
        binding.starLayout.setOnClickListener(view -> sendUserToStarActivity());
        binding.search.setOnClickListener(view -> sendUserToChatActivity());
        binding.videoCall.setOnClickListener(view -> createVideoCall());
    }

    private void createVideoCall() {
        Notification notification = new Notification(currentUser.getName(), "Incoming Video Call", TYPE_VIDEO_CALL, currentUser.getImage(), receiver.getToken(), currentUser.getUid(), receiver.getUid());
        FCMNotificationSender.SendNotification(ApplicationClass.context, notification);

        Intent intent = new Intent(this, CallActivity.class);
        intent.putExtra(context.getString(R.string.CALLER), currentUser.getUid());
        intent.putExtra(getString(R.string.RECEIVER), receiver.getUid());
        intent.putExtra(getString(R.string.IS_CALL_MADE), true);
        startActivity(intent);
    }

    private void sendUserToStarActivity() {
        Intent intent = new Intent(this, StarMessageActivity.class);
        intent.putExtra(getString(R.string.STAR_MESSAGE_WITH_RECEIVER), true);
        startActivity(intent);
    }

    private void sendUserToChatActivity() {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(getString(R.string.VISIT_USER_ID), receiverId);
        intent.putExtra(getString(R.string.SEARCH_MESSAGE), true);
        startActivity(intent);
    }

    private void loadUserInfo() {
        userDatabaseReference.child(receiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            receiver = snapshot.getValue(User.class);
                            updateUserUI();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateUserUI() {
        binding.userName.setText(receiver.getName());
        binding.userPhone.setText(receiver.getPhone_number());
        binding.userStatus.setText(receiver.getStatus());
        Picasso.get().load(receiver.getImage()).placeholder(R.drawable.profile_image).into(binding.userImage);
        updateStarredMessageCount();
    }

    private void updateStarredMessageCount() {
        int starredMessageCount = starMessages.size();
        binding.starredMessagesCount.setText(starredMessageCount == 0? "None": String.valueOf(starredMessageCount));
    }

    private void initToolBar() {
        setSupportActionBar(binding.mainPageToolbar.mainAppBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Contact Info");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showImagePreview(View thumbView, String url) {
        WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(thumbView, binding.expandedImage.cardView, binding.expandedImage.image, binding.container, url);
        binding.appBarLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (binding.expandedImage.cardView.getVisibility() == View.VISIBLE) {
            WhatsappLikeProfilePicPreview.Companion.dismissPhotoPreview();
            binding.appBarLayout.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }
}