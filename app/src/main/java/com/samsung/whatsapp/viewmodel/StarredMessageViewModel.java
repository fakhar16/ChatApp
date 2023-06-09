package com.samsung.whatsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.repository.MessageRepositoryImpl;

import java.util.ArrayList;

public class StarredMessageViewModel extends ViewModel {
    MutableLiveData<ArrayList<Message>> messages;
    public LiveData<ArrayList<Message>> getStarredMessage() {
        return messages;
    }

    public void init(String uid) {
        if (messages != null)
            return;

        messages = MessageRepositoryImpl.getInstance().getStarredMessages(uid);
    }
}
