package com.samsung.whatsapp.repository;

import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.ApplicationClass.messageDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.starMessagesDatabaseReference;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.repository.interfaces.IMessageRepository;
import com.samsung.whatsapp.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

public class MessageRepositoryImpl implements IMessageRepository {
    static MessageRepositoryImpl instance;
    private ArrayList<Message> mMessages;
    private  ArrayList<Message> mStarredMessages;
    private  ArrayList<Message> mStarredMessagesWithReceiver;
    private  ArrayList<Message> mMediaMessagesWithReceiver;
    private  ArrayList<Message> mDocMessagesWithReceiver;
    private  ArrayList<Message> mLinksMessagesWithReceiver;
    MutableLiveData<ArrayList<Message>> messages = new MutableLiveData<>();
    MutableLiveData<ArrayList<Message>> starMessages = new MutableLiveData<>();
    MutableLiveData<ArrayList<Message>> starMessagesWithReceiver = new MutableLiveData<>();
    MutableLiveData<ArrayList<Message>> mediaMessagesWithReceiver = new MutableLiveData<>();
    MutableLiveData<ArrayList<Message>> docMessagesWithReceiver = new MutableLiveData<>();
    MutableLiveData<ArrayList<Message>> linksMessagesWithReceiver = new MutableLiveData<>();

    public static MessageRepositoryImpl getInstance() {
        if(instance == null) {
            instance = new MessageRepositoryImpl();
        }
        return instance;
    }

    @Override
    public MutableLiveData<ArrayList<Message>> getMessages(String sender, String receiver) {
        mMessages = new ArrayList<>();
        loadMessages(sender, receiver);
        messages.setValue(mMessages);
        return messages;
    }

    @Override
    public MutableLiveData<ArrayList<Message>> getStarredMessages() {
        mStarredMessages = new ArrayList<>();
        loadStarMessages();
        starMessages.setValue(mStarredMessages);
        return starMessages;
    }

    @Override
    public MutableLiveData<ArrayList<Message>> getStarredMessagesMatchingReceiver() {
        mStarredMessagesWithReceiver = new ArrayList<>();
        loadStarMessagesWithReceiver();
        starMessagesWithReceiver.setValue(mStarredMessagesWithReceiver);
        return starMessagesWithReceiver;
    }

    @Override
    public MutableLiveData<ArrayList<Message>> getMediaMessagesMatchingReceiver(String receiver) {
        mMediaMessagesWithReceiver = new ArrayList<>();
        loadMediaMessagesWithReceiver(receiver);
        mediaMessagesWithReceiver.setValue(mMediaMessagesWithReceiver);
        return mediaMessagesWithReceiver;
    }

    @Override
    public MutableLiveData<ArrayList<Message>> getLinksMessagesMatchingReceiver(String receiver) {
        mLinksMessagesWithReceiver = new ArrayList<>();
        loadLinksMessagesWithReceiver(receiver);
        linksMessagesWithReceiver.setValue(mLinksMessagesWithReceiver);
        return linksMessagesWithReceiver;
    }

    @Override
    public MutableLiveData<ArrayList<Message>> getDocMessagesMatchingReceiver(String receiver) {
        mDocMessagesWithReceiver = new ArrayList<>();
        loadDocMessagesWithReceiver(receiver);
        docMessagesWithReceiver.setValue(mDocMessagesWithReceiver);
        return docMessagesWithReceiver;
    }

    public void loadMessages(String messageSenderId, String messageReceiverId) {
        messageDatabaseReference
                .child(messageSenderId)
                .child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mMessages.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Message message = snapshot1.getValue(Message.class);
                                mMessages.add(message);
                            }
                        }
                        messages.postValue(mMessages);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void loadStarMessages() {
        starMessagesDatabaseReference
                .child(Utils.currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mStarredMessages.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Message message = snapshot1.getValue(Message.class);
                                mStarredMessages.add(message);
                            }
                        }
                        starMessages.postValue(mStarredMessages);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void loadStarMessagesWithReceiver() {
        starMessagesDatabaseReference
                .child(Utils.currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mStarredMessagesWithReceiver.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Message message = snapshot1.getValue(Message.class);
                                mStarredMessagesWithReceiver.add(message);
                            }

                            ArrayList<Message> temp = new ArrayList<>(mStarredMessagesWithReceiver);
                            mStarredMessagesWithReceiver.clear();
                            for (Message message : Objects.requireNonNull(messages.getValue())) {
                                for (Message tempMessage : temp) {
                                    if (tempMessage.getMessageId().equals(message.getMessageId()))
                                        mStarredMessagesWithReceiver.add(message);
                                }
                            }
                        }
                        starMessagesWithReceiver.postValue(mStarredMessagesWithReceiver);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void loadMediaMessagesWithReceiver(String receiver) {
        messageDatabaseReference
                .child(Utils.currentUser.getUid())
                .child(receiver)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mMediaMessagesWithReceiver.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Message message = child.getValue(Message.class);
                                assert message != null;
                                if (message.getType().equals(context.getString(R.string.IMAGE)) || message.getType().equals(context.getString(R.string.VIDEO)))
                                    mMediaMessagesWithReceiver.add(message);
                            }
                        }
                        mediaMessagesWithReceiver.postValue(mMediaMessagesWithReceiver);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void loadLinksMessagesWithReceiver(String receiver) {
        messageDatabaseReference
                .child(Utils.currentUser.getUid())
                .child(receiver)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mLinksMessagesWithReceiver.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Message message = child.getValue(Message.class);
                                assert message != null;
                                if (message.getType().equals(context.getString(R.string.URL)))
                                    mLinksMessagesWithReceiver.add(message);
                            }
                        }
                        linksMessagesWithReceiver.postValue(mLinksMessagesWithReceiver);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void loadDocMessagesWithReceiver(String receiver) {
        messageDatabaseReference
                .child(Utils.currentUser.getUid())
                .child(receiver)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mDocMessagesWithReceiver.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Message message = child.getValue(Message.class);
                                assert message != null;
                                if (message.getType().equals(context.getString(R.string.PDF_FILES)))
                                    mDocMessagesWithReceiver.add(message);
                            }
                        }
                        docMessagesWithReceiver.postValue(mDocMessagesWithReceiver);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
