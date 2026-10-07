package com.example.kinapp.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.kinapp.R;

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

        cargarDatosSimulados();

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

    private void cargarDatosSimulados() {
        txtAdherencia.setText("0%");
        txtRacha.setText("0");
        txtSesiones.setText("0");
        txtMesActual.setText("Mes actual");
        txtProgresoHombro.setText("0/15");
        pbHombro.setProgress(0);
        txtProgresoRodilla.setText("0/20");
        pbRodilla.setProgress(0);
    }
}