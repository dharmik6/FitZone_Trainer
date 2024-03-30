package com.example.fitzonetrainer;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateWorkoutPlan extends AppCompatActivity {
    Button plan_butt;
    EditText plan_name;
    Spinner plan_spinner;
    ImageView plan_image;
    CardView plan_camera;
    ProgressDialog progressDialog; // Progress dialog for showing upload progress
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout_plan);
        plan_butt = findViewById(R.id.plan_butt);
        plan_name = findViewById(R.id.plan_name);
        plan_spinner = findViewById(R.id.plan_spinner);
        plan_image = findViewById(R.id.plan_image);
        plan_camera = findViewById(R.id.plan_camera);

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Initialize FirebaseFirestore and StorageReference
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        // goal spinner
        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(this,
                R.array.activity_array, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        plan_spinner.setAdapter(goalAdapter);
        plan_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iuser = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iuser, PICK_IMAGE_REQUEST);
            }
        });

        plan_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the workout plan name and goal from EditText and Spinner respectively
                String planName = plan_name.getText().toString().trim();
                String planGoal = plan_spinner.getSelectedItem().toString();

                // Check if the plan name is not empty
                if (!planName.isEmpty()) {
                    // Show the progress dialog
                    progressDialog = new ProgressDialog(CreateWorkoutPlan.this);
                    progressDialog.setMessage("Saving workout plan...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    // Save the data to Firestore
                    saveDataToFirestore(planName, planGoal);
                } else {
                    // Handle empty plan name
                    // You can show a toast message or provide some feedback to the user
                    Toast.makeText(CreateWorkoutPlan.this, "Please enter a plan name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to handle image selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            plan_image.setImageURI(selectedImageUri);
        }
    }

    private void saveDataToFirestore(String planName, String planGoal) {
        // Check if an image is selected
        if (selectedImageUri != null) {
            // Show progress dialog
            progressDialog = new ProgressDialog(CreateWorkoutPlan.this);
            progressDialog.setMessage("Uploading image...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Get reference to Firebase Storage and set the path for the image
            StorageReference imageRef = storageRef.child("workout_plan_images/" + UUID.randomUUID().toString());

            // Upload the image to Firebase Storage
            progressDialog = new ProgressDialog(CreateWorkoutPlan.this);
            progressDialog.setMessage("Uploading image...");
            progressDialog.setCancelable(false);
            progressDialog.show(); // Show progress dialog before starting the upload

            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded successfully, get the download URL
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Image download URL obtained, save data to Firestore
                                    String imageUrl = uri.toString();
                                    saveDataToFirestore(planName, planGoal, imageUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error uploading image
                            progressDialog.dismiss();
                            Toast.makeText(CreateWorkoutPlan.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // No image selected, save data without the image URL
            saveDataToFirestore(planName, planGoal, null);
        }
    }


    // Inside saveDataToFirestore method
    private void saveDataToFirestore(String planName, String planGoal, String imageUrl) {
        // Create a new document in the "workout_plans" collection with the provided data
        Map<String, Object> workoutPlan = new HashMap<>();
        workoutPlan.put("name", planName);
        workoutPlan.put("goal", planGoal);
        workoutPlan.put("created", "Trainer");

        // Get current date in dd/mm/yyyy format
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());

        workoutPlan.put("createdDate", currentDate);

        if (imageUrl != null) {
            workoutPlan.put("image", imageUrl); // Add the image URL to the document if it exists
        }

        // Add the workout plan data to Firestore
        db.collection("workout_plans").document(planName)
                .set(workoutPlan)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        // Document added successfully
                        Toast.makeText(CreateWorkoutPlan.this, "Workout plan added successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Finish the activity after successful addition
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        // Error adding document
                        Toast.makeText(CreateWorkoutPlan.this, "Failed to add workout plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
