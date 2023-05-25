package com.samsung.whatsapp.repository;

import androidx.lifecycle.MutableLiveData;

import com.samsung.whatsapp.model.User;

import java.util.ArrayList;

public interface ContactsRepository {
    MutableLiveData<ArrayList<User>> getContacts();
}
