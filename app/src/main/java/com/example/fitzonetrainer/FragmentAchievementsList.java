package com.example.fitzonetrainer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FragmentAchievementsList extends Fragment {

    RecyclerView achi_recyc;
    private CertificateAdapter adapter;
    private List<CertificateList> certificateLists;
    LinearLayout addAchievements;
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements_list, container, false);

        achi_recyc = view.findViewById(R.id.achi_recyc);
        achi_recyc.setHasFixedSize(true);
        achi_recyc.setLayoutManager(new LinearLayoutManager(getContext()));

        certificateLists = new ArrayList<>();
        adapter = new CertificateAdapter(getContext(), certificateLists);
        achi_recyc.setAdapter(adapter);

        addAchievements = view.findViewById(R.id.add_achiv);
        addAchievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddCertificate.class));
            }
        });

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading Certificates...");
        progressDialog.setCancelable(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchCertificatesOfLoggedInTrainer();
    }

    private void fetchCertificatesOfLoggedInTrainer() {
        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("trainers")
                    .document(userId)
                    .collection("certificates")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            progressDialog.dismiss();

                            // Clear the previous list of certificates
                            certificateLists.clear();

                            // Iterate through the query results
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                // Convert each document to a CertificateList object
                                CertificateList certificate = documentSnapshot.toObject(CertificateList.class);
                                // Add the certificate to the list
                                certificateLists.add(certificate);
                            }

                            // Notify the adapter that the dataset has changed
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();

                            // Handle failure
                            Toast.makeText(getContext(), "Failed to fetch certificates: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}