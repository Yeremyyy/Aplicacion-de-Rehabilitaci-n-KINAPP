package com.example.kinapp.vista;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
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
import com.google.mlkit.vision.pose.Pose;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pantalla de la rutina guiada de sentadillas. Abre la cámara frontal con CameraX, envía los
 * fotogramas a {@SentadillaAnalizador} (ML Kit Pose Detection) y responde a sus eventos:
 * actualiza el contador, dibuja el esqueleto y da instrucciones por voz (TextToSpeech).
 * Al completar las 12 repeticiones abre {@ResumenRutinaActivity} con el tiempo,
 * las repeticiones y las correcciones de la sesión.
 */
public class CamaraGuiadaActivity extends AppCompatActivity implements SentadillaAnalizador.SentadillaListener {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private PreviewView vistaCamara;
    private PoseOverlayView vistaEsqueleto;
    private TextView txtFeedback;
    private TextView txtContador;
    private ImageView btnCerrar;
    private ImageView btnSonido;
    private ExecutorService cameraExecutor;
    private TextToSpeech tts;
    private boolean sonidoActivado = true;
    private ProcessCameraProvider cameraProviderGlobal;
    private SentadillaAnalizador analizador;

    private long inicioRutinaMs = 0;



    /**
     * Inicializa la vista, el motor de voz y los botones, y solicita el permiso de cámara si falta.
     *
     * @param savedInstanceState Estado guardado previo de la actividad.
     * @throws android.view.InflateException Si falla la carga del layout XML.
     */
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
        vistaEsqueleto = findViewById(R.id.vista_esqueleto);
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
                if (tts != null) tts.stop();
            }
        });

        if (allPermissionsGranted()) {
            iniciarCamara();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
        }
    }

    /**
     * Configura CameraX con vista previa y análisis de imágenes sobre la cámara frontal, crea el
     * analizador de sentadillas y lanza la cuenta regresiva. Si CameraX no puede inicializarse,
     * avisa al usuario con un Toast.
     */
    private void iniciarCamara() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProviderGlobal = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(vistaCamara.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                analizador = new SentadillaAnalizador(this);
                imageAnalysis.setAnalyzer(cameraExecutor, analizador);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                cameraProviderGlobal.unbindAll();
                cameraProviderGlobal.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

                iniciarCuentaRegresiva();

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error al iniciar cámara", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Muestra y pronuncia las instrucciones previas a la rutina (ponerse de lado, prepararse) durante
     * 10 segundos. Al terminar, registra el instante de inicio y habilita el conteo de repeticiones.
     */
    private void iniciarCuentaRegresiva() {
        txtFeedback.setText("Ponte de lado frente a la cámara...");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sonidoActivado && tts != null) {
                tts.speak("Ponte de lado frente a la cámara.", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }, 500);

        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long segundos = millisUntilFinished / 1000;

                if (segundos == 5) {
                    txtFeedback.setText("Prepárate...");
                    if (sonidoActivado && tts != null) {
                        tts.speak("Prepárate.", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                } else if (segundos > 0 && segundos <= 3) {
                    txtFeedback.setText("Comenzamos en... " + segundos);
                    if (sonidoActivado && tts != null) {
                        tts.speak(String.valueOf(segundos), TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
            }

            @Override
            public void onFinish() {
                txtFeedback.setText("¡Comienza tus sentadillas!");
                if (sonidoActivado && tts != null) {
                    tts.speak("¡Comienza!", TextToSpeech.QUEUE_FLUSH, null, null);
                }
                if (analizador != null) {
                    inicioRutinaMs = SystemClock.elapsedRealtime();
                    analizador.iniciarRutina();
                }
            }
        }.start();
    }

    /**
     * Callback del analizador: actualiza el contador en pantalla. Al llegar a 12 repeticiones libera
     * la cámara y, tras 2,5 s, abre el resumen enviando duración, repeticiones y correcciones.
     *
     * @param reps Número de repeticiones válidas acumuladas.
     */
    @Override
    public void onRepeticion(int reps) {
        runOnUiThread(() -> {
            txtContador.setText(reps + " / 12");

            if (reps < 12) {
                if (sonidoActivado && tts != null) tts.speak(String.valueOf(reps), TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                if (sonidoActivado && tts != null) tts.speak("¡Rutina completada! Excelente trabajo.", TextToSpeech.QUEUE_FLUSH, null, null);
                if (cameraProviderGlobal != null) {
                    cameraProviderGlobal.unbindAll();
                }
                txtFeedback.setText("¡Rutina Completada!");

                final long duracionSeg = inicioRutinaMs == 0 ? 0 : (SystemClock.elapsedRealtime() - inicioRutinaMs) / 1000;
                final int correcciones = analizador != null ? analizador.getCorrecciones() : 0;

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent intent = new Intent(CamaraGuiadaActivity.this, ResumenRutinaActivity.class);
                    intent.putExtra("duracion_seg", duracionSeg);
                    intent.putExtra("repeticiones", 12);
                    intent.putExtra("correcciones", correcciones);
                    startActivity(intent);
                    finish();
                }, 2500);
            }
        });
    }

    /**
     * Callback del analizador: muestra un mensaje de retroalimentación y, si corresponde, lo lee en voz alta.
     *
     * @param mensaje  Texto de retroalimentación para el usuario.
     * @param usarVoz  Indica si el mensaje debe reproducirse con TextToSpeech.
     */
    @Override
    public void onFeedback(String mensaje, boolean usarVoz) {
        runOnUiThread(() -> {
            txtFeedback.setText(mensaje);
            if (usarVoz && sonidoActivado && tts != null) {
                tts.speak(mensaje, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }


    /**
     * Callback del analizador: entrega la pose detectada a la vista que dibuja el esqueleto.
     *
     * @param pose        Puntos clave detectados por ML Kit.
     * @param imageWidth  Ancho del fotograma analizado, ya rotado.
     * @param imageHeight Alto del fotograma analizado, ya rotado.
     */
    @Override
    public void onPoseDetectada(Pose pose, int imageWidth, int imageHeight) {
        runOnUiThread(() -> vistaEsqueleto.setPose(pose, imageWidth, imageHeight, true));
    }

    /**
     * Comprueba si el permiso de cámara está concedido.
     *
     * @return true si la app tiene permiso para usar la cámara.
     */
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Recibe la respuesta del usuario al permiso de cámara: inicia la cámara si lo concedió; si no,
     * informa y cierra la pantalla.
     *
     * @param requestCode  Código de la solicitud de permisos.
     * @param permissions  Permisos solicitados.
     * @param grantResults Resultado de cada permiso (concedido o denegado).
     */
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

    /**
     * Libera los recursos: detiene el hilo de análisis y el motor de voz.
     */
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