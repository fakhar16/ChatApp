package com.samsung.whatsapp.view.broadcast;

import static com.samsung.whatsapp.utils.Utils.INCOMING_MESSAGE_NOTIFICATION_ID;

import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationManagerCompat;

import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.fcm.FCMNotificationService;
import com.samsung.whatsapp.utils.FirebaseUtils;

public class ReplyBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        handleDirectReply(context, intent);
    }

    private void handleDirectReply(Context context, Intent intent) {
        Bundle remoteReply = RemoteInput.getResultsFromIntent(intent);
        String messageReceiverId = intent.getExtras().getString(ApplicationClass.application.getApplicationContext().getString(R.string.VISIT_USER_ID));
        String messageSenderId = intent.getExtras().getString(ApplicationClass.application.getApplicationContext().getString(R.string.CURRENT_USER_ID));

        if (remoteReply != null) {
            String message = remoteReply.getCharSequence(FCMNotificationService.KEY_TEXT_REPLY).toString();
            FirebaseUtils.sendMessage(message, messageSenderId, messageReceiverId);
            NotificationManagerCompat.from(context).cancel(INCOMING_MESSAGE_NOTIFICATION_ID);
        }
    }
}
