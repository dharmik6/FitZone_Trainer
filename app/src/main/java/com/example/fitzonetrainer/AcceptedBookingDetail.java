package com.example.fitzonetrainer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class AcceptedBookingDetail extends AppCompatActivity {
    AppCompatTextView booking_id, member_id, booking_date, start_time, end_time, booking_status;
    FirebaseFirestore db;
    ProgressDialog progressDialog; // Declare ProgressDialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_booking_detail);

        booking_id = findViewById(R.id.booking_id);
        member_id = findViewById(R.id.member_id);
        booking_date = findViewById(R.id.bookig_date);
        start_time = findViewById(R.id.start_time);
        end_time = findViewById(R.id.end_time);
        booking_status = findViewById(R.id.booking_status);

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");
        String status = intent.getStringExtra("status");

        booking_id.setText(id);
        member_id.setText(name);
        booking_status.setText(status);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching data..."); // Set the message for the dialog
        progressDialog.setCancelable(false); // Set whether the dialog is cancelable

        showProgressDialog(); // Show ProgressDialog before fetching data from Firestore

        db.collection("bookings").document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        dismissProgressDialog(); // Dismiss the ProgressDialog after data retrieval, whether success or failure
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Retrieve additional data and set them to respective TextViews
                                String bookingDate = document.getString("bookingDate");
                                String startTime = document.getString("startTime");
                                String endTime = document.getString("endTime");

                                Log.d("FirestoreData", "Booking Date: " + bookingDate);
                                Log.d("FirestoreData", "Start Time: " + startTime);
                                Log.d("FirestoreData", "End Time: " + endTime);

                                booking_date.setText(bookingDate != null ? bookingDate : "No date");
                                start_time.setText(startTime != null ? startTime : "No date");
                                end_time.setText(endTime != null ? endTime : "No date");
                            } else {
                                // Handle the case where the document does not exist
                            }
                        } else {
                            // Handle exceptions while fetching data from Firestore
                            FirebaseFirestoreException exception = (FirebaseFirestoreException) task.getException();
                            // Log the exception or handle it as needed
                        }
                    }
                });
    }

    private void showProgressDialog() {
        progressDialog.show(); // Show the ProgressDialog
    }

    private void dismissProgressDialog() {
        progressDialog.dismiss(); // Dismiss the ProgressDialog
    }
}
