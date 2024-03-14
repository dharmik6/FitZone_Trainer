package com.example.fitzonetrainer;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FragmentAppointmentsHome extends Fragment {

    CardView pennding , acepted ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_appointments_home, container, false) ;

        pennding = view.findViewById(R.id.pendding);
        acepted = view.findViewById(R.id.accepted);
        pennding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PendingAppointments.class);
                startActivity(intent);
            }
        });
        acepted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AcceptedAppintments.class);
                startActivity(intent);
            }
        });
        return view;
    }
}