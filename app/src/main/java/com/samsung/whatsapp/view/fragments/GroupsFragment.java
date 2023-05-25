package com.samsung.whatsapp.view.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.view.activities.GroupChatActivity;
import com.samsung.whatsapp.databinding.FragmentGroupsBinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GroupsFragment extends Fragment {

   private ArrayAdapter<String> arrayAdapter;
   private ArrayList<String> list_of_groups;

   private DatabaseReference groupRef;
   private FragmentGroupsBinding binding;

    public GroupsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGroupsBinding.inflate(inflater, container, false);
        groupRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.GROUPS));

        InitializeFields();
        RetrieveAndDisplayGroups();

        binding.listView.setOnItemClickListener((adapterView, view, position, id) -> {
           String currentGroupName = adapterView.getItemAtPosition(position).toString();

           Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
           groupChatIntent.putExtra(getString(R.string.GROUP_NAME), currentGroupName);
           startActivity(groupChatIntent);
       });

       return binding.getRoot();
    }


    private void InitializeFields() {
        list_of_groups = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, list_of_groups);
        binding.listView.setAdapter(arrayAdapter);
    }
    private void RetrieveAndDisplayGroups() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    set.add(dataSnapshot.getKey());
                }
                list_of_groups.clear();
                list_of_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
