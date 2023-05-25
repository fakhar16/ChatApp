package com.samsung.whatsapp.adapters;

import static com.samsung.whatsapp.ApplicationClass.messageDatabaseReference;
import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ItemReceiveBinding;
import com.samsung.whatsapp.databinding.ItemSentBinding;
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

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());

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

        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.message.setText(message.getMessage());

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

            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                View mView;
                final GestureDetector gestureDetector = new GestureDetector(ApplicationClass.context, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                        popup.onTouch(mView, e);
                        return super.onSingleTapConfirmed(e);
                    }
                });


                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    mView = view;
                    gestureDetector.onTouchEvent(motionEvent);
                    return false;
                }
            });

            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                View mView;
                final GestureDetector gestureDetector = new GestureDetector(ApplicationClass.context, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                        popup.onTouch(mView, e);
                        return super.onSingleTapConfirmed(e);
                    }
                });
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    mView = view;
                    gestureDetector.onTouchEvent(motionEvent);
                    return false;
                }
            });

//            viewHolder.binding.message.setOnTouchListener((view, motionEvent) -> {
//                popup.onTouch(view, motionEvent);
//                return false;
//            });
//
//            viewHolder.binding.image.setOnTouchListener((view, motionEvent) -> {
//                popup.onTouch(view, motionEvent);
//                return false;
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

        showMenuOnLongClick(holder, position);
    }

    private void showMenuOnLongClick(RecyclerView.ViewHolder holder, int pos) {
        TextView clicked_message = null;
        boolean wasMessageReceived = false;
//
        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            clicked_message = viewHolder.binding.message;

        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            clicked_message = viewHolder.binding.message;
            wasMessageReceived = true;
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout);

        LinearLayout star = bottomSheetDialog.findViewById(R.id.star);
        LinearLayout copy = bottomSheetDialog.findViewById(R.id.copy);
        LinearLayout forward = bottomSheetDialog.findViewById(R.id.forward);
        LinearLayout delete = bottomSheetDialog.findViewById(R.id.delete);

        clicked_message.setOnLongClickListener(view -> {
            bottomSheetDialog.show();
            return true;
        });

        Objects.requireNonNull(delete).setOnClickListener(view -> {
            Toast.makeText(context, "Delete clicked", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });
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
