package com.samsung.whatsapp.utils.bottomsheethandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.ContactAdapter;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.repository.ContactsRepositoryImpl;
import com.samsung.whatsapp.utils.FirebaseUtils;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.view.activities.ChatActivity;

public class ForwardMessageBottomSheetHandler {
    @SuppressLint("StaticFieldLeak")
    private static BottomSheetDialog bottomSheetDialog;
    private static Message message;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static void start(Context context, Message msg) {
        View contentView = View.inflate(context, R.layout.forward_message_bottom_sheet_layout, null);

        message = msg;
        mContext = context;
        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.show();


        TextView cancel = bottomSheetDialog.findViewById(R.id.cancel);
        EditText search = bottomSheetDialog.findViewById(R.id.search);
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
        search.setOnFocusChangeListener((view, b) -> {
            if (b)
                ll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        });
    }

    private static void showContactList(BottomSheetDialog dialog, Context context) {
        RecyclerView contactList = dialog.findViewById(R.id.contactList);
        assert contactList != null;
        contactList.setLayoutManager(new LinearLayoutManager(context));

        ContactAdapter adapter = new ContactAdapter(ContactsRepositoryImpl.getInstance().getContacts().getValue());
        contactList.addItemDecoration(new DividerItemDecoration(contactList.getContext(), DividerItemDecoration.VERTICAL));
        contactList.setAdapter(adapter);
    }

    public static void forwardMessage(String receiver) {
        FirebaseUtils.sendMessage(message.getMessage(), Utils.currentUser.getUid(), receiver);
        bottomSheetDialog.dismiss();

        sendUserToChatActivity(receiver);
    }

    private static void sendUserToChatActivity(String receiver) {
        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra(mContext.getString(R.string.VISIT_USER_ID), receiver);
        mContext.startActivity(intent);
    }
}
