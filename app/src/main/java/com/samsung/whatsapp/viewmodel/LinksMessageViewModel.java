package com.samsung.whatsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.repository.MessageRepositoryImpl;

import java.util.ArrayList;

public class LinksMessageViewModel extends ViewModel {
    MutableLiveData<ArrayList<Message>> messagesWithReceiver;
    public LiveData<ArrayList<Message>> getLinksMessageWithReceiver() {
        return messagesWithReceiver;
    }


    public void initLinksMessagesWithReceiver(String receiver) {
        if (messagesWithReceiver != null)
            return;

        messagesWithReceiver = MessageRepositoryImpl.getInstance().getLinksMessagesMatchingReceiver(receiver);
    }
}
