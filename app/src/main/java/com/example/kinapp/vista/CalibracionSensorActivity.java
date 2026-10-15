package com.example.kinapp.vista;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kinapp.R;

public class CalibracionSensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor acelerometro;
    private TextView txtEstadoSensor;
    private ImageView btnVolver;
    private boolean calibrado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibracion_sensor);

        txtEstadoSensor = findViewById(R.id.txt_estado_sensor);
        btnVolver = findViewById(R.id.btn_volver_calibracion);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (acelerometro != null && !calibrado) {
            sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (calibrado) return;

        float y = event.values[1];

        if (y > 8.5f && y < 10.5f) {
            calibrado = true;
            txtEstadoSensor.setText("¡Posición perfecta! Preparando cámara...");
            txtEstadoSensor.setTextColor(Color.parseColor("#4CAF50"));
            sensorManager.unregisterListener(this);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(CalibracionSensorActivity.this, CamaraGuiadaActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}