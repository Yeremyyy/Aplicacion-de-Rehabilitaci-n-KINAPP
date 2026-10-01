package com.example.kinapp.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kinapp.R;
import com.example.kinapp.controlador.UsuarioController;

public class LoginActivity extends AppCompatActivity {

    private EditText etCorreo;
    private EditText etContrasena;
    private Button btnIniciarSesion;
    private Button btnCrearCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

                if (UsuarioController.validarLogin(correo, pass)) {
                    Toast.makeText(LoginActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, PanelActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}