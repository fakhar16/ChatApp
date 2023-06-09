package com.samsung.whatsapp.repository.interfaces;

import androidx.lifecycle.MutableLiveData;

import com.samsung.whatsapp.model.Message;

import java.util.ArrayList;

public interface IMessageRepository {
    MutableLiveData<ArrayList<Message>> getMessages(String sender, String receiver);
    MutableLiveData<ArrayList<Message>> getStarredMessages(String uid);
}
