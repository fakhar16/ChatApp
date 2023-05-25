package com.samsung.whatsapp.repository;

import static com.samsung.whatsapp.ApplicationClass.messageDatabaseReference;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.model.Message;

import java.util.ArrayList;

public class MessageRepositoryImpl implements MessageRepository {
    static MessageRepositoryImpl instance;
    private final ArrayList<Message> mMessages = new ArrayList<>();
    MutableLiveData<ArrayList<Message>> messages = new MutableLiveData<>();

    public static MessageRepositoryImpl getInstance() {
        if(instance == null) {
            instance = new MessageRepositoryImpl();
        }
        return instance;
    }

    @Override
    public MutableLiveData<ArrayList<Message>> getMessages(String sender, String receiver) {
        if (mMessages.size() == 0)
            loadMessages(sender, receiver);

        messages.setValue(mMessages);
        return messages;
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
}
