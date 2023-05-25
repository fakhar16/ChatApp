package com.samsung.whatsapp.view.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samsung.whatsapp.adapters.UserAdapter;
import com.samsung.whatsapp.databinding.FragmentChatBinding;
import com.samsung.whatsapp.viewmodel.ContactsViewModel;


public class ChatFragment extends Fragment {
    private UserAdapter adapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        com.samsung.whatsapp.databinding.FragmentChatBinding binding = FragmentChatBinding.inflate(inflater, container, false);

        binding.chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        ContactsViewModel viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        viewModel.init();
        viewModel.getContacts().observe(getViewLifecycleOwner(), list -> adapter.notifyDataSetChanged());

        adapter = new UserAdapter(getContext(), viewModel.getContacts().getValue());
        binding.chatsList.addItemDecoration(new DividerItemDecoration(binding.chatsList.getContext(), DividerItemDecoration.VERTICAL));
        binding.chatsList.setAdapter(adapter);

        return binding.getRoot();
    }
}