package com.samsung.whatsapp.utils.bottomsheethandler;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.PhoneContactAdapter;
import com.samsung.whatsapp.model.PhoneContact;
import com.samsung.whatsapp.model.User;

import java.util.ArrayList;
import java.util.Objects;

public class ShareContactBottomSheetHandler {
    @SuppressLint("StaticFieldLeak")
    private static BottomSheetDialog bottomSheetDialog;
    private static ArrayList<PhoneContact> phoneContacts;
    @SuppressLint("StaticFieldLeak")
    private static PhoneContactAdapter adapter;

    private static ArrayList<User> users;
    private static String receiver;

    public static void start(Context context, String messageReceiverId) {
        View contentView = View.inflate(context, R.layout.share_contact_bottom_sheet_layout, null);

        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.show();

        phoneContacts = new ArrayList<>();
        users = new ArrayList<>();
        receiver = messageReceiverId;

        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot.hasChild(ApplicationClass.application.getApplicationContext().getString(R.string.NAME))) {
                            User user = dataSnapshot.getValue(User.class);
                            if (!Objects.requireNonNull(user).getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                users.add(user);
                            }
                        }
                    }
                }
                loadContactListFromPhone();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("Range")
    private static void loadContactListFromPhone() {

        @SuppressLint("Recycle") Cursor phones = ApplicationClass.application.getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (phones.moveToNext()) {
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            User user = getUserByPhone(users, phone);
            PhoneContact phoneContact = new PhoneContact(phone);

            if (name != null)
                phoneContact.setName(name);
            if (user != null) {
                if (user.getImage() != null)
                    phoneContact.setImage(user.getImage());
                if (user.getStatus() != null)
                    phoneContact.setStatus(user.getStatus());
            }
            if (!isNumberExist(name))
                phoneContacts.add(phoneContact);
        }
        setupListView();
    }

    private static boolean isNumberExist(String name) {
        for (PhoneContact phoneContact : phoneContacts) {
            if (phoneContact.getName().equals(name))
                return true;
        }
        return false;
    }

    private static User getUserByPhone(ArrayList<User> users, String phone) {
        for (User user : users) {
            if (user.getPhone_number().equals(phone))
                return user;
        }
        return null;
    }

    private static void setupListView() {
        adapter = new PhoneContactAdapter(ApplicationClass.application.getApplicationContext(), ShareContactBottomSheetHandler.phoneContacts, receiver);
        ListView contactList = bottomSheetDialog.findViewById(R.id.contactList);
        assert contactList != null;
        contactList.setAdapter(adapter);
    }

}
