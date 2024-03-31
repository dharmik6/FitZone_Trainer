package com.example.fitzonetrainer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.bumptech.glide.Glide;
import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
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

    AppCompatEditText up_name , up_phone, up_address ,up_experience , up_bio ;
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

        ArrayAdapter<Integer> ageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 10; i <= 100; i++) {
            ageAdapter.add(i);
        }
        up_age.setAdapter(ageAdapter);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        up_gender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> specializationAdapter = ArrayAdapter.createFromResource(this,
                R.array.specialization_array, android.R.layout.simple_spinner_item);
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        up_Specialization.setAdapter(specializationAdapter);  // Set the adapter here

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (up_phone.length() != 10) {
                    up_phone.setError("Phone number must have 10 digits");
                    up_phone.requestFocus();
                    return;
                }

                String name = up_name.getText().toString().trim();
                String phone = up_phone.getText().toString().trim();
                String address = up_address.getText().toString().trim();
                String experience = up_experience.getText().toString().trim();
                String bio = up_bio.getText().toString().trim();
                String specialization = up_Specialization.getSelectedItem().toString().trim();
                String gender = up_gender.getSelectedItem().toString().trim();
                String age = up_age.getSelectedItem().toString().trim();

                if (!name.isEmpty() && !phone.isEmpty() && !address.isEmpty() && !experience.isEmpty() && !bio.isEmpty() && !specialization.isEmpty() && !gender.isEmpty() && !age.isEmpty()) {
                    progressDialog = new ProgressDialog(UpdateProfile.this);
                    progressDialog.setMessage("Updating trainer data...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    Map<String, Object> trainerData = new HashMap<>();
                    trainerData.put("name", name);
                    trainerData.put("number", phone);
                    trainerData.put("address", address);
                    trainerData.put("experience", experience);
                    trainerData.put("bio", bio);
                    trainerData.put("specialization", specialization);
                    trainerData.put("gender", gender);
                    trainerData.put("age", age);

                    if (selectedImageUri != null) {
                        uploadImageAndSaveData(trainerData, trainerId);
                    } else {
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
                ImagePicker.Companion.with(UpdateProfile.this)
                        .crop()
                        .cropOval()
                        .provider(ImageProvider.BOTH)
                        .createIntentFromDialog(it -> {
                            startActivityForResult(it, PICK_IMAGE_REQUEST);
                            return null;
                        });
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("name");
            String phone = intent.getStringExtra("mobile");
            String address = intent.getStringExtra("address");
            String experience = intent.getStringExtra("experience");
            String bio = intent.getStringExtra("bio");
            String specialization = intent.getStringExtra("specialization");
            String gender = intent.getStringExtra("gender");
            String age = intent.getStringExtra("age");
            String imageUri = intent.getStringExtra("image");


            up_name.setText(name);
            up_phone.setText(phone);
            up_address.setText(address);
            up_experience.setText(experience);
            up_bio.setText(bio);

            if (specialization != null) {
                ArrayAdapter<CharSequence> specAdapter = (ArrayAdapter<CharSequence>) up_Specialization.getAdapter();
                int position = specAdapter.getPosition(specialization);
                if (position != -1) {
                    up_Specialization.setSelection(position);
                }
            }
            if (gender != null) {
                ArrayAdapter<CharSequence> genderAdpt = (ArrayAdapter<CharSequence>) up_gender.getAdapter();
                int position = genderAdpt.getPosition(gender);
                if (position != -1) {
                    up_gender.setSelection(position);
                }
            }
            if (age != null) {
                ArrayAdapter<Integer> ageAdpt = (ArrayAdapter<Integer>) up_age.getAdapter();
                int ageValue = Integer.parseInt(age);
                int position = ageAdpt.getPosition(ageValue);
                if (position != -1) {
                    up_age.setSelection(position);
                }
            }

            if (imageUri != null) {
                Glide.with(this)
                        .load(imageUri)
                        .into(up_image);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            up_image.setImageURI(selectedImageUri);
        }
    }

    private void uploadImageAndSaveData(final Map<String, Object> trainerData, final String trainerId) {
        StorageReference imageRef = storageRef.child("trainer_images/" + trainerId);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveDataToFirestore(trainerData, trainerId, imageUrl);
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateProfile.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDataToFirestore(Map<String, Object> trainerData, String trainerId, String imageUrl) {
        if (imageUrl != null) {
            trainerData.put("image", imageUrl);
        }

        db.collection("trainers").document(trainerId)
                .update(trainerData)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateProfile.this, "Trainer data updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(UpdateProfile.this, "Failed to update trainer data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
