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

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.UserAdapter;
import com.samsung.whatsapp.databinding.FragmentChatBinding;
import com.samsung.whatsapp.model.User;
import com.samsung.whatsapp.viewmodel.ContactsViewModel;

import java.util.ArrayList;
import java.util.Objects;


public class ChatFragment extends Fragment {
    private UserAdapter adapter;
    private FragmentChatBinding binding;
    private ContactsViewModel viewModel;
    boolean isUnreadFilterOn = false;

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
        binding.filter.setOnClickListener(view -> filterList());
        binding.unreadNoChatView.clearFilter.setOnClickListener(view -> {
            isUnreadFilterOn = false;
            binding.filter.setImageResource(R.drawable.baseline_filter_list_24);
            binding.searchView.setQueryHint("Search");
            adapter.filterList(viewModel.getContacts().getValue());
            binding.unreadNoChatView.getRoot().setVisibility(View.GONE);
        });
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

    private void filterList() {
        isUnreadFilterOn = !isUnreadFilterOn;
        binding.unreadNoChatView.getRoot().setVisibility(View.GONE);

        if (isUnreadFilterOn) {
            binding.filter.setImageResource(R.drawable.baseline_filter_list_off_24);
            binding.searchView.setQueryHint("Search unread chats");
            adapter.filterList(viewModel.getContactsWithUnreadChats().getValue());
            if (adapter.getItemCount() == 0) {
                binding.unreadNoChatView.getRoot().setVisibility(View.VISIBLE);
            }
        } else {
            binding.filter.setImageResource(R.drawable.baseline_filter_list_24);
            binding.searchView.setQueryHint("Search");
            adapter.filterList(viewModel.getContacts().getValue());
        }
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
        viewModel.getContactsWithUnreadChats().observe(getViewLifecycleOwner(), list -> adapter.notifyDataSetChanged());
    }

    private void filter(String text) {
        ArrayList<User> filteredList = new ArrayList<>();

        if (isUnreadFilterOn) {
            for (User item : Objects.requireNonNull(viewModel.getContactsWithUnreadChats().getValue()))
                if (item.getName().toLowerCase().contains(text.toLowerCase()))
                    filteredList.add(item);
        } else {
            for (User item : Objects.requireNonNull(viewModel.getContacts().getValue()))
                if (item.getName().toLowerCase().contains(text.toLowerCase()))
                    filteredList.add(item);
        }
        if (!filteredList.isEmpty()) {
            adapter.filterList(filteredList);
        } else {
            adapter.filterList(new ArrayList<>());
        }
    }
}