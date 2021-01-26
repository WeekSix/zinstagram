package com.example.zinstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser user;
    private DatabaseReference reference;
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
        reference = FirebaseDatabase.getInstance().getReference("Users");
        userID = user.getUid();
        photoReference = FirebaseStorage.getInstance().getReference(".pics/profilePhotos" + userID + ".jpeg");

        
        final TextView userNameTextView = (TextView) findViewById(R.id.userName);
        final TextView bioTextView = (TextView) findViewById(R.id.bio);
        final CircleImageView profilePhoto = (CircleImageView) findViewById(R.id.profilePhoto);

                reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get user's info
                user userProfile = snapshot.getValue(user.class);

                // show username and bio
                if (userProfile != null) {
                    String userName = userProfile.userName;
                    String bio = userProfile.bio;

                    Glide.with(ProfileActivity.this)
                            .load(photoReference)
                            .into(profilePhoto);

                    userNameTextView.setText(userName);
                    bioTextView.setText(userID);

                }
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


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to acquire user's information", Toast.LENGTH_LONG).show();
            }
        });


    }
}