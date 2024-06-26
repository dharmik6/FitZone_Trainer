package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AcceptedAppintments extends AppCompatActivity {
    RecyclerView RecycAcceptedList;
    private AcceptedBookingAdapter adapter;
    private List<BookingItemList> acceptedLists;
    TextView dataNotFoundText;
    ProgressDialog progressDialog; // Declare ProgressDialog

    @SuppressLint("MissingInflatedId")
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

        ImageView backPress = findViewById(R.id.back);
        backPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Initialize dataNotFoundText
        dataNotFoundText = findViewById(R.id.data_not_found_text);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..."); // Set the message for the dialog
        progressDialog.setCancelable(false); // Set whether the dialog is cancelable
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgressDialog(); // Show ProgressDialog when the activity resumes
        loadDietData(); // Load data when the activity resumes
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
                                    BookingItemList bookingList = new BookingItemList(name, email, status, id);
                                    acceptedLists.add(bookingList);
                                    adapter.notifyDataSetChanged(); // Notify adapter about data changes
                                    updateDataNotFoundVisibility(); // Update visibility after data changes
                                    progressDialog.dismiss(); // Dismiss the ProgressDialog
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to fetch user details
                                    progressDialog.dismiss(); // Dismiss the ProgressDialog
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    progressDialog.dismiss(); // Dismiss the ProgressDialog
                });
    }

    private void showProgressDialog() {
        progressDialog.show(); // Show the ProgressDialog
    }

    private void updateDataNotFoundVisibility() {
        if (acceptedLists.isEmpty()) { // Check if acceptedLists is empty
            dataNotFoundText.setVisibility(View.VISIBLE); // Show the TextView if the list is empty
            progressDialog.dismiss();
        } else {
            dataNotFoundText.setVisibility(View.GONE); // Hide the TextView if the list is not empty
        }
    }
}
