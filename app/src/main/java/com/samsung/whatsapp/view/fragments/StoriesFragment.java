package com.samsung.whatsapp.view.fragments;

import static com.samsung.whatsapp.utils.Utils.currentUser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.StatusAdapter;
import com.samsung.whatsapp.databinding.FragmentStoriesBinding;
import com.samsung.whatsapp.repository.StatusRepositoryImpl;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.viewmodel.StatusViewModel;
import com.squareup.picasso.Picasso;

public class StoriesFragment extends Fragment {
    private FragmentStoriesBinding binding;
    StatusAdapter statusAdapter;

    private final ActivityResultLauncher<Intent> imagePickActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Utils.showLoadingBar(requireActivity(), binding.progressbar.getRoot());

                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    StatusRepositoryImpl.getInstance().uploadStatus(data, currentUser, binding.progressbar.getRoot(), requireActivity());
                }
            }
        }
    });

    public StoriesFragment() {
        // Required empty public constructor
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStoriesBinding.inflate(inflater, container, false);

        binding.progressbar.dialogTitle.setText("Uploading Image");
        binding.progressbar.dialogDescription.setText("Please wait, while we are uploading your image...");

        Picasso.get().load(currentUser.getImage()).placeholder(R.drawable.profile_image).into(binding.image);


        StatusViewModel viewModel = new ViewModelProvider(this).get(StatusViewModel.class);
        viewModel.init();
        viewModel.getUserStatues().observe(getViewLifecycleOwner(), userStatuses -> statusAdapter.notifyDataSetChanged());

        statusAdapter = new StatusAdapter(getContext(), viewModel.getUserStatues().getValue());

        binding.statusList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.statusList.addItemDecoration(new DividerItemDecoration(binding.statusList.getContext(), DividerItemDecoration.VERTICAL));
        binding.statusList.setAdapter(statusAdapter);

        handleItemsClick();
        return binding.getRoot();
    }

    private void handleItemsClick() {
        binding.btnStatus.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            imagePickActivityResultLauncher.launch(intent);
        });

        binding.addStatus.setOnClickListener(view -> Toast.makeText(getContext(), "Add image from camera status here", Toast.LENGTH_SHORT).show());
    }
}