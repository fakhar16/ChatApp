package com.samsung.whatsapp.webrtc;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.videoUserDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.ACTION_REJECT_CALL;
import static com.samsung.whatsapp.utils.Utils.INCOMING_CALL_NOTIFICATION_ID;
import static com.samsung.whatsapp.utils.Utils.TYPE_DISCONNECT_CALL_BY_USER;
import static com.samsung.whatsapp.ApplicationClass.context;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivityCallBinding;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.utils.FirebaseUtils;
import com.samsung.whatsapp.view.activities.BaseActivity;
import com.samsung.whatsapp.webrtc.models.JavaScriptInterface;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class CallActivity extends BaseActivity {
    ActivityCallBinding binding;
    String sender = "";
    String receiver = "";
    String uniqueId = "";

//    Boolean isPeerConnected = false;
    Boolean isAudio = true;
    Boolean isVideo = true;
    BroadcastReceiver endCallReceiver;
    IntentFilter rejectFilter;
    private static final String TAG = "ConsoleCallActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NotificationManagerCompat.from(getApplicationContext()).cancel(INCOMING_CALL_NOTIFICATION_ID);

        rejectFilter = new IntentFilter();
        rejectFilter.addAction(ACTION_REJECT_CALL);

        endCallReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    if (intent.getAction().equals(ACTION_REJECT_CALL)) {
                        finish();
                    }
                }
            }
        };

        registerReceiver(endCallReceiver, rejectFilter);

        sender = getIntent().getStringExtra(context.getString(R.string.CALLER));

        binding.endCall.setOnClickListener(view -> disconnectCall());
        binding.endOngoingCall.setOnClickListener(view -> disconnectCall());
        binding.toggleAudioBtn.setOnClickListener(view -> {
            isAudio = !isAudio;
            callJavaScriptFunction("javascript:toggleAudio('" + isAudio +"')");
            if (isAudio) {
                binding.toggleAudioBtn.setImageResource(R.drawable.btn_unmute_normal);
            } else {
                binding.toggleAudioBtn.setImageResource(R.drawable.btn_mute_normal);
            }
        });
        binding.toggleVideoBtn.setOnClickListener(view -> {
            isVideo = !isVideo;
            callJavaScriptFunction("javascript:toggleVideo('" + isVideo +"')");
            if (isVideo) {
                binding.toggleVideoBtn.setImageResource(R.drawable.btn_video_normal);
            } else {
                binding.toggleVideoBtn.setImageResource(R.drawable.btn_video_muted);
            }
        });

        setupWebView();

        boolean isCallMade = getIntent().getBooleanExtra(context.getString(R.string.IS_CALL_MADE), false);

        if (isCallMade) {
            receiver = getIntent().getStringExtra(context.getString(R.string.RECEIVER));
            sendCallRequest();


            userDatabaseReference
                    .child(receiver)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getValue() != null) {
                                User user  = snapshot.getValue(User.class);
                                binding.callerName.setText(Objects.requireNonNull(user).getName());
                                Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(binding.callerImage);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void sendCallRequest() {
        videoUserDatabaseReference.child(receiver).child(context.getString(R.string.INCOMING)).setValue(sender);
        videoUserDatabaseReference.child(receiver).child(getString(R.string.IS_AVAILABLE))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (Objects.requireNonNull(snapshot.getValue()).toString().equals("true")) {
                                listenForConnId();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void listenForConnId() {
        videoUserDatabaseReference.child(receiver).child(getString(R.string.CONN_ID))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() == null)
                            return;

                        binding.callLayout.setVisibility(View.GONE);
                        binding.callControlLayout.setVisibility(View.VISIBLE);
                        callJavaScriptFunction("javascript:startCall('" + snapshot.getValue() + "')");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void setupWebView() {
        binding.webView.setWebChromeClient(new WebChromeClient() {
            public void onPermissionRequest(@NotNull PermissionRequest request) {
                request.grant(request.getResources());
            }
        });
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new JavaScriptInterface(), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        binding.webView.loadUrl(filePath);

        binding.webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(@NotNull WebView view, @NotNull String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }

    private void initializePeer() {
        uniqueId = getUniqueId();
        callJavaScriptFunction("javascript:init('" + uniqueId + "')");
        videoUserDatabaseReference.child(sender).child(context.getString(R.string.INCOMING))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null)
                            onCallRequest(snapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void onCallRequest(String caller) {
        Log.i(TAG, "onCallRequest: ");
        if (caller == null)
            return;


        boolean isCallAccepted = getIntent().getBooleanExtra(context.getString(R.string.CALL_ACCEPTED), false);

        if (isCallAccepted) {
            binding.callLayout.setVisibility(View.GONE);
            videoUserDatabaseReference.child(sender).child(context.getString(R.string.CONN_ID)).setValue(uniqueId);
            videoUserDatabaseReference.child(sender).child(context.getString(R.string.IS_AVAILABLE)).setValue(true);

            binding.callControlLayout.setVisibility(View.VISIBLE);
        }
    }

    public String getUniqueId() {
        return  UUID.randomUUID().toString();
    }

    public void callJavaScriptFunction(@NotNull final String function) {
        binding.webView.post(() -> binding.webView.loadUrl(function));
    }

    private void disconnectCall() {
        FirebaseUtils.sendNotification("", receiver, sender, TYPE_DISCONNECT_CALL_BY_USER);
        videoUserDatabaseReference.child(receiver).setValue(null);
        callJavaScriptFunction("javascript:disconnectCall()");
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        videoUserDatabaseReference.child(receiver).setValue(null);
        callJavaScriptFunction("javascript:disconnectCall()");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(endCallReceiver);
    }
}