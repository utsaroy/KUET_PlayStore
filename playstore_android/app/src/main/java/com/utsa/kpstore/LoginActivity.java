package com.utsa.kpstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText emailField, passwordField;
    TextView signupText, errorText, adminLoginText;
    Button loginButton;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            goToHome();
            return;
        }

        emailField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        errorText = findViewById(R.id.errorText);
        loginButton = findViewById(R.id.loginButton);
        signupText = findViewById(R.id.signupText);
        adminLoginText = findViewById(R.id.adminLoginText);

        loginButton.setOnClickListener(v -> userLogin());

        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });

        adminLoginText.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLogin.class);
            startActivity(intent);
        });
    }

    private void userLogin() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString();


        if (email.isEmpty() || password.isEmpty()) {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText("Please fill the fields");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText("Please enter a valid email");
            return;
        }

        errorText.setVisibility(View.GONE);
        loginButton.setEnabled(false);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        goToHome();
                    } else {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText("Failed To Login");
                    }
                });
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}