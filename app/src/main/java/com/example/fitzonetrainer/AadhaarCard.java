package com.example.fitzonetrainer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class AadhaarCard extends AppCompatActivity {

    AppCompatButton btnFront, btnBack, btnNext;
    ImageView adharFront, adharBack;
    String userId;

    private static final int PICK_FRONT_IMAGE_REQUEST = 1;
    private static final int PICK_BACK_IMAGE_REQUEST = 2;

    private FirebaseFirestore db;
    private Uri frontImageUri, backImageUri;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aadhaar_card);

        btnFront = findViewById(R.id.btn_front_side);
        btnBack = findViewById(R.id.btn_back_side);
        btnNext = findViewById(R.id.btn_next);

        adharFront = findViewById(R.id.IMG_adhar_front);
        adharBack = findViewById(R.id.IMG_adhar_back);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        userId = intent.getStringExtra("uid");

        // Initialize the progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading images...");
        progressDialog.setCancelable(false); // Prevent dismiss by tapping outside

        btnFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use ImagePicker to select an image from camera or gallery for front side
                ImagePicker.Companion.with(AadhaarCard.this)
                        .crop()
                        .crop(300 ,200)// Crop shape to square
                        .provider(ImageProvider.BOTH) // Or bothCameraGallery()
                        .createIntentFromDialog(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent it) {
                                startActivityForResult(it, PICK_FRONT_IMAGE_REQUEST);
                                return null;
                            }
                        });
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Use ImagePicker to select an image from camera or gallery for back side
                ImagePicker.Companion.with(AadhaarCard.this)
                        .crop()         // Enable cropping
                        .crop(300 ,200)    // Crop shape to square
                        .provider(ImageProvider.BOTH) // Or bothCameraGallery()
                        .createIntentFromDialog(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent it) {
                                startActivityForResult(it, PICK_BACK_IMAGE_REQUEST);
                                return null;
                            }
                        });
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save data to Firestore when Next button is clicked
                saveDataToFirestore();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_FRONT_IMAGE_REQUEST) {
                // Set the selected image to adharFront ImageView
                frontImageUri = data.getData();
                adharFront.setImageURI(frontImageUri);
            } else if (requestCode == PICK_BACK_IMAGE_REQUEST) {
                // Set the selected image to adharBack ImageView
                backImageUri = data.getData();
                adharBack.setImageURI(backImageUri);
            }
        }
    }

    private void saveDataToFirestore() {
        // Show the progress dialog
        progressDialog.show();

        // Check if both front and back images are selected
        if (frontImageUri != null && backImageUri != null) {
            // Create a reference to the Firebase Storage location where you want to store the images
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("trainer_images").child(userId);

            // Upload the front image to Firebase Storage
            storageRef.child("aadharFront").putFile(frontImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the download URL of the uploaded image
                        storageRef.child("aadharFront").getDownloadUrl().addOnSuccessListener(uri -> {
                            String frontImageUrl = uri.toString();

                            // Upload the back image to Firebase Storage
                            storageRef.child("aadharBack").putFile(backImageUri)
                                    .addOnSuccessListener(taskSnapshot1 -> {
                                        // Get the download URL of the uploaded image
                                        storageRef.child("aadharBack").getDownloadUrl().addOnSuccessListener(uri1 -> {
                                            String backImageUrl = uri1.toString();

                                            // Create a map to store user data
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("aadharFront", frontImageUrl);
                                            userData.put("aadharBack", backImageUrl);

                                            // Add the user data to Firestore using the UID
                                            db.collection("trainers")
                                                    .document(userId)
                                                    .update(userData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Data saved successfully
                                                        Toast.makeText(AadhaarCard.this, "Submit successful", Toast.LENGTH_SHORT).show();
                                                        // Dismiss the progress dialog
                                                        progressDialog.dismiss();
                                                        // Redirect to the next activity
                                                        redirectActivity(AadhaarCard.this, Login.class, userId);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Failed to save data
                                                        Toast.makeText(AadhaarCard.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                                        // Dismiss the progress dialog
                                                        progressDialog.dismiss();
                                                    });
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failures
                                        Toast.makeText(AadhaarCard.this, "Failed to upload back image.", Toast.LENGTH_SHORT).show();
                                        // Dismiss the progress dialog
                                        progressDialog.dismiss();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle failures
                        Toast.makeText(AadhaarCard.this, "Failed to upload front image.", Toast.LENGTH_SHORT).show();
                        // Dismiss the progress dialog
                        progressDialog.dismiss();
                    });
        } else {
            // Images are not selected
            Toast.makeText(this, "Please select both front and back images.", Toast.LENGTH_SHORT).show();
            // Dismiss the progress dialog
            progressDialog.dismiss();
        }
    }

    public static void redirectActivity(Activity activity, Class destination, String userId) {
        Intent intent = new Intent(activity, destination);
        intent.putExtra("uid", userId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}
