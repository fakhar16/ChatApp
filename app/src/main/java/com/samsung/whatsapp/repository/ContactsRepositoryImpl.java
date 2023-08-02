package com.samsung.whatsapp.repository;

import static com.samsung.whatsapp.ApplicationClass.context;
import static com.samsung.whatsapp.ApplicationClass.messageDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;


import android.annotation.SuppressLint;
import android.database.Cursor;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.repository.interfaces.IContactsRepository;
import com.samsung.whatsapp.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

public class ContactsRepositoryImpl implements IContactsRepository {
    static ContactsRepositoryImpl instance;
    private final ArrayList<User> mUsers = new ArrayList<>();
    MutableLiveData<ArrayList<User>> users = new MutableLiveData<>();
    MutableLiveData<ArrayList<User>> usersWithUnreadChats = new MutableLiveData<>();
    private final ArrayList<String> contactList = new ArrayList<>();

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

    @Override
    public MutableLiveData<ArrayList<User>> getContactsWithUnreadChats() {
        loadContactsWitUnreadChats();
        return usersWithUnreadChats;
    }

    private void loadContactsWitUnreadChats() {
        ArrayList<User> finalUsers = (ArrayList<User>) mUsers.clone();
        for (User user : mUsers) {
            messageDatabaseReference
                    .child(Utils.currentUser.getUid())
                    .child(user.getUid())
                    .limitToLast(1)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot child: snapshot.getChildren()) {
                                    Message message = child.getValue(Message.class);
                                    assert message != null;
                                    if (!message.isUnread()) {
                                        finalUsers.remove(user);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
        usersWithUnreadChats.postValue(finalUsers);
    }

    private void loadContacts() {
        loadContactListFromPhone();
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    mUsers.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot.hasChild(context.getString(R.string.NAME))) {
                            User user = dataSnapshot.getValue(User.class);
                            if (!Objects.requireNonNull(user).getUid().equals(FirebaseAuth.getInstance().getUid()) && contactList.contains(user.getPhone_number()))
                                mUsers.add(user);
                        }
                    }
                    users.postValue(mUsers);
                    loadContactsWitUnreadChats();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @SuppressLint("Range")
    private void loadContactListFromPhone() {
        @SuppressLint("Recycle") Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));

            if (phone != null) {
                contactList.add(phone);
            }
        }
    }
}
