package com.example.kinapp.vista;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kinapp.R;
import com.example.kinapp.controlador.SentadillaAnalizador;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CamaraGuiadaActivity extends AppCompatActivity implements SentadillaAnalizador.SentadillaListener {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private PreviewView vistaCamara;
    private TextView txtFeedback;
    private TextView txtContador;
    private ImageView btnCerrar;
    private ImageView btnSonido;
    private ExecutorService cameraExecutor;
    private TextToSpeech tts;
    private boolean sonidoActivado = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camara_guiada);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vistaCamara = findViewById(R.id.vista_camara_fondo);
        txtFeedback = findViewById(R.id.txt_feedback_ia);
        txtContador = findViewById(R.id.txt_contador_reps);
        btnCerrar = findViewById(R.id.btn_cerrar_camara);
        btnSonido = findViewById(R.id.btn_sonido);

        cameraExecutor = Executors.newSingleThreadExecutor();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("es", "CL"));
            }
        });

        btnCerrar.setOnClickListener(v -> finish());

        btnSonido.setOnClickListener(v -> {
            sonidoActivado = !sonidoActivado;
            if (sonidoActivado) {
                btnSonido.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            } else {
                btnSonido.setImageResource(android.R.drawable.ic_lock_silent_mode);
                tts.stop();
            }
        });

        if (allPermissionsGranted()) {
            iniciarCamara();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void iniciarCamara() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(vistaCamara.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new SentadillaAnalizador(this));

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error al iniciar cámara", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onRepeticion(int reps) {
        runOnUiThread(() -> {
            txtContador.setText(reps + " / 12");
            if (sonidoActivado) {
                tts.speak(String.valueOf(reps), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    @Override
    public void onFeedback(String mensaje, boolean usarVoz) {
        runOnUiThread(() -> {
            txtFeedback.setText(mensaje);
            if (usarVoz && sonidoActivado) {
                tts.speak(mensaje, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                iniciarCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}