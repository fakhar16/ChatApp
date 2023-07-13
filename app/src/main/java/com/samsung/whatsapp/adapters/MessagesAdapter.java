package com.samsung.whatsapp.adapters;

import static com.samsung.whatsapp.utils.Utils.ITEM_RECEIVE;
import static com.samsung.whatsapp.utils.Utils.ITEM_SENT;
import static com.samsung.whatsapp.utils.Utils.currentUser;
import static com.samsung.whatsapp.utils.Utils.getDateTimeString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.samsung.whatsapp.databinding.ItemMessageBinding;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.utils.bottomsheethandler.MessageBottomSheetHandler;
import com.samsung.whatsapp.view.activities.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {
    private ArrayList<Message> userMessageList;
    private final Context context;
    private final String senderId;
    private final String receiverId;

    public MessagesAdapter(Context context, String senderId, String receiverId, ArrayList<Message> userMessageList) {
        this.context = context;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMessageBinding layoutBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_message, parent, false);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layoutBinding.myLinearLayout.getLayoutParams();
        if (viewType == ITEM_RECEIVE) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            layoutBinding.myLinearLayout.setLayoutParams(params);
            layoutBinding.myLinearLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.receiver_messages_layout));
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            layoutBinding.myLinearLayout.setLayoutParams(params);
            layoutBinding.myLinearLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.sender_messages_layout));
        }

        return new MessageViewHolder(layoutBinding);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = userMessageList.get(position);
        if (Objects.equals(FirebaseAuth.getInstance().getUid(), message.getFrom())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    public int getItemPosition(String message_id) {
        for (Message message : userMessageList) {
            if (message.getMessageId().equals(message_id))
                return userMessageList.indexOf(message);
        }
        return -1;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = userMessageList.get(position);
        holder.setIsRecyclable(false);

//        int[] reactions = new int[]{
//                R.drawable.ic_fb_like,
//                R.drawable.ic_fb_love,
//                R.drawable.ic_fb_laugh,
//                R.drawable.ic_fb_wow,
//                R.drawable.ic_fb_sad,
//                R.drawable.ic_fb_angry
//        };

//        ReactionsConfig config = new ReactionsConfigBuilder(context)
//                .withReactions(reactions)
//                .build();

//        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
//            if (pos < 0)
//                return false;
//
//            if (holder.getClass() == SenderViewHolder.class) {
//                SenderViewHolder viewHolder = (SenderViewHolder) holder;
//                viewHolder.binding.feeling.setImageResource(reactions[pos]);
//                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
//            } else {
//                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
//                viewHolder.binding.feeling.setImageResource(reactions[pos]);
//                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
//            }
//            message.setFeeling(pos);
//
//            messageDatabaseReference
//                    .child(senderId)
//                    .child(receiverId)
//                    .child(message.getMessageId())
//                    .setValue(message);
//
//            messageDatabaseReference
//                    .child(receiverId)
//                    .child(senderId)
//                    .child(message.getMessageId())
//                    .setValue(message);
//            return true; // true is closing popup, false is requesting a new selection
//        });

        //Setting message and time
        holder.binding.message.setText(message.getMessage());
        holder.binding.messageTime.setText(getDateTimeString(message.getTime()));

        //Setting star visibility
        if (message.getStarred().contains(currentUser.getUid())) {
            holder.binding.star.setVisibility(View.VISIBLE);
        }

        //Setting image if message type is image
        if (message.getType().equals(context.getString(R.string.IMAGE))) {
            holder.binding.message.setVisibility(View.GONE);
            holder.binding.image.setVisibility(View.VISIBLE);

            Picasso.get().load(message.getMessage()).placeholder(R.drawable.profile_image).into(holder.binding.image);
        }

        //Setting video if message type is video
        if (message.getType().equals(context.getString(R.string.VIDEO))) {
            holder.binding.message.setVisibility(View.GONE);
            holder.binding.image.setVisibility(View.VISIBLE);
            holder.binding.videoPlayPreview.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getMessage()).centerCrop().placeholder(R.drawable.baseline_play_circle_outline_24).into(holder.binding.image);
            holder.binding.image.setOnClickListener(view -> ((ChatActivity)(context)).showVideoPreview(holder.binding.image, message.getMessage()));
        } else if (message.getType().equals(context.getString(R.string.IMAGE))) {
            holder.binding.image.setOnClickListener(view -> ((ChatActivity)(context)).showImagePreview(holder.binding.image, message.getMessage()));
        }

        //        if (message.getFeeling() >= 0) {
//            holder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
//            holder.binding.feeling.setVisibility(View.VISIBLE);
//        } else {
//            viewHolder.binding.feeling.setVisibility(View.GONE);
//        }

//
//        } else { // Receiver view holder
//            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
//
////            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
////                View mView;
////                final GestureDetector gestureDetector = new GestureDetector(ApplicationClass.context, new GestureDetector.SimpleOnGestureListener() {
////                    @Override
////                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
////                        popup.onTouch(mView, e);
////                        return super.onSingleTapConfirmed(e);
////                    }
////                });
////
////
////                @Override
////                public boolean onTouch(View view, MotionEvent motionEvent) {
////                    mView = view;
////                    gestureDetector.onTouchEvent(motionEvent);
////                    return false;
////                }
////            });
////
////            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
////                View mView;
////                final GestureDetector gestureDetector = new GestureDetector(ApplicationClass.context, new GestureDetector.SimpleOnGestureListener() {
////                    @Override
////                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
////                        popup.onTouch(mView, e);
////                        return super.onSingleTapConfirmed(e);
////                    }
////                });
////                @Override
////                public boolean onTouch(View view, MotionEvent motionEvent) {
////                    mView = view;
////                    gestureDetector.onTouchEvent(motionEvent);
////                    return false;
////                }
////            });
//
//        }
        View clicked_message = holder.binding.myLinearLayout;
        if (message.getType().equals(context.getString(R.string.IMAGE))) {
            clicked_message = holder.binding.image;
        }
        MessageBottomSheetHandler.start(context, message, holder.binding.star.getVisibility(), getItemViewType(userMessageList.indexOf(message)), userMessageList, clicked_message);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<Message> filterList) {
        userMessageList = filterList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        public final ItemMessageBinding binding;
        public MessageViewHolder(@NonNull ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
