package com.samsung.whatsapp.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;

import com.samsung.whatsapp.adapters.StarredMessagesAdapter;
import com.samsung.whatsapp.databinding.ActivityStarMessageBinding;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.viewmodel.StarredMessageViewModel;

import java.util.Objects;

public class StarMessageActivity extends AppCompatActivity {
    ActivityStarMessageBinding binding;
    private StarredMessagesAdapter adapter;
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStarMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolBar();

        binding.starMessagesList.setLayoutManager(new LinearLayoutManager(this));

        StarredMessageViewModel viewModel = new ViewModelProvider(this).get(StarredMessageViewModel.class);
        viewModel.init(Utils.currentUser.getUid());
        viewModel.getStarredMessage().observe(this, list -> adapter.notifyDataSetChanged());

        adapter = new StarredMessagesAdapter(this, viewModel.getStarredMessage().getValue());
        binding.starMessagesList.addItemDecoration(new DividerItemDecoration(binding.starMessagesList.getContext(), DividerItemDecoration.VERTICAL));
        binding.starMessagesList.setAdapter(adapter);
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
}