package com.samsung.whatsapp.view.fragments;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.adapters.MediaMessagesAdapter;
import com.samsung.whatsapp.databinding.FragmentMediaBinding;
import com.samsung.whatsapp.model.User;
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
        viewModel.getMediaMessageWithReceiver().observe(getViewLifecycleOwner(), messages -> {
            adapter.notifyDataSetChanged();
            updateMediaMessageLayout();
        });
    }

    private void updateMediaMessageLayout() {
        if (adapter.getItemCount() == 0) {
            binding.noMediaMessageLayout.setVisibility(View.VISIBLE);
            userDatabaseReference.child(receiverId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User receiver = snapshot.getValue(User.class);
                                assert receiver != null;
                                binding.noMediaDesc.setText(String.format("Tap + to share media with %s", receiver.getName()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            binding.noMediaMessageLayout.setVisibility(View.GONE);
        }
    }
}