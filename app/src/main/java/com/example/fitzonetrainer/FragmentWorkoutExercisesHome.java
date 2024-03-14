package com.example.fitzonetrainer;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FragmentWorkoutExercisesHome extends Fragment {

    CardView workoutPlan ;
    CardView exercisesList ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workout_exercises_home, container, false);

        workoutPlan = view.findViewById(R.id.workout_plan);
        exercisesList = view.findViewById(R.id.exercises_list);

        workoutPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WorkoutPlansList.class);
                startActivity(intent);
            }
        });
        exercisesList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ExercisesList.class);
                startActivity(intent);
            }
        });

        return view;
    }
}