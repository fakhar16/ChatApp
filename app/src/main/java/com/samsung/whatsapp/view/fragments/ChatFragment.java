package com.samsung.whatsapp.view.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samsung.whatsapp.adapters.UserAdapter;
import com.samsung.whatsapp.databinding.FragmentChatBinding;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.repository.ContactsRepositoryImpl;
import com.samsung.whatsapp.viewmodel.ContactsViewModel;

import java.util.ArrayList;
import java.util.Objects;


public class ChatFragment extends Fragment {
    private UserAdapter adapter;
    private FragmentChatBinding binding;
    private ContactsViewModel viewModel;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        handleItemsClick();
        return binding.getRoot();
    }

    private void handleItemsClick() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        setupViewModel();
        setupRecyclerView(viewModel);
    }

    private void setupRecyclerView(ContactsViewModel viewModel) {
        binding.chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(getContext(), viewModel.getContacts().getValue());
        binding.chatsList.addItemDecoration(new DividerItemDecoration(binding.chatsList.getContext(), DividerItemDecoration.VERTICAL));
        binding.chatsList.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        viewModel.init();
        viewModel.getContacts().observe(getViewLifecycleOwner(), list -> adapter.notifyDataSetChanged());
    }

    private void filter(String text) {
        ArrayList<User> filteredList = new ArrayList<>();

        for (User item : Objects.requireNonNull(ContactsRepositoryImpl.getInstance().getContacts().getValue())) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            adapter.filterList(filteredList);
        } else {
            adapter.filterList(new ArrayList<>());
        }
    }
}