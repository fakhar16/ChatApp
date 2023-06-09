package com.samsung.whatsapp.adapters;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.currentUser;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ItemStarMessageBinding;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.view.activities.StarMessageActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class StarredMessagesAdapter extends RecyclerView.Adapter<StarredMessagesAdapter.StarredMessagesViewHolder> {
    private final ArrayList<Message> messageList;
    private final Context context;
    private static final String TAG = "ConsoleStarredMessagesAdapter";

    public StarredMessagesAdapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }


    @NonNull
    @Override
    public StarredMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemStarMessageBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.item_star_message, parent, false);
        return new StarredMessagesViewHolder(layoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull StarredMessagesViewHolder holder, int position) {
        final User[] user = new User[1];
        Message message = messageList.get(position);

        Log.wtf(TAG, "onBindViewHolder: " + message.toString() );

        if (message.getFrom().equals(currentUser.getUid())) {
            user[0] = currentUser;
            bindMessageDetails(holder, message, Objects.requireNonNull(user[0]));
        } else {
            userDatabaseReference.child(message.getFrom())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                user[0] = snapshot.getValue(User.class);
                                bindMessageDetails(holder, message, Objects.requireNonNull(user[0]));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        holder.binding.image.setOnClickListener(view -> ((StarMessageActivity)(context)).showImagePreview(holder.binding.image, message.getMessage()));
    }

    private void bindMessageDetails(StarredMessagesViewHolder holder, Message message, User user) {
        if (user.getUid().equals(currentUser.getUid())) {
            holder.binding.userName.setText("You");
        } else {
            holder.binding.userName.setText(user.getName());
        }
        holder.binding.messageTime.setText(Utils.getTimeString(message.getTime()));
        holder.binding.messageDate.setText(Utils.getDateString(message.getTime()));
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(holder.binding.userImage);

        if (message.getType().equals(context.getString(R.string.TEXT))) {
            holder.binding.message.setText(message.getMessage());
        } else if (message.getType().equals(context.getString(R.string.IMAGE))) {
                holder.binding.message.setVisibility(View.GONE);
            holder.binding.image.setVisibility(View.VISIBLE);
            Picasso.get().load(message.getMessage()).placeholder(R.drawable.profile_image).into(holder.binding.image);
        }  else if (message.getType().equals(context.getString(R.string.VIDEO))) {
            //todo load video here
        }
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class StarredMessagesViewHolder extends RecyclerView.ViewHolder {
        public final ItemStarMessageBinding binding;
        public StarredMessagesViewHolder(@NonNull ItemStarMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
