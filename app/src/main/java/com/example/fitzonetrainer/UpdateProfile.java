package com.example.fitzonetrainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity {
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    AppCompatEditText up_name  ,up_phone , up_address ,up_experience , up_bio ;
    Spinner up_Specialization ,up_gender , up_age ;
    Button btn_update ;
    ImageView up_image ;
    ProgressDialog progressDialog;
    private FirebaseFirestore db;
    RelativeLayout camera ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);


        up_name = findViewById(R.id.up_name);
        up_phone = findViewById(R.id.up_phone);
        up_address = findViewById(R.id.up_address);
        up_experience = findViewById(R.id.up_experience);
        up_bio = findViewById(R.id.up_bio);
        up_Specialization = findViewById(R.id.up_Specialization);
        up_gender = findViewById(R.id.up_gender);
        up_age = findViewById(R.id.up_age);
        btn_update = findViewById(R.id.btn_update);
        up_image = findViewById(R.id.up_image);
        camera = findViewById(R.id.camera);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String trainerId = currentUser.getUid();


        // age spinner
        ArrayAdapter<Integer> ageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Populate the age spinner with ages from 18 to 100
        for (int i = 10; i <= 100; i++) {
            ageAdapter.add(i);
        }
        up_age.setAdapter(ageAdapter);

        // gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        up_gender.setAdapter(genderAdapter);

        // specialization spinner
        ArrayAdapter<CharSequence> specializationAdapter = ArrayAdapter.createFromResource(this,
                R.array.specialization_array, android.R.layout.simple_spinner_item);
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        up_Specialization.setAdapter(specializationAdapter);


        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = up_name.getText().toString().trim();
                String phone = up_phone.getText().toString().trim();
                String address = up_address.getText().toString().trim();
                String experience = up_experience.getText().toString().trim();
                String bio = up_bio.getText().toString().trim();
                String specialization = up_Specialization.getSelectedItem().toString().trim();
                String gender = up_gender.getSelectedItem().toString().trim();
                String age = up_age.getSelectedItem().toString().trim();

                if (!name.isEmpty() && !phone.isEmpty() && !address.isEmpty() && !experience.isEmpty() && !bio.isEmpty() && !specialization.isEmpty() && !gender.isEmpty() && !age.isEmpty()) {
                    // Show progress dialog
                    progressDialog = new ProgressDialog(UpdateProfile.this);
                    progressDialog.setMessage("Updating trainer data...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    // Create a map to store the data
                    Map<String, Object> trainerData = new HashMap<>();
                    trainerData.put("name", name);
                    trainerData.put("number", phone);
                    trainerData.put("address", address);
                    trainerData.put("experience", experience);
                    trainerData.put("bio", bio);
                    trainerData.put("specialization", specialization);
                    trainerData.put("gender", gender);
                    trainerData.put("age", age);

                    // Upload image to Firebase Storage if an image is selected
                    if (selectedImageUri != null) {
                        uploadImageAndSaveData(trainerData, trainerId);
                    } else {
                        // Add the data to Firestore without an image
                        saveDataToFirestore(trainerData, trainerId, null);
                    }
                } else {
                    Toast.makeText(UpdateProfile.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }

            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iuser = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iuser, PICK_IMAGE_REQUEST);
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        UpdateProfile.super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            up_image.setImageURI(selectedImageUri);
        }
    }
    private void uploadImageAndSaveData(final Map<String, Object> trainerData, final String trainerId) {
        // Get reference to Firebase Storage and set the path for the image
        StorageReference imageRef = storageRef.child("trainer_images/" + trainerId);

        // Upload the image to Firebase Storage
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
                                saveDataToFirestore(trainerData, trainerId,imageUrl);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error uploading image
                        progressDialog.dismiss();
                        Toast.makeText(UpdateProfile.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveDataToFirestore(Map<String, Object> trainerData, String trainerId, String imageUrl) {
        if (imageUrl != null) {
            trainerData.put("image", imageUrl); // Add the image URL to the document if it exists
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("trainers").document(trainerId)
                .update(trainerData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(UpdateProfile.this, "Trainer data updated successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Finish the activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(UpdateProfile.this, "Failed to update trainer data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}