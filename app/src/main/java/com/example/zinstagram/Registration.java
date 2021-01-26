package com.example.zinstagram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class Registration extends AppCompatActivity implements View.OnClickListener {
    private TextView signIn, registration;
    private EditText editTextUserName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextShortBio;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        signIn = (TextView) findViewById(R.id.signIn);
        signIn.setOnClickListener(this);

        registration = (Button) findViewById(R.id.register);
        registration.setOnClickListener(this);

        editTextUserName = (EditText) findViewById(R.id.userName);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
        editTextShortBio = (EditText) findViewById(R.id.shortBio);

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
        String shortBio = editTextShortBio.getText().toString().trim();

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
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }

        if(password.length() < 6) {
            editTextEmail.setError("Please enter a password length > 6.");
            editTextEmail.requestFocus();
            return;
        }

        if(confirmPassword.isEmpty()) {
            editTextConfirmPassword.setError("Please confirm your password");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if(password != confirmPassword) {

        }

        if(shortBio.isEmpty()) {
            editTextShortBio.setError("A quick bio helps people to know you!");
            editTextShortBio.requestFocus();
            return;
        }




    }
}