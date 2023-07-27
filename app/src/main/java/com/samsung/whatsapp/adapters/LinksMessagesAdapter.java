package com.samsung.whatsapp.adapters;

import static com.samsung.whatsapp.utils.Utils.getDateTimeString;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ItemMediaLinkBinding;
import com.samsung.whatsapp.model.Message;

import java.util.ArrayList;

public class LinksMessagesAdapter extends RecyclerView.Adapter<LinksMessagesAdapter.LinksMessagesViewHolder> {
    private final ArrayList<Message> messageList;
    private final Context context;
    public LinksMessagesAdapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public LinksMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemMediaLinkBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.item_media_link, parent, false);
        return new LinksMessagesViewHolder(layoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull LinksMessagesViewHolder holder, int position) {
        Message message = messageList.get(position);
        String linkedText = String.format("<a href=\"%s\">%s</a> ", message.getMessage(), message.getMessage());
        holder.binding.linkText.setText(Html.fromHtml(linkedText, Html.FROM_HTML_MODE_LEGACY));
        holder.binding.linkText.setMovementMethod(LinkMovementMethod.getInstance());
        holder.binding.linkText.setLinkTextColor(Color.BLUE);

        holder.binding.linkTime.setText(getDateTimeString(message.getTime()));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class LinksMessagesViewHolder extends RecyclerView.ViewHolder {
        public final ItemMediaLinkBinding binding;
        public LinksMessagesViewHolder(@NonNull ItemMediaLinkBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
