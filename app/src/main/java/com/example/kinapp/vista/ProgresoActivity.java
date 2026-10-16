package com.example.kinapp.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kinapp.controlador.ProgresoController;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.kinapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProgresoActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private TextView txtAdherencia;
    private TextView txtRacha;
    private TextView txtSesiones;
    private TextView txtMesActual;
    private TextView txtProgresoHombro;
    private ProgressBar pbHombro;
    private TextView txtProgresoRodilla;
    private ProgressBar pbRodilla;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso);

        btnVolver = findViewById(R.id.btn_volver_progreso);
        txtAdherencia = findViewById(R.id.txt_adherencia_val);
        txtRacha = findViewById(R.id.txt_racha_val);
        txtSesiones = findViewById(R.id.txt_sesiones_val);
        txtMesActual = findViewById(R.id.txt_mes_actual);
        txtProgresoHombro = findViewById(R.id.txt_progreso_hombro);
        pbHombro = findViewById(R.id.pb_hombro);
        txtProgresoRodilla = findViewById(R.id.txt_progreso_rodilla);
        pbRodilla = findViewById(R.id.pb_rodilla);
        bottomNav = findViewById(R.id.barra_navegacion_progreso);

        cargarDatosReales();

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProgresoActivity.this, PanelActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        bottomNav.setSelectedItemId(R.id.nav_progreso);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_progreso) {
                return true;
            } else if (itemId == R.id.nav_inicio) {
                startActivity(new Intent(ProgresoActivity.this, PanelActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_rutinas) {
                startActivity(new Intent(ProgresoActivity.this, RutinasActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_perfil) {
                startActivity(new Intent(ProgresoActivity.this, PerfilActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void cargarDatosReales() {
        int sesionesTotales = ProgresoController.obtenerSesionesTotales(this);
        int diasRacha = ProgresoController.obtenerDiasDeRacha(this);
        int adherencia = ProgresoController.obtenerPorcentajeAdherenciaMensual(this);

        txtSesiones.setText(String.valueOf(sesionesTotales));
        txtRacha.setText(String.valueOf(diasRacha));
        txtAdherencia.setText(adherencia + "%");

        String nombreMes = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES")).format(new Date());
        String mesCapitalizado = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
        txtMesActual.setText(mesCapitalizado);

        int sentadillasRestantes = ProgresoController.obtenerProgresoSentadillas(this);
        txtProgresoRodilla.setText(sentadillasRestantes + "/20");
        pbRodilla.setMax(20);
        pbRodilla.setProgress(sentadillasRestantes);

        txtProgresoHombro.setText("0/15");
        pbHombro.setMax(15);
        pbHombro.setProgress(0);
    }
}