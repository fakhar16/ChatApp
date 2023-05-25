package com.samsung.whatsapp.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samsung.whatsapp.R;
import com.samsung.whatsapp.databinding.ActivityGroupChatBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity {
    private String currentUserId;
    private String currentUserName;
    private DatabaseReference userRef;
    private DatabaseReference groupNameRef;
    private ActivityGroupChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String currentGroupName = getIntent().getExtras().get(getString(R.string.GROUP_NAME)).toString();
        setSupportActionBar(binding.groupChatBarLayout.mainAppBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(currentGroupName);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.USERS));
        groupNameRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.GROUPS)).child(currentGroupName);

        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        GetUserInfo();

        binding.sendMessageButton.setOnClickListener(view -> {
            SaveMessageInfoToDatabase();
            binding.userMessageInput.setText("");
            binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    DisplayMessages(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot snapshot) {
        Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String chatDate = (String) iterator.next().getValue();
            String chatMessage = (String) iterator.next().getValue();
            String chatName = (String) iterator.next().getValue();
            String chatTime = (String) iterator.next().getValue();

            binding.groupChatTextDisplay.append(chatName + " : \n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n");
            binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void SaveMessageInfoToDatabase() {
        String message = binding.userMessageInput.getText().toString();
        String messageKey = groupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        } else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            String currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            String currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            DatabaseReference groupMessageKeyRef = groupNameRef.child(Objects.requireNonNull(messageKey));

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put(getString(R.string.NAME), currentUserName);
            messageInfoMap.put(getString(R.string.MESSAGE), message);
            messageInfoMap.put(getString(R.string.DATE), currentDate);
            messageInfoMap.put(getString(R.string.TIME), currentTime);

            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void GetUserInfo() {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserName = Objects.requireNonNull(snapshot.child(getString(R.string.NAME)).getValue()).toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}