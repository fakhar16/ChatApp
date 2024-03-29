package com.samsung.whatsapp.adapters;

import static com.samsung.whatsapp.utils.FirebaseUtils.updateMessageUnreadStatus;
import static com.samsung.whatsapp.utils.Utils.ITEM_RECEIVE;
import static com.samsung.whatsapp.utils.Utils.ITEM_SENT;
import static com.samsung.whatsapp.utils.Utils.currentUser;
import static com.samsung.whatsapp.utils.Utils.getDateTimeString;
import static com.samsung.whatsapp.utils.Utils.isRecordingPlaying;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.rajat.pdfviewer.PdfViewerActivity;
import com.samsung.whatsapp.ApplicationClass;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ItemMessageBinding;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.utils.bottomsheethandler.MessageBottomSheetHandler;
import com.samsung.whatsapp.view.activities.ChatActivity;
import com.samsung.whatsapp.view.activities.SendContactActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

        updateMessageUnreadStatus(receiverId);
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

        //Setting video if message type is video
        if (message.getType().equals(context.getString(R.string.VIDEO))) {
            if (message.getCaption() == null)
                holder.binding.message.setVisibility(View.GONE);
            else
                holder.binding.message.setText(message.getCaption());
            holder.binding.image.setVisibility(View.VISIBLE);
            holder.binding.videoPlayPreview.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getMessage()).centerCrop().placeholder(R.drawable.baseline_play_circle_outline_24).into(holder.binding.image);
            holder.binding.image.setOnClickListener(view -> ((ChatActivity)(context)).showVideoPreview(holder.binding.image, message.getMessage()));
        } else if (message.getType().equals(context.getString(R.string.IMAGE))) { //Setting image if message type is image
            if (message.getCaption() == null)
                holder.binding.message.setVisibility(View.GONE);
            else
                holder.binding.message.setText(message.getCaption());
            holder.binding.image.setVisibility(View.VISIBLE);
            Picasso.get().load(message.getMessage()).placeholder(R.drawable.profile_image).into(holder.binding.image);
            holder.binding.image.setOnClickListener(view -> ((ChatActivity)(context)).showImagePreview(holder.binding.image, message.getMessage()));
        } else if (message.getType().equals(context.getString(R.string.PDF_FILES))) { //Setting file if message type is
            if (message.getCaption() == null)
                holder.binding.message.setVisibility(View.GONE);
            else
                holder.binding.message.setText(message.getCaption());

            holder.binding.fileName.setVisibility(View.VISIBLE);
            holder.binding.fileName.setText(message.getFilename());
            holder.binding.image.setVisibility(View.VISIBLE);
            holder.binding.image.setImageResource(R.drawable.baseline_picture_as_pdf_24);
            holder.binding.image.setOnClickListener(view -> context.startActivity(PdfViewerActivity.Companion.launchPdfFromUrl(context, message.getMessage(), message.getFilename(), "", true)));
        } else if (message.getType().equals(context.getString(R.string.URL))) {
            String linkedText = String.format("<a href=\"%s\">%s</a> ", message.getMessage(), message.getMessage());
            holder.binding.message.setText(Html.fromHtml(linkedText));
            holder.binding.message.setMovementMethod(LinkMovementMethod.getInstance());
            holder.binding.message.setLinkTextColor(Color.BLUE);
        } else if (message.getType().equals(context.getString(R.string.AUDIO_RECORDING))) {
            String file_path=ApplicationClass.application.getApplicationContext().getFilesDir().getPath() + "/" + message.getMessageId() + ".3gp";
            File file= new File(file_path);
            if (Utils.isRecordingFileExist(file)) {
                holder.binding.audioFileDuration.setText(Utils.getDuration(file));
            } else {
                FirebaseStorage.getInstance().getReferenceFromUrl(message.getMessage()).getFile(file);
            }
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
