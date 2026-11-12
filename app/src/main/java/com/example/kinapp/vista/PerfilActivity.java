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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.kinapp.R;
import com.example.kinapp.controlador.UsuarioController;
import com.example.kinapp.modelo.Usuario;

/**
 * Actividad encargada de gestionar la información de perfil del usuario,
 * actualización de datos personales, preferencias del sistema y cierre de sesión.
 */
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
    private Switch switchNotificaciones;
    private BottomNavigationView bottomNav;

    /**
     * Inicializa los componentes de la interfaz de usuario del perfil, recupera los datos
     * almacenados y configura los escuchadores de eventos para preferencias y edición.
     *
     * @param savedInstanceState Estado guardado previo de la actividad.
     * @throws android.view.InflateException Si ocurre un error al cargar el diseño XML.
     */
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
        switchNotificaciones = findViewById(R.id.switch_notificaciones);
        bottomNav = findViewById(R.id.barra_navegacion_perfil);

        cargarDatosPerfil();

        SharedPreferences prefs = getSharedPreferences("KinAppPrefs", MODE_PRIVATE);
        boolean notifActivas = prefs.getBoolean("notificaciones_activas", true);
        if (switchNotificaciones != null) {
            switchNotificaciones.setChecked(notifActivas);

            switchNotificaciones.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = getSharedPreferences("KinAppPrefs", MODE_PRIVATE).edit();
                editor.putBoolean("notificaciones_activas", isChecked);
                editor.apply();

                if (isChecked) {
                    Toast.makeText(PerfilActivity.this, "Notificaciones habilitadas", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PerfilActivity.this, "Notificaciones silenciadas", Toast.LENGTH_SHORT).show();
                }
            });
        }

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
                        UsuarioController.actualizarTelefono(PerfilActivity.this, nuevoTel, exito -> {
                            if (exito) {
                                cargarDatosPerfil();
                                Toast.makeText(PerfilActivity.this, "Teléfono actualizado", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PerfilActivity.this, "Error al actualizar teléfono", Toast.LENGTH_SHORT).show();
                            }
                        });
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
                        UsuarioController.actualizarContrasena(PerfilActivity.this, nuevaPass, exito -> {
                            if (exito) {
                                Toast.makeText(PerfilActivity.this, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(PerfilActivity.this, "Error al actualizar contraseña", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
                builder.show();
            }
        });

        btnCerrarSesion.setOnClickListener(v -> {
            UsuarioController.cerrarSesion();
            getSharedPreferences("SesionKinApp", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        bottomNav.setSelectedItemId(R.id.nav_perfil);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
                startActivity(new Intent(PerfilActivity.this, PanelActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_rutinas) {
                startActivity(new Intent(PerfilActivity.this, RutinasActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_progreso) {
                startActivity(new Intent(PerfilActivity.this, ProgresoActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                return true;
            }
            return false;
        });
    }

    /**
     * Carga los datos actuales del usuario autenticado y los asigna a los componentes de texto de la vista.
     */
    private void cargarDatosPerfil() {
        Usuario u = UsuarioController.getUsuarioActual();
        if (u != null) {
            txtNombre.setText(u.getNombre());
            txtCorreo.setText(u.getCorreo());
            txtTelefonoValor.setText(u.getTelefono() != null && !u.getTelefono().isEmpty() ? u.getTelefono() : "No especificado");
            txtFechaValor.setText(u.getFechaNac() != null && !u.getFechaNac() .isEmpty() ? u.getFechaNac() : "No especificada");
        }
    }
}