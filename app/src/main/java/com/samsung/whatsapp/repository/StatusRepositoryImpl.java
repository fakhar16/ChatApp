package com.samsung.whatsapp.repository;

import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.ApplicationClass.storiesDatabaseReference;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.Status;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.model.UserStatus;
import com.samsung.whatsapp.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class StatusRepositoryImpl implements StatusRepository {
    static StatusRepositoryImpl instance;
    private final ArrayList<UserStatus> mUserStatuses = new ArrayList<>();
    MutableLiveData<ArrayList<UserStatus>> userStatuses = new MutableLiveData<>();

    public static StatusRepositoryImpl getInstance() {
        if(instance == null) {
            instance = new StatusRepositoryImpl();
        }
        return instance;
    }

    @Override
    public MutableLiveData<ArrayList<UserStatus>> getStatuses() {
        if (mUserStatuses.size() == 0)
            loadStatuses();

        userStatuses.setValue(mUserStatuses);
        return userStatuses;
    }

    @Override
    public void uploadStatus(Intent data, User user, View dialog, Activity activity) {
        Date date = new Date();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference().child(context.getString(R.string.STATUS)).child(date.getTime() + "");

        reference.putFile(data.getData())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    UserStatus userStatus = new UserStatus();
                                    userStatus.setName(user.getName());
                                    userStatus.setProfileImage(user.getImage());
                                    userStatus.setLastUpdated(date.getTime());

                                    HashMap<String, Object> obj = new HashMap<>();
                                    obj.put(context.getString(R.string.NAME), userStatus.getName());
                                    obj.put(context.getString(R.string.PROFILE_IMAGE), userStatus.getProfileImage());
                                    obj.put(context.getString(R.string.LAST_UPDATED), userStatus.getLastUpdated());

                                    String imageUrl = uri.toString();
                                    Status status = new Status(imageUrl, userStatus.getLastUpdated());
                                    storiesDatabaseReference
                                            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                            .updateChildren(obj);

                                    storiesDatabaseReference
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .child(context.getString(R.string.STATUSES))
                                            .push()
                                            .setValue(status);

                                    Utils.dismissLoadingBar(activity, dialog);
                                });
                    }
                });
    }

    private void loadStatuses() {
        storiesDatabaseReference
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            mUserStatuses.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                UserStatus status = new UserStatus();
                                status.setName(dataSnapshot.child(context.getString(R.string.NAME)).getValue(String.class));
                                status.setProfileImage(dataSnapshot.child(context.getString(R.string.PROFILE_IMAGE)).getValue(String.class));
                                status.setLastUpdated(Objects.requireNonNull(dataSnapshot.child(context.getString(R.string.LAST_UPDATED)).getValue(Long.class)));

                                ArrayList<Status> statuses = new ArrayList<>();

                                for (DataSnapshot statusSnapShot : dataSnapshot.child(context.getString(R.string.STATUSES)).getChildren()) {
                                    Status sampleStatus = statusSnapShot.getValue(Status.class);
                                    statuses.add(sampleStatus);
                                }

                                status.setStatuses(statuses);
                                mUserStatuses.add(status);
                            }
                            userStatuses.postValue(mUserStatuses);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
