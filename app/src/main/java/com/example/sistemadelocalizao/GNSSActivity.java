package com.example.sistemadelocalizao;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class GNSSActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_UPDATES =1;
    private LocationManager locationManager;
    LocationListener locationListener;
    GnssStatus.Callback gnssCallback;
    private TextView textViewLocation;
    private TextView textViewGNSS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gnss);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        textViewLocation = findViewById(R.id.textViewLocationManager);
        textViewGNSS = findViewById(R.id.textViewGNSS);

        Button btnStartGNSS = findViewById(R.id.buttonStartGNSS);
        Button btnStopGNSS = findViewById(R.id.buttonStopGNSS);

        btnStartGNSS.setOnClickListener(v -> startGNSSUpdates());
        btnStopGNSS.setOnClickListener(v -> stopGNSSUpdates());

    }

    public void startGNSSUpdates(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    atualizaLocationTextView(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) { }

                @Override
                public void onProviderEnabled(@NonNull String provider) { }

                @Override
                public void onProviderDisabled(@NonNull String provider) { }
            };

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    0,
                    locationListener
            );

            gnssCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                    super.onSatelliteStatusChanged(status);
                    atualizaGNSSTextView(status);
                }
            };

            locationManager.registerGnssStatusCallback(gnssCallback, new Handler(Looper.getMainLooper()));

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_UPDATES);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_UPDATES) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGNSSUpdates();
            } else {
                Toast.makeText(this,
                        "Sem permissão para mostrar atualizações do sistema GNSS",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void stopGNSSUpdates() {
        if (gnssCallback != null) {
            try {
                locationManager.unregisterGnssStatusCallback(gnssCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }

        atualizaGNSSTextView(null);
        atualizaLocationTextView(null);
    }

    private void atualizaGNSSTextView(GnssStatus status) {

        StringBuilder sb = new StringBuilder();
        int count = status.getSatelliteCount();

        sb.append("Satélites visíveis: " + count).append("\n");

        for (int i=0; i < count; i++) {
            int svid = status.getSvid(i);
            float azimuth = status.getAzimuthDegrees(i);
            float elevation = status.getElevationDegrees(i);
            boolean used = status.usedInFix(i);
            sb.append("SVID").append(svid)
                    .append(" | Azimute: ").append(azimuth).append("°")
                    .append(" | Elevação").append(elevation).append("°")
                    .append(" | Usado no fix: ").append(used)
                    .append("\n");
        }
        textViewGNSS.setText(sb.toString());
    }
    private void atualizaLocationTextView(Location location) {
        TextView locationTextView = findViewById(R.id.textViewLocationManager);
        if (location == null) {
            String s = "Dados de Localização não disponíveis";
            locationTextView.setText(s);
            return;
        }

        String s = "Dados da última localização: \n";
        if (location != null) {
            s += "Latitude: " + location.getLatitude() + "\n";
            s += "Longitude: " + location.getLongitude() + "\n";
            s += "Altitude: " + location.getAltitude() + "\n";
            s += "Rumo (radianos): " + location.getBearing() + "\n";
            s += "Velocidade (m/s): " + location.getSpeed() + "\n";
            s += "Precisão (m): " + location.getAccuracy() + "\n";
        }

        locationTextView.setText(s);
    }
}
