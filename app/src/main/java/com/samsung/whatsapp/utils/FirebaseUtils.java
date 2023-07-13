package com.samsung.whatsapp.utils;

import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.ApplicationClass.imageStorageReference;
import static com.samsung.whatsapp.ApplicationClass.imageUrlDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.messageDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.starMessagesDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.videoStorageReference;
import static com.samsung.whatsapp.utils.Utils.TYPE_MESSAGE;
import static com.samsung.whatsapp.utils.Utils.currentUser;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.fcm.FCMNotificationSender;
import com.samsung.whatsapp.interfaces.MessageListenerCallback;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.model.Notification;
import com.samsung.whatsapp.model.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FirebaseUtils {
    public static void sendMessage(String message, String messageSenderId, String messageReceiverId) {
        if (!TextUtils.isEmpty(message)) {
            String messageSenderRef = context.getString(R.string.MESSAGES) + "/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = context.getString(R.string.MESSAGES) + "/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef =
                    messageDatabaseReference
                            .child(messageSenderId)
                            .child(messageReceiverId)
                            .push();

            String messagePushId = userMessageKeyRef.getKey();
            Message obj_message = new Message(messagePushId, message, "text", messageSenderId, messageReceiverId, new Date().getTime(), -1, "");

            Map<String, Object> messageBodyDetails = new HashMap<>();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, obj_message);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, obj_message);

            FirebaseDatabase.getInstance().getReference()
                    .updateChildren(messageBodyDetails);

            updateLastMessage(obj_message);
            sendNotification(message, messageReceiverId, messageSenderId, TYPE_MESSAGE);
        }
    }

    public static void sendNotification(String message, String receiverId, String senderId, String type) {
        userDatabaseReference
                .child(receiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User receiver = snapshot.getValue(User.class);
                            userDatabaseReference
                                    .child(senderId)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                User sender = snapshot.getValue(User.class);
                                                assert sender != null;
                                                assert receiver != null;
                                                Notification notification = new Notification(sender.getName(), message, type, sender.getImage(), receiver.getToken(), sender.getUid(), receiver.getUid());
                                                FCMNotificationSender.SendNotification(ApplicationClass.context, notification);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public static void updateLastMessage(Message message) {
        Map<String, Object> lastMsgObj = new HashMap<>();
        lastMsgObj.put(context.getString(R.string.LAST_MESSAGE_TIME), message.getTime());
        if (message.getType().equals(context.getString(R.string.IMAGE)))
            lastMsgObj.put(context.getString(R.string.LAST_MESSAGE_DETAILS), "Photo");
        else if (message.getType().equals(context.getString(R.string.VIDEO)))
            lastMsgObj.put(context.getString(R.string.LAST_MESSAGE_DETAILS), "Video");
        else
            lastMsgObj.put(context.getString(R.string.LAST_MESSAGE_DETAILS), message.getMessage());

        messageDatabaseReference
                .child(message.getFrom())
                .child(context.getString(R.string.LAST_MESSAGE_WITH_) + message.getTo())
                .updateChildren(lastMsgObj);

        messageDatabaseReference
                .child(message.getTo())
                .child(context.getString(R.string.LAST_MESSAGE_WITH_) + message.getFrom())
                .updateChildren(lastMsgObj);
    }

    public static void removeLastMessages(String sender, String receiver) {
        messageDatabaseReference
                .child(sender)
                .child(context.getString(R.string.LAST_MESSAGE_WITH_) + receiver)
                .removeValue();
    }

    public static void sendImage(Context context, String messageSenderId, String messageReceiverId, Uri fileUri) {
        MessageListenerCallback callback = (MessageListenerCallback) context;
        String messageSenderRef = context.getString(R.string.MESSAGES) + "/" + messageSenderId + "/" + messageReceiverId;
        String messageReceiverRef = context.getString(R.string.MESSAGES) + "/" + messageReceiverId + "/" + messageSenderId;

        DatabaseReference userMessageKeyRef =
                messageDatabaseReference
                        .child(messageSenderId)
                        .child(messageReceiverId)
                        .push();

        String messagePushId = userMessageKeyRef.getKey();
        StorageReference filePath = imageStorageReference.child(messagePushId + ".jpg");
        StorageTask<UploadTask.TaskSnapshot> uploadTask = filePath.putFile(fileUri);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return filePath.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onMessageSent();
                Uri downloadUrl = task.getResult();
                String myUrl = downloadUrl.toString();

                Message obj_message = new Message(messagePushId, myUrl, context.getString(R.string.IMAGE), messageSenderId, messageReceiverId, new Date().getTime(), -1, "");

                Map<String, Object> messageBodyDetails = new HashMap<>();
                messageBodyDetails.put(messageSenderRef + "/" + messagePushId, obj_message);
                messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, obj_message);

                FirebaseDatabase.getInstance().getReference()
                        .updateChildren(messageBodyDetails);

                Map<String, Object> imageUrlUserDetails = new HashMap<>();
                imageUrlUserDetails.put(messageSenderId, true);
                imageUrlUserDetails.put(messageReceiverId, true);

                assert messagePushId != null;
                imageUrlDatabaseReference
                        .child(messagePushId)
                        .updateChildren(imageUrlUserDetails);

                updateLastMessage(obj_message);
                sendNotification("Sent an image", messageReceiverId, messageSenderId, TYPE_MESSAGE);
            }
        }).addOnFailureListener(e -> callback.onMessageSentFailed());
    }

    public static void forwardImage(Context context, Message message, String receiver) {
        MessageListenerCallback callback = (MessageListenerCallback) context;
        Message obj_message = new Message(message.getMessageId(), message.getMessage(), message.getType(), currentUser.getUid(), receiver,new Date().getTime(), -1, "");

        String messageSenderRef = context.getString(R.string.MESSAGES) + "/" + obj_message.getFrom() + "/" + obj_message.getTo();
        String messageReceiverRef = context.getString(R.string.MESSAGES) + "/" + obj_message.getTo() + "/" + obj_message.getFrom();

        Map<String, Object> messageBodyDetails = new HashMap<>();
        messageBodyDetails.put(messageSenderRef + "/" + message.getMessageId(), obj_message);
        messageBodyDetails.put(messageReceiverRef + "/" + message.getMessageId(), obj_message);

        FirebaseDatabase.getInstance().getReference()
                .updateChildren(messageBodyDetails)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        callback.onMessageSent();
                    }
                });

        Map<String, Object> imageUrlUserDetails = new HashMap<>();
        imageUrlUserDetails.put(currentUser.getUid(), true);
        imageUrlUserDetails.put(receiver, true);

        imageUrlDatabaseReference
                .child(message.getMessageId())
                .updateChildren(imageUrlUserDetails);

        updateLastMessage(obj_message);
        sendNotification("Sent an image", obj_message.getTo(), obj_message.getFrom(), TYPE_MESSAGE);
    }

    public static void sendVideo(String messageSenderId, String messageReceiverId, Uri fileUri, Activity activity, View dialog) {
        String messageSenderRef = context.getString(R.string.MESSAGES) + "/" + messageSenderId + "/" + messageReceiverId;
        String messageReceiverRef = context.getString(R.string.MESSAGES) + "/" + messageReceiverId + "/" + messageSenderId;

        DatabaseReference userMessageKeyRef =
                messageDatabaseReference
                        .child(messageSenderId)
                        .child(messageReceiverId)
                        .push();

        String messagePushId = userMessageKeyRef.getKey();

        StorageReference filePath = videoStorageReference.child(messagePushId + ".mp4");

        filePath.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    //noinspection StatementWithEmptyBody
                    while (!uriTask.isSuccessful());

                    Utils.dismissLoadingBar(activity, dialog);

                    String downloadUri = uriTask.getResult().toString();
                    Message obj_message = new Message(messagePushId, downloadUri, context.getString(R.string.VIDEO), messageSenderId, messageReceiverId, new Date().getTime(), -1, "");

                    Map<String, Object> messageBodyDetails = new HashMap<>();
                    messageBodyDetails.put(messageSenderRef + "/" + messagePushId, obj_message);
                    messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, obj_message);

                    FirebaseDatabase.getInstance().getReference()
                            .updateChildren(messageBodyDetails);

                    updateLastMessage(obj_message);
                    sendNotification("Sent a video", messageReceiverId, messageSenderId, TYPE_MESSAGE);
                });
    }

    public static void starMessage(Message message) {
        String starredUser = message.getStarred() + ":" + Utils.currentUser.getUid();
        message.setStarred(context.getString(R.string.STARRED));

        starMessagesDatabaseReference
                .child(Utils.currentUser.getUid())
                .child(message.getMessageId())
                .setValue(message);

        messageDatabaseReference
                .child(message.getFrom())
                .child(message.getTo())
                .child(message.getMessageId())
                .child(context.getString(R.string.STARRED))
                .setValue(starredUser);

        messageDatabaseReference
                .child(message.getTo())
                .child(message.getFrom())
                .child(message.getMessageId())
                .child(context.getString(R.string.STARRED))
                .setValue(starredUser);
    }

    public static void unStarMessage(Message message) {
        String starredUser = message.getStarred().replace(":"+Utils.currentUser.getUid(), "");

        starMessagesDatabaseReference
                .child(Utils.currentUser.getUid())
                .child(message.getMessageId())
                .removeValue();

        messageDatabaseReference
                .child(message.getFrom())
                .child(message.getTo())
                .child(message.getMessageId())
                .child(context.getString(R.string.STARRED))
                .setValue(starredUser);

        messageDatabaseReference
                .child(message.getTo())
                .child(message.getFrom())
                .child(message.getMessageId())
                .child(context.getString(R.string.STARRED))
                .setValue(starredUser);
    }

    public static void deleteStarredMessage(String message_id) {
        starMessagesDatabaseReference
                .child(Utils.currentUser.getUid())
                .child(message_id)
                .removeValue();
    }

    public static void deleteMessage(Message message) {
        messageDatabaseReference
                .child(message.getFrom())
                .child(message.getTo())
                .child(message.getMessageId())
                .removeValue();

        if (message.getType().equals(context.getString(R.string.IMAGE))) {
            imageUrlDatabaseReference
                    .child(message.getMessageId())
                    .child(currentUser.getUid())
                    .removeValue();
        }
    }

    public static void deleteMessageForEveryone(Message message) {
        messageDatabaseReference
                .child(message.getFrom())
                .child(message.getTo())
                .child(message.getMessageId())
                .removeValue();

        messageDatabaseReference
                .child(message.getTo())
                .child(message.getFrom())
                .child(message.getMessageId())
                .removeValue();

        if (message.getType().equals(context.getString(R.string.IMAGE))) {
            imageUrlDatabaseReference
                    .child(message.getMessageId())
                    .child(message.getFrom())
                    .removeValue();

            imageUrlDatabaseReference
                    .child(message.getMessageId())
                    .child(message.getTo())
                    .removeValue();

            imageUrlDatabaseReference.child(message.getMessageId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                imageStorageReference.getStorage().getReferenceFromUrl(message.getMessage()).delete();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

//        if (message.getType().equals(context.getString(R.string.VIDEO))) {
////            videoStorageReference.getStorage().getReferenceFromUrl(message.getMessage()).delete();
//        }
    }
}
