package com.samsung.whatsapp.view.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.google.android.material.tabs.TabLayoutMediator;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.MediaLinksDocsViewPagerAdapter;
import com.samsung.whatsapp.databinding.ActivityMediaLinksDocsBinding;
import com.samsung.whatsapp.utils.WhatsappLikeProfilePicPreview;

import java.util.Objects;

public class MediaLinksDocsActivity extends AppCompatActivity {
    ActivityMediaLinksDocsBinding binding;
    String[] tabsName = new String[] {"Media", "Links", "Docs"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaLinksDocsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String receiverId = getIntent().getStringExtra(getString(R.string.RECEIVER_ID));

        MediaLinksDocsViewPagerAdapter adapter = new MediaLinksDocsViewPagerAdapter(this, receiverId);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabs, binding.viewPager,
                (tab, position) -> tab.setText(tabsName[position])).attach();

    }

    public void showImagePreview(View thumbView, String url) {
        WhatsappLikeProfilePicPreview.Companion.zoomImageFromThumb(thumbView, binding.expandedImage.cardView, binding.expandedImage.image, binding.getRoot().getRootView(), url);
    }

    @SuppressLint("UnsafeOptInUsageError")
    public void showVideoPreview(View thumbView, String url) {
        WhatsappLikeProfilePicPreview.Companion.zoomVideoFromThumb(thumbView, binding.expandedVideo.cardView, binding.getRoot().getRootView());
        ExoPlayer player = new ExoPlayer.Builder(this).build();
        binding.expandedVideo.video.setPlayer(player);
        binding.expandedVideo.video.setShowNextButton(false);
        binding.expandedVideo.video.setShowPreviousButton(false);

        MediaItem mediaItem = MediaItem.fromUri(url);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    public void onBackPressed() {
        if (binding.expandedImage.cardView.getVisibility() == View.VISIBLE) {
            WhatsappLikeProfilePicPreview.Companion.dismissPhotoPreview();
        } else if (binding.expandedVideo.cardView.getVisibility() == View.VISIBLE) {
            Objects.requireNonNull(binding.expandedVideo.video.getPlayer()).release();
            WhatsappLikeProfilePicPreview.Companion.dismissVideoPreview();
        } else {
            finish();
        }
    }
}