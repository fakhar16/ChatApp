package com.samsung.whatsapp.view.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.FragmentVideoBinding;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class VideoFragment extends Fragment {
    FragmentVideoBinding binding;
    private int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private ListenableFuture<ProcessCameraProvider> processCameraProvider;
    private ProcessCameraProvider cameraProvider;
    private VideoCapture<Recorder> videoCapture;
    Recording recording = null;
    private CountDownTimer countDownTimer;
    long counter = 0;

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVideoBinding.inflate(inflater, container, false);
        processCameraProvider = ProcessCameraProvider.getInstance(requireContext());
        processCameraProvider.addListener(() -> {
            try {
                cameraProvider = processCameraProvider.get();
                startCameraX(cameraProvider, cameraFacing);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
        handleItemClick();
        return binding.getRoot();
    }

    private void handleItemClick() {
        binding.closeCamera.setOnClickListener(view -> requireActivity().finish());
        binding.takeVideo.setOnClickListener(view -> captureVideo());
        binding.flipCamera.setOnClickListener(view -> flipCamera());
    }

    private void flipCamera() {
        if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
            cameraFacing = CameraSelector.LENS_FACING_FRONT;
        } else {
            cameraFacing = CameraSelector.LENS_FACING_BACK;
        }
        startCameraX(cameraProvider, cameraFacing);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider, int cameraFacing) {
        cameraProvider.unbindAll();

        //Camera selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraFacing)
                .build();

        //Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.preview.getSurfaceProvider());

        //Video capture use case
        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.SD))
                .build();

        videoCapture = VideoCapture.withOutput(recorder);

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, videoCapture);
    }

    private void captureVideo() {
        Recording recording1 = recording;
        if (recording1 != null) {
            recording1.stop();
            recording = null;
            return;
        }

        binding.takeVideo.setImageResource(R.drawable.baseline_stop_circle_24);

        String name = new SimpleDateFormat("yyy-MM-dd-HH-ss-SSS", Locale.US).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CameraX-Videos");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(requireContext().getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        recording = videoCapture.getOutput()
                .prepareRecording(requireContext(), options).withAudioEnabled()
                .start(ContextCompat.getMainExecutor(requireContext()), videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                        startVideo();

                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                        if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                            Intent data = new Intent();
                            data.putExtra(requireContext().getString(R.string.VIDEO_URI), ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri().toString());
                            data.putExtra(requireContext().getString(R.string.FILE_TYPE), requireContext().getString(R.string.VIDEO));
                            requireActivity().setResult(Activity.RESULT_OK, data);
                            requireActivity().finish();
                        } else {
                            recording.close();
                            recording = null;
                            Toast.makeText(requireContext(), "Error while saving the video", Toast.LENGTH_SHORT).show();
                        }
                        stopVideo();
                    }
                });
    }

    private void stopVideo() {
        binding.takeVideo.setImageResource(R.drawable.baseline_fiber_manual_record_24);
        binding.flipCamera.setVisibility(View.VISIBLE);
        binding.gallery.setVisibility(View.VISIBLE);
        binding.closeCamera.setVisibility(View.VISIBLE);
        countDownTimer.cancel();
    }

    private void startVideo() {
        binding.takeVideo.setImageResource(R.drawable.baseline_stop_circle_24);
        binding.flipCamera.setVisibility(View.GONE);
        binding.gallery.setVisibility(View.GONE);
        binding.closeCamera.setVisibility(View.GONE);

        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {

            public void onTick(long millisUntilFinished) {
                counter++;
                updateRecordTimerText(counter);
            }

            public void onFinish() {
                counter = 0;
                updateRecordTimerText(counter);
            }
        }.start();
    }

    private void updateRecordTimerText(long millis) {
        int minutes = (int)(millis) / 60;
        int seconds = (int)(millis) % 60;

        String timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds);

        binding.recordingTimer.setText(timeFormatted);
    }
}