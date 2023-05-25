package com.samsung.whatsapp.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.samsung.whatsapp.R;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        ImageView imageView = findViewById(R.id.image_viewer);
        String imageUrl = getIntent().getStringExtra(getString(R.string.URL));

        Picasso.get().load(imageUrl).placeholder(R.drawable.profile_image).into(imageView);
    }
}