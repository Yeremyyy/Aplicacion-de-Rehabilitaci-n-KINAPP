package com.example.kinapp.vista;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kinapp.R;
import com.example.kinapp.controlador.ProgresoController;

public class ResumenRutinaActivity extends AppCompatActivity {

    private ImageView btnCerrar;
    private Button btnFinalizarGuardar;
    private TextView txtTiempo;
    private TextView txtEjercicios;
    private TextView txtPrecision;
    private TextView txtReporte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resumen_rutina);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header_resumen), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, v.getPaddingBottom());
            return insets;
        });

        btnCerrar = findViewById(R.id.btn_cerrar_resumen);
        btnFinalizarGuardar = findViewById(R.id.btn_finalizar_guardar);
        txtTiempo = findViewById(R.id.txt_resumen_tiempo);
        txtEjercicios = findViewById(R.id.txt_resumen_ejercicios);
        txtPrecision = findViewById(R.id.txt_resumen_precision);
        txtReporte = findViewById(R.id.txt_reporte_ia);

        txtTiempo.setText("01:30");
        txtEjercicios.setText("12 / 12");
        txtPrecision.setText("98%");
        txtReporte.setText("Tuviste un excelente rango de movimiento en las sentadillas. Mantuviste la cadera estable y lograste la profundidad adecuada. ¡Sigue así!");

        btnCerrar.setOnClickListener(v -> finish());

        btnFinalizarGuardar.setOnClickListener(v -> {
            boolean guardado = ProgresoController.guardarRutinaCompletada(this, "Sentadillas");
            if (guardado) {
                Toast.makeText(this, "Progreso guardado correctamente", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResumenRutinaActivity.this, ProgresoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Error al guardar el progreso", Toast.LENGTH_SHORT).show();
            }
        });
    }
}