package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {
    TextInputEditText trainer_name, trainer_email, trainer_pass;
    AppCompatButton btn_registration;
    FirebaseFirestore db;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Find views
        trainer_name = findViewById(R.id.trainer_name);
        trainer_email = findViewById(R.id.member_email);
        trainer_pass = findViewById(R.id.member_pass);
        btn_registration = findViewById(R.id.btn_registration);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false); // Prevent dismissing on outside touch

        btn_registration.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String name = trainer_name.getText().toString().trim();
        String email = trainer_email.getText().toString().trim();
        String password = trainer_pass.getText().toString().trim();
        boolean isActive = false;

        if (TextUtils.isEmpty(name)) {
            trainer_name.setError("Please enter your name");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            trainer_email.setError("Please enter your email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            trainer_pass.setError("Please enter your password");
            return;
        }

        // Show progress dialog
        progressDialog.show();

        // Register the user with email and password using Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Dismiss progress dialog
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String userId = user.getUid();

                        // Create a user object
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("password", password);
                        userData.put("is_active", isActive);

                        // Add the user to Firestore with the generated UID
                        db.collection("trainers")
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(Registration.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    // Redirect to the profile activity
                                    redirectActivity(Registration.this, ProfileInformation.class, userId);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Registration.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                    // Handle failure
                                });
                    } else {
                        Toast.makeText(Registration.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void redirectActivity(Activity activity, Class destination, String name) {
        Intent intent = new Intent(activity, destination);
        intent.putExtra("uid", name);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}