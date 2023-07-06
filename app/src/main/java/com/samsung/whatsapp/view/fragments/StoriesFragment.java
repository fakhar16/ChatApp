package com.samsung.whatsapp.view.fragments;

import static com.samsung.whatsapp.utils.Utils.currentUser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.StatusAdapter;
import com.samsung.whatsapp.databinding.FragmentStoriesBinding;
import com.samsung.whatsapp.model.UserStatus;
import com.samsung.whatsapp.repository.StatusRepositoryImpl;
import com.samsung.whatsapp.utils.Utils;
import com.samsung.whatsapp.view.activities.CameraxActivity;
import com.samsung.whatsapp.viewmodel.StatusViewModel;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class StoriesFragment extends Fragment {
    private FragmentStoriesBinding binding;
    private StatusAdapter statusAdapter;
    private StatusViewModel viewModel;

    private final ActivityResultLauncher<Intent> imagePickActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Utils.showLoadingBar(requireActivity(), binding.progressbar.getRoot());

                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    StatusRepositoryImpl.getInstance().uploadStatus(data.getData(), currentUser, binding.progressbar.getRoot(), requireActivity());
                }
            }
        }
    });

    private final ActivityResultLauncher<Intent> imageCaptureActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        assert data != null;
                        Uri fileUri = Uri.parse(data.getStringExtra(getString(R.string.IMAGE_URI)));

                        Bitmap bitmap;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), fileUri);
                            Matrix matrix = new Matrix();
                            matrix.postRotate(-90);
                            Bitmap finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            OutputStream os=requireContext().getContentResolver().openOutputStream(fileUri);
                            finalBitmap.compress(Bitmap.CompressFormat.PNG,100,os);

                            Utils.showLoadingBar(requireActivity(), binding.progressbar.getRoot());
                            StatusRepositoryImpl.getInstance().uploadStatus(fileUri, currentUser, binding.progressbar.getRoot(), requireActivity());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
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

        initProgressBarDetails();
        loadUserInfo();
        setupViewModel();
        setupRecyclerView();
        handleItemsClick();

        return binding.getRoot();
    }

    private void loadUserInfo() {
        Picasso.get().load(currentUser.getImage()).placeholder(R.drawable.profile_image).into(binding.image);
    }

    @SuppressLint("SetTextI18n")
    private void initProgressBarDetails() {
        binding.progressbar.dialogTitle.setText("Uploading Image");
        binding.progressbar.dialogDescription.setText("Please wait, while we are uploading your image...");
    }

    private void setupRecyclerView() {
        statusAdapter = new StatusAdapter(getContext(), viewModel.getUserStatues().getValue());

        binding.statusList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.statusList.addItemDecoration(new DividerItemDecoration(binding.statusList.getContext(), DividerItemDecoration.VERTICAL));
        binding.statusList.setAdapter(statusAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StatusViewModel.class);
        viewModel.init();
        viewModel.getUserStatues().observe(getViewLifecycleOwner(), userStatuses -> statusAdapter.notifyDataSetChanged());
    }

    private void handleItemsClick() {
        binding.btnStatus.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            imagePickActivityResultLauncher.launch(intent);
        });

        binding.addStatus.setOnClickListener(view -> cameraButtonClicked());

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

    private void cameraButtonClicked() {
        Intent intent = new Intent(requireContext(), CameraxActivity.class);
        intent.putExtra(getString(R.string.IS_FROM_STORIES), true);
        imageCaptureActivityResultLauncher.launch(intent);
    }

    private void filter(String text) {
        ArrayList<UserStatus> filteredList = new ArrayList<>();

        for (UserStatus item : Objects.requireNonNull(viewModel.getUserStatues().getValue())) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (!filteredList.isEmpty()) {
            statusAdapter.filterList(filteredList);
        } else {
            statusAdapter.filterList(new ArrayList<>());
        }
    }
}