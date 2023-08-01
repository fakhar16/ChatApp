package com.samsung.whatsapp.view.activities;

import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.ApplicationClass.presenceDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.TYPE_VIDEO_CALL;
import static com.samsung.whatsapp.utils.Utils.currentUser;
import static com.samsung.whatsapp.utils.Utils.getFileSize;
import static com.samsung.whatsapp.utils.Utils.getFilename;
import static com.samsung.whatsapp.utils.Utils.getFileType;
import static com.samsung.whatsapp.utils.Utils.hideKeyboard;
import static com.samsung.whatsapp.utils.Utils.showLoadingBar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.MessagesAdapter;
import com.samsung.whatsapp.databinding.ActivityChatBinding;
import com.samsung.whatsapp.databinding.CustomChatBarBinding;
import com.samsung.whatsapp.fcm.FCMNotificationSender;
import com.samsung.whatsapp.interfaces.MessageListenerCallback;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.model.Notification;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.utils.FirebaseUtils;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;
import com.samsung.whatsapp.viewmodel.MessageViewModel;
import com.samsung.whatsapp.webrtc.CallActivity;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ChatActivity extends BaseActivity implements MessageListenerCallback {
    private String messageReceiverId;
    private MessagesAdapter messagesAdapter;
    private ActivityChatBinding binding;
    private CustomChatBarBinding customChatBarBinding;
    public static User receiver;
    private BottomSheetDialog bottomSheetDialog;
    private MessageViewModel viewModel;

    private final ActivityResultLauncher<Intent> docPickActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri fileUri = Objects.requireNonNull(data).getData();
                    prepareDocMessageForSending(fileUri, "", false, getFilename(this, fileUri), getFileSize(fileUri));
                }
            });
    private final ActivityResultLauncher<Intent> imagePickActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Uri fileUri = Objects.requireNonNull(data).getData();
                    if (getFileType(fileUri).equals("jpg")) {
                        prepareImageMessageForSending(fileUri, "",false);
                    } else if (getFileType(fileUri).equals("mp4")) {
                        prepareVideoMessageForSending(fileUri, "", false);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> mediaCaptureResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                String fileType = data.getStringExtra(getString(R.string.FILE_TYPE));

                if (fileType.equals(getString(R.string.IMAGE))) {
                    Uri fileUri = Uri.parse(data.getStringExtra(getString(R.string.IMAGE_URI)));

                    Bitmap bitmap;
                    try {
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), fileUri);
                        bitmap = ImageDecoder.decodeBitmap(source);
                        Matrix matrix = new Matrix();
                        matrix.preRotate(0);
                        Bitmap finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        OutputStream os=getContentResolver().openOutputStream(fileUri);
                        finalBitmap.compress(Bitmap.CompressFormat.PNG,100,os);

                        prepareImageMessageForSending(fileUri, "",false);
                        binding.capturedImage.cancel.setOnClickListener(view -> {
                            hideKeyboard(ChatActivity.this);
                            binding.capturedImage.cardView.setVisibility(View.GONE);
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (fileType.equals(getString(R.string.VIDEO))) {
                    Uri fileUri = Uri.parse(data.getStringExtra(getString(R.string.VIDEO_URI)));
                    prepareVideoMessageForSending(fileUri, "", false);
                    binding.capturedVideo.cancel.setOnClickListener(view -> {
                        hideKeyboard(ChatActivity.this);
                        binding.capturedImage.cardView.setVisibility(View.GONE);
                    });
                }
            }
        }
    });

    private void prepareImageMessageForSending(Uri fileUri, String messageId, boolean isImageFromClipboard) {
        binding.capturedImage.cardView.setVisibility(View.VISIBLE);
        Picasso.get().load(fileUri).into(binding.capturedImage.image);
        binding.capturedImage.receiverName.setText(receiver.getName());

        binding.capturedImage.sendMessage.setOnClickListener(view -> {
            String caption = binding.capturedImage.caption.getText().toString();
            binding.capturedImage.caption.setText("");
            hideKeyboard(this);
            binding.capturedImage.cardView.setVisibility(View.GONE);
            showLoadingBar(ChatActivity.this, binding.progressbar.getRoot());
            if (isImageFromClipboard) {
                Message obj_message = new Message(messageId, fileUri.toString(), getString(R.string.IMAGE), currentUser.getUid(), receiver.getUid(), new Date().getTime(), -1, "", true);
                FirebaseUtils.forwardImage(ChatActivity.this, obj_message, receiver.getUid(), caption);
            } else {
                FirebaseUtils.sendImage(ChatActivity.this, currentUser.getUid(), messageReceiverId, fileUri, caption);
            }
        });
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void prepareVideoMessageForSending(Uri fileUri, String messageId, boolean isVideoFromClipboard) {
        binding.capturedVideo.cardView.setVisibility(View.VISIBLE);
        binding.capturedVideo.receiverName.setText(receiver.getName());

        ExoPlayer player = new ExoPlayer.Builder(this).build();
        binding.capturedVideo.video.setShowNextButton(false);
        binding.capturedVideo.video.setShowPreviousButton(false);
        binding.capturedVideo.video.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(fileUri);
        player.setMediaItem(mediaItem);
        player.prepare();

        binding.capturedVideo.sendMessage.setOnClickListener(view -> {
            String caption = binding.capturedVideo.caption.getText().toString();
            binding.capturedVideo.caption.setText("");
            hideKeyboard(this);
            binding.capturedVideo.cardView.setVisibility(View.GONE);
            showLoadingBar(ChatActivity.this, binding.progressbar.getRoot());
            if (isVideoFromClipboard) {
                Message obj_message = new Message(messageId, fileUri.toString(), getString(R.string.VIDEO), currentUser.getUid(), receiver.getUid(),new Date().getTime(), -1, "", true);
                FirebaseUtils.forwardVideo(ChatActivity.this, obj_message, receiver.getUid(), caption);
            } else {
                FirebaseUtils.sendVideo(this, currentUser.getUid(), messageReceiverId, fileUri, caption);
            }
        });
    }

    private void prepareDocMessageForSending(Uri fileUri, String messageId, boolean isDocFromClipboard, String fileName, String fileSize) {
        binding.capturedImage.cardView.setVisibility(View.VISIBLE);
        binding.capturedImage.image.setImageResource(R.drawable.baseline_picture_as_pdf_24);
        binding.capturedImage.receiverName.setText(receiver.getName());

        binding.capturedImage.sendMessage.setOnClickListener(view -> {
            String caption = binding.capturedImage.caption.getText().toString();
            binding.capturedImage.caption.setText("");
            hideKeyboard(this);
            binding.capturedImage.cardView.setVisibility(View.GONE);
            showLoadingBar(ChatActivity.this, binding.progressbar.getRoot());
            Message obj_message = new Message(messageId, fileUri.toString(), getString(R.string.PDF_FILES), currentUser.getUid(), receiver.getUid(),new Date().getTime(), -1, "", fileName, fileSize, true);
            if (isDocFromClipboard) {
                FirebaseUtils.forwardDoc(ChatActivity.this, obj_message, receiver.getUid(), caption);
            } else {
                FirebaseUtils.sendDoc(ChatActivity.this, currentUser.getUid(), messageReceiverId, fileUri, fileName, fileSize, caption);
            }
        });
    }

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
                if (Objects.requireNonNull(binding.messageInputText.getText()).length() != 0) {
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
//        binding.recordView.activity = this;
//        binding.recordView.callback = (AudioRecordView.Callback) this;

        messageReceiverId = getIntent().getExtras().getString(getString(R.string.VISIT_USER_ID));

        initToolBar();
        setupViewModel();
        setupRecyclerView();
        setupAttachmentBottomSheetMenu();
        handleButtonClicks();
        checkIfSearchMessageTriggered();
        initProgressBar();
        handleMessageEditTextListener();
    }

    private void initProgressBar() {
        binding.progressbar.dialogTitle.setText(getString(R.string.SENDING_FILE_TITLE));
        binding.progressbar.dialogDescription.setText(getString(R.string.SENDING_FILE_DESCRIPTION));
    }

    private void checkIfSearchMessageTriggered() {
        if (getIntent().getBooleanExtra(getString(R.string.SEARCH_MESSAGE), false)) {
            binding.chatToolBar.getRoot().setVisibility(View.INVISIBLE);
            binding.searchBar.setVisibility(View.VISIBLE);
            binding.search.requestFocus();
        }

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        binding.cancel.setOnClickListener(view -> {
            binding.search.setQuery("", false);
            binding.chatToolBar.getRoot().setVisibility(View.VISIBLE);
            binding.searchBar.setVisibility(View.GONE);
        });
    }

    private void filter(String text) {
        ArrayList<Message> filteredList = new ArrayList<>();

        for (Message item : Objects.requireNonNull(viewModel.getMessage().getValue())) {
            if (item.getMessage().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            messagesAdapter.filterList(filteredList);
        } else {
            messagesAdapter.filterList(new ArrayList<>());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        viewModel.init(currentUser.getUid(), messageReceiverId);

        viewModel.getMessage().observe(this, messages -> {
            messagesAdapter.notifyDataSetChanged();
            scrollToMessage();
        });
    }

    private void setupRecyclerView() {
        binding.userMessageList.setLayoutManager(new LinearLayoutManager(this));
        messagesAdapter = new MessagesAdapter( ChatActivity.this, currentUser.getUid(), messageReceiverId, viewModel.getMessage().getValue());
        binding.userMessageList.setAdapter(messagesAdapter);


        binding.userMessageList.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                binding.userMessageList.postDelayed(() -> {
                    if (messagesAdapter.getItemCount() != 0)
                        binding.userMessageList.smoothScrollToPosition(messagesAdapter.getItemCount() - 1);
                }, 100);
            }
        });
    }

    private void setupAttachmentBottomSheetMenu() {
        View contentView = View.inflate(ChatActivity.this, R.layout.attachment_bottom_sheet_layout, null);

        bottomSheetDialog = new BottomSheetDialog(ChatActivity.this);
        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        ((View) contentView.getParent()).setBackgroundColor(Color.TRANSPARENT);
    }

    private void scrollToMessage() {
        if (messagesAdapter.getItemCount() != 0) {
            if (getIntent().hasExtra(getString(R.string.MESSAGE_ID))) {
                int position = messagesAdapter.getItemPosition(getIntent().getStringExtra(getString(R.string.MESSAGE_ID)));
                getIntent().removeExtra(getString(R.string.MESSAGE_ID));
                binding.userMessageList.postDelayed(() -> {
                    Objects.requireNonNull(binding.userMessageList.findViewHolderForAdapterPosition(position)).itemView.findViewById(R.id.my_linear_layout).setBackgroundTintList(ContextCompat.getColorStateList(ChatActivity.this, R.color.colorPrimary));
                    new Handler(Looper.getMainLooper()).postDelayed(() -> Objects.requireNonNull(binding.userMessageList.findViewHolderForAdapterPosition(position)).itemView.findViewById(R.id.my_linear_layout).setBackgroundTintList(null), 500);
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
        binding.sendMessageBtn.setOnClickListener(view -> sendMessage());
        binding.camera.setOnClickListener(view -> cameraButtonClicked());
        binding.attachMenu.setOnClickListener(view -> showAttachmentMenu());

        customChatBarBinding.voiceCall.setOnClickListener(view -> Toast.makeText(this, receiver.getName(), Toast.LENGTH_SHORT).show());
        customChatBarBinding.videoCall.setOnClickListener(view -> createVideoCall());
        customChatBarBinding.userImage.setOnClickListener(view -> WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(customChatBarBinding.userImage, binding.expandedImage.cardView, binding.expandedImage.image, binding.chatToolBar.getRoot().getRootView(), receiver.getImage()));
        customChatBarBinding.userInfo.setOnClickListener(view -> sendUserToProfileActivity());
    }

    private void handleMessageEditTextListener() {
        binding.messageInputText.addListener(() -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData primaryClipData = clipboardManager.getPrimaryClip();

            if (primaryClipData != null) {
                ClipData.Item item = primaryClipData.getItemAt(0);
                if (primaryClipData.getItemCount() > 1) {
                    ClipData.Item message_id_item = primaryClipData.getItemAt(1);
                    ClipData.Item message_type_item = primaryClipData.getItemAt(2);
                    ClipData.Item file_name_item = primaryClipData.getItemAt(3);
                    ClipData.Item file_size_item = primaryClipData.getItemAt(4);
                    Uri uri = item.getUri();

                    binding.messageInputText.setText("");
                    hideKeyboard(this);

                    if (message_type_item.getText().toString().equals(context.getString(R.string.IMAGE))) {
                        prepareImageMessageForSending(uri, message_id_item.getText().toString(), true);
                    } else if (message_type_item.getText().toString().equals(context.getString(R.string.VIDEO))) {
                        prepareVideoMessageForSending(uri, message_id_item.getText().toString(), true);
                    } else if (message_type_item.getText().toString().equals(context.getString(R.string.PDF_FILES))) {
                        prepareDocMessageForSending(uri, message_id_item.getText().toString(), true, file_name_item.getText().toString(), file_size_item.getText().toString());
                    }
                }
            }
        });
    }

    private void sendMessage() {
        String message = Objects.requireNonNull(binding.messageInputText.getText()).toString();
        if (URLUtil.isValidUrl(message)) {
            FirebaseUtils.sendURLMessage(message, currentUser.getUid(), receiver.getUid());
        } else {
            FirebaseUtils.sendMessage(message, currentUser.getUid(), receiver.getUid());
        }
        binding.messageInputText.setText("");
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

    private void sendUserToProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(getString(R.string.RECEIVER_ID), receiver.getUid());
        startActivity(intent);
    }

    private void showAttachmentMenu() {
        bottomSheetDialog.show();

        LinearLayout camera = bottomSheetDialog.findViewById(R.id.camera_btn);
        LinearLayout attachment = bottomSheetDialog.findViewById(R.id.photo_video_attachment_btn);
        LinearLayout doc = bottomSheetDialog.findViewById(R.id.document_btn);

        Button cancel = bottomSheetDialog.findViewById(R.id.cancel);
        
        assert camera != null;
        assert attachment != null;
        assert doc != null;
        assert cancel != null;

        camera.setOnClickListener(view -> {
            cameraButtonClicked();
            bottomSheetDialog.dismiss();
        });
        attachment.setOnClickListener(view -> {
            attachmentButtonClicked();
            bottomSheetDialog.dismiss();
        });
        doc.setOnClickListener(view -> {
            attachDocButtonClicked();
            bottomSheetDialog.dismiss();
        });
        cancel.setOnClickListener(view -> bottomSheetDialog.dismiss());
    }

    private void attachDocButtonClicked() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        docPickActivityResultLauncher.launch(intent);
    }

    private void cameraButtonClicked() {
        Intent intent = new Intent(ChatActivity.this, CameraxActivity.class);
        mediaCaptureResultLauncher.launch(intent);
    }

    private void attachmentButtonClicked() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/* video/*");
        imagePickActivityResultLauncher.launch(intent);
    }

    public void showImagePreview(View thumbView, String url) {
        WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(thumbView, binding.expandedImage.cardView, binding.expandedImage.image, binding.chatToolBar.getRoot().getRootView(), url);
    }

    @SuppressLint("UnsafeOptInUsageError")
    public void showVideoPreview(View thumbView, String url) {
        WhatsappLikeProfilePicPreview.Companion.zoomVideoFromThumb(thumbView, binding.expandedVideo.cardView, binding.chatToolBar.getRoot().getRootView());
        ExoPlayer player = new ExoPlayer.Builder(this).build();
        binding.expandedVideo.video.setPlayer(player);
        binding.expandedVideo.video.setShowNextButton(false);
        binding.expandedVideo.video.setShowPreviousButton(false);

        MediaItem mediaItem = MediaItem.fromUri(url);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
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
        } else if (binding.expandedVideo.cardView.getVisibility() == View.VISIBLE) {
            Objects.requireNonNull(binding.expandedVideo.video.getPlayer()).release();
            binding.userMessageList.setClickable(true);
            WhatsappLikeProfilePicPreview.Companion.dismissVideoPreview();
        } else {
            finish();
        }
    }

    @Override
    public void onMessageSent() {
        Utils.dismissLoadingBar(this, binding.progressbar.getRoot());
    }

    @Override
    public void onMessageSentFailed() {
        Utils.dismissLoadingBar(this, binding.progressbar.getRoot());
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