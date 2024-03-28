package com.example.fitzonetrainer;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

public class FragmentMemberData extends Fragment {

    private RecyclerView recyclerView;
    private MemberDataAdapter dataAdapter;
    private List<MemberList> memberList;
    private TextView dataNotFoundText;
    private ProgressDialog progressDialog;
    private MaterialSearchBar user_data_searchbar;
    private List<MemberList> filteredList1;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_member_data, container, false);

        // Initialize search bar
        user_data_searchbar = view.findViewById(R.id.user_data_searchbar);

        // Setup MaterialSearchBar
        user_data_searchbar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // Handle search state changes
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                // Perform search
                filter1(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                // Handle button clicks
            }
        });

        recyclerView = view.findViewById(R.id.data_recyc_members);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dataNotFoundText = view.findViewById(R.id.data_not_show);
        updateDataNotFoundVisibility();

        filteredList1 = new ArrayList<>();
        memberList = new ArrayList<>();
        dataAdapter = new MemberDataAdapter(getContext(), memberList);
        recyclerView.setAdapter(dataAdapter);

        // Show ProgressDialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Query Firestore for data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        listenerRegistration = db.collection("users").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                // Handle error
                return;
            }

            memberList.clear(); // Clear previous data

            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String image = documentSnapshot.getString("image");
                String uid = documentSnapshot.getString("name");

                MemberList member = new MemberList(name, email, image, uid);
                memberList.add(member);
            }

            filteredList1.addAll(memberList); // Initialize filteredList with all members
            dataAdapter.notifyDataSetChanged(); // Notify adapter of dataset change
            updateDataNotFoundVisibility();

            // Dismiss ProgressDialog when data is loaded
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });

        return view;
    }

    private void filter1(String query) {
        List<MemberList> filteredList1 = new ArrayList<>();
        for (MemberList member : memberList) {
            if (member.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList1.add(member);
            }
        }
        dataAdapter.filterList1(filteredList1);
    }

    // Method to update visibility of "Data not found" message based on data availability
    private void updateDataNotFoundVisibility() {
        if (memberList != null && memberList.isEmpty()) {
            dataNotFoundText.setVisibility(View.VISIBLE);
        } else {
            dataNotFoundText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
