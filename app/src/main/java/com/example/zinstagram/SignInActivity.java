package com.example.zinstagram;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.zinstagram.R;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;



public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailPassword";
    private TextView register;
    // [declare_auth]
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        register = (TextView) findViewById((R.id.register));
        register.setOnClickListener(this);

        // Initialize Firebase Auth
        //mAuth = FirebaseAuth.getInstance(R.id.userName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                startActivity(new Intent(this, Registration.class));
                break;
        }

    }

}
