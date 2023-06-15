package com.samsung.whatsapp.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.samsung.whatsapp.adapters.StarredMessagesAdapter;
import com.samsung.whatsapp.databinding.ActivityStarMessageBinding;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;
import com.samsung.whatsapp.viewmodel.StarredMessageViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class StarMessageActivity extends AppCompatActivity {
    private ActivityStarMessageBinding binding;
    private StarredMessagesAdapter adapter;
    private StarredMessageViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStarMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolBar();
        setupViewModel();
        setupRecyclerView();
        handleItemsClick();
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

    private void setupRecyclerView() {
        binding.starMessagesList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StarredMessagesAdapter(this, viewModel.getStarredMessage().getValue());
        binding.starMessagesList.addItemDecoration(new DividerItemDecoration(binding.starMessagesList.getContext(), DividerItemDecoration.VERTICAL));
        binding.starMessagesList.setAdapter(adapter);
    }

    private void updateStarredMessageLayout() {
        if (adapter.getItemCount() == 0) {
            binding.noStarMessageLayout.setVisibility(View.VISIBLE);
        } else {
            binding.noStarMessageLayout.setVisibility(View.GONE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StarredMessageViewModel.class);
        viewModel.init(Utils.currentUser.getUid());
        viewModel.getStarredMessage().observe(this, list -> {
            adapter.notifyDataSetChanged();
            updateStarredMessageLayout();
        });
    }

    private void initToolBar() {
        setSupportActionBar(binding.mainPageToolbar.mainAppBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Starred Messages");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showImagePreview(View thumbView, String url) {
        WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(thumbView, binding.expandedImage.cardView, binding.expandedImage.image, binding.container, url);
        binding.appBarLayout.setVisibility(View.GONE);
    }

    private void filter(String text) {
        ArrayList<Message> filteredList = new ArrayList<>();

        for (Message item : Objects.requireNonNull(viewModel.getStarredMessage().getValue())) {
            if (item.getMessage().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            adapter.filterList(filteredList);
        } else {
            adapter.filterList(new ArrayList<>());
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.expandedImage.cardView.getVisibility() == View.VISIBLE) {
            WhatsappLikeProfilePicPreview.Companion.dismissPhotoPreview();
            binding.appBarLayout.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }
}