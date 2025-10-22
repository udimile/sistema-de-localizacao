package com.example.sistemadelocalizao;

import android.content.Context;
import android.location.GnssStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class GNSSPlotActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_UPDATES = 1;
    private LocationManager locationManager;
    LocationListener locationListener;
    GnssStatus.Callback gnssCallback;
    GNSSView gnssView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gnssplot);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gnssView = findViewById(R.id.GNSSViewid);
        //startGnssUpdate();
    }
}