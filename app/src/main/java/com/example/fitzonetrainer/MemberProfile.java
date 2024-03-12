package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ekn.gruzer.gaugelibrary.HalfGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberProfile extends AppCompatActivity {
    TextView data_name;
    ImageView data_image;
    ImageView member_data_pro_back;
    TextView data_cur_weight,data_cur_height,data_activity,data_gender,data_goal,data_age,data_email;
    LineChart lineChart;
    ProgressDialog progressDialog;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);

        // Initialize your TextView elements
        data_name = findViewById(R.id.data_name);
        data_email = findViewById(R.id.data_email);
        data_activity = findViewById(R.id.data_activity);
        data_gender = findViewById(R.id.data_gender);
        data_age = findViewById(R.id.data_age);
        data_goal = findViewById(R.id.data_goal);
        data_cur_height = findViewById(R.id.data_cur_height);
        data_cur_weight = findViewById(R.id.data_cur_weight);
        data_image = findViewById(R.id.data_image);
        lineChart = findViewById(R.id.lineChart);
        member_data_pro_back = findViewById(R.id.member_data_pro_back);
        member_data_pro_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        String memberid = intent.getStringExtra("uid");
//        String memberemailid = intent.getStringExtra("email");

        // Query Firestore for data
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String membername = documentSnapshot.getString("name");
                String memberactivity = documentSnapshot.getString("activity");
                String membergoal = documentSnapshot.getString("goal");
                String memberweight = documentSnapshot.getString("weight");
                String memberheight = documentSnapshot.getString("height");
                String memberimage = documentSnapshot.getString("image");
                String memberemail = documentSnapshot.getString("email");
                String memberage = documentSnapshot.getString("age");
                String membergender = documentSnapshot.getString("gender");

                if (memberid.equals(membername)) {
                    // Display the data only if they match
                    data_name.setText(membername != null ? membername : "No name");
                    data_email.setText(memberemail != null ? memberemail : "No email");
                    data_goal.setText(membergoal != null ? membergoal : "No goal");
                    data_age.setText(memberage != null ? memberage : "No age");
                    data_activity.setText(memberactivity != null ? memberactivity : "No activity");
                    data_cur_height.setText(memberheight != null ? memberheight : "No age");
                    data_gender.setText(membergender != null ? membergender : "No gender");
                    data_cur_weight.setText(memberweight != null ? memberweight : "No gender");
                    if (memberimage != null) {
                        Glide.with(MemberProfile.this)
                                .load(memberimage)
                                .into(data_image);
                    }

                    // Get the weight and height as strings from TextViews
                    String weightStr = memberweight;
//                    .getText().toString().trim();
                    String heightStr = memberheight;
//                    .getText().toString().trim();

                    // Parse the strings to floats, if they are numeric
                    float weight = TextUtils.isDigitsOnly(weightStr) ? Float.parseFloat(weightStr) : 0;
                    float height = TextUtils.isDigitsOnly(heightStr) ? Float.parseFloat(heightStr) / 100 : 0; // Convert height to meters

                    // Calculate BMI
                    float bmi = calculateBMIValue(weight, height);

                    // Display the BMI
                    TextView tvResult = findViewById(R.id.totle_bmi_rep);
                    tvResult.setText("Your BMI : " + bmi);

                    // Setting up HalfGauge and CardView for BMI chart
                    HalfGauge halfGauge = findViewById(R.id.halfGauge);
                    CardView cardView = findViewById(R.id.tv_result);

                    double value = bmi; // Set your value here

                    // Setting up color ranges for HalfGauge
                    Range range1 = new Range();
                    range1.setColor(Color.parseColor("#2739C9"));
                    range1.setFrom(15);
                    range1.setTo(16);
                    halfGauge.addRange(range1);

                    Range range2 = new Range();
                    range2.setColor(Color.parseColor("#3977F0"));
                    range2.setFrom(16.0);
                    range2.setTo(18.5);
                    halfGauge.addRange(range2);

                    Range range3 = new Range();
                    range3.setColor(Color.parseColor("#5CC2D8"));
                    range3.setFrom(18.5);
                    range3.setTo(25);
                    halfGauge.addRange(range3);

                    Range range4 = new Range();
                    range4.setColor(Color.parseColor("#F7CC4A"));
                    range4.setFrom(25);
                    range4.setTo(30.0);
                    halfGauge.addRange(range4);

                    Range range5 = new Range();
                    range5.setColor(Color.parseColor("#F29837"));
                    range5.setFrom(30.0);
                    range5.setTo(35.0);
                    halfGauge.addRange(range5);

                    Range range6 = new Range();
                    range6.setColor(Color.parseColor("#D8313B"));
                    range6.setFrom(35.0);
                    range6.setTo(40.0);
                    halfGauge.addRange(range6);

                    // Setting up color ranges for CardView based on BMI value
                    int color;
                    String bmiCategory;
                    if (bmi < 16) {
                        color = Color.parseColor("#2739C9"); // Severely underweight
                        bmiCategory = "Severely underweight";
                    } else if (bmi < 18.5) {
                        color = Color.parseColor("#3977F0"); // Underweight
                        bmiCategory = "Underweight";
                    } else if (bmi < 25) {
                        color = Color.parseColor("#5CC2D8"); // Normal weight
                        bmiCategory = "Healthy weight";
                    } else if (bmi < 30) {
                        color = Color.parseColor("#F7CC4A"); // Overweight
                        bmiCategory = " Overweight";
                    } else if (bmi < 35) {
                        color = Color.parseColor("#F29837"); // Obese Class I
                        bmiCategory = "Moderately obese";
                    } else {
                        color = Color.parseColor("#D8313B"); // Obese Class II
                        bmiCategory = "Severely obese";
                    }

                    cardView.setCardBackgroundColor(color);
                    TextView valueText = findViewById(R.id.value_text);
                    valueText.setText(bmiCategory);

                    halfGauge.setMinValue(15);
                    halfGauge.setMaxValue(40.0);
                    halfGauge.setValue(value);
                }
            }
        });

        // Create a list of entries representing the data points on the chart
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 90));
        entries.add(new Entry(1, 85));
        entries.add(new Entry(2, 70));
        entries.add(new Entry(3, 75));
        entries.add(new Entry(4, 70));

        // Create a dataset from the entries
        LineDataSet dataSet = new LineDataSet(entries, "Label for the dataset");
        dataSet.setColor(Color.BLUE); // Set the color of the line
        dataSet.setValueTextColor(Color.RED); // Set the color of the values
        dataSet.setLineWidth(2f); // Set the width of the line

        // Create a LineData object with the dataset
        LineData lineData = new LineData(dataSet);

        // Set the data to the chart
        lineChart.setData(lineData);

        // Customize the appearance of the chart
        lineChart.getDescription().setEnabled(false); // Disable description
        lineChart.setTouchEnabled(true); // Enable touch gestures
        lineChart.setDragEnabled(true); // Enable drag and drop gestures
        lineChart.setScaleEnabled(true); // Enable scaling gestures
        lineChart.setPinchZoom(true); // Enable pinch zoom
        lineChart.setDrawGridBackground(false); // Disable grid background

        // Customize the X axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Position at the bottom
        xAxis.setGranularity(1f); // Interval between each X axis value

        // Customize the Y axis
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setGranularity(5f); // Interval between each Y axis value

        // Disable the right Y axis
        lineChart.getAxisRight().setEnabled(false);

        // Invalidate the chart to refresh
        lineChart.invalidate();


    }

    private float calculateBMIValue(float weight, float height) {
        return weight / (height * height);
    }
}
