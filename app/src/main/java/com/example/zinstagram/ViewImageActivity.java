package com.example.zinstagram;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ViewImageActivity extends AppCompatActivity {

    private ImageView imageView;
    private static String TAG = "ViewImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "CLICK INTO OPEN IMAGE");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        imageView = findViewById(R.id.clickImageView);

        if(getIntent().hasExtra("image_url")) {
            String imageUrl = getIntent().getStringExtra("image_url");

            Picasso.get().load(imageUrl).into(imageView);
        }
    }

    public void disappear(View view) {finish();}
}