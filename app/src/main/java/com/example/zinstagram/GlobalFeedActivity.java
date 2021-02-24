package com.example.zinstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class GlobalFeedActivity extends AppCompatActivity {

    private static final String TAG = "GlobalFeedActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    ArrayList<PostedPhoto> photoList;
    private Switch aSwitch;

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    GlobalRecyclerAdapter globalRecyclerAdapter;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_feed);

        context = GlobalFeedActivity.this;

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        photoList = new ArrayList <PostedPhoto> ();

        aSwitch = findViewById(R.id.globalButton);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {   //Enable Switch
                    Toast.makeText(GlobalFeedActivity.this, "Redirect to Profile Feed", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(GlobalFeedActivity.this, ProfileActivity.class));
                    finish();
                } else {
                    //Switch disabled
                    Log.e(TAG, "failed to redirect to profile feed");
                }
            }
        });

        recyclerView = findViewById(R.id.globalRecyclerView);
        layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        Toast.makeText(GlobalFeedActivity.this, "Loading images", Toast.LENGTH_SHORT).show();

        reloadPhotos();
    }

//    private void reloadPhotos() {
//        photoList.clear();
//        db.collection("Photos")
//                .orderBy("timestamp", Query.Direction.DESCENDING)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                PostedPhoto postedPhoto = document.toObject(PostedPhoto.class);
//                                photoList.add(postedPhoto);
//                                Log.d(TAG, document.getId() + " => " + document.getData());
//                            }
//                        } else {
//                            Log.d(TAG, "Error getting documents: ", task.getException());
//                        }
//
//                        globalRecyclerAdapter = new GlobalRecyclerAdapter(context, photoList);
//                        recyclerView.setAdapter(globalRecyclerAdapter);
//                        recyclerView.setHasFixedSize(true);
//                    }
//                });
//    }

    private void reloadPhotos() {
        photoList.clear();
        db.collection("Photos")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                PostedPhoto postedPhoto = document.toObject(PostedPhoto.class);
                                photoList.add(postedPhoto);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        globalRecyclerAdapter = new GlobalRecyclerAdapter(context, photoList);
                        recyclerView.setAdapter(globalRecyclerAdapter);
                        recyclerView.setHasFixedSize(true);
                    }
                });
    }

    public user getApp(){
        return ((user) getApplicationContext());
    }
}