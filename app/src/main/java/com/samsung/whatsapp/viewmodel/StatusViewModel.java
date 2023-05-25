package com.samsung.whatsapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.samsung.whatsapp.model.UserStatus;
import com.samsung.whatsapp.repository.StatusRepositoryImpl;

import java.util.ArrayList;

public class StatusViewModel extends ViewModel {
    MutableLiveData<ArrayList<UserStatus>> userStatuses;
    public LiveData<ArrayList<UserStatus>> getUserStatues() {
        return userStatuses;
    }

    public void init() {
        if (userStatuses != null)
            return;

        userStatuses = StatusRepositoryImpl.getInstance().getStatuses();
    }
}
