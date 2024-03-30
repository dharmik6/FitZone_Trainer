package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.drjacky.imagepicker.ImagePicker;
import com.github.drjacky.imagepicker.constant.ImageProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileInformation extends AppCompatActivity {
    CircleImageView pro_image;
    AppCompatEditText phone, address, bio, experience;
    Spinner specialization, gender, age;
    RelativeLayout user_camera;
    private FirebaseFirestore db;
    private Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ProgressDialog progressDialog;
    AppCompatButton btn_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_information);

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        pro_image = findViewById(R.id.circleImageView);
        user_camera = findViewById(R.id.user_camera);
        phone = findViewById(R.id.Phone_number);
        address = findViewById(R.id.address);
        experience = findViewById(R.id.experience);
        bio = findViewById(R.id.bio);
        specialization = findViewById(R.id.Specialization);
        gender = findViewById(R.id.gender);
        age = findViewById(R.id.age);
        btn_next = findViewById(R.id.btn_profile_next);

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if phone number is empty
                String userPhone = phone.getText().toString().trim();
                if (userPhone.isEmpty()) {
                    phone.setError("Phone number is required");
                    return;
                }

                // Check if address is empty
                String userAddress = address.getText().toString().trim();
                if (userAddress.isEmpty()) {
                    address.setError("Address is required");
                    return;
                }

                // Check if phone number is 10 digits
                if (!isValidPhoneNumber(userPhone)) {
                    phone.setError("Phone number must be 10 digits");
                    return;
                }

                // Proceed with saving data to Firestore
                saveDataToFirestore();
            }
        });

        // specialization spinner
        ArrayAdapter<CharSequence> specializationAdapter = ArrayAdapter.createFromResource(this,
                R.array.specialization_array, android.R.layout.simple_spinner_item);
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specialization.setAdapter(specializationAdapter);

        // age spinner
        ArrayAdapter<Integer> ageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Populate the age spinner with ages from 18 to 100
        for (int i = 18; i <= 100; i++) {
            ageAdapter.add(i);
        }

        age.setAdapter(ageAdapter);

        // goal spinner
        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(goalAdapter);

        user_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use ImagePicker to select an image from camera or gallery
                ImagePicker.Companion.with(ProfileInformation.this)
                        .crop()         // Enable cropping
                        .cropOval()     // Crop shape to oval
                        .provider(ImageProvider.BOTH) // Or bothCameraGallery()
                        .createIntentFromDialog(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent it) {
                                startActivityForResult(it, PICK_IMAGE_REQUEST);
                                return null;
                            }
                        });
            }
        });

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            pro_image.setImageURI(selectedImageUri);
        } else if (resultCode == RESULT_OK && data != null) {
            // Use ImagePicker's getFilePath method to retrieve the selected image URI
            selectedImageUri = Uri.parse(ImagePicker.Companion.getFilePath(data));
            pro_image.setImageURI(selectedImageUri);
        }
    }

    // In the ProfileUserName activity
    private void saveDataToFirestore() {
        String userspecialization = specialization.getSelectedItem().toString().trim();
        String usergender = gender.getSelectedItem().toString().trim();
        String userage = age.getSelectedItem().toString().trim();
        String userexperience = experience.getText().toString().trim();
        String userbio = bio.getText().toString().trim();
        String userAddress = address.getText().toString().trim();
        String userPhone = phone.getText().toString().trim();

        // Get the UID from the Intent extras
        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");

        // Show progress dialog
        progressDialog.show();

        // Create a reference to the Firebase Storage location where you want to store the image
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("trainer_images").child(uid);

        // Upload the image to Firebase Storage
        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL of the uploaded image
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Create a map to store user data
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("specialization", userspecialization);
                        userData.put("gender", usergender);
                        userData.put("age", userage);
                        userData.put("experience", userexperience);
                        userData.put("bio", userbio);
                        userData.put("address", userAddress);
                        userData.put("number", userPhone);
                        userData.put("image", imageUrl); // Save the image URL instead of URI

                        // Add the user data to Firestore using the same UID
                        db.collection("trainers")
                                .document(uid)
                                .update(userData)
                                .addOnSuccessListener(aVoid -> {
                                    // Dismiss progress dialog
                                    progressDialog.dismiss();
                                    Toast.makeText(ProfileInformation.this, "Submit successful", Toast.LENGTH_SHORT).show();
                                    // Redirect to the next activity
                                    redirectActivity(ProfileInformation.this, Login.class, uid);
                                })
                                .addOnFailureListener(e -> {
                                    // Dismiss progress dialog
                                    progressDialog.dismiss();
                                    // Failed to save data
                                    Toast.makeText(ProfileInformation.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    // Dismiss progress dialog
                    progressDialog.dismiss();
                    // Handle failures
                    Toast.makeText(ProfileInformation.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });

    }

    // In the Registration activity
    // Change the redirectActivity method to pass the UID instead of the name
    public static void redirectActivity(Activity activity, Class destination, String uid) {
        Intent intent = new Intent(activity, destination);
        intent.putExtra("uid", uid);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    // Helper method to validate phone number
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.length() == 10;
    }
}
