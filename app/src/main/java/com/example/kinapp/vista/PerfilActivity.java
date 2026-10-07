package com.example.kinapp.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.kinapp.R;
import com.example.kinapp.controlador.UsuarioController;
import com.example.kinapp.modelo.Usuario;

public class PerfilActivity extends AppCompatActivity {

    private TextView txtNombre;
    private TextView txtCorreo;
    private Button btnCerrarSesion;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        txtNombre = findViewById(R.id.txt_nombre_perfil);
        txtCorreo = findViewById(R.id.txt_correo_valor);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
        bottomNav = findViewById(R.id.barra_navegacion_perfil);

        Usuario u = UsuarioController.getUsuarioActual();
        if (u != null) {
            txtNombre.setText(u.getNombre());
            txtCorreo.setText(u.getCorreo());
        }

        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsuarioController.cerrarSesion();

                SharedPreferences prefs = getSharedPreferences("SesionKinApp", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();

                Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        bottomNav.setSelectedItemId(R.id.nav_perfil);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_perfil) {
                return true;
            } else if (itemId == R.id.nav_inicio) {
                Intent intent = new Intent(PerfilActivity.this, PanelActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_rutinas) {
                Intent intent = new Intent(PerfilActivity.this, RutinasActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_progreso) {
                Intent intent = new Intent(PerfilActivity.this, ProgresoActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}