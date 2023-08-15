package com.samsung.whatsapp.utils.bottomsheethandler;

import static com.samsung.whatsapp.ApplicationClass.context;
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
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.PhoneContactAdapter;
import com.samsung.whatsapp.model.PhoneContact;
import com.samsung.whatsapp.model.User;

import java.util.ArrayList;
import java.util.Objects;

public class ShareContactBottomSheetHandler {
    @SuppressLint("StaticFieldLeak")
    private static BottomSheetDialog bottomSheetDialog;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static void start(Context context) {
        View contentView = View.inflate(context, R.layout.share_contact_bottom_sheet_layout, null);

        mContext = context;
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.show();

        loadContactListFromPhone();
    }

    @SuppressLint("Range")
    private static void loadContactListFromPhone() {
        ArrayList<User> users = new ArrayList<>();
        ArrayList<PhoneContact> phoneContacts = new ArrayList<>();

        @SuppressLint("Recycle") Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);


        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot.hasChild(context.getString(R.string.NAME))) {
                            User user = dataSnapshot.getValue(User.class);
                            if (!Objects.requireNonNull(user).getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                users.add(user);
                            }
                        }
                    }
                }

                while (phones.moveToNext()) {
                    String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    User user = getUserByPhone(users, phone);
                    if (user != null) {
                        PhoneContact phoneContact = new PhoneContact(phone);
                        if (name != null)
                            phoneContact.setName(name);
                        if (user.getImage() != null)
                            phoneContact.setImage(user.getImage());
                        if (user.getStatus() != null)
                            phoneContact.setStatus(user.getStatus());

                        phoneContacts.add(phoneContact);
                    }
                }
                setupListView(phoneContacts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private static User getUserByPhone(ArrayList<User> users, String phone) {
        for (User user : users) {
            if (user.getPhone_number().equals(phone))
                return user;
        }
        return null;
    }

    private static void setupListView(ArrayList<PhoneContact> phoneContacts) {
        PhoneContactAdapter adapter = new PhoneContactAdapter(context, phoneContacts);
        ListView contactList = bottomSheetDialog.findViewById(R.id.contactList);
        contactList.setAdapter(adapter);
    }
}
