package com.example.zinstagram;

import android.app.Application;

/*
Create user Object for new users and send the Object to Firebase Storage.
 */
public class user {
    public String userName, email, bio, displayPicPath, photoUrl, uid;

    public user() {
        //default constructor
    }

    public user(String userName, String email, String bio, String uid) {
        this.userName = userName;
        this.email = email;
        this.bio = bio;
        this.displayPicPath = uid + "/displayPic.jpg";
        this.uid = uid;
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

}
