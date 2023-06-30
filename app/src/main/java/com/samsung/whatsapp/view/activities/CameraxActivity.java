package com.samsung.whatsapp.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.samsung.whatsapp.R;
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

        CameraXViewPagerAdapter adapter = new CameraXViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabs, binding.viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(tabsName[position]);
                    }
                }).attach();

    }
}