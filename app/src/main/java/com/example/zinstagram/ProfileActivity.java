package com.example.zinstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.health.UidHealthStats;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser user;
    private CollectionReference reference;
    private StorageReference photoReference;
    private String userID;
    private Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        signOut = (Button) findViewById(R.id.signOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Sign Out
                FirebaseAuth.getInstance().signOut();
                //redirect to Sign In page
                startActivity(new Intent(ProfileActivity.this, SignInActivity.class));
            }
        });

        //Get unique user ID
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseFirestore.getInstance().collection("users");
        userID = user.getUid();
        //photoReference = FirebaseStorage.getInstance().getReference("pics/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "profile.jpeg");


        
        final TextView userNameTextView = (TextView) findViewById(R.id.userName);
        final TextView bioTextView = (TextView) findViewById(R.id.bio);
        final TextView UID = (TextView) findViewById(R.id.UID);
        final CircleImageView profilePhoto = (CircleImageView) findViewById(R.id.profilePhoto);


        reference.document(userID).get().
                addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                // get user's info
                user userProfile = documentSnapshot.toObject(user.class);

                // show username and bio
                if (userProfile != null) {
                    String userName = userProfile.userName;
                    String bio = userProfile.bio;
//                    photoReference = FirebaseStorage.getInstance().getReference("gs://zinstagram-79519.appspot.com/pics/ebhKqyYbeCT5bW9Y6JqqYKm6ch93/profile.jpeg");
//
//                    Glide.with(ProfileActivity.this)
//                            .load(photoReference)
//                            .into(profilePhoto);

                    if(getIntent().hasExtra("bitmap")) {
                        Bitmap bitmap = getIntent().getParcelableExtra("bitmap");
                        profilePhoto.setImageBitmap(bitmap);
                    }

                    userNameTextView.setText(userName);
                    bioTextView.setText(bio);

                }
            }

        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Failed to acquire user's information", Toast.LENGTH_LONG).show();
                    }
                });
    }

    //            private Uri getThumbnailUrl(DatabaseReference reference) {
//                photoReference.getDownloadUrl()
//                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
//                            @Override
//                            public void onSuccess(Uri uri) {
//                                uri = getMetadata().getDownloadUrl();
//
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(ProfileActivity.this, "Could not find the profile phto..", Toast.LENGTH_LONG).show();
//                            }
//                        })
//            }
}