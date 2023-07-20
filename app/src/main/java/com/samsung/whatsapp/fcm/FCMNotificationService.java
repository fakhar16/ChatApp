package com.samsung.whatsapp.fcm;

import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.utils.Utils.ACTION_REJECT_CALL;
import static com.samsung.whatsapp.utils.Utils.INCOMING_CALL_CHANNEL_ID;
import static com.samsung.whatsapp.utils.Utils.INCOMING_CALL_NOTIFICATION_ID;
import static com.samsung.whatsapp.utils.Utils.INCOMING_MESSAGE_NOTIFICATION_ID;
import static com.samsung.whatsapp.utils.Utils.MESSAGE_CHANNEL_ID;
import static com.samsung.whatsapp.utils.Utils.TYPE_DISCONNECT_CALL_BY_OTHER_USER;
import static com.samsung.whatsapp.utils.Utils.TYPE_DISCONNECT_CALL_BY_USER;
import static com.samsung.whatsapp.utils.Utils.TYPE_MESSAGE;
import static com.samsung.whatsapp.utils.Utils.TYPE_VIDEO_CALL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Person;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.view.activities.CallingActivity;
import com.samsung.whatsapp.view.activities.ChatActivity;
import com.samsung.whatsapp.view.broadcast.HungUpBroadcast;
import com.samsung.whatsapp.view.broadcast.ReplyBroadcast;
import com.samsung.whatsapp.webrtc.CallActivity;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class FCMNotificationService extends FirebaseMessagingService {
    public static final String KEY_TEXT_REPLY = "text_reply";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> data =  remoteMessage.getData();

        String type = data.get(context.getString(R.string.TYPE));

        if (Objects.equals(type, TYPE_MESSAGE)) {
            try {
                showMessageNotification(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (Objects.equals(type, TYPE_VIDEO_CALL)) {
            try {
                showVideoCallNotification(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (Objects.equals(type, TYPE_DISCONNECT_CALL_BY_USER)) {
            NotificationManagerCompat.from(context).cancel(INCOMING_CALL_NOTIFICATION_ID);
            // Todo: Show missed call log here
        } else if (Objects.equals(type, TYPE_DISCONNECT_CALL_BY_OTHER_USER)) {
            getApplicationContext().sendBroadcast(new Intent(ACTION_REJECT_CALL));
        }

        super.onMessageReceived(remoteMessage);
    }

    private void showMessageNotification(Map<String, String> data) throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Bitmap bitmap = Picasso.get().load(data.get(context.getString(R.string.ICON))).get();
        String senderId = data.get(context.getString(R.string.SENDER_ID));
        String receiverId = data.get(context.getString(R.string.RECEIVER_ID));
        String title = data.get(context.getString(R.string.TITLE));
        String message = data.get(context.getString(R.string.MESSAGE));

        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(context.getString(R.string.VISIT_USER_ID), senderId);
        intent.putExtra(context.getString(R.string.CURRENT_USER_ID), receiverId);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationChannel channel = new NotificationChannel(MESSAGE_CHANNEL_ID, "Message Notification", NotificationManager.IMPORTANCE_HIGH);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        //Direct Reply Intent
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY).setLabel("Reply").build();
        Intent replyIntent = new Intent(context, ReplyBroadcast.class);
        replyIntent.putExtra(context.getString(R.string.VISIT_USER_ID), senderId);
        replyIntent.putExtra(context.getString(R.string.CURRENT_USER_ID), receiverId);
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, 0, replyIntent, PendingIntent.FLAG_ONE_SHOT|PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.app_logo, "Reply", replyPendingIntent).addRemoteInput(remoteInput).build();

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, MESSAGE_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.app_logo)
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(action);

        NotificationManagerCompat.from(this).notify(INCOMING_MESSAGE_NOTIFICATION_ID, notification.build());
    }

    private void showVideoCallNotification(Map<String, String> data) throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        AudioAttributes audioAttr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioManager.STREAM_RING)
                .build();

        NotificationChannelCompat notificationChannel =
                new NotificationChannelCompat.Builder(INCOMING_CALL_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
                        .setName("Incoming calls")
                        .setDescription("Incoming audio and video call alerts")
                        .setSound(soundUri, audioAttr)
                        .build();

        String title = data.get(context.getString(R.string.TITLE));
        String icon = data.get(context.getString(R.string.ICON));
        String receiverId = data.get(context.getString(R.string.RECEIVER_ID));
        String senderId = data.get(context.getString(R.string.SENDER_ID));

        Bitmap bitmap = Picasso.get().load(icon).get();
        Icon largeIcon = Icon.createWithBitmap(bitmap);

        //Accept call intents
        Intent answerIntent = new Intent(getApplicationContext(), CallActivity.class);
        answerIntent.putExtra(context.getString(R.string.CALL_ACCEPTED), true);
        answerIntent.putExtra(context.getString(R.string.CALLER), receiverId);
        PendingIntent answerPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //Reject call intents
        Intent rejectIntent = new Intent(getApplicationContext(), HungUpBroadcast.class);
        rejectIntent.putExtra(context.getString(R.string.RECEIVER_ID), receiverId);
        rejectIntent.putExtra(context.getString(R.string.SENDER_ID), senderId);
        PendingIntent rejectPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //Show incoming call full screen intents
        Intent showIncomingCallIntent = new Intent(getApplicationContext(), CallingActivity.class);
        showIncomingCallIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        showIncomingCallIntent.putExtra(context.getString(R.string.IMAGE), icon);
        showIncomingCallIntent.putExtra(context.getString(R.string.NAME), title);
        showIncomingCallIntent.putExtra(context.getString(R.string.FRIEND_USER_NAME), receiverId);
        PendingIntent showIncomingCallPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, showIncomingCallIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create a new call with the user as caller.
        Person incoming_caller = new Person.Builder()
                .setName(title)
                .setIcon(largeIcon)
                .setImportant(true)
                .build();

        // Create a call style notification for an incoming call.
        Notification.Builder builder = new Notification.Builder(context, INCOMING_CALL_CHANNEL_ID)
                .setSmallIcon(Icon.createWithResource(context, R.drawable.app_logo))
                .setContentTitle("Incoming call")
                .setContentText("Whatsapp video call")
                .setStyle(Notification.CallStyle.forIncomingCall(incoming_caller, rejectPendingIntent, answerPendingIntent))
                .setContentIntent(showIncomingCallPendingIntent)
                .setFullScreenIntent(showIncomingCallPendingIntent, true)
                .addPerson(incoming_caller)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_CALL);

        NotificationManagerCompat.from(getApplicationContext()).createNotificationChannel(notificationChannel);
        NotificationManagerCompat.from(getApplicationContext()).notify(INCOMING_CALL_NOTIFICATION_ID, builder.build());
    }
}
