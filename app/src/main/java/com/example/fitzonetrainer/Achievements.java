package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Achievements extends AppCompatActivity {
    TextView achi_name, achi_description;
    ImageView achi_image;
    Button achi_del_butt, achi_upe_butt;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        achi_name = findViewById(R.id.achievements_name);
        achi_description = findViewById(R.id.achievements_description);
        achi_image = findViewById(R.id.achievements_image);
        achi_del_butt = findViewById(R.id.del);
        achi_upe_butt = findViewById(R.id.up);

        progressDialog = new ProgressDialog(Achievements.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show(); // Show loading dialog

        // Load achievements data
        loadAchievementsData();

        achi_upe_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle update button click
                updateCertificate();
            }
        });

        achi_del_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle delete button click
                deleteCertificate();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Reload achievements data on resume
        loadAchievementsData();
    }

    private void loadAchievementsData() {
        Intent intent = getIntent();
        String dietid = intent.getStringExtra("name");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        // Query Firestore for data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("trainers")
                .document(userId)
                .collection("certificates")
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String name = documentSnapshot.getString("name");
                        String description = documentSnapshot.getString("description");
                        String image = documentSnapshot.getString("imageUrl");
                        // Check if the userNameFromIntent matches the user
                        if (dietid.equals(name)) {
                            // Display the data only if they match
                            achi_name.setText(name != null ? name : "No name");
                            achi_description.setText(description != null ? description : "No username");
                            if (image != null) {
                                Glide.with(Achievements.this)
                                        .load(image)
                                        .into(achi_image);
                            }
                        }
                    }
                    progressDialog.dismiss(); // Dismiss loading dialog after data is loaded
                });
    }

    private void updateCertificate() {
        // Get the name and description of the diet
        String achiName = achi_name.getText().toString();
        String achiDisc = achi_description.getText().toString();
        String imageUrl = ""; // Initialize imageUrl variable

        // Get the image URL if available
        Drawable drawable = achi_image.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            imageUrl = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }

        // Start the UpdateDiet activity and pass data as extras
        Intent intent = new Intent(Achievements.this, UpdateCertificate.class);
        intent.putExtra("certi_name", achiName);
        intent.putExtra("certi_description", achiDisc);
        intent.putExtra("certi_imageUrl", imageUrl); // Pass the image URL
        startActivity(intent);

        finish();
    }

    private void deleteCertificate() {
        progressDialog.show(); // Show progress dialog

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        String achiName = achi_name.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("trainers")
                .document(userId)
                .collection("certificates")
                .whereEqualTo("name", achiName) // Find the certificate to delete
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete() // Delete the certificate document
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss(); // Dismiss progress dialog on success
                                        Toast.makeText(Achievements.this, "Certificate deleted successfully", Toast.LENGTH_SHORT).show();
                                        // Navigate back or perform any other action as needed
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss(); // Dismiss progress dialog on failure
                                        Toast.makeText(Achievements.this, "Failed to delete certificate", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        progressDialog.dismiss(); // Dismiss progress dialog on failure
                        Toast.makeText(Achievements.this, "Error fetching certificate data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to show a toast message
    private void showToast(String message) {
        Toast.makeText(Achievements.this, message, Toast.LENGTH_SHORT).show();
    }
}
