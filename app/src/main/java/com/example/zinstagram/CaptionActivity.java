package com.example.zinstagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;;

public class CaptionActivity extends AppCompatActivity {

    private static final String TAG = "Caption Activity";
    private ImageView imageView;
    private Button btnCancel;
    private Button btnSubmit;
    private EditText caption;
    private Switch aSwitch;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private String timeStamp = "";
    private String captionStr;
    private String curPhotoPath;
    private Boolean enableHashtag = false;
    private String hashtags = "";
    Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caption);

        imageView = findViewById(R.id.captionImage);
        btnCancel = findViewById(R.id.captionCancel);
        btnSubmit = findViewById(R.id.captionSubmit);
        caption = findViewById(R.id.caption);
        aSwitch = findViewById(R.id.enableButton);

        imageView.setImageBitmap(getApp().getBitmap());

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Toast.makeText(CaptionActivity.this, "Generating hashtags",
                            Toast.LENGTH_SHORT).show();
                    enableHashtag = true;
                    autoHashtags(enableHashtag);
                } else {
                    // The toggle is disabled
                    Toast.makeText(CaptionActivity.this, "Cancelling hashtags",
                            Toast.LENGTH_SHORT).show();
                    enableHashtag = false;
                    autoHashtags(enableHashtag);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CaptionActivity.this, "Caption Cancelled",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CaptionActivity.this, ProfileActivity.class));
                finish();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captionStr = caption.getText().toString();
                if (TextUtils.isEmpty(captionStr)) {
                    Toast.makeText(CaptionActivity.this, "Please add your text", Toast.LENGTH_SHORT).show();
                    return;
                } else if (captionStr.length() > 200) {
                    Toast.makeText(CaptionActivity.this, "Your text is too long (more than 200 characters).", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    try {
                        uploadImage(getApp().getBitmap());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(CaptionActivity.this, "Submitted", Toast.LENGTH_SHORT).show();

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            startActivity(new Intent(CaptionActivity.this, ProfileActivity.class));
                            finish();
                        }
                    }, 3000);
                }
            }
        });
    }

    private void uploadImage(Bitmap bitmapConvert) throws IOException {
        // after user took a picture, cut image to square and downscale the image to 1024*1024,
        Bitmap squaredBitmap = null;

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

        curPhotoPath = getApp().getCurrentPhoto();
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
        userId = FirebaseAuth.getInstance().getUid();
        //set cloud storage path
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("pics").child(userId + "/" + timeStamp + ".jpg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG,"Photo Posed" + reference.toString());
                        Toast.makeText(CaptionActivity.this, "Photo Posted", Toast.LENGTH_SHORT).show();
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
        Log.d(TAG, "Start to update photo info");
        captionStr = caption.getText().toString();
        Map<String, Object> photo = new HashMap<>();
        photo.put("uid", userId);
        photo.put("storageRef", String.valueOf(uri));
        photo.put("timestamp", timeStamp);
        photo.put("caption", captionStr);

        db = FirebaseFirestore.getInstance();
        db.collection("Photos").document(userId).collection("postedPhotos")
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
    }

    private void autoHashtags(Boolean enableHashtag) {
        hashtags = "";
        if (enableHashtag == true) {
            InputImage image = InputImage.fromBitmap(getApp().getBitmap(),0);
            //set the minimum confidence
            ImageLabelerOptions options;
            options = new ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build();
            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
            labeler.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                        @Override
                        public void onSuccess(List<ImageLabel> firebaseVisionImageLabels) {
                            for (ImageLabel label : firebaseVisionImageLabels) {
                                String text = label.getText();
                                int Index = label.getIndex();
                                float confidence = label.getConfidence();
                                Log.e(TAG, "Hashtags: "+ Index +"  ** text:"+text+" ** confidence"+confidence);
                                hashtags = hashtags + " #" + text;
                            }
                            captionStr = caption.getText().toString();
                            caption.setText(captionStr + hashtags);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CaptionActivity.this, "Failed to generate hashtags",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            captionStr = caption.getText().toString();
            if ( captionStr.contains("#")) {
                String[] origin = captionStr.split(" #");
                caption.setText(origin[0]);
            }
        }
    }



    public user getApp(){
        return ((user) getApplicationContext());
    }
}