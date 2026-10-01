package com.example.kinapp.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.kinapp.R;

public class PanelActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView txtNombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        txtNombreUsuario = findViewById(R.id.txt_nombreUsuario);

        String nombre = getIntent().getStringExtra("NOMBRE_USUARIO");
        if (nombre != null) {
            txtNombreUsuario.setText(nombre);
        }

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
}