package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AcceptedAppintments extends AppCompatActivity {
    RecyclerView RecycAcceptedList ;
    private AcceptedBookingAdapter adapter;
    private List<BookingItemList> acceptedLists;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_appintments);

        RecycAcceptedList = findViewById(R.id.acceptedList);
        RecycAcceptedList.setHasFixedSize(true);
        RecycAcceptedList.setLayoutManager(new LinearLayoutManager(this));

        acceptedLists = new ArrayList<>();
        adapter = new AcceptedBookingAdapter(this, acceptedLists);

        RecycAcceptedList.setAdapter(adapter);

    }
    protected void onResume() {
        super.onResume();
        loadDietData();
    }

    private void loadDietData() {
        acceptedLists.clear(); // Clear the previous list
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bookings")
                .whereEqualTo("paymentStatus", "confirmed")
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
                                    acceptedLists.add(bookingList);
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