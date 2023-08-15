package com.samsung.whatsapp.adapters;

import android.content.Context;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PhoneContactAdapter extends ArrayAdapter<PhoneContact> {
    public PhoneContactAdapter(@NonNull Context context, ArrayList<PhoneContact> arrayList) {
        super(context, 0, arrayList);
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

        return currentItemView;
    }
}
