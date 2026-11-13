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
import java.util.Locale;

/**
 * Actividad encargada de mostrar el resumen analítico y métricas de la rutina finalizada.
 * Permite confirmar y persistir los resultados en la base de datos de forma asíncrona.
 */
public class ResumenRutinaActivity extends AppCompatActivity {

    private ImageView btnCerrar;
    private Button btnFinalizarGuardar;
    private TextView txtTiempo;
    private TextView txtEjercicios;
    private TextView txtPrecision;
    private TextView txtReporte;

    /**
     * Inicializa los componentes visuales, aplica insets de sistema y configura
     * los eventos de interacción para cerrar o guardar la rutina completada.
     *
     * @param savedInstanceState Estado guardado previo de la actividad.
     * @throws android.view.InflateException Si falla la carga del layout XML.
     */
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

        long duracionSeg = getIntent().getLongExtra("duracion_seg", 0);
        int repeticiones = getIntent().getIntExtra("repeticiones", 0);
        int correcciones = getIntent().getIntExtra("correcciones", 0);
        int precision = Math.max(0, 100 - correcciones * 10);

        txtTiempo.setText(String.format(Locale.US, "%02d:%02d", duracionSeg / 60, duracionSeg % 60));
        txtEjercicios.setText(repeticiones + " / 12");
        txtPrecision.setText(precision + "%");
        txtReporte.setText(correcciones == 0
                ? "Completaste las 12 sentadillas sin correcciones de profundidad. ¡Sigue así!"
                : "Completaste las 12 sentadillas. El sistema te pidió bajar más la cadera "
                + correcciones + (correcciones == 1 ? " vez" : " veces")
                + ". Intenta flexionar más las rodillas en la próxima rutina.");

        btnCerrar.setOnClickListener(v -> finish());

        btnFinalizarGuardar.setOnClickListener(v -> {
            btnFinalizarGuardar.setEnabled(false);
            ProgresoController.guardarRutinaCompletada(this, "Sentadillas", guardado -> {
                if (guardado) {
                    Toast.makeText(ResumenRutinaActivity.this, "Progreso guardado correctamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResumenRutinaActivity.this, ProgresoActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    btnFinalizarGuardar.setEnabled(true);
                    Toast.makeText(ResumenRutinaActivity.this, "Error al guardar el progreso", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}