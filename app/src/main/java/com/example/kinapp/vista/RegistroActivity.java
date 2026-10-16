package com.example.kinapp.vista;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.kinapp.R;
import com.example.kinapp.controlador.UsuarioController;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegistroActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 100;
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
    private ImageView btnObtenerUbicacion;
    private TextView txtUbicacionDetectada;
    private String ubicacionFinal = "No especificada";

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
        btnObtenerUbicacion = findViewById(R.id.btn_obtener_ubicacion);
        txtUbicacionDetectada = findViewById(R.id.txt_ubicacion_detectada);

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

        etFechaNac.setFocusable(false);
        etFechaNac.setClickable(true);
        etFechaNac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calc = Calendar.getInstance();
                int year = calc.get(Calendar.YEAR);
                int month = calc.get(Calendar.MONTH);
                int day = calc.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(RegistroActivity.this, (view, y, m, d) -> {
                    String date = d + "/" + (m + 1) + "/" + y;
                    etFechaNac.setText(date);
                }, year, month, day);
                dialog.show();
            }
        });

        btnObtenerUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solicitarUbicacion();
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombre.getText().toString().trim();
                String correo = etCorreo.getText().toString().trim();
                String pass = etPass.getText().toString().trim();
                String passConf = etPassConfirmar.getText().toString().trim();
                String fecha = etFechaNac.getText().toString().trim();

                if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || passConf.isEmpty() || fecha.isEmpty()) {
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

                if (UsuarioController.registrarUsuario(RegistroActivity.this, correo, pass, nombre, fecha, ubicacionFinal)) {
                    Toast.makeText(RegistroActivity.this, "Cuenta creada con éxito en " + ubicacionFinal, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegistroActivity.this, "El correo ya está registrado", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void solicitarUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            obtenerCoordenadas();
        }
    }

    @SuppressLint("MissingPermission")
    private void obtenerCoordenadas() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                traducirCoordenadas(location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "Buscando satélites GPS. Intenta de nuevo en unos segundos.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Por favor, enciende el GPS de tu teléfono", Toast.LENGTH_LONG).show();
        }
    }

    private void traducirCoordenadas(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocation(lat, lon, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                Address direccion = direcciones.get(0);
                ubicacionFinal = direccion.getLocality() + ", " + direccion.getCountryName();
                txtUbicacionDetectada.setText(ubicacionFinal);
                Toast.makeText(this, "Ubicación detectada: " + ubicacionFinal, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            txtUbicacionDetectada.setText("Lat: " + lat + ", Lon: " + lon);
            ubicacionFinal = "Lat: " + lat + ", Lon: " + lon;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerCoordenadas();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}