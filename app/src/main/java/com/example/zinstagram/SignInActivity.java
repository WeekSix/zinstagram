package com.example.zinstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.PriorityGoalRow;

import android.os.Bundle;
import com.example.zinstagram.R;
import android.content.Intent;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    // declare variables
    private TextView register;
    private EditText editTextEmail, editTextPassword;
    private Button signIn;
    private ProgressBar progressBar;

    // declare firebase auth
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //Direction to registration page
        register = (TextView) findViewById((R.id.register));
        register.setOnClickListener(this);

        //Initialize elements
        signIn = (Button) findViewById(R.id.signIn);
        signIn.setOnClickListener(this);

        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPassword = (EditText) findViewById(R.id.password);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }else{
            //Sign In Pate
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                startActivity(new Intent(this, Registration.class));
                break;
            case R.id.signIn:
                userLogin();
                break;
        }

    }

    private void userLogin() {
        //Get users credentials and convert to string
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //Validate
        if(email.isEmpty()) {
            editTextEmail.setError("Email address is required");
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

        progressBar.setVisibility(View.VISIBLE);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(SignInActivity.this, "User does not exist", Toast.LENGTH_LONG).show();
        }

        //Pass user email and password to Firebase and sign in
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                //get current user
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user.isEmailVerified()) {
                //redirect to profile page
                startActivity(new Intent(SignInActivity.this, ProfileActivity.class));
                } else {
                    // sent verification if email is verified
                    user.sendEmailVerification();
                    Toast.makeText(SignInActivity.this, "Please verify your email", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(SignInActivity.this,"Failed to login! Please check your email/password", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }

        });
    }

}
