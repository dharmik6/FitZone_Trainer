package com.example.fitzonetrainer;

import static android.content.ContentValues.TAG;

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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditWorkout extends AppCompatActivity {

    CircleImageView img_wor_plan_image;
    AppCompatTextView img_wor_plan_name, total_exe;
    LinearLayout add_wor_pan;
    Button add_wor_plan_but;
    RecyclerView wor_plan_recyc;
    ProgressDialog progressDialog;
    private EditWorkoutAdapter adapter;
    private List<WorExercisesItemList> worExercisesItemLists;

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

        wor_plan_recyc.setHasFixedSize(true);
        wor_plan_recyc.setLayoutManager(new LinearLayoutManager(this));

        worExercisesItemLists = new ArrayList<>();
        adapter = new EditWorkoutAdapter(this, worExercisesItemLists);
        wor_plan_recyc.setAdapter(adapter);

        Intent intent = getIntent();
        String wid = intent.getStringExtra("wid");
        String name = intent.getStringExtra("name");

        img_wor_plan_name.setText(name);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        // Fetch exercise details
        fetchAndDisplayExerciseDetails(wid);

        add_wor_pan.setOnClickListener(v -> {
            Intent intent1 = new Intent(EditWorkout.this, EditWorkoutList.class);
            intent1.putExtra("wid", wid);
            startActivity(intent1);
        });
        add_wor_plan_but.setOnClickListener(v -> onBackPressed());

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(view -> onBackPressed());
    }

    // Function to fetch and display exercise details
    private void fetchAndDisplayExerciseDetails(String wid) {
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
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error fetching workout document", e));
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
                            worExercisesItemLists.add(item);
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
