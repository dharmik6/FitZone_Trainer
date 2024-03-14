package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PendingAppointments extends AppCompatActivity {
    RecyclerView pendingList ;
    private PendingBookingAdapter adapter;
    private List<BookingItemList> PendingLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_appointments);

        pendingList = findViewById(R.id.PendingList);
        pendingList.setHasFixedSize(true);
        pendingList.setLayoutManager(new LinearLayoutManager(this));

        PendingLists = new ArrayList<>();
        adapter = new PendingBookingAdapter(this, PendingLists);

        pendingList.setAdapter(adapter);

    }
    @Override
    protected void onResume() {
        super.onResume();
        loadDietData();
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
                                    BookingItemList bookingList = new BookingItemList(name, email, status ,id);
                                    PendingLists.add(bookingList);
                                    adapter.notifyDataSetChanged(); // Notify adapter about data changes
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to fetch user details
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }


}