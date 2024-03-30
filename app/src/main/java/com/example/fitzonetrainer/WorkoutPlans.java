package com.example.fitzonetrainer;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class WorkoutPlans extends AppCompatActivity {
    RecyclerView recyc_exe_data;
    Button dele_plan_data;
    CardView edit_plan_tr;
    TextView created_date, created, created_level;
    TextView totla_exe_plan, plan_name_exe;
    CircleImageView img_wor_plan;
    private WorkoutPlansShowAdapter adapter;
    private List<ExercisesItemList> exercisesItemLists;
    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_plans);

        recyc_exe_data = findViewById(R.id.recyc_exe_data);
        dele_plan_data = findViewById(R.id.dele_plan_data);
        edit_plan_tr = findViewById(R.id.edit_plan_tr);
        totla_exe_plan = findViewById(R.id.totla_exe_plan);

        created_date = findViewById(R.id.created_date);
        created = findViewById(R.id.created);
        created_level = findViewById(R.id.created_level);

        plan_name_exe = findViewById(R.id.plan_name_exe);
        img_wor_plan = findViewById(R.id.img_wor_plan);

        Intent intent = getIntent();
        String eid = intent.getStringExtra("name");
        String edd = intent.getStringExtra("image");
        String level = intent.getStringExtra("body");
        String wid = intent.getStringExtra("id");

        plan_name_exe.setText(eid);
        created_level.setText(level);
        Glide.with(this).load(edd).into(img_wor_plan);

        recyc_exe_data.setHasFixedSize(true);
        recyc_exe_data.setLayoutManager(new LinearLayoutManager(this));

        exercisesItemLists = new ArrayList<>();
        adapter = new WorkoutPlansShowAdapter(this, exercisesItemLists);
        recyc_exe_data.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        edit_plan_tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkoutPlans.this, EditWorkout.class);
                intent.putExtra("name", eid);
                intent.putExtra("image", edd);
                intent.putExtra("wid", wid);
                startActivity(intent);
            }
        });

        dele_plan_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                progressDialog.show();
                db.collection("workout_plans").document(eid)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(WorkoutPlans.this, "Document deleted successfully", Toast.LENGTH_SHORT).show();
                                Intent intent1 = new Intent(WorkoutPlans.this, WorkoutPlansList.class);
                                startActivity(intent1);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Log.e("Firestore", "Error deleting document", e);
                                Toast.makeText(WorkoutPlans.this, "Failed to delete document", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(WorkoutPlans.this, WorkoutPlansList.class);
                intent1.putExtra("worid", wid);
                startActivity(intent1);
            }
        });

        // Retrieve the workout plan details from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("workout_plans").document(eid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Retrieve created and created_date from Firestore document
                            String createdText = documentSnapshot.getString("created");
                            String createdDateText = documentSnapshot.getString("createdDate");

                            // Set the retrieved values to the TextViews
                            created.setText(createdText);
                            created_date.setText(createdDateText);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching workout plan details: ", e);
                    }
                });
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        // Retrieve the workout ID from Intent extras
        Intent intent = getIntent();
        String wid = intent.getStringExtra("id");

        // Clear the existing list of exercises before reloading
        exercisesItemLists.clear();

        // Fetch and display exercise details
        fetchAndDisplayExerciseDetails(wid);
    }

    // Function to fetch exercise details
    private void fetchExerciseDetails(String exerciseId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("exercises")
                .document(exerciseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ExercisesItemList item = documentSnapshot.toObject(ExercisesItemList.class);
                        if (item != null) {
                            exercisesItemLists.add(item);
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error fetching exercise document", e));
    }
    private void updateTotalExercises(int size) {
        totla_exe_plan.setText("Total Exercises: " + size);
    }

}