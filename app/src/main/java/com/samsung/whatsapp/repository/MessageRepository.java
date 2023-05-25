package com.samsung.whatsapp.repository;

import androidx.lifecycle.MutableLiveData;

import com.samsung.whatsapp.model.Message;

import java.util.ArrayList;

public interface MessageRepository {
    MutableLiveData<ArrayList<Message>> getMessages(String sender, String receiver);
}
