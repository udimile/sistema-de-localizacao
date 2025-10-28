package com.example.sistemadelocalizao;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationListener;

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
        startGNSSUpdate();
    }

    protected void onStop() {
        super.onStop();
        stopGNSSUpdate();
    }

    public void startGNSSUpdate(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                }
            };

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    (float) 0,
                    (android.location.LocationListener) locationListener
            );

            gnssCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                    gnssView.newStatus(status);
                }
            };

            locationManager.registerGnssStatusCallback(gnssCallback, new Handler(Looper.getMainLooper()));

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_UPDATES);
        }
    }
    public void stopGNSSUpdate() {
        // desliga a GnssCallback
        if (gnssCallback != null) {
            try {
                locationManager.unregisterGnssStatusCallback(gnssCallback);
            } catch (Exception e) {
                e.printStackTrace(); // evita crash se já tiver sido desregistrado
            }
        }
        // desliga o location listener
        if (locationListener != null) {
            locationManager.removeUpdates((android.location.LocationListener) locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_UPDATES) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // O usuário acabou de dar a permissão
                startGNSSUpdate();
            } else {
                // O usuário não deu a permissão solicitada
                Toast.makeText(this, "Sem permissão para mostrar informações do sistema GNSS",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}