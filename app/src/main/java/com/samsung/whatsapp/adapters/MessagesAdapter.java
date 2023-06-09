package com.samsung.whatsapp.adapters;

import static com.samsung.whatsapp.ApplicationClass.messageDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.currentUser;
import static com.samsung.whatsapp.utils.Utils.getDateTimeString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ItemReceiveBinding;
import com.samsung.whatsapp.databinding.ItemSentBinding;
import com.samsung.whatsapp.utils.FCMMessaging;
import com.samsung.whatsapp.view.activities.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class MessagesAdapter extends RecyclerView.Adapter {
    private final ArrayList<Message> userMessageList;
    private final Context context;
    private final String senderId;
    private final String receiverId;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    public MessagesAdapter(Context context, String senderId, String receiverId, ArrayList<Message> userMessageList) {
        this.context = context;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == ITEM_SENT) {
            ItemSentBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.item_sent, parent, false);
            return new SenderViewHolder(layoutBinding);
        } else {
            ItemReceiveBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(layoutBinding);
        }
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = userMessageList.get(position);
        holder.setIsRecyclable(false);

        int[] reactions = new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (pos < 0)
                return false;

            if (holder.getClass() == SenderViewHolder.class) {
                SenderViewHolder viewHolder = (SenderViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            message.setFeeling(pos);

            messageDatabaseReference
                    .child(senderId)
                    .child(receiverId)
                    .child(message.getMessageId())
                    .setValue(message);

            messageDatabaseReference
                    .child(receiverId)
                    .child(senderId)
                    .child(message.getMessageId())
                    .setValue(message);
            return true; // true is closing popup, false is requesting a new selection
        });

        String fromUserId = message.getFrom();
        String fromMessageType = message.getType();

        //Sent view holder
        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());
            viewHolder.binding.messageTime.setText(getDateTimeString(message.getTime()));

            if (message.getStarred().contains(currentUser.getUid())) {
                viewHolder.binding.star.setVisibility(View.VISIBLE);
            }

            if (fromMessageType.equals(context.getString(R.string.IMAGE))) {
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.image.setVisibility(View.VISIBLE);

                Picasso.get().load(message.getMessage()).placeholder(R.drawable.profile_image).into(viewHolder.binding.image);
            } else if (fromMessageType.equals(context.getString(R.string.VIDEO))) {
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).placeholder(R.drawable.baseline_play_circle_outline_24).into(viewHolder.binding.image);
//                viewHolder.binding.video.setVisibility(View.VISIBLE);
//

            }

            if (message.getFeeling() >= 0) {
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            if (message.getType().equals(context.getString(R.string.VIDEO))) {
                Glide.with(context).load(message.getMessage()).centerCrop().placeholder(R.drawable.baseline_play_circle_outline_24).into(viewHolder.binding.image);
                viewHolder.binding.image.setOnClickListener(view -> ((ChatActivity)(context)).showVideoPreview(viewHolder.binding.image, message.getMessage()));
            } else if (message.getType().equals(context.getString(R.string.IMAGE))) {
                viewHolder.binding.image.setOnClickListener(view -> ((ChatActivity)(context)).showImagePreview(viewHolder.binding.image, message.getMessage()));
            }

        } else { // Receiver view holder
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());
            viewHolder.binding.messageTime.setText(getDateTimeString(message.getTime()));

            if (message.getStarred().contains(currentUser.getUid())) {
                viewHolder.binding.star.setVisibility(View.VISIBLE);
            }

            if (fromMessageType.equals(context.getString(R.string.IMAGE))) {
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.image.setVisibility(View.VISIBLE);

                Picasso.get().load(message.getMessage()).placeholder(R.drawable.profile_image).into(viewHolder.binding.image);
            }

            if (message.getFeeling() >= 0) {
                viewHolder.binding.feeling.setImageResource(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            if (message.getType().equals(context.getString(R.string.VIDEO))) {
                Glide.with(context).load(message.getMessage()).centerCrop().placeholder(R.drawable.baseline_play_circle_outline_24).into(viewHolder.binding.image);
                viewHolder.binding.image.setOnClickListener(view -> ((ChatActivity)(context)).showVideoPreview(viewHolder.binding.image, message.getMessage()));
            } else if (message.getType().equals(context.getString(R.string.IMAGE))) {
                viewHolder.binding.image.setOnClickListener(view -> ((ChatActivity)(context)).showImagePreview(viewHolder.binding.image, message.getMessage()));
            }

//            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
//                View mView;
//                final GestureDetector gestureDetector = new GestureDetector(ApplicationClass.context, new GestureDetector.SimpleOnGestureListener() {
//                    @Override
//                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
//                        popup.onTouch(mView, e);
//                        return super.onSingleTapConfirmed(e);
//                    }
//                });
//
//
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    mView = view;
//                    gestureDetector.onTouchEvent(motionEvent);
//                    return false;
//                }
//            });
//
//            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
//                View mView;
//                final GestureDetector gestureDetector = new GestureDetector(ApplicationClass.context, new GestureDetector.SimpleOnGestureListener() {
//                    @Override
//                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
//                        popup.onTouch(mView, e);
//                        return super.onSingleTapConfirmed(e);
//                    }
//                });
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    mView = view;
//                    gestureDetector.onTouchEvent(motionEvent);
//                    return false;
//                }
//            });

            userDatabaseReference
                    .child(fromUserId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                if (snapshot.hasChild(context.getString(R.string.IMAGE))) {
                                    String receiverImage = Objects.requireNonNull(snapshot.child(context.getString(R.string.IMAGE)).getValue()).toString();
                                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(viewHolder.binding.profileImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
        showMenuOnLongClick(holder, message);
    }

    private void showMenuOnLongClick(RecyclerView.ViewHolder holder, Message message) {
        View clicked_message = null;

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            clicked_message = viewHolder.binding.getRoot();
        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            clicked_message = viewHolder.binding.getRoot();
        }

        View contentView = View.inflate(context, R.layout.message_bottom_sheet_layout, null);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(contentView);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        ((View) contentView.getParent()).setBackgroundColor(Color.TRANSPARENT);

        LinearLayout star = bottomSheetDialog.findViewById(R.id.star);
//        LinearLayout copy = bottomSheetDialog.findViewById(R.id.copy);
//        LinearLayout forward = bottomSheetDialog.findViewById(R.id.forward);
//        LinearLayout delete = bottomSheetDialog.findViewById(R.id.delete);
        Button cancel = bottomSheetDialog.findViewById(R.id.cancel);

        clicked_message.setOnLongClickListener(view -> {
            bottomSheetDialog.show();
            return true;
        });

        Objects.requireNonNull(star).setOnClickListener(view -> {
            FCMMessaging.starMessage(message);
            bottomSheetDialog.dismiss();
        });
        Objects.requireNonNull(cancel).setOnClickListener(view -> bottomSheetDialog.dismiss());
    }

//    private void deleteSentMessage(final int position, final MessageViewHolder holder) {
//        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
//        rootRef.child(context.getString(R.string.MESSAGES))
//                .child(userMessageList.get(position).getFrom())
//                .child(userMessageList.get(position).getTo())
//                .child(userMessageList.get(position).getMessageId())
//                .removeValue()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(holder.itemView.getContext(), "Deleted message successfully", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(holder.itemView.getContext(), "Error while deleting messaging", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void deleteReceiveMessage(final int position, final MessageViewHolder holder) {
//        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
//        rootRef.child(context.getString(R.string.MESSAGES))
//                .child(userMessageList.get(position).getTo())
//                .child(userMessageList.get(position).getFrom())
//                .child(userMessageList.get(position).getMessageId())
//                .removeValue()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(holder.itemView.getContext(), "Deleted message successfully", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(holder.itemView.getContext(), "Error while deleting messaging", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder) {
//        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
//        rootRef.child(context.getString(R.string.MESSAGES))
//                .child(userMessageList.get(position).getFrom())
//                .child(userMessageList.get(position).getTo())
//                .child(userMessageList.get(position).getMessageId())
//                .removeValue()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        rootRef.child(context.getString(R.string.MESSAGES))
//                                .child(userMessageList.get(position).getTo())
//                                .child(userMessageList.get(position).getFrom())
//                                .child(userMessageList.get(position).getMessageId())
//                                .removeValue()
//                                .addOnCompleteListener(task1 -> {
//                                    if (task1.isSuccessful()) {
//                                        Toast.makeText(holder.itemView.getContext(), "Deleted message successfully", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    } else {
//                        Toast.makeText(holder.itemView.getContext(), "Error while deleting messaging", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        public final ItemSentBinding binding;
        public SenderViewHolder(@NonNull ItemSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

     static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        public final ItemReceiveBinding binding;
        public ReceiverViewHolder(@NonNull ItemReceiveBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
