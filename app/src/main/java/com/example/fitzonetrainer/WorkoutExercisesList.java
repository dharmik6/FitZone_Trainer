package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

public class WorkoutExercisesList extends AppCompatActivity {
    MaterialSearchBar wor_exe_searchbar;
    RecyclerView edit_exe_tr;

    private WorkoutExercisesListAdapter adapter;
    private List<WorkoutExercisesListItem> exercisesItemLists;
    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_exercises_list);
        edit_exe_tr = findViewById(R.id.edit_exe_tr);
        wor_exe_searchbar = findViewById(R.id.wor_exe_searchbar);

        Intent intent = getIntent();
        String wid = intent.getStringExtra("wid");

        // Setup MaterialSearchBar
        wor_exe_searchbar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // Handle search state changes
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                // Perform search
                filter(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                // Handle button clicks
            }
        });

        edit_exe_tr.setHasFixedSize(true);
        edit_exe_tr.setLayoutManager(new LinearLayoutManager(this));

        exercisesItemLists = new ArrayList<>(); // Initialize exercisesItemLists
        adapter = new WorkoutExercisesListAdapter(this, exercisesItemLists); // Use correct adapter
        edit_exe_tr.setAdapter(adapter);

        // Show ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Query Firestore for data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("exercises").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String name = documentSnapshot.getString("name");
                String body = documentSnapshot.getString("body");
                String image = documentSnapshot.getString("imageUrl");
                String id = documentSnapshot.getId();
                WorkoutExercisesListItem exe = new WorkoutExercisesListItem(name, body, image, id,wid);
                exercisesItemLists.add(exe);
            }

            // Now that we have fetched all exercises, let's filter out the ones that are already in the workout plan
            List<WorkoutExercisesListItem> filteredExercises = new ArrayList<>();
            if (wid != null) {
                // Fetch the exename array of the workout plan
                db.collection("workout_plans").document(wid).get().addOnSuccessListener(workoutPlanDocument -> {
                    if (workoutPlanDocument.exists()) {
                        List<String> exename = (List<String>) workoutPlanDocument.get("exename");
                        if (exename != null) {
                            // Iterate through all exercises and exclude the ones that are present in the exename array
                            for (WorkoutExercisesListItem exercise : exercisesItemLists) {
                                if (!exename.contains(exercise.getId())) {
                                    filteredExercises.add(exercise);
                                }
                            }
                        } else {
                            filteredExercises.addAll(exercisesItemLists);
                        }
                    } else {
                        filteredExercises.addAll(exercisesItemLists);
                    }

                    // Update the adapter with the filtered exercises
                    adapter.filterList(filteredExercises);
                    // Dismiss ProgressDialog when data is loaded
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(e -> {
                    // Handle failures
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
            } else {
                // If wid is null, add all exercises to the filtered list
                filteredExercises.addAll(exercisesItemLists);
                adapter.filterList(filteredExercises);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(e -> {
            // Handle failures
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
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


    private void filter(String query) {
        List<WorkoutExercisesListItem> filteredList = new ArrayList<>();
        for (WorkoutExercisesListItem member : exercisesItemLists) {
            if (member.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(member);
            }
        }
        adapter.filterList(filteredList);
    }
}
