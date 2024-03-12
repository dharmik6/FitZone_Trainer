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
        String did = intent.getStringExtra("name");
        String ddd = intent.getStringExtra("description");
        String dima = intent.getStringExtra("imageUrl");

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
                // Get the updated description
                String updatedDescription = certificate_description.getText().toString().trim();

                // Check if the description is not empty
                if (TextUtils.isEmpty(updatedDescription)) {
                    Toast.makeText(UpdateCertificate.this, "Please enter a description", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Display progress dialog
                progressDialog = new ProgressDialog(UpdateCertificate.this);
                progressDialog.setMessage("Updating Certificate...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Update certificate data in Firestore
                db.collection("trainers")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection("certificates")
                        .whereEqualTo("name", did)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                String certificateDocumentId = documentSnapshot.getId();
                                Log.d("UpdateCertificate", "Certificate Document ID: " + certificateDocumentId); // Log the document ID

                                db.collection("trainers")
                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .collection("certificates")
                                        .document(certificateDocumentId)
                                        .update("description", updatedDescription)
                                        .addOnSuccessListener(aVoid -> {
                                            // Update successful
                                            Toast.makeText(UpdateCertificate.this, "Certificate updated successfully", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            finish(); // Close activity after updating
                                        })
                                        .addOnFailureListener(e -> {
                                            // Update failed
                                            Toast.makeText(UpdateCertificate.this, "Failed to update certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            Log.e("UpdateCertificate", "Failed to update certificate", e);
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Error occurred while fetching the certificate
                            Toast.makeText(UpdateCertificate.this, "Failed to fetch certificate information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            Log.e("UpdateCertificate", "Failed to fetch certificate information", e);
                        });
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

}
