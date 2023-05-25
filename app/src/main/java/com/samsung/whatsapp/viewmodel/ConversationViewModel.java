package com.samsung.whatsapp.viewmodel;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.User;

import java.util.ArrayList;
import java.util.Objects;

public class ConversationViewModel extends ViewModel {
    MutableLiveData<ArrayList<User>> users;

    public LiveData<ArrayList<User>> getUsers() {
        if (users == null) {
            users = new MutableLiveData<>();
            loadConversations();
        }
        return users;
    }

    public void loadConversations() {
        ArrayList<User> userArrayList = new ArrayList<>();

        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    userArrayList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot.hasChild(context.getString(R.string.NAME))) {
                            User user = dataSnapshot.getValue(User.class);

                            if (!Objects.requireNonNull(user).getUid().equals(FirebaseAuth.getInstance().getUid()))
                                userArrayList.add(user);
                            }
                    }
                    users.setValue(userArrayList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
