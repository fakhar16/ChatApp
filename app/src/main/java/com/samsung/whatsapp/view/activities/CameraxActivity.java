package com.samsung.whatsapp.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayoutMediator;
import com.samsung.whatsapp.adapters.CameraXViewPagerAdapter;
import com.samsung.whatsapp.databinding.ActivityCameraxBinding;

public class CameraxActivity extends AppCompatActivity {
    ActivityCameraxBinding binding;
    String[] tabsName = new String[] {"Photo", "Video"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int tabCount = 2;

        if (getIntent().getBooleanExtra("isFromStories", false))
            tabCount = 1;


        CameraXViewPagerAdapter adapter = new CameraXViewPagerAdapter(this, tabCount);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabs, binding.viewPager,
                (tab, position) -> tab.setText(tabsName[position])).attach();

    }
}