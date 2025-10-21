package com.example.sistemadelocalizao;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_UPDATES = 1;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private TextView locationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Button btnStart = findViewById(R.id.buttonStart);
        Button btnStop = findViewById(R.id.buttonStop);
        locationTextView = findViewById(R.id.textLocationInfo);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        btnStart.setOnClickListener(v -> startLocationUpdates());
        btnStop.setOnClickListener(v -> stopLocationUpdates());
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

            long timeInterval = 5 * 1000;
            locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, timeInterval)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        atualizaLocationTextView(location);
                    }
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback, null);

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_UPDATES);
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            atualizaLocationTextView(null);
            Toast.makeText(this, "Atualizações de localização paradas", Toast.LENGTH_SHORT).show();
        }
    }
    public void atualizaLocationTextView(Location location) {
        StringBuilder s = new StringBuilder("Dados da Última Localização:\n");

        if (location != null) {
            s.append("Latitude (G/M/S): ")
                    .append(Location.convert(location.getLatitude(), Location.FORMAT_SECONDS)).append("\n");
            s.append("Longitude (G/M/S): ")
                    .append(Location.convert(location.getLongitude(), Location.FORMAT_SECONDS)).append("\n");
            s.append("Altitude: ").append(location.getAltitude()).append("\n");
            s.append("Rumo (radianos): ").append(location.getBearing()).append("\n");
            s.append("Velocidade (m/s): ").append(location.getSpeed()).append("\n");
            s.append("Precisão (m): ").append(location.getAccuracy()).append("\n");
        } else {
            s.append("Nenhuma atualização disponível.");
        }

        locationTextView.setText(s.toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_UPDATES) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this,
                        "Sem permissão para mostrar atualizações da sua localização",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}