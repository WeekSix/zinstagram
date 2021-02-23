package com.example.zinstagram;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CommentActivity extends AppCompatActivity {

    private static final String TAG = "Comment Activity: ";
    //Firebase Variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private String userID;
    private String imageURL;
    private String caption;
    private String checkUid;
    private Button btnSend;
    private Button btnDelete;
    private Button btnBack;
    private String timeStamp;
    private String comments;
    private EditText commentEditText;
    private String commentUsername;
    private String userProfileUrl;

    ArrayList<Comments> commentsList;
    ArrayList<String> imageID;
    ArrayList<String> commentsID;

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    CommentViewAdapter commentViewAdapter;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //set comment activity with activity_view_image layout page.
        setContentView(R.layout.activity_view_image);

        if(getIntent().hasExtra("image_url")) {
            imageURL = getIntent().getStringExtra("image_url");
        }

        context = CommentActivity.this;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        commentsList = new ArrayList<Comments>();
        imageID = new ArrayList<String>();
        commentsID = new ArrayList<String>();
        //initial bottoms;
        commentEditText = findViewById(R.id.editComments);
        btnSend = findViewById(R.id.btnComments);
        btnDelete = findViewById(R.id.btnDeletePost);
        btnBack = findViewById(R.id.btnBack);
        //initial layouts;
        recyclerView = findViewById(R.id.commentRecyclerView);
        layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        Toast.makeText(CommentActivity.this, "Loading comments",
                Toast.LENGTH_LONG).show();

        reloadComments();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comments = commentEditText.getText().toString();
                if (TextUtils.isEmpty(comments)) {
                    Toast.makeText(CommentActivity.this, "Please type your comment.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (comments.length() > 200) {
                    Toast.makeText(CommentActivity.this, "Your comment is too long (more than 200 characters)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CommentActivity.this, "Successfully commented", Toast.LENGTH_SHORT).show();
                    uploadComments();
                }
            }
        });

        btnDelete.setOnClickListener(v -> deletePost());

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void reloadComments() {
        imageID.clear();
        commentsID.clear();
        commentsList.clear();

        Log.d(TAG, "Start to reload comments");

        database.collection("Photos")
                .whereEqualTo("storageRef", imageURL)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // use Postedphoto template to load data from firebase.
                                PostedPhoto postedPhoto = document.toObject(PostedPhoto.class);
                                imageID.add(document.getId());
                                caption = postedPhoto.getCaption();
                                checkUid = postedPhoto.getUid();
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            if(userID.equals(checkUid)) {
                                //if current user,enable delete
                                btnDelete.setEnabled(true);
                            }

                            database.collection("Comments")
                                    .whereEqualTo("photoRef", imageURL)
                                    .orderBy("timestamp", Query.Direction.ASCENDING)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Comments comments = document.toObject(Comments.class);
                                                    commentsID.add(document.getId());
                                                    commentsList.add(comments);
                                                    Log.d(TAG, document.getId() + " : " + document.getData());
                                                }
                                            } else {
                                                Log.d(TAG, "Error onComplete getting documents: ", task.getException());
                                            }
                                            //Adapt view to comment layout.
                                            commentViewAdapter = new CommentViewAdapter(context, commentsList, caption, imageURL);
                                            recyclerView.setAdapter(commentViewAdapter);
                                            recyclerView.setHasFixedSize(true);
                                        }
                                    });
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void uploadComments() {
        Log.d(TAG, "uploadComments");
        comments = commentEditText.getText().toString();
        DocumentReference documentReference = database.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshots, @Nullable FirebaseFirestoreException error) {
                commentUsername = documentSnapshots.getString("userName");
                String displayPicPath = documentSnapshots.getString("displayPicPath");
                StorageReference displayReference = FirebaseStorage.getInstance().getReference().child(displayPicPath);
                displayReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        userProfileUrl = uri.toString();
                        Log.d(TAG, "the profile ref for user " + userID  +"is " + userProfileUrl);
                    }
                });

                timeStamp = String.valueOf(System.currentTimeMillis());

                Map<String, Object> comment = new HashMap<>();
                comment.put("uid", userID);
                comment.put("username", commentUsername);
                comment.put("profileRef", userProfileUrl);
                comment.put("timestamp", timeStamp);
                comment.put("comment", comments);
                comment.put("photoRef", imageURL);

                database.collection("Comments")
                        .add(comment)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "Successfully retrieved documentSnapshot with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, " Error: Failed upload Comment");
                            }
                        });
            }
        });

        commentEditText.setText("");
        reloadComments();
    }

    private void deletePost() {
        if(btnDelete.isEnabled()) {
            database.collection("Photos").document(imageID.get(0))
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "image successfully deleted!");
                            Toast.makeText(CommentActivity.this, "Post Deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error: Failed deleting document", e);
                        }
                    });

            for (int i = 0; i < commentsID.size(); i++) {
                database.collection("Comments").document(commentsID.get(i))
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "comments successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error: Failed deleting comments", e);
                            }
                        });
            }

            Intent intent = new Intent(CommentActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //public user getApp() { return ((user) getApplicationContext()); }
}