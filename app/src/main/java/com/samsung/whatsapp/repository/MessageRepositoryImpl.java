package com.samsung.whatsapp.repository;

import static com.samsung.whatsapp.ApplicationClass.messageDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.starMessagesDatabaseReference;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.repository.interfaces.IMessageRepository;

import java.util.ArrayList;

public class MessageRepositoryImpl implements IMessageRepository {
    static MessageRepositoryImpl instance;
    private ArrayList<Message> mMessages;
    private  ArrayList<Message> mStarredMessages;
    MutableLiveData<ArrayList<Message>> messages = new MutableLiveData<>();
    MutableLiveData<ArrayList<Message>> starMessages = new MutableLiveData<>();

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
    public MutableLiveData<ArrayList<Message>> getStarredMessages(String uid) {
        mStarredMessages = new ArrayList<>();
        loadStarMessages(uid);
        starMessages.setValue(mStarredMessages);
        return starMessages;
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

    public void loadStarMessages(String uid) {
        starMessagesDatabaseReference
                .child(uid)
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
}
