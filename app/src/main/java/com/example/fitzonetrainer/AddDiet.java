package com.example.fitzonetrainer;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddDiet extends AppCompatActivity {

    EditText diet_name, diet_description;
    CardView diet_camera;
    ImageView diet_image;
    Button diet_add;
    // Declare Firestore instance

    ProgressDialog progressDialog; // Progress dialog for showing upload progress
    private FirebaseFirestore db;
    // Uri to store the selected image URI
    // Declare Firebase Storage reference
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diet);

        diet_name = findViewById(R.id.diet_name);
        diet_description = findViewById(R.id.diet_description);
        diet_camera = findViewById(R.id.diet_camera);
        diet_image = findViewById(R.id.diet_image);
        diet_add = findViewById(R.id.diet_add);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Initialize Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference();
        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        diet_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iuser = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iuser, PICK_IMAGE_REQUEST);
            }
        });

        diet_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user input values
                String name = diet_name.getText().toString();
                String description = diet_description.getText().toString();

                // Check if name, description, and image URI are not empty
                if (!name.isEmpty() && !description.isEmpty() && selectedImageUri != null) {
                    // Upload image to Firebase Storage
                    uploadImageToStorage(name, description);
                } else {
                    // Handle empty fields or no selected image
                    // You can show a toast message or provide some feedback to the user
                }
            }
        });


        ImageView backPress = findViewById(R.id.back_press);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    // Method to handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            diet_image.setImageURI(selectedImageUri);
        }
    }
    // Method to upload image to Firebase Storage
    // Method to upload image to Firebase Storage
    private void uploadImageToStorage(final String name, final String description) {
        // Show ProgressDialog
        progressDialog.show();

        // Create a reference to "diet_images" folder and a unique filename
        StorageReference imageRef = storageRef.child("diet_images/" + UUID.randomUUID().toString());

        // Upload file to Firebase Storage
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Save the download URL to Firestore
                                String image = uri.toString();

                                // Create a new diet object with name, description, and image URL
                                DietList dietList = new DietList(name, description, image);

                                // Add the diet to Firestore
                                addDietToFirestore(name,dietList);

                                // Hide ProgressDialog
                                progressDialog.dismiss();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Hide ProgressDialog
                        progressDialog.dismiss();

                        // Handle failure
                        // You can show an error message or handle the failure as per your requirement
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        // Calculate progress percentage
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        // Update progress dialog
                        progressDialog.setProgress((int) progress);
                    }
                });
    }

    // Method to add diet to Firestore
    // Method to add diet to Firestore
    private void addDietToFirestore(String name, DietList diet) {
        db.collection("diets")
                .document(name)  // Set the document ID to the diet name
                .set(diet)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle success
                        // You can show a success message or navigate back to the previous screen
                        onBackPressed(); // Example: Navigate back to the previous screen
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        // You can show an error message or handle the failure as per your requirement
                    }
                });
    }
}
