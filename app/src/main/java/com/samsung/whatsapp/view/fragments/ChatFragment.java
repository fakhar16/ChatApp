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
import com.samsung.whatsapp.viewmodel.ConversationViewModel;

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

        adapter = new UserAdapter(getContext());

        binding.chatsList.addItemDecoration(new DividerItemDecoration(binding.chatsList.getContext(), DividerItemDecoration.VERTICAL));
        binding.chatsList.setAdapter(adapter);

        ConversationViewModel viewModel = new ViewModelProvider(this).get(ConversationViewModel.class);
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            adapter.updateUserList(users);
            adapter.notifyDataSetChanged();
        });

        return binding.getRoot();
    }
}