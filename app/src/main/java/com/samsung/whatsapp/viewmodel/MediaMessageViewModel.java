package com.samsung.whatsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.repository.MessageRepositoryImpl;

import java.util.ArrayList;

public class MediaMessageViewModel extends ViewModel {
    MutableLiveData<ArrayList<Message>> messagesWithReceiver;
    public LiveData<ArrayList<Message>> getMediaMessageWithReceiver() {
        return messagesWithReceiver;
    }


    public void initMediaMessagesWithReceiver(String receiver) {
        if (messagesWithReceiver != null)
            return;

        messagesWithReceiver = MessageRepositoryImpl.getInstance().getMediaMessagesMatchingReceiver(receiver);
    }
}
