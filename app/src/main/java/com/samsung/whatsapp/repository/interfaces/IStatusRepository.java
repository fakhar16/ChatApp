package com.samsung.whatsapp.repository.interfaces;

import android.app.Activity;
import android.net.Uri;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.model.UserStatus;

import java.util.ArrayList;

public interface IStatusRepository {
    MutableLiveData<ArrayList<UserStatus>> getStatuses();
    void uploadStatus(Uri data, User user, View dialog, Activity activity);
}
