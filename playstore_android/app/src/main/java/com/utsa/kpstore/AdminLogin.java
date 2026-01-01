package com.utsa.kpstore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminLogin extends AppCompatActivity {

    private EditText usernameField, passwordField;
    private TextView errorText, userLoginText;
    private Button loginButton;
    private DatabaseReference adminDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Database reference
        adminDatabaseReference = FirebaseDatabase.getInstance().getReference("admin");

        // Check if admin is already logged in
        if (isAdminLoggedIn()) {
            navigateToAdminDashboard();
            return;
        }

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        errorText = findViewById(R.id.errorText);
        loginButton = findViewById(R.id.loginButton);
        userLoginText = findViewById(R.id.signupText);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performAdminLogin());

        userLoginText.setOnClickListener(v -> {
            Intent intent = new Intent(AdminLogin.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void performAdminLogin() {
        String username = usernameField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Validate username format (no special characters for database path)
        if (!isValidUsername(username)) {
            showError("Invalid username format");
            return;
        }

        hideError();
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        // Query the admin database
        adminDatabaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loginButton.setEnabled(true);
                loginButton.setText("Login");

                if (snapshot.exists()) {
                    // Admin username exists, check password
                    String storedPassword = snapshot.child("password").getValue(String.class);
                    
                    if (storedPassword != null && storedPassword.equals(password)) {
                        // Login successful
                        saveAdminSession(username);
                        Toast.makeText(AdminLogin.this, "Welcome Admin: " + username, Toast.LENGTH_SHORT).show();
                        navigateToAdminDashboard();
                    } else {
                        // Password incorrect
                        showError("Incorrect password");
                    }
                } else {
                    // Admin username not found
                    showError("Admin not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                showError("Login failed: " + error.getMessage());
            }
        });
    }

    private boolean isValidUsername(String username) {
        // Check if username contains only alphanumeric characters and underscores
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    private void saveAdminSession(String username) {
        // Save admin login session in SharedPreferences
        SharedPreferences preferences = getSharedPreferences("AdminSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isAdminLoggedIn", true);
        editor.putString("adminUsername", username);
        editor.putLong("loginTime", System.currentTimeMillis());
        editor.apply();
    }

    private boolean isAdminLoggedIn() {
        SharedPreferences preferences = getSharedPreferences("AdminSession", MODE_PRIVATE);
        return preferences.getBoolean("isAdminLoggedIn", false);
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(AdminLogin.this, AdminHome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }

    public static void logoutAdmin(AdminLogin activity) {
        // Logout admin by clearing SharedPreferences
        SharedPreferences preferences = activity.getSharedPreferences("AdminSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}