package com.example.hua.coolweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WeatherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}
