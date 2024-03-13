package com.example.fitzonetrainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpdateCertificate extends AppCompatActivity {
    ImageView certificate_image;
    CardView update_camera;
    TextView certificate_name;
    EditText certificate_description;
    Button btn_certificate;

    ProgressDialog progressDialog; // Progress dialog for showing upload progress
    private FirebaseFirestore db;
    private StorageReference storageRef; // Firebase Storage reference
    Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_certificate);

        certificate_image = findViewById(R.id.certificate_image);
        update_camera = findViewById(R.id.update_camera);
        certificate_name = findViewById(R.id.certificate_name);
        certificate_description = findViewById(R.id.certificate_description);
        btn_certificate = findViewById(R.id.btn_certificate);

        Intent intent = getIntent();
        String did = intent.getStringExtra("certi_name");
        String ddd = intent.getStringExtra("certi_description");
        String dima = intent.getStringExtra("certi_imageUrl");

        db = FirebaseFirestore.getInstance();
        // Initialize FirebaseStorage reference
        storageRef = FirebaseStorage.getInstance().getReference();

        certificate_name.setText(did);
        certificate_description.setText(ddd);

        if (!TextUtils.isEmpty(dima)) {
            // Decode the Base64 encoded image URL to a Bitmap
            byte[] decodedString = Base64.decode(dima, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            // Set the decoded Bitmap to the ImageView
            certificate_image.setImageBitmap(decodedByte);
        }

        update_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iuser = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iuser, PICK_IMAGE_REQUEST);
            }
        });

        btn_certificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user input values
                String newName = certificate_name.getText().toString();
                String newDescription = certificate_description.getText().toString();

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            certificate_image.setImageURI(selectedImageUri);
        }
    }
    private void uploadImage(final String newName, final String newDescription) {
        // Show progress dialog
        progressDialog = new ProgressDialog(UpdateCertificate.this);
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
                                Toast.makeText(UpdateCertificate.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Image upload failed
                        progressDialog.dismiss();
                        Toast.makeText(UpdateCertificate.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFirestoreDocument(String newName, String newDescription, String newImageUrl) {
        Log.d("UpdateCertificate", "Document Name: " + newName); // Log document name
        // Create a map with the updated fields
        Map<String, Object> certificateData = new HashMap<>();
        certificateData.put("name", newName);
        certificateData.put("description", newDescription);
        certificateData.put("imageUrl", newImageUrl);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        // Check if the certificate already exists
        db.collection("trainers")
                .document(userId)
                .collection("certificates")
                .whereEqualTo("name", newName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                // Update the existing document
                                db.collection("trainers")
                                        .document(userId)
                                        .collection("certificates")
                                        .document(task.getResult().getDocuments().get(0).getId()) // Assuming there's only one document with the same name
                                        .update(certificateData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Document updated successfully
                                                    Toast.makeText(UpdateCertificate.this, "Certificate information updated successfully", Toast.LENGTH_SHORT).show();
                                                    finish(); // Finish the activity after successful update
                                                } else {
                                                    // Error updating document
                                                    Toast.makeText(UpdateCertificate.this, "Failed to update certificate information: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                // Document does not exist, create a new one
                                db.collection("trainers")
                                        .document(userId)
                                        .collection("certificates")
                                        .add(certificateData)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    // Document created successfully
                                                    Toast.makeText(UpdateCertificate.this, "New certificate added successfully", Toast.LENGTH_SHORT).show();
                                                    finish(); // Finish the activity after successful update
                                                } else {
                                                    // Error creating document
                                                    Toast.makeText(UpdateCertificate.this, "Failed to add new certificate: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Error getting documents
                            Toast.makeText(UpdateCertificate.this, "Failed to check certificate existence: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
