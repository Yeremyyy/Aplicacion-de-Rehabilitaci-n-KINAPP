package com.example.kinapp.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;

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
     *Inicializa la interfaz y comprueba si Firebase
     * conserva una sesión activa; si es así abre el panel, y si no muestra el formulario de acceso.
     *
     * @param savedInstanceState Estado guardado previo de la actividad.
     * @throws android.view.InflateException Si falla la carga del layout XML.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        UsuarioController.restaurarSesion(exito -> {
            if (exito) {
                irAlPanel();
            } else {
                setContentView(R.layout.activity_login);
                inicializarVistas();
            }
        });
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

                btnIniciarSesion.setEnabled(false);
                UsuarioController.validarLogin(correo, pass, (exito, mensaje) -> {
                    if (exito) {
                        Toast.makeText(LoginActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                        irAlPanel();
                    } else {
                        btnIniciarSesion.setEnabled(true);
                        Toast.makeText(LoginActivity.this, mensaje, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    /**
     * Abre el panel principal y cierra la pantalla de acceso.
     */
    private void irAlPanel() {
        startActivity(new Intent(LoginActivity.this, PanelActivity.class));
        finish();
    }
}