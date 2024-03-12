package com.example.fitzonetrainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpdateDiet extends AppCompatActivity {

    ImageView update_image;
    CardView update_camera;
    TextView update_name;
    EditText update_description;
    Button update_but;

    ProgressDialog progressDialog; // Progress dialog for showing upload progress
    private FirebaseFirestore db;
    private StorageReference storageRef; // Firebase Storage reference
    Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_diet);

        update_image = findViewById(R.id.update_image);
        update_camera = findViewById(R.id.update_camera);
        update_name = findViewById(R.id.update_name);
        update_description = findViewById(R.id.update_description);
        update_but = findViewById(R.id.update_but);

        Intent intent = getIntent();
        String did = intent.getStringExtra("name");
        String ddd = intent.getStringExtra("description");
        String dima = intent.getStringExtra("imageUrl");

        // Initialize FirebaseFirestore
        db = FirebaseFirestore.getInstance();
        // Initialize FirebaseStorage reference
        storageRef = FirebaseStorage.getInstance().getReference();

        // Set the received data to the EditText fields
        update_name.setText(did);
        update_description.setText(ddd);

        // Check if the image URL is not empty
        if (!TextUtils.isEmpty(dima)) {
            // Decode the Base64 encoded image URL to a Bitmap
            byte[] decodedString = Base64.decode(dima, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            // Set the decoded Bitmap to the ImageView
            update_image.setImageBitmap(decodedByte);
        }



        update_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iuser = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iuser, PICK_IMAGE_REQUEST);
            }
        });

        update_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user input values
                String newName = update_name.getText().toString();
                String newDescription = update_description.getText().toString();

                // Check if name, description, and image URI are not empty
                if (!newName.isEmpty() && !newDescription.isEmpty() && selectedImageUri != null) {
                    // Upload image to Firebase Storage
                    uploadImage(newName, newDescription);
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
            update_image.setImageURI(selectedImageUri);
        }
    }
    private void uploadImage(final String newName, final String newDescription) {
        // Show progress dialog
        progressDialog = new ProgressDialog(UpdateDiet.this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Define a reference to the location where you want to store the new image
        String imageName = UUID.randomUUID().toString(); // Generate a unique name for the image
        StorageReference imageRef = storageRef.child("images/" + imageName); // Store images in a "images" folder

        // Upload the image to Firebase Storage
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image uploaded successfully
                        // Get the download URL for the new image
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Dismiss progress dialog
                                progressDialog.dismiss();
                                // Get the new image URL
                                String newImageUrl = uri.toString();
                                // Update Firestore document with the new image URL
                                updateFirestoreDocument(newName, newDescription, newImageUrl);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error getting download URL
                                progressDialog.dismiss();
                                Toast.makeText(UpdateDiet.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Image upload failed
                        progressDialog.dismiss();
                        Toast.makeText(UpdateDiet.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateFirestoreDocument(String newName, String newDescription, String newImageUrl) {
        // Create a map with the updated fields
        Map<String, Object> dietData = new HashMap<>();
        dietData.put("name", newName);
        dietData.put("description", newDescription);
        dietData.put("imageUrl", newImageUrl);

        // Update the document in Firestore
        db.collection("diets")
                .document(newName)  // Replace "your_document_id" with the actual document ID
                .update(dietData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Document updated successfully
                            Toast.makeText(UpdateDiet.this, "Diet information updated successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Finish the activity after successful update
                        } else {
                            // Error updating document
                            Toast.makeText(UpdateDiet.this, "Failed to update diet information: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
