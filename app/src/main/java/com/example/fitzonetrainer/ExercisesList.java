package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

public class ExercisesList extends AppCompatActivity {
    MaterialSearchBar exe_searchbar;
    LinearLayout add_exe;
    RecyclerView exe_recyc;

    private ExercisesAdapter adapter;
    private List<ExercisesItemList> exercisesItemLists;
    private ProgressDialog progressDialog;


    private TextView dataNotFoundText;
    List<ExercisesItemList> filteredList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises_list);

        dataNotFoundText = findViewById(R.id.data_not_show);
        updateDataNotFoundVisibility();


        exe_recyc = findViewById(R.id.exe_recyc);
        add_exe = findViewById(R.id.add_exe);
        exe_searchbar = findViewById(R.id.exe_searchbar);

        // Setup MaterialSearchBar
        exe_searchbar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
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
        add_exe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExercisesList.this,AddExercises.class));
            }
        });

        exe_recyc.setHasFixedSize(true);
        exe_recyc.setLayoutManager(new LinearLayoutManager(this));

        exercisesItemLists = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ExercisesAdapter(this, exercisesItemLists);
        exe_recyc.setAdapter(adapter);

        // Show ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Load data initially
//        loadExercisesData();

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    //    @Override
    protected void onResume() {
        super.onResume();
        // Reload data every time activity is resumed
        loadExercisesData();
    }

    private void loadExercisesData() {
        exercisesItemLists.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("exercises").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String name = documentSnapshot.getString("name");
                String body = documentSnapshot.getString("body");
                String image = documentSnapshot.getString("imageUrl");
                String id = documentSnapshot.getId();
                ExercisesItemList exe = new ExercisesItemList(name, body, image, id);
                exercisesItemLists.add(exe);
            }
            filteredList.addAll(exercisesItemLists);

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
    }

    private void filter(String query) {
        List<ExercisesItemList> filteredList = new ArrayList<>();
        for (ExercisesItemList member : exercisesItemLists) {
            if (member.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(member);
            }
        }
        adapter.filterList(filteredList);
    }

    private void updateDataNotFoundVisibility() {
        if (filteredList != null && filteredList.isEmpty()) {
            dataNotFoundText.setVisibility(View.VISIBLE);
        } else {
            dataNotFoundText.setVisibility(View.GONE);
        }
    }
}