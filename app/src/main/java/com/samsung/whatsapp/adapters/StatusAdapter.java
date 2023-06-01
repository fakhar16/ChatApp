package com.samsung.whatsapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.whatsapp.model.Status;
import com.samsung.whatsapp.model.UserStatus;
import com.samsung.whatsapp.view.activities.MainActivity;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ItemStatusBinding;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {
    Context context;
    ArrayList<UserStatus> userStatuses;

    public StatusAdapter(Context context, ArrayList<UserStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
    }

    @NonNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        ItemStatusBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.item_status, parent, false);

        return new StatusViewHolder(layoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
        UserStatus userStatus = userStatuses.get(position);

        if (userStatus.getStatuses().size() == 0)
            return;

        Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);
        Picasso.get().load(lastStatus.getImageUrl()).placeholder(R.drawable.profile_image).into(holder.binding.image);

        holder.binding.circularStatusView.setPortionsCount(userStatus.getStatuses().size());
        holder.binding.name.setText(userStatus.getName());
        holder.binding.status.setText(new SimpleDateFormat("hh:mm a", Locale.US).format(new Date(userStatus.getLastUpdated())));

        holder.binding.statusView.setOnClickListener(view -> {
            ArrayList<MyStory> myStories = new ArrayList<>();

            for (Status status : userStatus.getStatuses()) {
                myStories.add(new MyStory(status.getImageUrl()));
            }

            new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                    .setStoriesList(myStories) // Required
                    .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                    .setTitleText(userStatus.getName()) // Default is Hidden
                    .setSubtitleText("") // Default is Hidden
                    .setTitleLogoUrl(userStatus.getProfileImage()) // Default is Hidden
                    .setStoryClickListeners(new StoryClickListeners() {
                        @Override
                        public void onDescriptionClickListener(int position1) {
                            //your action
                        }

                        @Override
                        public void onTitleIconClickListener(int position1) {
                            //your action
                        }
                    }) // Optional Listeners
                    .build() // Must be called before calling show method
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }

    static class StatusViewHolder extends RecyclerView.ViewHolder {
        public ItemStatusBinding binding;

        public StatusViewHolder(@NonNull ItemStatusBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
