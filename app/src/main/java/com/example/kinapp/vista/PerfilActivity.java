package com.example.kinapp.vista;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.kinapp.R;
import com.example.kinapp.controlador.UsuarioController;
import com.example.kinapp.modelo.Usuario;

public class PerfilActivity extends AppCompatActivity {

    private TextView txtNombre;
    private TextView txtCorreo;
    private TextView txtTelefonoValor;
    private TextView txtFechaValor;
    private LinearLayout btnEditarTelefono;
    private LinearLayout btnEspecialista;
    private LinearLayout btnNotificacionesRutina;
    private LinearLayout btnPrivacidad;
    private LinearLayout btnCambiarPass;
    private Button btnCerrarSesion;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        txtNombre = findViewById(R.id.txt_nombre_perfil);
        txtCorreo = findViewById(R.id.txt_correo_valor);
        txtTelefonoValor = findViewById(R.id.txt_telefono_valor);
        txtFechaValor = findViewById(R.id.txt_fecha_valor);
        btnEditarTelefono = findViewById(R.id.btn_editar_telefono);
        btnEspecialista = findViewById(R.id.btn_especialista);
        btnNotificacionesRutina = findViewById(R.id.btn_notificaciones_rutina);
        btnPrivacidad = findViewById(R.id.btn_privacidad);
        btnCambiarPass = findViewById(R.id.btn_cambiar_pass);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
        bottomNav = findViewById(R.id.barra_navegacion_perfil);

        cargarDatosPerfil();

        btnEditarTelefono.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PerfilActivity.this);
                builder.setTitle("Actualizar teléfono");
                final EditText input = new EditText(PerfilActivity.this);
                input.setInputType(InputType.TYPE_CLASS_PHONE);
                builder.setView(input);

                builder.setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoTel = input.getText().toString().trim();
                    if (!nuevoTel.isEmpty()) {
                        UsuarioController.actualizarTelefono(PerfilActivity.this, nuevoTel);
                        cargarDatosPerfil();
                        Toast.makeText(PerfilActivity.this, "Teléfono actualizado", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
                builder.show();
            }
        });

        btnCambiarPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PerfilActivity.this);
                builder.setTitle("Nueva contraseña");
                final EditText input = new EditText(PerfilActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                builder.setPositiveButton("Actualizar", (dialog, which) -> {
                    String nuevaPass = input.getText().toString().trim();
                    if (!nuevaPass.isEmpty()) {
                        UsuarioController.actualizarContrasena(PerfilActivity.this, nuevaPass);
                        Toast.makeText(PerfilActivity.this, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
                builder.show();
            }
        });

        btnEspecialista.setOnClickListener(v -> Toast.makeText(PerfilActivity.this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show());
        btnNotificacionesRutina.setOnClickListener(v -> Toast.makeText(PerfilActivity.this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show());
        btnPrivacidad.setOnClickListener(v -> Toast.makeText(PerfilActivity.this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show());

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
                startActivity(new Intent(PerfilActivity.this, PanelActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_rutinas) {
                startActivity(new Intent(PerfilActivity.this, RutinasActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_progreso) {
                startActivity(new Intent(PerfilActivity.this, ProgresoActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    private void cargarDatosPerfil() {
        Usuario u = UsuarioController.getUsuarioActual();
        if (u != null) {
            txtNombre.setText(u.getNombre());
            txtCorreo.setText(u.getCorreo());
            txtFechaValor.setText(u.getFechaNac());
            if (u.getTelefono() == null || u.getTelefono().isEmpty()) {
                txtTelefonoValor.setText("Añadir teléfono");
            } else {
                txtTelefonoValor.setText(u.getTelefono());
            }
        }
    }
}