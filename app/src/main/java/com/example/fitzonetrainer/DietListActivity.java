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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DietListActivity extends AppCompatActivity {
    private RecyclerView diet_recyc;
    private DietAdapter adapter;
    private List<DietList> dietLists;
    private TextView dataNotFoundText;
    private LinearLayout add_diet;
    private ProgressDialog progressDialog; // Progress dialog for showing loading indicator

    boolean isFirstLoad = true; // Flag to track the first load

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_list);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..."); // Set loading message

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        dataNotFoundText = findViewById(R.id.data_not_show);
        updateDataNotFoundVisibility();

        diet_recyc = findViewById(R.id.diet_recyclerView);
        diet_recyc.setHasFixedSize(true);
        diet_recyc.setLayoutManager(new LinearLayoutManager(this));

        dietLists = new ArrayList<>();
        adapter = new DietAdapter(this, dietLists);
        diet_recyc.setAdapter(adapter);

        add_diet = findViewById(R.id.add_diet);
        add_diet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent int1 = new Intent(DietListActivity.this, AddDiet.class);
                startActivity(int1);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load diet data every time the activity resumes
        loadDietData();
    }

    private void loadDietData() {
        // Show progress dialog
        progressDialog.show();

        dietLists.clear(); // Clear the previous list
        adapter.notifyDataSetChanged(); // Notify adapter to reflect the changes

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("diets")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Hide progress dialog when data is fetched
                    progressDialog.dismiss();

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String name = documentSnapshot.getString("name");
                        String description = documentSnapshot.getString("description");
                        String image = documentSnapshot.getString("imageUrl");
                        DietList diet = new DietList(name, description, image);
                        dietLists.add(diet);
                    }
                    adapter.notifyDataSetChanged(); // Notify adapter about data changes
                    updateDataNotFoundVisibility(); // Update visibility of "no data" message
                })
                .addOnFailureListener(e -> {
                    // Hide progress dialog on failure
                    progressDialog.dismiss();
                    // Handle failure
                });
    }

    private void updateDataNotFoundVisibility() {
        if (dietLists != null && dietLists.isEmpty()) {
            dataNotFoundText.setVisibility(View.VISIBLE);
        } else {
            dataNotFoundText.setVisibility(View.GONE);
        }
    }
}
