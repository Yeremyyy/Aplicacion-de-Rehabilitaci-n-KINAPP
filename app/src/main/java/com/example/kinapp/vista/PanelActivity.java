package com.example.kinapp.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kinapp.R;
import com.example.kinapp.controlador.UsuarioController;
import com.example.kinapp.modelo.Usuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PanelActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView txtNombreUsuario;
    private ImageView btnNotificaciones;
    private Button btnComenzarTerapia;
    private LinearLayout btnCatalogo;
    private LinearLayout btnProgreso;
    private LinearLayout btnCamara;
    private LinearLayout btnMensajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        txtNombreUsuario = findViewById(R.id.txt_nombreUsuario);
        btnNotificaciones = findViewById(R.id.btn_notificaciones);
        btnComenzarTerapia = findViewById(R.id.btn_comenzarTerapia);
        btnCatalogo = findViewById(R.id.btn_catalogo_grid);
        btnProgreso = findViewById(R.id.btn_progreso_grid);
        btnCamara = findViewById(R.id.btn_camara_grid);
        btnMensajes = findViewById(R.id.btn_mensajes_grid);

        Usuario u = UsuarioController.getUsuarioActual();
        if (u != null) {
            txtNombreUsuario.setText(u.getNombre());
        }

        btnNotificaciones.setOnClickListener(v -> mostrarToastEnDesarrollo());
        btnCatalogo.setOnClickListener(v -> mostrarToastEnDesarrollo());
        btnMensajes.setOnClickListener(v -> mostrarToastEnDesarrollo());
        btnCamara.setOnClickListener(v -> mostrarToastEnDesarrollo());

        btnComenzarTerapia.setOnClickListener(v -> {
            startActivity(new Intent(PanelActivity.this, CalibracionSensorActivity.class));
        });

        btnProgreso.setOnClickListener(v -> {
            startActivity(new Intent(PanelActivity.this, ProgresoActivity.class));
            overridePendingTransition(0, 0);
        });

        bottomNav = findViewById(R.id.barra_navegacion_panel);
        bottomNav.setSelectedItemId(R.id.nav_inicio);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
                return true;
            } else if (itemId == R.id.nav_rutinas) {
                startActivity(new Intent(PanelActivity.this, RutinasActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_progreso) {
                startActivity(new Intent(PanelActivity.this, ProgresoActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                startActivity(new Intent(PanelActivity.this, PerfilActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void mostrarToastEnDesarrollo() {
        Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show();
    }
}