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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
                db.collection("users").document(memberid).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String membername = documentSnapshot.getString("name");
                        String memberactivity = documentSnapshot.getString("activity");
                        String membergoal = documentSnapshot.getString("goal");
//                String memberweight = documentSnapshot.getString("weight");
                        String memberheight = documentSnapshot.getString("height");
                        String memberimage = documentSnapshot.getString("image");
                        String userId = documentSnapshot.getId();

                        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                        db1.collection("users").document(userId).collection("weight").get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                            List<Entry> entries = new ArrayList<>();
                            for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots1) {
                                String memberweight = documentSnapshot1.getString("weight");
                                String dateStr = documentSnapshot1.getString("date");

//                if (memberid.equals(membername)) {
                                // Display the data only if they match
                                data_name.setText(membername != null ? membername : "No name");
                                data_activity.setText(memberactivity != null ? memberactivity : "No activity");
                                data_cur_weight.setText(memberweight != null ? memberweight : "No address");
//                                data_height.setText(memberheight != null ? memberheight : "No age");
//                                data_cur_height.setText(memberheight != null ? memberheight : "No age");
                                data_gender.setText(membergoal != null ? membergoal : "No gender");
                                data_cur_weight.setText(memberweight != null ? memberweight : "No gender");
                                if (memberimage != null) {
                                    Glide.with(MemberProfile.this)
                                            .load(memberimage)
                                            .into(data_image);
                                }

                                try {
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM");
                                    Date date = dateFormat.parse(dateStr); // Parse the date string

                                    // Subtract one day's worth of milliseconds (24 hours * 60 minutes * 60 seconds * 1000 milliseconds)
                                    long milliseconds = date.getTime() + (1 * 24 * 60 * 60 * 1000);

                                    long days = milliseconds / (1000 * 60 * 60 * 24); // Convert milliseconds to days
                                    float yValue = Float.parseFloat(memberweight); // Parse the weight value

                                    // Create an Entry with xValue as days and yValue as weight
                                    entries.add(new Entry(days, yValue));
                                } catch (ParseException e) {
                                    e.printStackTrace(); // Handle the parse exception
                                }

//                            show_Height.setText(memberheight != null ? memberheight : "No height");
//                            show_Weight.setText(memberweight != null ? memberweight : "No weight");

                                // Get the weight and height as strings from TextViews
                                String weightStr = memberweight != null ? memberweight :  "No weight";
                                String heightStr = memberheight;

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
                            setChart(entries);
//                    }
                        });
                    }
                });
            }
            private void setChart(List<Entry> entries) {
                LineDataSet dataSet = new LineDataSet(entries, "Weight Entries");
                dataSet.setColor(Color.BLUE);
                dataSet.setValueTextColor(Color.RED);
                dataSet.setLineWidth(2f);
                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);
                lineChart.getDescription().setEnabled(false);
                lineChart.setTouchEnabled(true);
                lineChart.setDragEnabled(true);
                lineChart.setScaleEnabled(true);
                lineChart.setPinchZoom(true);
                lineChart.setDrawGridBackground(false);
                XAxis xAxis = lineChart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        // Convert days to milliseconds
                        long millis = (long) value * 24 * 60 * 60 * 1000;
                        // Format date to exclude time
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM");
                        return dateFormat.format(new Date(millis));
                    }
                });
                YAxis yAxis = lineChart.getAxisLeft();
                yAxis.setGranularity(5f);
                lineChart.getAxisRight().setEnabled(false);
                lineChart.invalidate();
            }
            private float calculateBMIValue(float weight, float height) {
                return weight / (height * height);
            }
        }