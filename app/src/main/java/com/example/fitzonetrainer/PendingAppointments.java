package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PendingAppointments extends AppCompatActivity {
    RecyclerView pendingList;
    private PendingBookingAdapter adapter;
    private List<BookingItemList> PendingLists;
    TextView dataNotFoundText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_appointments);

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        pendingList = findViewById(R.id.PendingList);
        pendingList.setHasFixedSize(true);
        pendingList.setLayoutManager(new LinearLayoutManager(this));

        PendingLists = new ArrayList<>();
        adapter = new PendingBookingAdapter(this, PendingLists);

        pendingList.setAdapter(adapter);

        // Initialize dataNotFoundText
        dataNotFoundText = findViewById(R.id.data_not_found_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDietData(); // Load diet data when the activity resumes
    }

    private void loadDietData() {
        PendingLists.clear(); // Clear the previous list
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bookings")
                .whereEqualTo("paymentStatus", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String userId = documentSnapshot.getString("userId");
                        String status = documentSnapshot.getString("paymentStatus");
                        String id = documentSnapshot.getId();
                        // Fetch user details using userId
                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userDocumentSnapshot -> {
                                    // Retrieve user details
                                    String name = userDocumentSnapshot.getString("name");
                                    String email = userDocumentSnapshot.getString("email");
                                    // Create a BookingItemList object with user details
                                    BookingItemList bookingList = new BookingItemList(name, email, status, id);
                                    PendingLists.add(bookingList);
                                    adapter.notifyDataSetChanged(); // Notify adapter about data changes
                                    updateDataNotFoundVisibility(); // Update visibility after data changes
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to fetch user details
                                });
                    }
                    // Update visibility in case the list is empty initially
                    updateDataNotFoundVisibility();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void updateDataNotFoundVisibility() {
        if (PendingLists.isEmpty()) { // Check if PendingLists is empty
            dataNotFoundText.setVisibility(View.VISIBLE); // Show the TextView if the list is empty
        } else {
            dataNotFoundText.setVisibility(View.GONE); // Hide the TextView if the list is not empty
        }
    }
}
