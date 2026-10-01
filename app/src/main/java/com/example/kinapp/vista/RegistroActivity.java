package com.example.kinapp.vista;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kinapp.R;
import com.example.kinapp.controlador.UsuarioController;

public class RegistroActivity extends AppCompatActivity {

    private ImageView btnVolver;
    private RadioGroup rgTipoUsuario;
    private RadioButton rbPaciente;
    private RadioButton rbEspecialista;
    private EditText etNombre;
    private EditText etFechaNac;
    private EditText etCorreo;
    private LinearLayout layoutEspecialista;
    private EditText etRut;
    private EditText etRegistroSis;
    private EditText etInstitucion;
    private EditText etPass;
    private EditText etPassConfirmar;
    private CheckBox chkTerminos;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        btnVolver = findViewById(R.id.btn_volver_registro);
        rgTipoUsuario = findViewById(R.id.rg_tipo_usuario);
        rbPaciente = findViewById(R.id.rb_paciente);
        rbEspecialista = findViewById(R.id.rb_especialista);
        etNombre = findViewById(R.id.input_nombre);
        etFechaNac = findViewById(R.id.input_fecha_nac);
        etCorreo = findViewById(R.id.input_correo_registro);
        layoutEspecialista = findViewById(R.id.layout_datos_especialista);
        etRut = findViewById(R.id.input_rut_especialista);
        etRegistroSis = findViewById(R.id.input_registro_sis);
        etInstitucion = findViewById(R.id.input_institucion);
        etPass = findViewById(R.id.input_pass_registro);
        etPassConfirmar = findViewById(R.id.input_pass_confirmar);
        chkTerminos = findViewById(R.id.chk_terminos);
        btnRegistrar = findViewById(R.id.btn_registrar);

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rgTipoUsuario.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_especialista) {
                    layoutEspecialista.setVisibility(View.VISIBLE);
                } else {
                    layoutEspecialista.setVisibility(View.GONE);
                }
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombre.getText().toString().trim();
                String correo = etCorreo.getText().toString().trim();
                String pass = etPass.getText().toString().trim();
                String passConf = etPassConfirmar.getText().toString().trim();

                if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || passConf.isEmpty()) {
                    Toast.makeText(RegistroActivity.this, "Llena los campos obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pass.equals(passConf)) {
                    Toast.makeText(RegistroActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!chkTerminos.isChecked()) {
                    Toast.makeText(RegistroActivity.this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (UsuarioController.registrarUsuario(correo, pass, nombre)) {
                    Toast.makeText(RegistroActivity.this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegistroActivity.this, "El correo ya está registrado", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}