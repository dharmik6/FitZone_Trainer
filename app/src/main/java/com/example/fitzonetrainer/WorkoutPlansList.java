package com.example.fitzonetrainer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlansList extends AppCompatActivity {

    MaterialSearchBar plan_searchbar;
    LinearLayout add_exercises;
    RecyclerView plan_recyc;
    private WorkoutPlansAdapter adapter;
    private List<WorkoutPlansItemList> exercisesItemLists;
    private ProgressDialog progressDialog;
    List<WorkoutPlansItemList> filteredList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_plans_list);

        plan_recyc = findViewById(R.id.plan_recyc);
        plan_searchbar = findViewById(R.id.plan_searchbar);

        // Setup MaterialSearchBar
        plan_searchbar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
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

        plan_recyc.setHasFixedSize(true);
        plan_recyc.setLayoutManager(new LinearLayoutManager(this));

        exercisesItemLists = new ArrayList<>(); // Initialize exercisesItemLists
        filteredList = new ArrayList<>();
        adapter = new WorkoutPlansAdapter(this, exercisesItemLists); // Use correct adapter
        plan_recyc.setAdapter(adapter);

        // Show ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Query Firestore for data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("workout_plans").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String name = documentSnapshot.getString("name");
                String body = documentSnapshot.getString("goal");
                String image = documentSnapshot.getString("image");
                String id = documentSnapshot.getId();
                WorkoutPlansItemList exe = new WorkoutPlansItemList(name, body, image,id);
                exercisesItemLists.add(exe);
            }
            filteredList.addAll(exercisesItemLists); // Initialize filteredList with all members

            adapter.notifyDataSetChanged();
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

         add_exercises = findViewById(R.id.add_exercises);
         add_exercises.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(WorkoutPlansList.this,CreateWorkoutPlan.class));
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
        List<WorkoutPlansItemList> filteredList = new ArrayList<>();
        for (WorkoutPlansItemList member : exercisesItemLists) {
            if (member.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(member);
            }
        }
        adapter.filterList(filteredList);
    }
}