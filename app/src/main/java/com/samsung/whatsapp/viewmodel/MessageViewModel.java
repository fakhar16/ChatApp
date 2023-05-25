package com.samsung.whatsapp.viewmodel;

import static com.samsung.whatsapp.ApplicationClass.messageDatabaseReference;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.model.Message;

import java.util.ArrayList;

public class MessageViewModel extends ViewModel {
//    private static final String TAG = "ConsoleMessageViewModel";
    MutableLiveData<ArrayList<Message>> messages;
    public LiveData<ArrayList<Message>> getMessage(String sender, String receiver) {
        if (messages == null) {
            messages = new MutableLiveData<>();
            loadMessages(sender, receiver);
        }
        return messages;
    }

    public void loadMessages(String messageSenderId, String messageReceiverId) {
        ArrayList<Message> messageArrayList = new ArrayList<>();

        messageDatabaseReference
                .child(messageSenderId)
                .child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageArrayList.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Message message = snapshot1.getValue(Message.class);
                                messageArrayList.add(message);
                            }
                        }
                            messages.setValue(messageArrayList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
