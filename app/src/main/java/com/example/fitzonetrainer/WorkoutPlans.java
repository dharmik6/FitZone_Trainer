package com.example.fitzonetrainer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class WorkoutPlans extends AppCompatActivity {
    RecyclerView recyc_exe_data;
    Button dele_plan_data;
    CardView edit_plan_tr;
    TextView totla_exe_plan,plan_name_exe;
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


        plan_name_exe = findViewById(R.id.plan_name_exe);
        img_wor_plan = findViewById(R.id.img_wor_plan);

        Intent intent = getIntent();
        String eid = intent.getStringExtra("name");
        String edd = intent.getStringExtra("image");

        // Set the received data to the EditText fields
        plan_name_exe.setText(eid);
        // Set the received data to the ImageView
        if (!TextUtils.isEmpty(edd)) {
            try {
                // Decode the base64 string to a Bitmap
                byte[] decodedString = Base64.decode(edd, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                // Set the decoded Bitmap to the ImageView
                img_wor_plan.setImageBitmap(decodedByte);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                // Handle the case where the base64 string is invalid
            }
        }

        recyc_exe_data.setHasFixedSize(true);
        recyc_exe_data.setLayoutManager(new LinearLayoutManager(this));

        exercisesItemLists = new ArrayList<>(); // Initialize exercisesItemLists
        adapter = new WorkoutPlansShowAdapter(this, exercisesItemLists); // Use correct adapter
        recyc_exe_data.setAdapter(adapter);

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
                ExercisesItemList exe = new ExercisesItemList(name, body, image, id);
                exercisesItemLists.add(exe);
            }
            adapter.notifyDataSetChanged();
            updateTotalExercises(); // Update total exercises count
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
        edit_plan_tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = plan_name_exe.getText().toString(); // Retrieve the text from plan_name TextView
                Intent intent = new Intent(WorkoutPlans.this, EditWorkout.class);
                intent.putExtra("name", name);
                startActivity(intent);
            }
        });

        dele_plan_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the Firestore instance
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                progressDialog.show();
                // Access the collection and delete the document
                db.collection("workout_plans").document(eid)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Document successfully deleted
                                progressDialog.dismiss();
                                Toast.makeText(WorkoutPlans.this, "Document deleted successfully", Toast.LENGTH_SHORT).show();
                                Intent intent1=new Intent(WorkoutPlans.this,WorkoutPlansList.class);
                                startActivity(intent1);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle any errors
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
                onBackPressed();
            }
        });
    }
    // Method to update total exercises count
    private void updateTotalExercises() {
        totla_exe_plan.setText("Total Exercises: " + exercisesItemLists.size());
    }
}