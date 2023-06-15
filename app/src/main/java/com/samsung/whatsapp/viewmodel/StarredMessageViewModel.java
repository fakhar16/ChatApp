package com.samsung.whatsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.repository.MessageRepositoryImpl;

import java.util.ArrayList;

public class StarredMessageViewModel extends ViewModel {
    MutableLiveData<ArrayList<Message>> messages;
    MutableLiveData<ArrayList<Message>> messagesWithReceiver;
    public LiveData<ArrayList<Message>> getStarredMessage() {
        return messages;
    }
    public LiveData<ArrayList<Message>> getStarredMessageWithReceiver() {
        return messagesWithReceiver;
    }

    public void initStarMessages() {
        if (messages != null)
            return;

        messages = MessageRepositoryImpl.getInstance().getStarredMessages();
    }

    public void initStarMessagesWithReceiver() {
        if (messagesWithReceiver != null)
            return;

        messagesWithReceiver = MessageRepositoryImpl.getInstance().getStarredMessagesMatchingReceiver();
    }
}
