package com.samsung.whatsapp.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.StarredMessagesAdapter;
import com.samsung.whatsapp.databinding.ActivityStarMessageBinding;
import com.samsung.whatsapp.interfaces.MessageListenerCallback;
import com.samsung.whatsapp.model.Message;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;
import com.samsung.whatsapp.viewmodel.StarredMessageViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class StarMessageActivity extends AppCompatActivity implements MessageListenerCallback {
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

        if (getIntent().getBooleanExtra(getString(R.string.STAR_MESSAGE_WITH_RECEIVER), false)) {
            viewModel.initStarMessagesWithReceiver();
            viewModel.getStarredMessageWithReceiver().observe(this, list -> {
                adapter.notifyDataSetChanged();
                updateStarredMessageLayout();
            });
            adapter = new StarredMessagesAdapter(this, viewModel.getStarredMessageWithReceiver().getValue());
        } else {
            viewModel.initStarMessages();
            viewModel.getStarredMessage().observe(this, list -> {
                adapter.notifyDataSetChanged();
                updateStarredMessageLayout();
            });
            adapter = new StarredMessagesAdapter(this, viewModel.getStarredMessage().getValue());
        }


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

    @SuppressLint("UnsafeOptInUsageError")
    public void showVideoPreview(View thumbView, String url) {
        binding.appBarLayout.setVisibility(View.GONE);
        WhatsappLikeProfilePicPreview.Companion.zoomVideoFromThumb(thumbView, binding.expandedVideo.cardView, binding.mainPageToolbar.getRoot().getRootView());
        ExoPlayer player = new ExoPlayer.Builder(this).build();
        binding.expandedVideo.video.setPlayer(player);
        binding.expandedVideo.video.setShowNextButton(false);
        binding.expandedVideo.video.setShowPreviousButton(false);

        MediaItem mediaItem = MediaItem.fromUri(url);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    private void filter(String text) {
        ArrayList<Message> filteredList = new ArrayList<>();

        ArrayList<Message> list;

        if (getIntent().getBooleanExtra(getString(R.string.STAR_MESSAGE_WITH_RECEIVER), false)) {
            list = viewModel.getStarredMessageWithReceiver().getValue();
        } else {
            list = viewModel.getStarredMessage().getValue();
        }

        for (Message item : Objects.requireNonNull(list)) {
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
        } else if (binding.expandedVideo.cardView.getVisibility() == View.VISIBLE) {
            Objects.requireNonNull(binding.expandedVideo.video.getPlayer()).release();
            binding.starMessagesList.setClickable(true);
            WhatsappLikeProfilePicPreview.Companion.dismissVideoPreview();
            binding.appBarLayout.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }

    @Override
    public void onMessageSent() {
        Utils.dismissLoadingBar(this, binding.progressbar.getRoot());
    }

    @Override
    public void onMessageSentFailed() {
        Utils.dismissLoadingBar(this, binding.progressbar.getRoot());
    }
}