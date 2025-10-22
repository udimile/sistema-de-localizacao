package com.example.sistemadelocalizao;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private Button LocationBtn;
    private Button GNSSbtn;
    private Button GNSSPlotbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationBtn = findViewById(R.id.location_btn);
        GNSSbtn = findViewById(R.id.GNSS_btn);
        GNSSPlotbtn = findViewById(R.id.GNSSPlot_btn);

        LocationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            startActivity(intent);
        });

        GNSSbtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GNSSActivity.class);
            startActivity(intent);
        });

        GNSSPlotbtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GNSSPlotActivity.class);
            startActivity(intent);
        });
    }
}