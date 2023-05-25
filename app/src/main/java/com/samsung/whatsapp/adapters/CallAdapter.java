package com.samsung.whatsapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.samsung.whatsapp.model.Call;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.CallsDisplayLayoutBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.CallViewHolder> {
    private final List<Call> callList;
    private final Context context;

    public CallAdapter(List<Call> callList, Context context) {
        this.callList = callList;
        this.context = context;
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CallsDisplayLayoutBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.calls_display_layout, parent, false);

        return new CallViewHolder(layoutBinding);
    }

    @SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {

        Call call = callList.get(position);

        holder.binding.userProfileName.setText(call.getUserName());
        holder.binding.userCallDate.setText(call.getDate());
        holder.binding.callTextType.setText(call.getCallType());
        Picasso.get().load(call.getUserImageUrl()).placeholder(R.drawable.profile_image).into(holder.binding.usersProfileImage);

        if (call.getCallType().equals(context.getString(R.string.MISSED_CALL))) {
            holder.binding.userProfileName.setTextColor(R.color.colorPrimaryDark);
        }

        switch (call.getCallChannel()) {
            case "Audio":
                holder.binding.callImageType.setImageDrawable(context.getDrawable(R.drawable.baseline_call_24));
                break;
            case "Video":
                holder.binding.callImageType.setImageDrawable(context.getDrawable(R.drawable.baseline_videocam_24));
                break;
        }
    }
    @Override
    public int getItemCount() {
        return callList.size();
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        public final CallsDisplayLayoutBinding binding;
        public CallViewHolder(@NonNull CallsDisplayLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
