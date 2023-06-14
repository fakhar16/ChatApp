package com.samsung.whatsapp.utils.bottomsheethandler;

import static com.samsung.whatsapp.utils.Utils.ITEM_RECEIVE;
import static com.samsung.whatsapp.utils.Utils.currentUser;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.Objects;

public class DeleteMessageBottomSheetHandler {
    public static void start(Context context, int VIEW_TYPE, Message message, ArrayList<Message> messages) {
        View contentView = View.inflate(context, R.layout.delete_message_bottom_sheet_layout, null);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        ((View) contentView.getParent()).setBackgroundColor(Color.TRANSPARENT);
        bottomSheetDialog.show();

        TextView delete_for_everyone = bottomSheetDialog.findViewById(R.id.delete_for_everyone);
        TextView delete_for_me = bottomSheetDialog.findViewById(R.id.delete_for_me);
        Button cancel = bottomSheetDialog.findViewById(R.id.cancel);

        if (VIEW_TYPE == ITEM_RECEIVE) {
            Objects.requireNonNull(delete_for_everyone).setVisibility(View.GONE);
        }

        //Cancel button handler
        Objects.requireNonNull(cancel).setOnClickListener(view -> bottomSheetDialog.dismiss());

        //Delete for everyone clicked
        Objects.requireNonNull(delete_for_everyone).setOnClickListener(view -> {
            FirebaseUtils.deleteMessageForEveryone(message);
            updateLastMessage(messages, message);
            bottomSheetDialog.dismiss();
        });

        Objects.requireNonNull(delete_for_me).setOnClickListener(view -> {
            FirebaseUtils.deleteMessage(message);
            updateLastMessage(messages, message);
            bottomSheetDialog.dismiss();
        });
    }

    private static void updateLastMessage(ArrayList<Message> userMessageList, Message message) {
        // if user removed last message in the list
        if (userMessageList.indexOf(message) == userMessageList.size() -1 && userMessageList.size() >= 2)
            FirebaseUtils.updateLastMessage(userMessageList.get(userMessageList.size() - 2));
        //if user removed the only message
        if (userMessageList.size() == 1)
            FirebaseUtils.removeLastMessages(message.getFrom(), message.getTo());

        //if deleted message is starred
        if (message.getStarred().contains(":" + currentUser.getUid()))
            FirebaseUtils.deleteStarredMessage(message.getMessageId());
    }
}
