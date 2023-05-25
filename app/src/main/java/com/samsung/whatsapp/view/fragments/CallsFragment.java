package com.samsung.whatsapp.view.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samsung.whatsapp.R;
import com.samsung.whatsapp.adapters.CallAdapter;
import com.samsung.whatsapp.databinding.FragmentCallsBinding;
import com.samsung.whatsapp.model.Call;

import java.util.ArrayList;
import java.util.List;

public class CallsFragment extends Fragment {
    private FragmentCallsBinding binding;

    public CallsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCallsBinding.inflate(inflater, container, false);
        binding.callList.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Call> calls = new ArrayList<>();

        calls.add(new Call(
                "1",
                "Monkey D. Luffy",
                "15/04/2020, 9:14 pm",
                "https://i.pinimg.com/736x/11/44/3f/11443fac4c8a44f29e5e418124e8d870.jpg",
                getString(R.string.INCOMING),
                getString(R.string.AUDIO)
        ));

        calls.add(new Call(
                "2",
                "Roronoa Zoro",
                "15/04/2020, 9:15 pm",
                "https://i.pinimg.com/236x/2c/9e/0f/2c9e0f0f72943eb8585a1c0ef9f44689.jpg",
                getString(R.string.OUTGOING),
                getString(R.string.VIDEO)));

        calls.add(new Call(
                "3",
                "Vinsmoke Sanji",
                "15/04/2020, 9:18 pm",
                "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTbD5pUD1znRvKlSCL3dPfgyz5POwLOuGMCuyprzku_&s",
                getString(R.string.MISSED_CALL),
                getString(R.string.VIDEO)));

        calls.add(new Call(
                "1",
                "Shanks",
                "15/04/2020, 9:24 pm",
                "https://res.cloudinary.com/dz209s6jk/image/upload/v1624362238/Avatars/fqlfempxuef549f71oor.jpg",
                getString(R.string.INCOMING),
                getString(R.string.VIDEO)));

        binding.callList.setAdapter(new CallAdapter(calls, getContext()));

        return binding.getRoot();
    }
}