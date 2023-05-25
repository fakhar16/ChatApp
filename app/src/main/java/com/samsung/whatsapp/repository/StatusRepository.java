package com.samsung.whatsapp.repository;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.model.UserStatus;

import java.util.ArrayList;

public interface StatusRepository {
    MutableLiveData<ArrayList<UserStatus>> getStatuses();
    void uploadStatus(Intent data, User user, View dialog, Activity activity);
}
