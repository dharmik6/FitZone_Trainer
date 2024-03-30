package com.example.fitzonetrainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class PendingBookingDetail extends AppCompatActivity {
    AppCompatTextView booking_id, member_id, booking_date, start_time, end_time, booking_status;
    FirebaseFirestore db;
    Button accept , cancel ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_booking_detail);

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        booking_id = findViewById(R.id.booking_id);
        member_id = findViewById(R.id.member_id);
        booking_date = findViewById(R.id.bookig_date);
        start_time = findViewById(R.id.start_time);
        end_time = findViewById(R.id.end_time);
        booking_status = findViewById(R.id.booking_status);
        accept = findViewById(R.id.btn_accept);
        cancel = findViewById(R.id.cancel);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");
        String status = intent.getStringExtra("status");

        booking_id.setText(id);
        member_id.setText(name);
        booking_status.setText(status);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch additional data from Firestore
        // Fetch additional data from Firestore
        Log.d("id" , id);
        db.collection("bookings").document(id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update paymentStatus to "confirmed" in Firestore
                db.collection("bookings").document(id)
                        .update("paymentStatus", "confirmed")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Update successful
                                Toast.makeText(PendingBookingDetail.this, "Booking confirmed", Toast.LENGTH_SHORT).show();
                                // Optionally, you may also finish this activity or perform any other action after the update
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle errors while updating
                                Log.e("FirestoreUpdate", "Error updating document", e);
                                Toast.makeText(PendingBookingDetail.this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update paymentStatus to "confirmed" in Firestore
                db.collection("bookings").document(id)
                        .update("paymentStatus", "canceled")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Update successful
                                Toast.makeText(PendingBookingDetail.this, "Booking canceled", Toast.LENGTH_SHORT).show();
                                // Optionally, you may also finish this activity or perform any other action after the update
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle errors while updating
                                Log.e("FirestoreUpdate", "Error updating document", e);
                                Toast.makeText(PendingBookingDetail.this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }
}