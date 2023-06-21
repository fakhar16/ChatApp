package com.samsung.whatsapp.utils.bottomsheethandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.ContactAdapter;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.repository.ContactsRepositoryImpl;
import com.samsung.whatsapp.utils.FirebaseUtils;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.view.activities.ChatActivity;

import java.util.ArrayList;
import java.util.Objects;

public class ForwardMessageBottomSheetHandler {
    @SuppressLint("StaticFieldLeak")
    private static BottomSheetDialog bottomSheetDialog;
    private static Message message;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    @SuppressLint("StaticFieldLeak")
    private static ContactAdapter adapter;

    public static void start(Context context, Message msg) {
        View contentView = View.inflate(context, R.layout.forward_message_bottom_sheet_layout, null);

        message = msg;
        mContext = context;
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.show();


        TextView cancel = bottomSheetDialog.findViewById(R.id.cancel);
        SearchView search = bottomSheetDialog.findViewById(R.id.search);
        LinearLayout ll = bottomSheetDialog.findViewById(R.id.upperBar);

        assert cancel != null;
        assert search != null;
        assert ll != null;

        showContactList(bottomSheetDialog, context);

        //Cancel button handler
        cancel.setOnClickListener(view -> {
            if (ll.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                ll.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                ((InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                search.clearFocus();
            } else {
                bottomSheetDialog.dismiss();
            }
        });

        //search focus handler
        search.setOnQueryTextFocusChangeListener((view, b) -> {
            if (b)
                ll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        });


        //search filter handler
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private static void filter(String text) {
        ArrayList<User> filteredList = new ArrayList<>();

        for (User item : Objects.requireNonNull(ContactsRepositoryImpl.getInstance().getContacts().getValue())) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            adapter.filterList(filteredList);
        }else {
            adapter.filterList(new ArrayList<>());
        }
    }

    private static void showContactList(BottomSheetDialog dialog, Context context) {
        RecyclerView contactList = dialog.findViewById(R.id.contactList);
        assert contactList != null;
        contactList.setLayoutManager(new LinearLayoutManager(context));

        adapter = new ContactAdapter(context, ContactsRepositoryImpl.getInstance().getContacts().getValue());
        contactList.addItemDecoration(new DividerItemDecoration(contactList.getContext(), DividerItemDecoration.VERTICAL));
        contactList.setAdapter(adapter);
    }

    public static void forwardMessage(Context context, String receiver) {
        if (message.getType().equals(context.getString(R.string.TEXT)))
            FirebaseUtils.sendMessage(message.getMessage(), Utils.currentUser.getUid(), receiver);
        else if (message.getType().equals(context.getString(R.string.IMAGE))) {
            FirebaseUtils.forwardImage(context, message, receiver);
        }
        bottomSheetDialog.dismiss();
        sendUserToChatActivity(receiver);
    }

    private static void sendUserToChatActivity(String receiver) {
        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra(mContext.getString(R.string.VISIT_USER_ID), receiver);
        mContext.startActivity(intent);
    }
}
