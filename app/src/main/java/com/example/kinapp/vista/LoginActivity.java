package com.example.kinapp.vista;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kinapp.R;
import com.example.kinapp.controlador.UsuarioController;

/**
 * Actividad encargada de gestionar el acceso de los usuarios a la aplicación.
 * Valida credenciales e implementa restauración automática de sesión de forma asíncrona.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etCorreo;
    private EditText etContrasena;
    private Button btnIniciarSesion;
    private Button btnCrearCuenta;

    /**
     * Inicializa la interfaz, comprueba preferencias compartidas para restauración
     * de sesión y configura los listeners de los botones de acceso y registro.
     *
     * @param savedInstanceState Estado guardado previo de la actividad.
     * @throws android.view.InflateException Si falla la carga del layout XML.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("SesionKinApp", MODE_PRIVATE);
        String correoGuardado = prefs.getString("correo_usuario", null);

        if (correoGuardado != null) {
            UsuarioController.restaurarSesion(LoginActivity.this, correoGuardado, exito -> {
                if (exito) {
                    Intent intent = new Intent(LoginActivity.this, PanelActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.apply();
                    setContentView(R.layout.activity_login);
                    inicializarVistas();
                }
            });
        } else {
            setContentView(R.layout.activity_login);
            inicializarVistas();
        }
    }

    /**
     * Enlaza las vistas del layout con los atributos locales y configura los eventos de clic.
     */
    private void inicializarVistas() {
        etCorreo = findViewById(R.id.txt_email);
        etContrasena = findViewById(R.id.txt_contrasena);
        btnIniciarSesion = findViewById(R.id.btn_iniciarSesion);
        btnCrearCuenta = findViewById(R.id.btn_CrearCuenta);

        btnCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = etCorreo.getText().toString().trim();
                String pass = etContrasena.getText().toString().trim();

                if (correo.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                UsuarioController.validarLogin(LoginActivity.this, correo, pass, isValid -> {
                    if (isValid) {
                        SharedPreferences prefs = getSharedPreferences("SesionKinApp", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("correo_usuario", correo);
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, PanelActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}