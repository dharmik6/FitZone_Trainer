package com.example.fitzonetrainer;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditWorkout extends AppCompatActivity {

    CircleImageView img_wor_plan_image;
    AppCompatTextView img_wor_plan_name, total_exe;
    LinearLayout add_wor_pan;
    Button add_wor_plan_but;
    RecyclerView wor_plan_recyc;
    ProgressDialog progressDialog;
    private EditWorkoutAdapter adapter;
    private List<WorExercisesItemList> exercisesItemLists;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);

        img_wor_plan_image = findViewById(R.id.img_wor_plan_image);
        img_wor_plan_name = findViewById(R.id.img_wor_plan_name);
        total_exe = findViewById(R.id.total_exe);
        wor_plan_recyc = findViewById(R.id.wor_plan_recyc);

        add_wor_pan = findViewById(R.id.add_wor_pan);
        add_wor_plan_but = findViewById(R.id.add_wor_plan_but);

        Intent intent = getIntent();
        String wid = intent.getStringExtra("wid");
        String name = intent.getStringExtra("name");
        String ima = intent.getStringExtra("image");

        String id = intent.getStringExtra("id");

        wor_plan_recyc.setHasFixedSize(true);
        wor_plan_recyc.setLayoutManager(new LinearLayoutManager(this));

        exercisesItemLists = new ArrayList<>();
        adapter = new EditWorkoutAdapter(this, exercisesItemLists,wid);
        wor_plan_recyc.setAdapter(adapter);

//        Log.d("wid",wid);
        img_wor_plan_name.setText(name);
        // Load image into ImageView using Glide library
        Glide.with(this)
                .load(ima)
                .into(img_wor_plan_image);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        if (id != null) {
            // Update Firestore document with the new exercise ID
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("workout_plans")
                    .document(wid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> exerciseIds = (List<String>) documentSnapshot.get("exename");
                            if (exerciseIds == null) {
                                // Create a new array and add the ID
                                exerciseIds = new ArrayList<>();
                            }
                            exerciseIds.add(id);
                            // Update the Firestore document with the updated array
                            Map<String, Object> data = new HashMap<>();
                            data.put("exename", exerciseIds);
                            db.collection("workout_plans")
                                    .document(wid)
                                    .set(data, SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully updated!"))
                                    .addOnFailureListener(e -> Log.w(TAG, "Error updating document", e));
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error fetching document", e));
        }

        progressDialog.show();

        // Fetch exercise details
        fetchAndDisplayExerciseDetails(wid, id);

        add_wor_pan.setOnClickListener(v -> {
            Intent intent1 = new Intent(EditWorkout.this, WorkoutExercisesList.class);
            intent1.putExtra("wid", wid);
            startActivity(intent1);
        });
        add_wor_plan_but.setOnClickListener(v -> onBackPressed());

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(view -> onBackPressed());
    }

    // Function to fetch and display exercise details
    private void fetchAndDisplayExerciseDetails(String wid, String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("workout_plans")
                .document(wid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> exerciseIds = (List<String>) documentSnapshot.get("exename");
                        if (exerciseIds != null) {
                            for (String exerciseId : exerciseIds) {
                                fetchExerciseDetails(exerciseId);
                            }
                            // Update total exercises count
                            updateTotalExercises(exerciseIds.size());
                        }
                    }
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error fetching workout document", e);
                    progressDialog.dismiss();
                });
    }

    // Function to fetch exercise details
    private void fetchExerciseDetails(String exerciseId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("exercises")
                .document(exerciseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        WorExercisesItemList item = documentSnapshot.toObject(WorExercisesItemList.class);
                        if (item != null) {
                            exercisesItemLists.add(item);
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error fetching exercise document", e));
    }

    // Function to update total exercises count
    private void updateTotalExercises(int size) {
        total_exe.setText("Total Exercises: " + size);
    }
}
