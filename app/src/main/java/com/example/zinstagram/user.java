package com.example.zinstagram;
/*
Create user Object for new users and send the Object to Firebase Storage.
 */
public class user {
    public String userName, email, bio;

    public user() {
        //default constructor
    }

    public user(String userName, String email, String bio ) {
        this.userName = userName;
        this.email = email;
        this.bio = bio;
    }
}
