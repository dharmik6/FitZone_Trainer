package com.example.fitzonetrainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Login extends AppCompatActivity {
    Button login;
    TextInputEditText user_email, user_password;

    TextView txt_forgot_pass;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        user_email = findViewById(R.id.tr_email);
        user_password = findViewById(R.id.tr_password);
        txt_forgot_pass = findViewById(R.id.txt_forgot_pass);
        login = findViewById(R.id.btn_login);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        login.setOnClickListener(view -> validateCredentials());

        txt_forgot_pass.setOnClickListener(view -> resetPassword());

        TextView create = findViewById(R.id.create_account);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(Login.this, Registration.class);
            }
        });

    }

    private void validateCredentials() {
        String email = user_email.getText().toString().trim();
        String password = user_password.getText().toString().trim();

        // Start validation
        if (TextUtils.isEmpty(email)) {
            user_email.setError("Email is compulsory");
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
            return;
        }
        if (TextUtils.isEmpty(password)) {
            user_password.setError("Password is compulsory");
            return;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(Login.this, "Invalid Email Address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() != 6) {
            user_password.setError("Password must be 6 digits");
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.show();

        // Check if the email exists in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("trainers");

        usersRef.whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // Email exists in the database
                                // Check if the trainer is active
                                boolean isActive = task.getResult().getDocuments().get(0).getBoolean("is_active");
                                if (isActive) {
                                    // Trainer is active, proceed with signing in
                                    mAuth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    pd.dismiss(); // Dismiss the progress dialog
                                                    if (task.isSuccessful()) {
                                                        setLoggedInFlag();

                                                        SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = pref.edit();

                                                        editor.putBoolean("flag", true);
                                                        editor.apply();

                                                        Intent intent = new Intent(Login.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish(); // Close the login activity
                                                        // Sign in success, update UI with the signed-in user's information
                                                        Toast.makeText(Login.this, "Sign in Successful!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        // If sign in fails, display a message to the user.
                                                        Toast.makeText(Login.this, "Please Enter the Correct Email id and Password", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    // Trainer is not active, show a message
                                    pd.dismiss(); // Dismiss the progress dialog
                                    Toast.makeText(Login.this, "Please wait for admin approval", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Email doesn't exist in the database
                                Toast.makeText(Login.this, "Email doesn't exist", Toast.LENGTH_SHORT).show();
                                pd.dismiss(); // Dismiss the progress dialog
                            }
                        } else {
                            // Error fetching data
                            Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            pd.dismiss(); // Dismiss the progress dialog
                        }
                    }
                });
    }

    private void resetPassword() {
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your email address to reset the password");

        // Create an EditText for the user to input the email address
        final EditText emailInput = new EditText(this);
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(emailInput);

        // Set up the buttons
        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = emailInput.getText().toString().trim();

                // Check if the email is valid
                if (!isValidEmail(email)) {
                    Toast.makeText(Login.this, "Invalid Email Address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Send the password reset email
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Login.this, "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        // Show the AlertDialog
        builder.show();
    }


    private boolean isValidEmail(String email) {
        // Email validation pattern
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return !TextUtils.isEmpty(email) && email.matches(emailPattern);
    }

    private void setLoggedInFlag() {
        // Save the logged-in flag to SharedPreferences
        SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("flag", true);
        editor.apply();
    }

    public static void redirectActivity(Activity activity, Class secondActivity) {
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }
}
