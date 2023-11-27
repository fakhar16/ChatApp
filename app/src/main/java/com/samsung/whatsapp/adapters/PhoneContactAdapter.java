package com.samsung.whatsapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.model.PhoneContact;
import com.samsung.whatsapp.view.activities.SendContactActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PhoneContactAdapter extends ArrayAdapter<PhoneContact> {
    private final String receiverId;

    public PhoneContactAdapter(Context context, ArrayList<PhoneContact> arrayList, String receiver) {
        super(context, 0, arrayList);
        receiverId = receiver;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View currentItemView = convertView;
        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.contacts_list_item, parent, false);
        }

        PhoneContact currentPhoneContact = getItem(position);

        ImageView image = currentItemView.findViewById(R.id.image);
        TextView name = currentItemView.findViewById(R.id.name);
        TextView status = currentItemView.findViewById(R.id.status);

        Picasso.get().load(currentPhoneContact.getImage()).placeholder(R.drawable.profile_image).into(image);
        name.setText(currentPhoneContact.getName());
        status.setText(currentPhoneContact.getStatus());

        View finalCurrentItemView = currentItemView;
        currentItemView.setOnClickListener(view -> {
            Intent intent = new Intent(finalCurrentItemView.getContext(), SendContactActivity.class);
            intent.putExtra("name", currentPhoneContact.getName());
            intent.putExtra("phone", currentPhoneContact.getPhone());
            intent.putExtra("image", currentPhoneContact.getImage());
            intent.putExtra(finalCurrentItemView.getContext().getString(R.string.VISIT_USER_ID), receiverId);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            finalCurrentItemView.getContext().startActivity(intent);
        });

        return currentItemView;
    }
}
