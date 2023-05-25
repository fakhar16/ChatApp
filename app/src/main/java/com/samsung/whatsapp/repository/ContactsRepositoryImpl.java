package com.samsung.whatsapp.repository;

import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;


import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.User;

import java.util.ArrayList;
import java.util.Objects;

public class ContactsRepositoryImpl implements ContactsRepository {
    static ContactsRepositoryImpl instance;
    private final ArrayList<User> mUsers = new ArrayList<>();
    MutableLiveData<ArrayList<User>> users = new MutableLiveData<>();

    public static ContactsRepositoryImpl getInstance() {
        if(instance == null) {
            instance = new ContactsRepositoryImpl();
        }
        return instance;
    }

    @Override
    public MutableLiveData<ArrayList<User>> getContacts() {
        if (mUsers.size() == 0)
            loadContacts();

        users.setValue(mUsers);
        return users;
    }

    private void loadContacts() {
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    mUsers.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot.hasChild(context.getString(R.string.NAME))) {
                            User user = dataSnapshot.getValue(User.class);

                            if (!Objects.requireNonNull(user).getUid().equals(FirebaseAuth.getInstance().getUid()))
                                mUsers.add(user);
                        }
                    }
                    users.postValue(mUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
