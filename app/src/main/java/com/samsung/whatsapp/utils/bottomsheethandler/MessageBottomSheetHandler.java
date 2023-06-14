package com.samsung.whatsapp.utils.bottomsheethandler;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.utils.FirebaseUtils;
import com.samsung.whatsapp.utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

public class MessageBottomSheetHandler {
    public static void start(Context context, Message message, int STAR_VISIBILITY, int VIEW_TYPE, ArrayList<Message> messages, View clicked_message) {
        View contentView = View.inflate(context, R.layout.message_bottom_sheet_layout, null);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        ((View) contentView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        LinearLayout star = bottomSheetDialog.findViewById(R.id.star);
        LinearLayout copy = bottomSheetDialog.findViewById(R.id.copy);
        LinearLayout forward = bottomSheetDialog.findViewById(R.id.forward);
        LinearLayout delete = bottomSheetDialog.findViewById(R.id.delete);
        Button cancel = bottomSheetDialog.findViewById(R.id.cancel);

        assert star != null;
        assert copy != null;
        assert forward != null;
        assert delete != null;
        assert cancel != null;

        //Copy click handler
        copy.setOnClickListener(view -> {
            Utils.copyMessage(message.getMessage());
            bottomSheetDialog.dismiss();
        });

        //Cancel click handler
        cancel.setOnClickListener(view -> bottomSheetDialog.dismiss());

        //Star click handler
        if (STAR_VISIBILITY == View.VISIBLE) {
            ((TextView)(Objects.requireNonNull(bottomSheetDialog.findViewById(R.id.star_text)))).setText(context.getString(R.string.UNSTAR));
            ((ImageView)(Objects.requireNonNull(bottomSheetDialog.findViewById(R.id.star_icon)))).setImageResource(R.drawable.baseline_unstar_24);
            star.setOnClickListener(view -> {
                FirebaseUtils.unStarMessage(message);
                bottomSheetDialog.dismiss();
            });
        } else {
            ((TextView)(Objects.requireNonNull(bottomSheetDialog.findViewById(R.id.star_text)))).setText(context.getString(R.string.STAR));
            ((ImageView)(Objects.requireNonNull(bottomSheetDialog.findViewById(R.id.star_icon)))).setImageResource(R.drawable.baseline_star_24);
            star.setOnClickListener(view -> {
                FirebaseUtils.starMessage(message);
                bottomSheetDialog.dismiss();
            });
        }

        //Delete click handler
        delete.setOnClickListener(view -> {
            DeleteMessageBottomSheetHandler.start(context, VIEW_TYPE, message, messages);
            bottomSheetDialog.dismiss();
        });

        //Forward click handler
        forward.setOnClickListener(view -> {
            ForwardMessageBottomSheetHandler.start(context, message);
            bottomSheetDialog.dismiss();
        });



        //Showing bottom sheet dialog
        clicked_message.setOnLongClickListener(view -> {
            bottomSheetDialog.show();
            return true;
        });
    }
}
