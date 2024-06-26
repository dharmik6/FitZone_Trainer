package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {
    AppCompatTextView tr_name, tr_email, tr_catagory, tr_mobile, tr_gender, tr_bio, tr_address, tr_experience, tr_charg;
    ImageView tr_image;
    CardView edit;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        initializeViews();
        setListeners();
        fetchProfileData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data whenever the activity is resumed
        fetchProfileData();
    }

    private void initializeViews() {
        tr_name = findViewById(R.id.trainer_name);
        tr_email = findViewById(R.id.email);
        tr_address = findViewById(R.id.address);
        tr_bio = findViewById(R.id.bio);
        tr_catagory = findViewById(R.id.catagory);
        tr_mobile = findViewById(R.id.mobile);
        tr_gender = findViewById(R.id.gender);
        tr_image = findViewById(R.id.trainer_img);
        tr_experience = findViewById(R.id.experience);
        tr_charg = findViewById(R.id.charge);
        edit = findViewById(R.id.edit);
    }

    private void setListeners() {
        CardView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



    }

    private void fetchProfileData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("trainers").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                progressDialog.dismiss(); // Dismiss the loading dialog
                if (documentSnapshot.exists()) {
                    String trname = documentSnapshot.getString("name");
                    String trexperience = documentSnapshot.getString("experience");
                    String traddress = documentSnapshot.getString("address");
                    String trcharg = documentSnapshot.getString("charge");
                    String trmobile = documentSnapshot.getString("number");
                    String trcatagory = documentSnapshot.getString("specialization");
                    String trbio = documentSnapshot.getString("bio");
                    String trgender = documentSnapshot.getString("gender");
                    String tremail = documentSnapshot.getString("email");
                    String trimage = documentSnapshot.getString("image");

                    tr_name.setText(trname != null ? trname : "No name");
                    tr_experience.setText(trexperience != null ? trexperience : "No username");
                    tr_address.setText(traddress != null ? traddress : "No activity");
                    tr_email.setText(tremail != null ? tremail : "No address");
                    tr_charg.setText(trcharg != null ? trcharg : "Not Avalible");
                    tr_mobile.setText(trmobile != null ? trmobile : "No gender");
                    tr_catagory.setText(trcatagory != null ? trcatagory : "No email");
                    tr_bio.setText(trbio != null ? trbio : "No number");
                    tr_gender.setText(trgender != null ? trgender : "No goal");

                    Intent intent = new Intent(Profile.this, UpdateProfile.class);

                    // Put profile data as extras
                    intent.putExtra("name", trname);
                    intent.putExtra("experience", trexperience);
                    intent.putExtra("address", traddress);
                    intent.putExtra("charge", trcharg);
                    intent.putExtra("mobile", trmobile);
                    intent.putExtra("catagory", trcatagory);
                    intent.putExtra("bio", trbio);
                    intent.putExtra("gender", trgender);
                    intent.putExtra("email", tremail);
                    intent.putExtra("image", trimage);

                    edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(intent);
                        }
                    });
                    // Start UpdateProfile activity

                    if (trimage != null) {
                        Glide.with(Profile.this)
                                .load(trimage)
                                .into(tr_image);
                    }
                }
            }).addOnFailureListener(e -> {
                progressDialog.dismiss(); // Dismiss the loading dialog on failure
                Toast.makeText(Profile.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            progressDialog.dismiss(); // Dismiss the loading dialog if user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }


}
