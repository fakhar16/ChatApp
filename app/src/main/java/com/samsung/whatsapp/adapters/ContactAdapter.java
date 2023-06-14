package com.samsung.whatsapp.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.UsersDisplayLayoutBinding;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.utils.bottomsheethandler.ForwardMessageBottomSheetHandler;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    private ArrayList<User> userList;

    public ContactAdapter(ArrayList<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        UsersDisplayLayoutBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.users_display_layout, parent, false);

        return new ContactViewHolder(layoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);

        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(holder.binding.usersProfileImage);
        holder.binding.userProfileStatus.setVisibility(View.GONE);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(125, 125);
        holder.binding.usersProfileImage.setLayoutParams(layoutParams);

        holder.binding.itemView.setOnClickListener(view -> ForwardMessageBottomSheetHandler.forwardMessage(user.getUid()));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<User> filterList) {
        userList = filterList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        public final UsersDisplayLayoutBinding binding;
        public ContactViewHolder(@NonNull UsersDisplayLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            binding.setUser(user);
            binding.executePendingBindings();
        }
    }
}
