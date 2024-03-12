package com.example.fitzonetrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(() -> {

            SharedPreferences pref = getSharedPreferences("login", MODE_PRIVATE);
            Boolean check = pref.getBoolean("flag", false);

            Intent inext;
            if (check) {
                // User is logged in, navigate to home_page
                inext = new Intent(SplashScreen.this, MainActivity.class);
            } else {
                // User is not logged in, navigate to login_page
                inext = new Intent(SplashScreen.this, Login.class);
            }

            startActivity(inext);
            finish();
        }, 2001);

    }
}