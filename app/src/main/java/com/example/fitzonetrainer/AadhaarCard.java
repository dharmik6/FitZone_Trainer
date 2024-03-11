package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;

public class AadhaarCard extends AppCompatActivity {
    AppCompatButton btnFront , btnBack ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aadhaar_card);

        btnFront = findViewById(R.id.btn_front_side);
        btnBack = findViewById(R.id.btn_back_side);


    }
}