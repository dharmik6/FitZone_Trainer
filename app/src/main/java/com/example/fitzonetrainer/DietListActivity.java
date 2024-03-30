package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
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
    LinearLayout add_diet;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_list);

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
             Intent int1 = new Intent(DietListActivity.this , AddDiet.class);
             startActivity(int1);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDietData();
    }

    private void loadDietData() {
        dietLists.clear(); // Clear the previous list
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("diets")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String name = documentSnapshot.getString("name");
                        String description = documentSnapshot.getString("description");
                        String image = documentSnapshot.getString("imageUrl");
                        DietList diet = new DietList(name, description, image);
                        dietLists.add(diet);
                    }
                    adapter.notifyDataSetChanged(); // Notify adapter about data changes
                })
                .addOnFailureListener(e -> {
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