package com.example.zinstagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.health.UidHealthStats;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.MoreObjects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static String TAG = "ProfileActivity";

    static final int REQUEST_TAKE_PHOTO = 1000;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private CollectionReference firestoreReference, photoFirebaseRef;
    private FirebaseFirestore db;
    private StorageReference displayReference;
    private String uid;
    private Button signOut;

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<PostedPhoto> photoList;
    Context context;
    Uri imageUri;

    String curPhotoPath;
    String timeStamp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        photoList = new ArrayList<>();

        //Sign Out the current user
        signOut = (Button) findViewById(R.id.signOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Logged Out",
                        Toast.LENGTH_SHORT).show();
                try {
                    //Sign Out
                    finish();
                    FirebaseAuth.getInstance().signOut();
                    //redirect to Sign In page
                    startActivity(new Intent(ProfileActivity.this, SignInActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        // initial layout views;
        final TextView userNameTextView = (TextView) findViewById(R.id.userName);
        final TextView bioTextView = (TextView) findViewById(R.id.bio);
        final CircleImageView profilePhoto = (CircleImageView) findViewById(R.id.profilePhoto);

        if(mAuth.getCurrentUser() == null){
            Log.d(TAG,"User NOT found, redirect Sign In.");
            Intent intent = new Intent(ProfileActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        } else {
            //Get unique user ID
            user = mAuth.getCurrentUser();
            firestoreReference = db.collection("users");
            uid = user.getUid();

            //set recycle view variables
            recyclerView = findViewById(R.id.recyclerView);
            layoutManager = new GridLayoutManager(this, 3);
            recyclerView.setLayoutManager(layoutManager);

            Log.d(TAG,"User found, countinue to profile page.");
        }

        // Set User basic info
        firestoreReference.document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Log.d(TAG,"onSuccess: " + uid);
                        // get user's info
                        user userProfile = documentSnapshot.toObject(user.class);

                        // show username and bio
                        if (userProfile != null) {
                            String userName = userProfile.userName;
                            String bio = userProfile.bio;
                            String displayPicPath = userProfile.displayPicPath;
                            Log.d(TAG,"get user :" + uid);

                            if(getIntent().hasExtra("bitmap")) {
                                //set photo from registration
                                Log.d(TAG,"Photo received from Registration: " + uid);

                                Bitmap bitmap = getIntent().getParcelableExtra("bitmap");
                                profilePhoto.setImageBitmap(bitmap);
                            } else {
                                Log.d(TAG,"Photo received from Could Storage: " + uid);
                                // Set photo to display from storage
                                displayReference = FirebaseStorage.getInstance().getReference().child(displayPicPath);
                                displayReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "The display photo url:" + uri);
                                        Glide.with(ProfileActivity.this)
                                                .load(uri)
                                                .into(profilePhoto);
                                    }
                                });
                            }
                            // set profile name and bio.
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

        //set user photos
        reloadPhoto();
    }

    private void reloadPhoto() {
        Log.d(TAG,"reloadPhoto Start");
        //photoList.clear();
        photoFirebaseRef = db.collection("Photos")
                .document(uid).collection("PostedPhotos");
        Log.d(TAG, "photo referce get:" + photoFirebaseRef.toString());

        photoFirebaseRef.orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            //load firebase data set to local class object
                            PostedPhoto postedPhoto = documentSnapshot.toObject(PostedPhoto.class);
                            photoList.add(postedPhoto);
                            Log.d(TAG,"Photo Query GET" + postedPhoto.getTimestamp());
                        }

                        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(context, photoList);
                        recyclerView.setAdapter(recyclerViewAdapter);
                        recyclerView.setHasFixedSize(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Photo Query Failed");
                    }
                });
    }

    private File creatImageFile() throws IOException {
        //store time stamp and use it as the image name
        String imageName = new java.text.SimpleDateFormat("yyyyMMddHHmmss")
                .format(new java.util.Date());
        //get image from file system
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        //get path of the new created image for ACTION_VIEW intents
        curPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void takePhoto(View view) throws IOException {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file, set capture photo to the file
            File photo = null;
            try {
                photo = creatImageFile();
            } catch (IOException exception) {
                Toast.makeText(ProfileActivity.this, "Failed to save the captured photo ",Toast.LENGTH_LONG).show();
            }

            if(photo != null) {
                // save the file uri
                imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photo);
                //Startup Camera
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO){
            switch (resultCode){
                case RESULT_OK:
                    Log.i(TAG, "onActivityResult: RESULT OK");
                    Bitmap bitmapConvert = null;
                    try {
                        bitmapConvert = MediaStore.Images.Media.getBitmap(
                                this.getContentResolver(), imageUri);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        handleUploadPhoto(bitmapConvert);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case RESULT_CANCELED:
                    Log.i(TAG, "onActivityResult: RESULT CANCELLED");
                    break;
                default:
                    break;

            }
        }
    }

    private void handleUploadPhoto(Bitmap bitmapConvert) throws IOException {
        Bitmap squaredBitmap;

        //crop captured image to square
        // width > height
        if (bitmapConvert.getWidth() >= bitmapConvert.getHeight()) {
            squaredBitmap = Bitmap.createBitmap(
                    bitmapConvert,
                    (bitmapConvert.getWidth() - bitmapConvert.getHeight())/2,
                    0,
                    bitmapConvert.getHeight(),
                    bitmapConvert.getHeight()
            );

        } else {
            //height ? width
            squaredBitmap = Bitmap.createBitmap(
                    bitmapConvert,
                    0,
                    (bitmapConvert.getHeight() - bitmapConvert.getWidth())/2,
                    bitmapConvert.getWidth(),
                    bitmapConvert.getWidth()
            );
        }

        ExifInterface ei = new ExifInterface(curPhotoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        Bitmap rotatedBitmap;
        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(squaredBitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(squaredBitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(squaredBitmap, 270);
                break;
//            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = squaredBitmap;
        }

        Bitmap finalBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 1024, 1024, true);
        //compress final bitmap
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        timeStamp = String.valueOf(System.currentTimeMillis());
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("pics").child(uid + "/" + timeStamp + "jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(ProfileActivity.this, "Photo Posted", Toast.LENGTH_LONG).show();
                        getDownLoadUrl(reference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"Posting photo failed", e.getCause());
                    }
                });
    }

    private Bitmap rotateImage(Bitmap squaredBitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(squaredBitmap, 0, 0, squaredBitmap.getWidth(), squaredBitmap.getHeight(),
                matrix, true);
    }

    private void getDownLoadUrl(StorageReference reference) {
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "uri get:" + uri);
                updatePhotoInfo(uri);
            }
        });
    }

    private void updatePhotoInfo(Uri uri) {
        // Store captured photo information in firebase database.
        Map<String, Object> photo = new HashMap<>();
        photo.put("uid", uid);
        photo.put("storageRef", String.valueOf(uri));
        photo.put("timestamp", timeStamp);

        db.collection("Photos").document(uid).collection("postedPhotos")
                .add(photo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        reloadPhoto();
    }


//    private String getFileExtension(Uri uri) {
//        ContentResolver cR = getContentResolver();
//        MimeTypeMap mime = MimeTypeMap.getSingleton();
//        return mime.getExtensionFromMimeType(cR.getType(uri));
//    }
}

