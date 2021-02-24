package com.example.zinstagram;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

/*
Create user Object for new users and send the Object to Firebase Storage.
 */
public class user extends Application {
    public String userName, email, bio, displayPicPath, photoUrl, uid;
    private String profileRef = "";
    private Bitmap bitmap;
    private String currentPhoto = "";
    private Uri photoUri;

    public user(){}

    public user(String userName, String email, String bio, String uid) {
        this.userName = userName;
        this.email = email;
        this.bio = bio;
        this.displayPicPath = uid + "/displayPic.jpg";
        this.uid = uid;
    }

    public user(String userName, String email, String bio, String uid, String profileRef) {
        this.userName = userName;
        this.email = email;
        this.bio = bio;
        this.displayPicPath = uid + "/displayPic.jpg";
        this.uid = uid;
        this.profileRef = profileRef;
    }

    public String getUid(String uid) {
        return uid;
    }

    public user(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setUserProfileImage(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUserProfileImage() {
        return photoUrl;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getCurrentPhoto() {
        return currentPhoto;
    }

    public void setCurrentPhoto(String currentPhoto) {
        this.currentPhoto = currentPhoto;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }
}

