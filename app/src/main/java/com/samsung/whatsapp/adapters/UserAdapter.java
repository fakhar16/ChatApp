package com.samsung.whatsapp.adapters;

import static com.samsung.whatsapp.ApplicationClass.messageDatabaseReference;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.view.activities.ChatActivity;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.UsersDisplayLayoutBinding;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final ArrayList<User> userList;
    private final Context context;

    public UserAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        UsersDisplayLayoutBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.users_display_layout, parent, false);

        return new UserViewHolder(layoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.bind(userList.get(position));
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(holder.binding.usersProfileImage);

        messageDatabaseReference
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                        .child(ApplicationClass.context.getString(R.string.LAST_MESSAGE_WITH_) + user.getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String lastMsg = snapshot.child(ApplicationClass.context.getString(R.string.LAST_MESSAGE_DETAILS)).getValue(String.class);
                                    long lastMsgTime = Objects.requireNonNull(snapshot.child(ApplicationClass.context.getString(R.string.LAST_MESSAGE_TIME)).getValue(Long.class));
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                                    holder.binding.userLastSeenTime.setText(dateFormat.format(new Date(lastMsgTime)));
                                    holder.binding.userProfileStatus.setText(lastMsg);
                                } else {
                                    holder.binding.userProfileStatus.setText(R.string.TAP_TO_CHAT);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        holder.itemView.setOnClickListener(view -> {
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra(ApplicationClass.context.getString(R.string.VISIT_USER_ID), user.getUid());
            chatIntent.putExtra(ApplicationClass.context.getString(R.string.CURRENT_USER_ID), Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            context.startActivity(chatIntent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        public final UsersDisplayLayoutBinding binding;
        public UserViewHolder(@NonNull UsersDisplayLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            binding.setUser(user);
            binding.executePendingBindings();
        }
    }
}
