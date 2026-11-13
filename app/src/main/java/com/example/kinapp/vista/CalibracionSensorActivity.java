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

/**
 * Pantalla de calibración previa a la rutina. Usa el acelerómetro del dispositivo para verificar
 * que el teléfono esté en posición vertical y, cuando lo está, abre {@CamaraGuiadaActivity}.
 */
public class CalibracionSensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor acelerometro;
    private TextView txtEstadoSensor;
    private ImageView btnVolver;
    private boolean calibrado = false;

    /**
     * Inicializa la vista y obtiene el acelerómetro mediante el SensorManager.
     *
     * @param savedInstanceState Estado guardado previo de la actividad.
     * @throws android.view.InflateException Si falla la carga del layout XML.
     */
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

    /**
     * Registra el listener del acelerómetro mientras la pantalla está visible y no se haya calibrado.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (acelerometro != null && !calibrado) {
            sensorManager.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Libera el listener del acelerómetro al salir de la pantalla para evitar consumo innecesario de batería.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    /**
     * Evalúa la gravedad medida en el eje Y: si está entre 8,5 y 10,5 m/s² el teléfono está vertical.
     * En ese caso muestra el mensaje de éxito, detiene el sensor y, tras 2 segundos, abre la cámara guiada.
     *
     * @param event Evento con los valores actuales del acelerómetro (m/s²).
     */
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

    /**
     * No se utiliza; la calibración no depende de la precisión del sensor.
     *
     * @param sensor   Sensor cuya precisión cambió.
     * @param accuracy Nuevo nivel de precisión.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}