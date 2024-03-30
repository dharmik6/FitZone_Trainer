package com.example.fitzonetrainer;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout ;
    NavigationView navigationView;
    TextView text_title;
    ImageView settings ;

    ImageView navImg;
    TextView navName,navEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationview);
        text_title = findViewById(R.id.text_title);
        ImageView menu = findViewById(R.id.show_menu);
        settings = findViewById(R.id.settings);

        View headerView = navigationView.getHeaderView(0);

        navImg = headerView.findViewById(R.id.nav_imag);
        navName = headerView.findViewById(R.id.nav_name);
        navEmail = headerView.findViewById(R.id.nav_email);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Now you can use the userId as needed
            Log.d(TAG, "Current user ID: " + userId);
        } else {
            // Handle the case where the user is not signed in
            Log.d(TAG, "No user is currently signed in");
        }


        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d(TAG, "onCreate: userId "+userId);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("trainers").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String aname = documentSnapshot.getString("name");
                    String aemail = documentSnapshot.getString("email");
                    String aimg = documentSnapshot.getString("image");

                    Log.d(TAG, "onCreate: aname "+aname+"aemail"+aemail);
                    navName.setText(aname != null ? aname : "No name");
                    navEmail.setText(aemail != null ? aemail : "No name");
                    if (aimg != null) {
                        Glide.with(MainActivity.this)
                                .load(aimg)
                                .into(navImg);
                    }
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }


        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectActivity(MainActivity.this , Settings.class);
            }
        });


        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDrawer(drawerLayout);
            }
        });
        // for defulte fragment show
        loadFragment(new FragmentAppointmentsHome(), true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_appointments) {
                    loadFragment(new FragmentAppointmentsHome(), false);

                } else if (itemId == R.id.nav_members) {
                    loadFragment(new FragmentMemberData(), false);

                } else if (itemId == R.id.nav_achievements) {
                    loadFragment(new FragmentAchievementsList(), false);

                } else if (itemId == R.id.nav_workout_exercises) {
                    loadFragment(new FragmentWorkoutExercisesHome(), false);

                } else if (itemId == R.id.nav_diet_plans) {
                   redirectActivity(MainActivity.this , DietListActivity.class);

                } else if (itemId == R.id.nav_profile) {
                    redirectActivity(MainActivity.this , Profile.class);
                }

                closeDrawer(drawerLayout);

                return true;
            }
        });
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void redirectActivity(Activity activity, Class secondActivity) {
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // If drawer is not open
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
            } else {
                // If there are no fragments in the back stack, replace with FragmentAppointmentsHome
                loadFragment(new FragmentAppointmentsHome(), false);
            }
        }
    }


    public void loadFragment(Fragment fragment, boolean flag)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if(flag)
            ft.add(R.id.fragment_container,fragment);
        else
            ft.replace(R.id.fragment_container,fragment);
        ft.commit();

        // Set title based on the loaded fragment
//        if (fragment instanceof Fragment_Member_list) {
//            text_title.setText("Members List");
//        }
//        else if (fragment instanceof ) {
//            text_title.setText("Some Other Title");
//        }
//        else {
//            // Handle other fragments accordingly
//        }
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}