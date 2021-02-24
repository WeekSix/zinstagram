package com.example.zinstagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registration extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegistrationActivity";

    private Button signIn, registration, photoUpload;
    private EditText editTextUserName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextBio;
    private ProgressBar progressBar;

    private CircleImageView profilePhoto;
    private int TAKE_IMAGE_CODE = 10001;
    private Bitmap bitmap;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    String uid;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        signIn = (Button) findViewById(R.id.signIn);
        signIn.setOnClickListener(this);

        registration = (Button) findViewById(R.id.register);
        registration.setOnClickListener(this);

        photoUpload = (Button) findViewById(R.id.photoUpload);
        photoUpload.setOnClickListener(this);

        profilePhoto = (CircleImageView) findViewById(R.id.profilePhoto);
        editTextUserName = (EditText) findViewById(R.id.userName);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
        editTextBio = (EditText) findViewById(R.id.bio);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        database = FirebaseFirestore.getInstance();
        //A3
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
                //Click banner to go back to home page
            case R.id.signIn:
                startActivity(new Intent(this, SignInActivity.class));
                break;
                // register new user method
            case R.id.photoUpload:
                takePhoto();
                break;

            case R.id.register:
                registration();
                break;
        }
    }

    private void registration() {
        //declare text strings
        String userName = editTextUserName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String bio = editTextBio.getText().toString().trim();

        //Validate if all texts are filled
        if(userName.isEmpty()) {
            editTextUserName.setError("User name is required!");
            editTextUserName.requestFocus();
            return;
        }

        if(email.isEmpty()) {
            editTextEmail.setError("Email address is required!");
            editTextEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address.");
            editTextEmail.requestFocus();
            return;
        }

        if(password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if(password.length() < 6) {
            editTextPassword.setError("Please enter a password length > 6.");
            editTextPassword.requestFocus();
            editTextPassword.getText().clear();
            return;
        }

        if(confirmPassword.isEmpty()) {
            editTextConfirmPassword.setError("Please confirm your password");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if(!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Password not matching");
            editTextConfirmPassword.requestFocus();
            editTextPassword.getText().clear();
            editTextConfirmPassword.getText().clear();
            return;
        }

        if(bio.isEmpty()) {
            editTextBio.setError("A quick bio helps people to know you!");
            editTextBio.requestFocus();
            return;
        }

        if (bio.length() > 60) {
            Toast.makeText(this, "Please give a shorter biography (less than 100 characters).", Toast.LENGTH_SHORT).show();
            editTextBio.getText().clear();
            return;
        }

        //Log.d(TAG, "in registration");
        progressBar.setVisibility(View.VISIBLE);
        //Upload info to firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "in registration " + email);
                        if (task.isSuccessful()) {
                            uid = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "Register for user ->" + uid);

                            Log.d(TAG, "createUserWithEmail:success");
                            user user = new user(userName, email, bio, uid); //create new object for new user


                            Map<String, Object> hashUser = new HashMap<>();
                            hashUser.put("userName", user.userName);
                            hashUser.put("email", user.email);
                            hashUser.put("bio", user.bio);
                            hashUser.put("displayPicPath", "pics/" + user.displayPicPath);

                            //FirebaseDatabase database = FirebaseDatabase.getInstance(); //get Firebase database

                            //Store users value into its FirebaseDatabase object
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(uid)
                                    .set(hashUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // Prompt message to tell user whether registration is successful or not.
                                                Log.d(TAG, "Try to save user info to firebase");
                                                mAuth.getCurrentUser().sendEmailVerification();
                                                Toast.makeText(Registration.this, "User registered successful! Verification sent to your email", Toast.LENGTH_LONG).show();
                                                try {
                                                    Thread.sleep(500);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                // Upload display photo to Firebase Storage
                                                handleUploadPhoto(bitmap);

                                                try {
                                                    Thread.sleep(3000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                //Redirect to Profile Page and display user profile photo
                                                startActivity(new Intent(Registration.this, ProfileActivity.class).putExtra("bitmap", bitmap));
                                            } else {
                                                Toast.makeText(Registration.this, "Username already exist. Please try again.", Toast.LENGTH_LONG).show();
                                            }
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        } else {
                            Toast.makeText(Registration.this, "Failed to register. Please try again Later", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Failed Register");
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }


    // Open camera and capture image
    public void takePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, TAKE_IMAGE_CODE);
        }
    }

    // get bitmap for captured image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_IMAGE_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    bitmap = (Bitmap) data.getExtras().get("data");
                    profilePhoto.setImageBitmap(bitmap);
            }
        }
    }

    //Upload thumbnail to Firebase
    private void handleUploadPhoto(Bitmap bitmap) {
        ByteArrayOutputStream thumbnail = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, thumbnail);
        Log.d(TAG,"Start to upload profile photo");

        //save photo in the path
        if (mAuth.getCurrentUser() != null) {
            //set photo path
            StorageReference reference = FirebaseStorage.getInstance().getReference()
                    .child("pics/" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("displayPic.jpg");

            reference.putBytes(thumbnail.toByteArray())
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                getThumbnailUrl(reference);
                            }
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progressPercent = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressBar.setProgress((int)progressPercent);
                        }
                    });
        }
    }
    //get uploaded photo uri
    private void getThumbnailUrl(StorageReference reference) {
        Log.d(TAG,"Start to download profile photo url");
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess: " + uri);
                        setUserProfileUrl(uri);
                        Log.d(TAG, "proceed to set profile url ");
                    }
                });

    }

    private void setUserProfileUrl(Uri uri){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String profileRef = uri.toString();

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("profileRef", profileRef);

        Log.d(TAG, "set profile URl with  => " + profileRef);

        database.collection("users").document(userId)
                .set(userUpdate, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, userId + "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}