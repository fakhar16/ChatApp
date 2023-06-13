package com.samsung.whatsapp.view.broadcast;


import static com.samsung.whatsapp.ApplicationClass.videoUserDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.INCOMING_CALL_NOTIFICATION_ID;
import static com.samsung.whatsapp.utils.Utils.TYPE_DISCONNECT_CALL_BY_OTHER_USER;

import androidx.core.app.NotificationManagerCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.utils.FirebaseUtils;

public class HungUpBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String receiverId = intent.getStringExtra(ApplicationClass.context.getString(R.string.RECEIVER_ID));
        String senderId = intent.getStringExtra(ApplicationClass.context.getString(R.string.SENDER_ID));

        videoUserDatabaseReference.child(receiverId).setValue(null);

        NotificationManagerCompat.from(context).cancel(INCOMING_CALL_NOTIFICATION_ID);
        FirebaseUtils.sendNotification("", senderId, receiverId, TYPE_DISCONNECT_CALL_BY_OTHER_USER);
    }

}