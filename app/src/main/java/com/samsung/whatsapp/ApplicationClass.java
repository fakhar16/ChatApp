package com.samsung.whatsapp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ApplicationClass extends Application {

    public static DatabaseReference userDatabaseReference;
    public static DatabaseReference messageDatabaseReference;
    public static DatabaseReference storiesDatabaseReference;
    public static DatabaseReference presenceDatabaseReference;
    public static DatabaseReference videoUserDatabaseReference;
    public static DatabaseReference profileImagesDatabaseReference;
    public static DatabaseReference starMessagesDatabaseReference;
    public static StorageReference userProfilesImagesReference;
    public static StorageReference imageStorageReference;
    public static StorageReference videoStorageReference;

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        initializeDatabaseReferences(firebaseDatabase);
        initializeStorageReferences();
        keepSyncingReferences();
    }

    private void keepSyncingReferences() {
        userDatabaseReference.keepSynced(true);
        messageDatabaseReference.keepSynced(true);
        presenceDatabaseReference.keepSynced(true);
        storiesDatabaseReference.keepSynced(true);
        videoUserDatabaseReference.keepSynced(true);
        profileImagesDatabaseReference.keepSynced(true);
        starMessagesDatabaseReference.keepSynced(true);
    }

    private void initializeStorageReferences() {
        userProfilesImagesReference = FirebaseStorage.getInstance().getReference().child(getString(R.string.PROFILE_IMAGES));
        imageStorageReference = FirebaseStorage.getInstance().getReference().child(context.getString(R.string.IMAGE_FILES));
        videoStorageReference = FirebaseStorage.getInstance().getReference().child(context.getString(R.string.VIDEO_FILES));
    }

    private void initializeDatabaseReferences(FirebaseDatabase firebaseDatabase) {
        userDatabaseReference = firebaseDatabase.getReference(getString(R.string.USERS));
        messageDatabaseReference = firebaseDatabase.getReference(getString(R.string.MESSAGES));
        presenceDatabaseReference = firebaseDatabase.getReference(getString(R.string.PRESENCE));
        storiesDatabaseReference = firebaseDatabase.getReference(getString(R.string.STORIES));
        videoUserDatabaseReference = firebaseDatabase.getReference(getString(R.string.VIDEO_USERS));
        profileImagesDatabaseReference = firebaseDatabase.getReference(getString(R.string.PROFILE_IMAGES));
        starMessagesDatabaseReference = firebaseDatabase.getReference(getString(R.string.STARRED_MESSAGES));
    }
}