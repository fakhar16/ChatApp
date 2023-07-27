package com.samsung.whatsapp.view.fragments;

import static com.samsung.whatsapp.ApplicationClass.userDatabaseReference;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.adapters.LinksMessagesAdapter;
import com.samsung.whatsapp.databinding.FragmentLinksBinding;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.viewmodel.LinksMessageViewModel;

public class LinksFragment extends Fragment {
    FragmentLinksBinding binding;
    LinksMessagesAdapter adapter;
    String receiverId;
    private LinksMessageViewModel viewModel;
    public LinksFragment() {
        // Required empty public constructor
    }

    public LinksFragment(String receiverId) {
        this.receiverId = receiverId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLinksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        setupViewModel();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        binding.linksMessagesList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LinksMessagesAdapter(getContext(), viewModel.getLinksMessageWithReceiver().getValue());
        binding.linksMessagesList.addItemDecoration(new DividerItemDecoration(binding.linksMessagesList.getContext(), DividerItemDecoration.VERTICAL));
        binding.linksMessagesList.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(LinksMessageViewModel.class);
        viewModel.initLinksMessagesWithReceiver(receiverId);
        viewModel.getLinksMessageWithReceiver().observe(getViewLifecycleOwner(), messages -> {
            adapter.notifyDataSetChanged();
            updateMediaMessageLayout();
        });
    }

    private void updateMediaMessageLayout() {
        if (adapter.getItemCount() == 0) {
            binding.noLinksMessageLayout.setVisibility(View.VISIBLE);
            userDatabaseReference.child(receiverId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User receiver = snapshot.getValue(User.class);
                                assert receiver != null;
                                binding.noLinksDesc.setText(String.format("Links you send and receive with %s will appear here", receiver.getName()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            binding.noLinksMessageLayout.setVisibility(View.GONE);
        }
    }
}