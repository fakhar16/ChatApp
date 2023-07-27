package com.samsung.whatsapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.samsung.whatsapp.view.fragments.DocsFragment;
import com.samsung.whatsapp.view.fragments.LinksFragment;
import com.samsung.whatsapp.view.fragments.MediaFragment;

public class MediaLinksDocsViewPagerAdapter extends FragmentStateAdapter {
    private final String receiverId;
    public MediaLinksDocsViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String receiverId) {
        super(fragmentActivity);
        this.receiverId = receiverId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new MediaFragment(receiverId);
            case 1:
                return new LinksFragment(receiverId);
            case 2:
                return new DocsFragment(receiverId);
        }
        return new MediaFragment(receiverId);
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
