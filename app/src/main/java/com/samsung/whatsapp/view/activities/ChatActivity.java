package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.presenceDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.TYPE_VIDEO_CALL;
import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.utils.Utils.showLoadingBar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.adapters.MessagesAdapter;
import com.samsung.whatsapp.fcm.FCMNotificationSender;
import com.samsung.whatsapp.model.Notification;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.utils.FCMMessaging;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;
import com.samsung.whatsapp.viewmodel.MessageViewModel;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivityChatBinding;
import com.samsung.whatsapp.databinding.CustomChatBarBinding;
import com.samsung.whatsapp.webrtc.CallActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class ChatActivity extends BaseActivity {
    private String messageReceiverId, messageSenderId;
    private MessagesAdapter messagesAdapter;
    private ActivityChatBinding binding;
    private CustomChatBarBinding customChatBarBinding;
    public static User receiver, sender;
//    private static final String TAG = "ConsoleChatActivity";

    private final ActivityResultLauncher<Intent> imagePickActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        binding.progressbar.dialogTitle.setText(R.string.SENDING_FILE_TITLE);
                        binding.progressbar.dialogDescription.setText(getString(R.string.SENDING_FILE_DESCRIPTION));
                        showLoadingBar(ChatActivity.this, binding.progressbar.getRoot());

                        Intent data = result.getData();
                        if (data.getData() != null) {
                            Uri fileUri = Objects.requireNonNull(data).getData();
                            FCMMessaging.sendImage(messageSenderId, messageReceiverId, fileUri, ChatActivity.this, binding.progressbar.getRoot());
                        }
                    }
                }
            });

    private final ActivityResultLauncher<Intent> imageCaptureActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        binding.progressbar.dialogTitle.setText(getString(R.string.SENDING_FILE_TITLE));
                        binding.progressbar.dialogDescription.setText(getString(R.string.SENDING_FILE_DESCRIPTION));
                        showLoadingBar(ChatActivity.this, binding.progressbar.getRoot());

                        Bundle bundle = result.getData().getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get(context.getString(R.string.DATA));
                        Uri uri = getImageUri(ApplicationClass.context, bitmap);
                        FCMMessaging.sendImage(messageSenderId, messageReceiverId, uri, ChatActivity.this, binding.progressbar.getRoot());
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messageReceiverId = getIntent().getExtras().getString(getString(R.string.VISIT_USER_ID));
        messageSenderId = getIntent().getExtras().getString(getString(R.string.CURRENT_USER_ID));

        initializeFields();
        updateStatusIndicator();
        initializeSenderAndReceiver();
    }

    private void updateStatusIndicator() {
        final Handler handler = new Handler(Looper.getMainLooper());
        binding.messageInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                presenceDatabaseReference
                        .child(messageSenderId)
                        .setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);

            }
            final Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    presenceDatabaseReference
                            .child(messageSenderId)
                            .setValue("Online");
                }
            };
        });

        presenceDatabaseReference
                .child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String status = snapshot.getValue(String.class);
                            if (!Objects.requireNonNull(status).isEmpty()) {
                                if (status.equals("Offline")) {
                                    customChatBarBinding.status.setVisibility(View.GONE);
                                } else {
                                    customChatBarBinding.status.setVisibility(View.VISIBLE);
                                    customChatBarBinding.status.setText(status);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initializeSenderAndReceiver() {
        userDatabaseReference
                .child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        receiver = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        userDatabaseReference
                .child(messageSenderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sender = snapshot.getValue(User.class);
                        updateChatBarDetails();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initializeFields() {
        setSupportActionBar(binding.chatToolBar.mainAppBar);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customChatBarBinding = CustomChatBarBinding.inflate(layoutInflater);
        actionBar.setCustomView(customChatBarBinding.getRoot());

        MessageViewModel viewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        viewModel.init(messageSenderId, messageReceiverId);
        viewModel.getMessage().observe(this, messages -> messagesAdapter.notifyDataSetChanged());

        binding.userMessageList.setLayoutManager(new LinearLayoutManager(this));
        messagesAdapter = new MessagesAdapter( ChatActivity.this, messageSenderId, messageReceiverId, viewModel.getMessage().getValue());
        binding.userMessageList.setAdapter(messagesAdapter);



        handleButtonClicks();
    }

    private void updateChatBarDetails() {
        customChatBarBinding.name.setText(receiver.getName());
        Picasso.get().load(receiver.getImage()).placeholder(R.drawable.profile_image).into(customChatBarBinding.userImage);
    }

    private void handleButtonClicks() {
        binding.sendMessageBtn.setOnClickListener(view -> {
            String message = binding.messageInputText.getText().toString();
            FCMMessaging.sendMessage(message, sender.getUid(), receiver.getUid());
            binding.messageInputText.setText("");
        });
        binding.camera.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            imageCaptureActivityResultLauncher.launch(intent);
        });
        binding.attachment.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickActivityResultLauncher.launch(intent);
        });
        customChatBarBinding.voiceCall.setOnClickListener(view -> Toast.makeText(this, receiver.getName(), Toast.LENGTH_SHORT).show());
        customChatBarBinding.videoCall.setOnClickListener(view -> {
            Notification notification = new Notification(sender.getName(), "Incoming Video Call", TYPE_VIDEO_CALL, sender.getImage(), receiver.getToken(), sender.getUid(), receiver.getUid());
            FCMNotificationSender.SendNotification(ApplicationClass.context, notification);

            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra(context.getString(R.string.CALLER), sender.getUid());
            intent.putExtra(getString(R.string.RECEIVER), receiver.getUid());
            intent.putExtra(getString(R.string.IS_CALL_MADE), true);
            startActivity(intent);
        });
        customChatBarBinding.userImage.setOnClickListener(view -> WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(customChatBarBinding.userImage, binding.expandedImageCardview, binding.expandedImage, binding.chatToolBar.getRoot().getRootView(), receiver.getImage()));
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
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}