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
import com.samsung.whatsapp.adapters.DocMessagesAdapter;
import com.samsung.whatsapp.databinding.FragmentDocsBinding;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.viewmodel.DocMessageViewModel;

public class DocsFragment extends Fragment {
    FragmentDocsBinding binding;
    DocMessagesAdapter adapter;
    String receiverId;
    DocMessageViewModel viewModel;
    public DocsFragment() {
        // Required empty public constructor
    }

    public DocsFragment(String receiverId) {
        this.receiverId = receiverId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDocsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        setupViewModel();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        binding.docsMessagesList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DocMessagesAdapter(getContext(), viewModel.getDocMessageWithReceiver().getValue());
        binding.docsMessagesList.addItemDecoration(new DividerItemDecoration(binding.docsMessagesList.getContext(), DividerItemDecoration.VERTICAL));
        binding.docsMessagesList.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DocMessageViewModel.class);
        viewModel.initDocMessagesWithReceiver(receiverId);
        viewModel.getDocMessageWithReceiver().observe(getViewLifecycleOwner(), messages -> {
            adapter.notifyDataSetChanged();
            updateMediaMessageLayout();
        });
    }

    private void updateMediaMessageLayout() {
        if (adapter.getItemCount() == 0) {
            binding.noDocsMessageLayout.setVisibility(View.VISIBLE);
            userDatabaseReference.child(receiverId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User receiver = snapshot.getValue(User.class);
                                assert receiver != null;
                                binding.noDocsDesc.setText(String.format("Tap + to share documents with %s", receiver.getName()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else {
            binding.noDocsMessageLayout.setVisibility(View.GONE);
        }
    }
}