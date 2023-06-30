package com.samsung.whatsapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.samsung.whatsapp.view.fragments.PhotoFragment;
import com.samsung.whatsapp.view.fragments.VideoFragment;

public class CameraXViewPagerAdapter extends FragmentStateAdapter {
    public CameraXViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PhotoFragment();
            case 1:
                return new VideoFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
