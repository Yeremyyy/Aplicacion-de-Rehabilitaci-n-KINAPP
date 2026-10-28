package com.example.kinapp.controlador;

import android.annotation.SuppressLint;
import android.media.Image;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

/**
 * Aqui esta el motor de Inteligencia Artificial y Visión Computacional para la aplicación.
 * Implementa ImageAnalysis.Analyzer (CameraX) para procesar fotogramas en tiempo real.
 * Utiliza Google ML Kit Pose Detection para aislar el esqueleto del paciente, calcular
 * ángulos articulares y proveer feedback de voz si la técnica biomecánica es incorrecta.
 */
public class SentadillaAnalizador implements ImageAnalysis.Analyzer {

    private final PoseDetector detector;
    private final SentadillaListener listener;
    private boolean enPosicionBaja = false;
    private int repeticiones = 0;
    private long ultimoFeedback = 0;
    private long tiempoEnZonaMedia = 0;
    private boolean rutinaIniciada = false;

    /**
     * Es la interfaz de comunicación (Callback) utilizada para enviar los resultados
     * del análisis biomecánico de vuelta a la Capa Vista de CamaraGuiadaActivity.
     */
    public interface SentadillaListener {
        void onRepeticion(int reps);
        void onFeedback(String mensaje, boolean usarVoz);
        void onPoseDetectada(Pose pose, int width, int height);
    }

    /**
     * Inicializa el analizador configurando el modelo de ML Kit en modo de alta precisión (STREAM_MODE).
     *
     * @param listener Instancia de la interfaz que escuchará y manejará los eventos de la cámara en CamaraGuiadaActivity.
     */
    public SentadillaAnalizador(SentadillaListener listener) {
        this.listener = listener;
        AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build();
        detector = PoseDetection.getClient(options);
    }

    /**
     * Libera el bloqueo de análisis de postura. Permite que el analizador comience a procesar
     * fotogramas solo después de que termine la cuenta regresiva propuesta de 10sg aprox.
     */
    public void iniciarRutina() {
        this.rutinaIniciada = true;
    }

    /**
     * Es un metodo central del motor ImageAnalysis. Recibe un flujo continuo de fotogramas llamado Frames
     * directamente desde el hardware de la cámara, los convierte a InputImage y los envía
     * al modelo de ML Kit para detectar la postura humana.
     *
     * @param imageProxy Objeto de bajo nivel que envuelve el frame capturado. Debe cerrarse para evitar fugas de memoria.
     */

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            int rotation = imageProxy.getImageInfo().getRotationDegrees();
            int width = (rotation == 90 || rotation == 270) ? mediaImage.getHeight() : mediaImage.getWidth();
            int height = (rotation == 90 || rotation == 270) ? mediaImage.getWidth() : mediaImage.getHeight();

            InputImage image = InputImage.fromMediaImage(mediaImage, rotation);
            detector.process(image)
                    .addOnSuccessListener(pose -> {
                        listener.onPoseDetectada(pose, width, height);
                        procesarPose(pose);
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        }
    }


    /**
     * Es el núcleo del algoritmo biomecánico. Extrae las coordenadas espaciales (X, Y) de cadera, rodilla y tobillo.
     * Implementa un filtro de seguridad (Likelihood > 0.6f) para ignorar detecciones con baja confiabilidad, basicamente es la probabilidad de confianza,
     * evitando falsos positivos por mala iluminación u oclusión.
     * Además, aplica temporizadores de estabilización para evitar bombardear al usuario con feedback de voz repetitivo.
     *
     * @param pose Objeto devuelto por ML Kit que contiene los 33 puntos clave (Landmarks) del esqueleto.
     */
    private void procesarPose(Pose pose) {
        if (!rutinaIniciada || repeticiones >= 12) return;

        PoseLandmark cadera = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rodilla = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark tobillo = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (cadera == null || rodilla == null || tobillo == null) return;

        if (cadera.getInFrameLikelihood() < 0.6f ||
                rodilla.getInFrameLikelihood() < 0.6f ||
                tobillo.getInFrameLikelihood() < 0.6f) {
            return;
        }

        double angulo = calcularAngulo(cadera, rodilla, tobillo);
        long tiempoActual = System.currentTimeMillis();

        if (angulo > 150) {
            tiempoEnZonaMedia = 0;
            if (enPosicionBaja) {
                repeticiones++;
                listener.onRepeticion(repeticiones);
                listener.onFeedback("¡Excelente!", false);
                enPosicionBaja = false;
            }
        } else if (angulo < 110) {
            tiempoEnZonaMedia = 0;
            enPosicionBaja = true;
        } else if (angulo >= 110 && angulo <= 140 && !enPosicionBaja) {
            if (tiempoEnZonaMedia == 0) {
                tiempoEnZonaMedia = tiempoActual;
            } else if (tiempoActual - tiempoEnZonaMedia > 1500) {
                if (tiempoActual - ultimoFeedback > 4000) {
                    listener.onFeedback("Baja un poco más la cadera", true);
                    ultimoFeedback = tiempoActual;
                }
            }
        }
    }

    /**
     * Calcula geométricamente el ángulo interno formado por tres articulaciones clave.
     * Utiliza la función arcotangente (atan2) para determinar la inclinación en un plano 2D
     * y aplica corrección de valor absoluto para asegurar que el ángulo resultante siempre esté entre 0° y 180°.
     *
     * @param p1 Punto de origen superior (ej. Cadera).
     * @param p2 Punto de vértice o pivote (ej. Rodilla).
     * @param p3 Punto de origen inferior (ej. Tobillo).
     * @return double Ángulo articular expresado en grados.
     */
    private double calcularAngulo(PoseLandmark p1, PoseLandmark p2, PoseLandmark p3) {
        double angulo = Math.toDegrees(
                Math.atan2(p3.getPosition().y - p2.getPosition().y, p3.getPosition().x - p2.getPosition().x) -
                        Math.atan2(p1.getPosition().y - p2.getPosition().y, p1.getPosition().x - p2.getPosition().x)
        );
        angulo = Math.abs(angulo);
        if (angulo > 180) {
            angulo = 360.0 - angulo;
        }
        return angulo;
    }
}