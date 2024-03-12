package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import android.app.ProgressDialog;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;

public class Diet extends AppCompatActivity {

    TextView name_diet,description_diet;
    ImageView image_diet;
    Button diet_del_butt,diet_upe_butt;
    ProgressDialog progressDialog;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet);

        name_diet = findViewById(R.id.name_diet);
        description_diet = findViewById(R.id.description_diet);
        image_diet = findViewById(R.id.image_diet);
        diet_del_butt = findViewById(R.id.diet_del_butt);
        diet_upe_butt = findViewById(R.id.diet_upd_butt);

        Intent intent = getIntent();
        String dietid = intent.getStringExtra("name");

        progressDialog = new ProgressDialog(Diet.this);
        progressDialog.setMessage("Deleting...");
        progressDialog.setCancelable(false);

        // Query Firestore for data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("diets").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String name = documentSnapshot.getString("name");
                String description = documentSnapshot.getString("description");
                String image = documentSnapshot.getString("imageUrl");
//                 Check if the userNameFromIntent matches the user
                if (dietid.equals(name)) {
                    // Display the data only if they match
                    name_diet.setText(name != null ? name : "No name");
                    description_diet.setText(description != null ? description : "No username");
                    if (image != null) {
                        Glide.with(Diet.this)
                                .load(image)
                                .into(image_diet);
                    }
                } else {
                    // userNameFromIntent and user don't match, handle accordingly
//                    showToast("User data does not match the intent.");
                }
            }
        });

        diet_upe_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the name and description of the diet
                String dietName = name_diet.getText().toString();
                String ddescription = description_diet.getText().toString();
                String imageUrl = ""; // Initialize imageUrl variable

                // Get the image URL if available
                Drawable drawable = image_diet.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] byteArray = baos.toByteArray();
                    imageUrl = Base64.encodeToString(byteArray, Base64.DEFAULT);
                }

                // Start the UpdateDiet activity and pass data as extras
                Intent intent = new Intent(Diet.this, UpdateDiet.class);
                intent.putExtra("name", dietName);
                intent.putExtra("description", ddescription);
                intent.putExtra("imageUrl", imageUrl); // Pass the image URL
                startActivity(intent);
            }
        });

        diet_del_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String dietid = intent.getStringExtra("name");

                progressDialog.show();
                // Get the name of the diet from the TextView
                String dietName = name_diet.getText().toString();

                // Query Firestore for the document ID of the diet entry based on its name
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("diets")
                        .whereEqualTo("name", dietName)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                // Get the document ID
                                String documentId = documentSnapshot.getId();

                                // Delete the document from Firestore
                                db.collection("diets")
                                        .document(dietid)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            // Document successfully deleted
                                            // Optionally, you can show a toast or perform any other action
                                            progressDialog.dismiss();
                                            showToast("Diet deleted successfully");
                                            finish();

                                        })
                                        .addOnFailureListener(e -> {
                                            // Handle any errors that may occur
                                            progressDialog.dismiss();
                                            showToast("Failed to delete diet");
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle any errors that may occur
                            progressDialog.dismiss();
                            showToast("Failed to delete diet");
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

    // Method to show a toast message
    private void showToast(String message) {
        Toast.makeText(Diet.this, message, Toast.LENGTH_SHORT).show();
    }
}