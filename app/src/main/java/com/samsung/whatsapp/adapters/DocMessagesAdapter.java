package com.samsung.whatsapp.adapters;

import static com.samsung.whatsapp.utils.Utils.getFileSize;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.rajat.pdfviewer.PdfViewerActivity;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ItemMediaDocBinding;
import com.samsung.whatsapp.model.Message;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class DocMessagesAdapter extends RecyclerView.Adapter<DocMessagesAdapter.DocMessagesViewHolder> {
    private final ArrayList<Message> messageList;
    private final Context context;
    public DocMessagesAdapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public DocMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ItemMediaDocBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.item_media_doc, parent, false);
        return new DocMessagesViewHolder(layoutBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DocMessagesViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.binding.fileName.setText(message.getFilename());
        holder.binding.fileLayout.setOnClickListener(view -> context.startActivity(PdfViewerActivity.Companion.launchPdfFromUrl(context, message.getMessage(), message.getFilename(), "", true)));
        try {
            holder.binding.fileSize.setText(getFileSize(message.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class DocMessagesViewHolder extends RecyclerView.ViewHolder {
        public final ItemMediaDocBinding binding;
        public DocMessagesViewHolder(@NonNull ItemMediaDocBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
