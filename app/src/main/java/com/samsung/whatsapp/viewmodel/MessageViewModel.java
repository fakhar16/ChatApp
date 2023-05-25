package com.samsung.whatsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.repository.MessageRepositoryImpl;

import java.util.ArrayList;

public class MessageViewModel extends ViewModel {
    MutableLiveData<ArrayList<Message>> messages;
    public LiveData<ArrayList<Message>> getMessage() {
        return messages;
    }

    public void init(String sender, String receiver) {
        if (messages != null)
            return;

        messages = MessageRepositoryImpl.getInstance().getMessages(sender, receiver);
    }
}
