package com.samsung.whatsapp.view.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import android.provider.MediaStore;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.FragmentPhotoBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PhotoFragment extends Fragment {
    private FragmentPhotoBinding binding;
    private int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private int flashMode = ImageCapture.FLASH_MODE_OFF;
    private ListenableFuture<ProcessCameraProvider> processCameraProvider;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    Camera camera;
    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPhotoBinding.inflate(inflater, container, false);
        processCameraProvider = ProcessCameraProvider.getInstance(requireContext());
        processCameraProvider.addListener(() -> {
            try {
                cameraProvider = processCameraProvider.get();
                startCameraX(cameraProvider, cameraFacing, flashMode);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));

        handleItemClick();
        return binding.getRoot();
    }

    private void handleItemClick() {
        binding.closeCamera.setOnClickListener(view -> requireActivity().finish());
        binding.takeImage.setOnClickListener(view -> capturePhoto());
        binding.flipCamera.setOnClickListener(view -> flipCamera());
        binding.toggleFlash.setOnClickListener(view -> toggleFlash());
    }

    private void toggleFlash() {
        switch (flashMode) {
            case ImageCapture.FLASH_MODE_OFF:
                flashMode = ImageCapture.FLASH_MODE_ON;
                binding.toggleFlash.setImageResource(R.drawable.baseline_flash_on_24);
                break;

            case ImageCapture.FLASH_MODE_ON:
                flashMode = ImageCapture.FLASH_MODE_AUTO;
                binding.toggleFlash.setImageResource(R.drawable.baseline_flash_auto_24);
                break;

            case ImageCapture.FLASH_MODE_AUTO:
                flashMode = ImageCapture.FLASH_MODE_OFF;
                binding.toggleFlash.setImageResource(R.drawable.baseline_flash_off_24);
                break;
        }

        startCameraX(cameraProvider, cameraFacing, flashMode);
    }

    private void flipCamera() {
        if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
            cameraFacing = CameraSelector.LENS_FACING_FRONT;
        } else {
            cameraFacing = CameraSelector.LENS_FACING_BACK;
        }
        startCameraX(cameraProvider, cameraFacing, flashMode);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider, int cameraFacing, int flashMode) {
        cameraProvider.unbindAll();

        //Camera selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraFacing)
                .build();

        //Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.preview.getSurfaceProvider());

        //Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetResolution(new Size(800, 800))
                .setFlashMode(flashMode)
                .build();

        camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private void capturePhoto() {
        String name = new SimpleDateFormat("yyy-MM-dd-HH-ss-SSS", Locale.US).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraX-Pictures");

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(requireContext().getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                .build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Intent data = new Intent();
                        data.putExtra(requireContext().getString(R.string.IMAGE_URI), Objects.requireNonNull(outputFileResults.getSavedUri()).toString());
                        data.putExtra(requireContext().getString(R.string.FILE_TYPE), requireContext().getString(R.string.IMAGE));
                        requireActivity().setResult(Activity.RESULT_OK, data);
                        requireActivity().finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(requireContext(), "Error while saving the image : " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}