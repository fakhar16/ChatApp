package com.samsung.whatsapp;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ApplicationClass extends Application {

    public static DatabaseReference userDatabaseReference;
    public static DatabaseReference messageDatabaseReference;
    public static DatabaseReference contactsDatabaseReference;
    public static DatabaseReference storiesDatabaseReference;
    public static DatabaseReference presenceDatabaseReference;
    public static DatabaseReference videoUserDatabaseReference;
    public static DatabaseReference profileImagesDatabaseReference;
    public static DatabaseReference starMessagesDatabaseReference;
    public static DatabaseReference imageUrlDatabaseReference;
    public static DatabaseReference videoUrlDatabaseReference;
    public static DatabaseReference docsUrlDatabaseReference;
    public static StorageReference userProfilesImagesReference;
    public static StorageReference imageStorageReference;
    public static StorageReference videoStorageReference;
    public static StorageReference docsStorageReference;

    public static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        initializeDatabaseReferences(firebaseDatabase);
        initializeStorageReferences();
        keepSyncingReferences();
    }

    private void keepSyncingReferences() {
        userDatabaseReference.keepSynced(true);
        messageDatabaseReference.keepSynced(true);
        contactsDatabaseReference.keepSynced(true);
        presenceDatabaseReference.keepSynced(true);
        storiesDatabaseReference.keepSynced(true);
        videoUserDatabaseReference.keepSynced(true);
        profileImagesDatabaseReference.keepSynced(true);
        starMessagesDatabaseReference.keepSynced(true);
        imageUrlDatabaseReference.keepSynced(true);
        videoUrlDatabaseReference.keepSynced(true);
        docsUrlDatabaseReference.keepSynced(true);
    }

    private void initializeStorageReferences() {
        userProfilesImagesReference = FirebaseStorage.getInstance().getReference().child(getString(R.string.PROFILE_IMAGES));
        imageStorageReference = FirebaseStorage.getInstance().getReference().child(application.getApplicationContext().getString(R.string.IMAGE_FILES));
        videoStorageReference = FirebaseStorage.getInstance().getReference().child(application.getApplicationContext().getString(R.string.VIDEO_FILES));
        docsStorageReference = FirebaseStorage.getInstance().getReference().child(getString(R.string.PDF_FILES));
    }

    private void initializeDatabaseReferences(FirebaseDatabase firebaseDatabase) {
        userDatabaseReference = firebaseDatabase.getReference(getString(R.string.USERS));
        messageDatabaseReference = firebaseDatabase.getReference(getString(R.string.MESSAGES));
        contactsDatabaseReference = firebaseDatabase.getReference(getString(R.string.CONTACTS));
        presenceDatabaseReference = firebaseDatabase.getReference(getString(R.string.PRESENCE));
        storiesDatabaseReference = firebaseDatabase.getReference(getString(R.string.STORIES));
        videoUserDatabaseReference = firebaseDatabase.getReference(getString(R.string.VIDEO_USERS));
        profileImagesDatabaseReference = firebaseDatabase.getReference(getString(R.string.PROFILE_IMAGES));
        starMessagesDatabaseReference = firebaseDatabase.getReference(getString(R.string.STARRED_MESSAGES));
        imageUrlDatabaseReference = firebaseDatabase.getReference(getString(R.string.IMAGE_URL_USED_BY_USERS));
        videoUrlDatabaseReference = firebaseDatabase.getReference(getString(R.string.VIDEO_URL_USED_BY_USERS));
        docsUrlDatabaseReference = firebaseDatabase.getReference(getString(R.string.doc_url_used_by_users));
    }
}