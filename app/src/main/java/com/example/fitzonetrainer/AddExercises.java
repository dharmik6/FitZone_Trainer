package com.example.fitzonetrainer;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AddExercises extends AppCompatActivity {
    Button add_butt;
    EditText exe_name,exe_equipment,exe_description;
    ImageView exe_image;
    CardView exe_camera;
    Spinner exe_body;
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
        setContentView(R.layout.activity_add_exercises);

        exe_name = findViewById(R.id.exe_name);
        exe_equipment = findViewById(R.id.exe_equipment);
        exe_description = findViewById(R.id.exe_description);
        exe_image = findViewById(R.id.exe_image);
        exe_camera = findViewById(R.id.exe_camera);
        exe_body = findViewById(R.id.exe_body);
        add_butt = findViewById(R.id.add_butt);

        // goal spinner
        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(this,
                R.array.exe_array, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exe_body.setAdapter(goalAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        // Initialize Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference();
        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        exe_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iuser = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iuser, PICK_IMAGE_REQUEST);
            }
        });
        add_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user input values
                final String name = exe_name.getText().toString().trim();
                final String body = exe_body.getSelectedItem().toString().trim();
                final String equipment = exe_equipment.getText().toString().trim();
                final String description = exe_description.getText().toString().trim();

                // Validate input fields
                if (name.isEmpty()) {
                    exe_name.setError("Please enter exercise name");
                    exe_name.requestFocus();
                    return;
                }

                if (body.isEmpty()) {
                    // You can set a default selection for the spinner or handle it differently
                    // For example, you can show a toast message or set a default value programmatically
                    Toast.makeText(AddExercises.this, "Please select exercise body", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (equipment.isEmpty()) {
                    exe_equipment.setError("Please enter exercise equipment");
                    exe_equipment.requestFocus();
                    return;
                }

                if (description.isEmpty()) {
                    exe_description.setError("Please enter exercise description");
                    exe_description.requestFocus();
                    return;
                }

                if (selectedImageUri == null) {
                    Toast.makeText(AddExercises.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If all fields are filled, upload image to Firebase Storage and save exercise data to Firestore
                uploadImageToStorage(name, description, equipment, body);
            }
        });


        ImageView backPress = findViewById(R.id.back);
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
            exe_image.setImageURI(selectedImageUri);
        }
    }

    // Method to upload image to Firebase Storage and save exercise data to Firestore
    private void uploadImageToStorage(final String name, final String description, final String equipment, final String body) {
        progressDialog.show();
        final StorageReference imageRef = storageRef.child("exercise_images/" + System.currentTimeMillis() + ".jpg");
        UploadTask uploadTask = imageRef.putFile(selectedImageUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    // Image uploaded successfully, now save exercise data to Firestore
                    saveExerciseToFirestore(name, description, equipment, body, downloadUri.toString());
                } else {
                    // Handle failures
                    progressDialog.dismiss();
                    Toast.makeText(AddExercises.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to save exercise data to Firestore
    private void saveExerciseToFirestore(String name, String description, String equipment, String body, String imageUrl) {
        Map<String, Object> exerciseData = new HashMap<>();
        exerciseData.put("name", name);
        exerciseData.put("description", description);
        exerciseData.put("equipment", equipment);
        exerciseData.put("body", body);
        exerciseData.put("imageUrl", imageUrl);

        db.collection("exercises")
                .document(name) // Generates a unique ID for the document
                .set(exerciseData, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Exercise data saved successfully
                            Toast.makeText(AddExercises.this, "Exercise added successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Finish the activity
                        } else {
                            // Handle failures
                            Toast.makeText(AddExercises.this, "Failed to add exercise", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}