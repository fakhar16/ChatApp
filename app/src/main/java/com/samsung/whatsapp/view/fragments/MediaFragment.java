package com.samsung.whatsapp.view.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samsung.whatsapp.adapters.MediaMessagesAdapter;
import com.samsung.whatsapp.databinding.FragmentMediaBinding;
import com.samsung.whatsapp.viewmodel.MediaMessageViewModel;

public class MediaFragment extends Fragment {
    FragmentMediaBinding binding;
    MediaMessagesAdapter adapter;
    String receiverId;

    private MediaMessageViewModel viewModel;
    public MediaFragment() {
        // Required empty public constructor
    }

    public MediaFragment(String receiverId) {
        this.receiverId = receiverId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMediaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        setupViewModel();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        binding.mediaMessagesList.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new MediaMessagesAdapter(getContext(), viewModel.getMediaMessageWithReceiver().getValue());
        binding.mediaMessagesList.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MediaMessageViewModel.class);
        viewModel.initMediaMessagesWithReceiver(receiverId);
        viewModel.getMediaMessageWithReceiver().observe(getViewLifecycleOwner(), list -> adapter.notifyDataSetChanged());
    }
}