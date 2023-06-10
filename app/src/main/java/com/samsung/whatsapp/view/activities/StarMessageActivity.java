package com.samsung.whatsapp.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.samsung.whatsapp.adapters.StarredMessagesAdapter;
import com.samsung.whatsapp.databinding.ActivityStarMessageBinding;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;
import com.samsung.whatsapp.viewmodel.StarredMessageViewModel;

import java.util.Objects;

public class StarMessageActivity extends AppCompatActivity {
    ActivityStarMessageBinding binding;
    private StarredMessagesAdapter adapter;
    StarredMessageViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStarMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolBar();
        setupViewModel();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        binding.starMessagesList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StarredMessagesAdapter(this, viewModel.getStarredMessage().getValue());
        binding.starMessagesList.addItemDecoration(new DividerItemDecoration(binding.starMessagesList.getContext(), DividerItemDecoration.VERTICAL));
        binding.starMessagesList.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StarredMessageViewModel.class);
        viewModel.init(Utils.currentUser.getUid());
        viewModel.getStarredMessage().observe(this, list -> adapter.notifyDataSetChanged());
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