package com.samsung.whatsapp.repository.interfaces;

import androidx.lifecycle.MutableLiveData;

import com.samsung.whatsapp.model.User;

import java.util.ArrayList;

public interface IContactsRepository {
    MutableLiveData<ArrayList<User>> getContacts();
}
