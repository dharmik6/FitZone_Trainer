package com.example.fitzonetrainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;

public class Exercises extends AppCompatActivity {
    ImageView show_image;
    TextView show_name,show_body,show_equipment,show_description;
    Button exe_delete,exe_update;
    ProgressDialog progressExerciseslog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        exe_update = findViewById(R.id.exe_update);
        exe_delete = findViewById(R.id.exe_delete);
        show_image = findViewById(R.id.show_image);
        show_name = findViewById(R.id.show_name);
        show_body = findViewById(R.id.show_body);
        show_equipment = findViewById(R.id.show_equipment);
        show_description = findViewById(R.id.show_description);

        progressExerciseslog = new ProgressDialog(Exercises.this);
        progressExerciseslog.setMessage("Deleting...");
        progressExerciseslog.setCancelable(false);

        Intent intent = getIntent();
        String exeid = intent.getStringExtra("name");

        loadDietData(exeid);

        exe_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the name and description of the diet
                String dietName = show_name.getText().toString();
                String ddescription = show_description.getText().toString();
                String body = show_body.getText().toString();
                String equipment = show_equipment.getText().toString();
                String imageUrl = ""; // Initialize imageUrl variable

                // Get the image URL if available
                Drawable drawable = show_image.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] byteArray = baos.toByteArray();
                    imageUrl = Base64.encodeToString(byteArray, Base64.DEFAULT);
                }

                Intent intent1=new Intent(Exercises.this, UpdateExercises.class);
                intent1.putExtra("name", dietName);
                intent1.putExtra("description", ddescription);
                intent1.putExtra("body", body);
                intent1.putExtra("equipment", equipment);
                intent1.putExtra("imageUrl", imageUrl); // Pass the image URL
                startActivity(intent1);
            }
        });

        exe_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show progress dialog
                progressExerciseslog.show();

                // Get reference to Firestore and the collection "exercises"
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("exercises")
                        .whereEqualTo("name", exeid) // Query for the exercise with the matching name
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            // Loop through each document matching the query (there should be only one)
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                // Delete the document
                                db.collection("exercises").document(documentSnapshot.getId())
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Deletion successful
                                            progressExerciseslog.dismiss();
                                            Toast.makeText(Exercises.this, "Exercise deleted successfully", Toast.LENGTH_SHORT).show();
                                            // Finish the current activity to go back
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Deletion failed
                                            progressExerciseslog.dismiss();
                                            Toast.makeText(Exercises.this, "Failed to delete exercise", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Querying failed
                            progressExerciseslog.dismiss();
                            Toast.makeText(Exercises.this, "Failed to fetch exercise data", Toast.LENGTH_SHORT).show();
                        });
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
        Intent intent = getIntent();
        String exeid = intent.getStringExtra("name");
        loadDietData(exeid);
    }

    private void loadDietData(String exeid) {
        progressExerciseslog.setMessage("Loading...");
        progressExerciseslog.show();

        // Query Firestore for data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("exercises").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String name = documentSnapshot.getString("name");
                String body = documentSnapshot.getString("body");
                String equipment = documentSnapshot.getString("equipment");
                String description = documentSnapshot.getString("description");
                String image = documentSnapshot.getString("imageUrl");
                if (exeid.equals(name)) {
                    // Display the data only if they match
                    show_name.setText(name != null ? name : "No name");
                    show_equipment.setText(equipment != null ? equipment : "No name");
                    show_description.setText(description != null ? description : "No name");
                    show_body.setText(body != null ? body : "No username");
                    if (image != null) {
                        Glide.with(Exercises.this)
                                .load(image)
                                .into(show_image);
                    }
                    progressExerciseslog.dismiss();
                    return; // Exit the loop after finding the matching exercise
                }
            }
            // If exercise data is not found, dismiss progress dialog
            progressExerciseslog.dismiss();
            Toast.makeText(Exercises.this, "Failed to load exercise data", Toast.LENGTH_SHORT).show();
        });
    }
}
