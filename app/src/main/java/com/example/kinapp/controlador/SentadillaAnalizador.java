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

public class SentadillaAnalizador implements ImageAnalysis.Analyzer {

    private final PoseDetector detector;
    private final SentadillaListener listener;
    private boolean enPosicionBaja = false;
    private int repeticiones = 0;
    private long ultimoFeedback = 0;
    private long tiempoEnZonaMedia = 0;
    private boolean rutinaIniciada = false;

    public interface SentadillaListener {
        void onRepeticion(int reps);
        void onFeedback(String mensaje, boolean usarVoz);
        void onPoseDetectada(Pose pose, int width, int height);
    }

    public SentadillaAnalizador(SentadillaListener listener) {
        this.listener = listener;
        AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build();
        detector = PoseDetection.getClient(options);
    }

    public void iniciarRutina() {
        this.rutinaIniciada = true;
    }

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