package com.samsung.whatsapp.adapters;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;
import static com.samsung.whatsapp.utils.Utils.currentUser;
import static com.samsung.whatsapp.utils.Utils.isRecordingPlaying;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.rajat.pdfviewer.PdfViewerActivity;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ItemStarMessageBinding;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.utils.bottomsheethandler.MessageBottomSheetHandler;
import com.samsung.whatsapp.view.activities.ChatActivity;
import com.samsung.whatsapp.view.activities.SendContactActivity;
import com.samsung.whatsapp.view.activities.StarMessageActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StarredMessagesAdapter extends RecyclerView.Adapter<StarredMessagesAdapter.StarredMessagesViewHolder> {
    private ArrayList<Message> messageList;
    private final Context context;
//    private static final String TAG = "ConsoleStarredMessagesAdapter";

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

        holder.binding.lowerInfo.setOnClickListener(view -> {
            Intent intent = new Intent(context, ChatActivity.class);
            String visit_user_id = message.getFrom().equals(currentUser.getUid())? message.getTo(): message.getFrom();
            intent.putExtra(context.getString(R.string.VISIT_USER_ID), visit_user_id);
            intent.putExtra(context.getString(R.string.MESSAGE_ID), message.getMessageId());
            context.startActivity(intent);
        });

        View clicked_message = holder.binding.myLinearLayout;
        if (message.getType().equals(context.getString(R.string.IMAGE))) {
            clicked_message = holder.binding.image;
        } else if (message.getType().equals(context.getString(R.string.VIDEO))) {
            clicked_message = holder.binding.videoPlayPreview;
        }
        MessageBottomSheetHandler.start(context, message, holder.binding.star.getVisibility(), 0, messageList, clicked_message);
    }

    @SuppressLint("SetTextI18n")
    private void bindMessageDetails(StarredMessagesViewHolder holder, Message message, User user) {
        holder.binding.messageTime.setText(Utils.getTimeString(message.getTime()));
        holder.binding.messageDate.setText(Utils.getDateString(message.getTime()));
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile_image).into(holder.binding.userImage);
        holder.binding.userName.setText(user.getUid().equals(currentUser.getUid())? "You": user.getName());
        holder.binding.myLinearLayout.setBackground(user.getUid().equals(currentUser.getUid())? ContextCompat.getDrawable(context, R.drawable.sender_messages_layout) : ContextCompat.getDrawable(context, R.drawable.receiver_messages_layout));

        if (message.getType().equals(context.getString(R.string.TEXT))) {
            holder.binding.message.setText(message.getMessage());
        } else if (message.getType().equals(context.getString(R.string.IMAGE))) {
                holder.binding.message.setVisibility(View.GONE);
                holder.binding.image.setVisibility(View.VISIBLE);
                Picasso.get().load(message.getMessage()).placeholder(R.drawable.profile_image).into(holder.binding.image);
                holder.binding.image.setOnClickListener(view -> ((StarMessageActivity)(context)).showImagePreview(holder.binding.image, message.getMessage()));
        }  else if (message.getType().equals(context.getString(R.string.VIDEO))) {
            holder.binding.message.setVisibility(View.GONE);
            holder.binding.image.setVisibility(View.VISIBLE);
            holder.binding.videoPlayPreview.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getMessage()).centerCrop().placeholder(R.drawable.baseline_play_circle_outline_24).into(holder.binding.image);
            holder.binding.videoPlayPreview.setOnClickListener(view -> ((StarMessageActivity)(context)).showVideoPreview(holder.binding.image, message.getMessage()));
        } else if (message.getType().equals(context.getString(R.string.PDF_FILES))) {
            holder.binding.message.setVisibility(View.GONE);
            holder.binding.image.setVisibility(View.VISIBLE);
            holder.binding.image.setImageResource(R.drawable.baseline_file_present_24);
            holder.binding.image.setOnClickListener(view -> context.startActivity(PdfViewerActivity.Companion.launchPdfFromUrl(context, message.getMessage(), message.getFilename(), "", true)));
        } else if (message.getType().equals(context.getString(R.string.AUDIO_RECORDING))) {
            String file_path= ApplicationClass.application.getApplicationContext().getFilesDir().getPath() + "/" + message.getMessageId() + ".3gp";
            File file= new File(file_path);
            holder.binding.audioFileDuration.setText(Utils.getDuration(file));
            holder.binding.message.setVisibility(View.GONE);
            holder.binding.audioRecordingLayout.setVisibility(View.VISIBLE);
            holder.binding.playRecording.setOnClickListener(view -> {
                isRecordingPlaying = !isRecordingPlaying;
                if (isRecordingPlaying) {
                    holder.binding.playRecording.setImageResource(R.drawable.baseline_pause_24);
                    Utils.playAudioRecording(file.getPath());
                    Utils.updateAudioDurationUI(Utils.getDurationLong(file), holder.binding.audioFileDuration, holder.binding.playRecording, holder.binding.audioSeekBar);
                } else {
                    Utils.countDownTimer.cancel();
                    holder.binding.playRecording.setImageResource(R.drawable.baseline_play_arrow_24);
                    Utils.stopPlayingRecording();
                    holder.binding.audioFileDuration.setText(Utils.getDuration(file));
                    holder.binding.audioSeekBar.setProgress(0);
                }
            });
        } else if (message.getType().equals(context.getString(R.string.CONTACT))) {
            holder.binding.message.setVisibility(View.GONE);
            holder.binding.contactLayout.setVisibility(View.VISIBLE);
            holder.binding.viewContact.setVisibility(View.VISIBLE);
            ApplicationClass.contactsDatabaseReference.child(message.getMessage())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<String> list = new ArrayList<>();
                            if (snapshot.exists()) {
                                for (DataSnapshot child : snapshot.getChildren()) {
                                    list.add(child.getValue(String.class));
                                }

                                holder.binding.contactName.setText(list.get(1));
                                Picasso.get().load(list.get(0)).into(holder.binding.contactImage);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            holder.binding.viewContact.setOnClickListener(view -> {
                Intent intent = new Intent(context, SendContactActivity.class);
                intent.putExtra("contactId", message.getMessage());
                intent.putExtra("IsViewContact", true);
                context.startActivity(intent);
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<Message> filterList) {
        messageList = filterList;
        notifyDataSetChanged();
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
