package com.samsung.whatsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.repository.ContactsRepositoryImpl;

import java.util.ArrayList;

public class ContactsViewModel extends ViewModel {
    MutableLiveData<ArrayList<User>> users;

    public LiveData<ArrayList<User>> getContacts() {
        return users;
    }

    public void init() {
        if (users != null)
            return;

        users = ContactsRepositoryImpl.getInstance().getContacts();
    }
}
