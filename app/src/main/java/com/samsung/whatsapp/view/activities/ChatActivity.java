package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.presenceDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.TYPE_VIDEO_CALL;
import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.utils.Utils.currentUser;
import static com.samsung.whatsapp.utils.Utils.getFileType;
import static com.samsung.whatsapp.utils.Utils.getImageUri;
import static com.samsung.whatsapp.utils.Utils.showLoadingBar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.adapters.MessagesAdapter;
import com.samsung.whatsapp.fcm.FCMNotificationSender;
import com.samsung.whatsapp.model.Notification;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.utils.FirebaseUtils;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;
import com.samsung.whatsapp.viewmodel.MessageViewModel;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivityChatBinding;
import com.samsung.whatsapp.databinding.CustomChatBarBinding;
import com.samsung.whatsapp.webrtc.CallActivity;
import com.squareup.picasso.Picasso;
//import com.tougee.recorderview.AudioRecordView;

import java.util.Objects;

public class ChatActivity extends BaseActivity{
    private String messageReceiverId;
    private MessagesAdapter messagesAdapter;
    private ActivityChatBinding binding;
    private CustomChatBarBinding customChatBarBinding;
    public static User receiver;
    private BottomSheetDialog bottomSheetDialog;
    private static final String TAG = "ConsoleChatActivity";

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
                        Uri fileUri = Objects.requireNonNull(data).getData();
                        if (getFileType(fileUri).equals("jpg")) {
                            FirebaseUtils.sendImage(currentUser.getUid(), messageReceiverId, fileUri, ChatActivity.this, binding.progressbar.getRoot());
                        } else if (getFileType(fileUri).equals("mp4")) {
                            FirebaseUtils.sendVideo(currentUser.getUid(), messageReceiverId, fileUri, ChatActivity.this, binding.progressbar.getRoot());
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
                        FirebaseUtils.sendImage(currentUser.getUid(), messageReceiverId, uri, ChatActivity.this, binding.progressbar.getRoot());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFields();
        updateStatusIndicator();
        initializeReceiver();
    }

    private void updateStatusIndicator() {
        final Handler handler = new Handler(Looper.getMainLooper());
        binding.messageInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (binding.messageInputText.getText().length() != 0) {
                    binding.camera.setVisibility(View.GONE);
                    binding.sendMessageBtn.setVisibility(View.VISIBLE);
                } else {
                    binding.camera.setVisibility(View.VISIBLE);
                    binding.sendMessageBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

                presenceDatabaseReference
                        .child(currentUser.getUid())
                        .setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);

            }
            final Runnable userStoppedTyping = () -> presenceDatabaseReference
                    .child(currentUser.getUid())
                    .setValue("Online");
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

    private void initializeReceiver() {
        userDatabaseReference
                .child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        receiver = snapshot.getValue(User.class);
                        updateChatBarDetails();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initToolBar() {
        setSupportActionBar(binding.chatToolBar.mainAppBar);

        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customChatBarBinding = CustomChatBarBinding.inflate(layoutInflater);
        actionBar.setCustomView(customChatBarBinding.getRoot());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initializeFields() {
        messageReceiverId = getIntent().getExtras().getString(getString(R.string.VISIT_USER_ID));
        initToolBar();

//        binding.recordView.activity = this;
//        binding.recordView.callback = (AudioRecordView.Callback) this;

        MessageViewModel viewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        viewModel.init(currentUser.getUid(), messageReceiverId);

        viewModel.getMessage().observe(this, messages -> {
            messagesAdapter.notifyDataSetChanged();
            scrollToMessage();
        });

        binding.userMessageList.setLayoutManager(new LinearLayoutManager(this));
        messagesAdapter = new MessagesAdapter( ChatActivity.this, currentUser.getUid(), messageReceiverId, viewModel.getMessage().getValue());
        binding.userMessageList.setAdapter(messagesAdapter);

        View contentView = View.inflate(ChatActivity.this, R.layout.attachment_bottom_sheet_layout, null);

        bottomSheetDialog = new BottomSheetDialog(ChatActivity.this);
        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        ((View) contentView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        binding.userMessageList.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                binding.userMessageList.postDelayed(() -> {
                    if (messagesAdapter.getItemCount() != 0)
                        binding.userMessageList.smoothScrollToPosition(messagesAdapter.getItemCount() - 1);
                }, 100);
            }
        });

        handleButtonClicks();
    }

    private void scrollToMessage() {
        if (messagesAdapter.getItemCount() != 0) {
            if (getIntent().hasExtra(getString(R.string.MESSAGE_ID))) {
                int position = messagesAdapter.getItemPosition(getIntent().getStringExtra(getString(R.string.MESSAGE_ID)));
                getIntent().removeExtra(getString(R.string.MESSAGE_ID));
                binding.userMessageList.postDelayed(() -> {
                    Objects.requireNonNull(binding.userMessageList.findViewHolderForAdapterPosition(position)).itemView.findViewById(R.id.my_linear_layout).setBackgroundTintList(ContextCompat.getColorStateList(ChatActivity.this, R.color.colorPrimary));
                    new Handler().postDelayed(() -> Objects.requireNonNull(binding.userMessageList.findViewHolderForAdapterPosition(position)).itemView.findViewById(R.id.my_linear_layout).setBackgroundTintList(null), 500);
                }, 200);
            } else {
                binding.userMessageList.smoothScrollToPosition(messagesAdapter.getItemCount() - 1);
            }
        }
    }

    private void updateChatBarDetails() {
        customChatBarBinding.name.setText(receiver.getName());
        Picasso.get().load(receiver.getImage()).placeholder(R.drawable.profile_image).into(customChatBarBinding.userImage);
    }

    private void handleButtonClicks() {
        binding.sendMessageBtn.setOnClickListener(view -> {
            FirebaseUtils.sendMessage(binding.messageInputText.getText().toString(), currentUser.getUid(), receiver.getUid());
            binding.messageInputText.setText("");
        });
        binding.camera.setOnClickListener(view -> cameraButtonClicked());
        customChatBarBinding.voiceCall.setOnClickListener(view -> Toast.makeText(this, receiver.getName(), Toast.LENGTH_SHORT).show());
        customChatBarBinding.videoCall.setOnClickListener(view -> {
            Notification notification = new Notification(currentUser.getName(), "Incoming Video Call", TYPE_VIDEO_CALL, currentUser.getImage(), receiver.getToken(), currentUser.getUid(), receiver.getUid());
            FCMNotificationSender.SendNotification(ApplicationClass.context, notification);

            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra(context.getString(R.string.CALLER), currentUser.getUid());
            intent.putExtra(getString(R.string.RECEIVER), receiver.getUid());
            intent.putExtra(getString(R.string.IS_CALL_MADE), true);
            startActivity(intent);
        });
        customChatBarBinding.userImage.setOnClickListener(view -> WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(customChatBarBinding.userImage, binding.expandedImage.cardView, binding.expandedImage.image, binding.chatToolBar.getRoot().getRootView(), receiver.getImage()));
        binding.attachMenu.setOnClickListener(view -> showAttachmentMenu());
    }

    private void showAttachmentMenu() {
        bottomSheetDialog.show();

        LinearLayout camera = bottomSheetDialog.findViewById(R.id.camera_btn);
        LinearLayout attachment = bottomSheetDialog.findViewById(R.id.photo_video_attachment_btn);
        Button cancel = bottomSheetDialog.findViewById(R.id.cancel);

        Objects.requireNonNull(camera).setOnClickListener(view -> {
            cameraButtonClicked();
            bottomSheetDialog.dismiss();
        });
        Objects.requireNonNull(attachment).setOnClickListener(view -> {
            attachmentButtonClicked();
            bottomSheetDialog.dismiss();
        });
        Objects.requireNonNull(cancel).setOnClickListener(view -> bottomSheetDialog.dismiss());
    }

    private void cameraButtonClicked() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageCaptureActivityResultLauncher.launch(intent);
    }

    private void attachmentButtonClicked() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/* video/*");
        imagePickActivityResultLauncher.launch(intent);
    }

    public void showImagePreview(View thumbView, String url) {
        WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(thumbView, binding.expandedImage.cardView, binding.expandedImage.image, binding.chatToolBar.getRoot().getRootView(), url);
    }

    public void showVideoPreview(View thumbView, String url) {
        WhatsappLikeProfilePicPreview.Companion.zoomVideoFromThumb(thumbView, binding.expandedVideoCardView, binding.chatToolBar.getRoot().getRootView());

        MediaController mediaController= new MediaController(ChatActivity.this);
        mediaController.setAnchorView(binding.expandedVideoCardView);

        binding.video.setMediaController(mediaController);
        binding.video.setVideoURI(Uri.parse(url));
        binding.video.requestFocus();
        binding.video.start();
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

    @Override
    public void onBackPressed() {
        if (binding.expandedImage.cardView.getVisibility() == View.VISIBLE) {
            WhatsappLikeProfilePicPreview.Companion.dismissPhotoPreview();
        } else if (binding.expandedVideoCardView.getVisibility() == View.VISIBLE) {
            binding.video.stopPlayback();
            binding.userMessageList.setClickable(true);
            WhatsappLikeProfilePicPreview.Companion.dismissVideoPreview();
        } else {
            finish();
        }
    }

//    @Override
//    public boolean isReady() {
//        return true;
//    }
//
//    @Override
//    public void onRecordCancel() {
//        Log.wtf(TAG, "onRecordCancel: called");
//        binding.camera.setVisibility(View.VISIBLE);
//        binding.attachMenu.setVisibility(View.VISIBLE);
//        binding.messageInputText.setVisibility(View.VISIBLE);
//        binding.sendMessageBtn.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void onRecordEnd() {
//        Log.wtf(TAG, "onRecordEnd: called");
//        binding.camera.setVisibility(View.VISIBLE);
//        binding.attachMenu.setVisibility(View.VISIBLE);
//        binding.messageInputText.setVisibility(View.VISIBLE);
//        binding.sendMessageBtn.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void onRecordStart() {
//        Log.wtf(TAG, "onRecordStart: called");
//        binding.camera.setVisibility(View.INVISIBLE);
//        binding.attachMenu.setVisibility(View.INVISIBLE);
//        binding.messageInputText.setVisibility(View.INVISIBLE);
//        binding.sendMessageBtn.setVisibility(View.INVISIBLE);
//    }
}