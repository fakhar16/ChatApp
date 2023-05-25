package com.samsung.whatsapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.samsung.whatsapp.view.fragments.ChatFragment;
import com.samsung.whatsapp.view.fragments.StoriesFragment;

import java.util.ArrayList;

public class TabAccessorAdapter extends FragmentStateAdapter {
    private final ArrayList<Fragment> fragmentList = new ArrayList<>();
    public TabAccessorAdapter(@NonNull FragmentManager fm, @NonNull Lifecycle lifecycle) {
        super(fm, lifecycle);
        fragmentList.add(new ChatFragment());
        fragmentList.add(new StoriesFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}
