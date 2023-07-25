package com.samsung.whatsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ItemMediaMessageBinding;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.view.activities.MediaLinksDocsActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MediaMessagesAdapter extends RecyclerView.Adapter<MediaMessagesAdapter.MediaMessagesViewHolder> {
    private final ArrayList<Message> messageList;
    private final Context context;
    public MediaMessagesAdapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MediaMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemMediaMessageBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.item_media_message, parent, false);
        return new MediaMessagesViewHolder(layoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaMessagesViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.getType().equals(context.getString(R.string.VIDEO))) {
            holder.binding.play.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getMessage()).centerCrop().placeholder(R.drawable.baseline_play_circle_outline_24).into(holder.binding.image);
            holder.binding.image.setOnClickListener(view -> ((MediaLinksDocsActivity)(context)).showVideoPreview(holder.binding.image, message.getMessage()));
        } else {
            Picasso.get().load(message.getMessage()).placeholder(R.drawable.profile_image).into(holder.binding.image);
            holder.binding.image.setOnClickListener(view -> ((MediaLinksDocsActivity)(context)).showImagePreview(holder.binding.image, message.getMessage()));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MediaMessagesViewHolder extends RecyclerView.ViewHolder {
        public final ItemMediaMessageBinding binding;
        public MediaMessagesViewHolder(@NonNull ItemMediaMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
