package com.example.zinstagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registration extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegistrationActivity";

    private Button signIn, registration;
    private EditText editTextUserName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextBio;
    private ProgressBar progressBar;

    private CircleImageView profilePhoto;
    private int TAKE_IMAGE_CODE = 10001;
    private Uri uri;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        signIn = (Button) findViewById(R.id.signIn);
        signIn.setOnClickListener(this);

        registration = (Button) findViewById(R.id.register);
        registration.setOnClickListener(this);

        profilePhoto = (CircleImageView) findViewById(R.id.profilePhoto);
        editTextUserName = (EditText) findViewById(R.id.userName);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
        editTextBio = (EditText) findViewById(R.id.bio);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
                //Click banner to go back to home page
            case R.id.signIn:
                startActivity(new Intent(this, SignInActivity.class));
                break;
                // register new user method
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
            return;
        }

        if(bio.isEmpty()) {
            editTextBio.setError("A quick bio helps people to know you!");
            editTextBio.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //Upload info to firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()) {
                        user user = new user(userName, email, bio); //create new object for new user

                        //FirebaseDatabase database = FirebaseDatabase.getInstance(); //get Firebase database

                        //Store users value into its database object
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnCompleteListener(task1 -> {
                                    // Prompt message to tell user whether registration is successful or not.
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(Registration.this, "User registered successful!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(this, SignInActivity.class));
                                    } else {
                                        Toast.makeText(Registration.this, "Username already exist. Please try again.", Toast.LENGTH_LONG).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                });
                    } else {
                        Toast.makeText(Registration.this, "Failed to register. Please try again Later", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    // Open camera and capture image
    public void uploadPhoto(View view) {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (photoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(photoIntent, TAKE_IMAGE_CODE);
        }
    }

    // get bitmap for captured image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_IMAGE_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    profilePhoto.setImageBitmap(bitmap);
                    handleUploadPhoto(bitmap);
            }
        }
    }

    //Upload thumbnail to Firebase
    private void handleUploadPhoto(Bitmap bitmap) {
        ByteArrayOutputStream thumbnail = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, thumbnail);

        if (mAuth.getCurrentUser() != null) {
            String userID = mAuth.getCurrentUser().getUid();
            StorageReference reference = FirebaseStorage.getInstance().getReference()
                    .child("pics/" + userID)
                    .child("profile.jpeg");

        reference.putBytes(thumbnail.toByteArray())
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            getThumbnailUrl(reference);
                        }
                    }
                });
    }
    }

    private void getThumbnailUrl(StorageReference reference) {
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess: " + uri);
                        setUserProfileUrl(uri);
                    }
                });
    }

    private void setUserProfileUrl(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Registration.this, "Photo upload Successful...", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Registration.this, "Profile photo failed...", Toast.LENGTH_LONG).show();
                    }
                });
    }
}