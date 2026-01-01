package com.utsa.kpstore;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.utsa.kpstore.models.User;

public class SignupActivity extends AppCompatActivity {
    EditText nameField, emailField, passwordField;
    TextView errorText, loginText;
    Button signupButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        nameField = findViewById(R.id.nameField);
        emailField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);

        errorText = findViewById(R.id.errorText);
        loginText = findViewById(R.id.signupText);
        signupButton = findViewById(R.id.loginButton);

        signupButton.setOnClickListener(v -> {
            errorText.setVisibility(INVISIBLE);
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString();
            if(name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                errorText.setVisibility(VISIBLE);
                errorText.setText("Please fill all the field");
                return;
            }

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            signupButton.setEnabled(false);
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task->{
                if(task.isSuccessful()){
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    if(firebaseUser != null){
                        String uid = firebaseUser.getUid();
                        User user = new User(name, email);
                        reference.child(uid).setValue(user).addOnCompleteListener(dbTask -> {
                            if(dbTask.isSuccessful()) {
                                goToHome();
                            }
                        });
                    } else {
                        goToHome();
                    }
                } else {
                    errorText.setVisibility(VISIBLE);
                    signupButton.setEnabled(true);
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        errorText.setText("User with email already exists");
                    } else{
                        errorText.setText("Something went wrong");
                    }
                }
            });
        });
    }

    private void goToHome() {
        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}