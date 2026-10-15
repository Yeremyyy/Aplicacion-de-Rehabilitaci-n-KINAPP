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

    public interface SentadillaListener {
        void onRepeticion(int reps);
        void onFeedback(String mensaje, boolean usarVoz);
    }

    public SentadillaAnalizador(SentadillaListener listener) {
        this.listener = listener;
        AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build();
        detector = PoseDetection.getClient(options);
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            detector.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Pose>() {
                        @Override
                        public void onSuccess(Pose pose) {
                            procesarPose(pose);
                        }
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        }
    }

    private void procesarPose(Pose pose) {
        PoseLandmark cadera = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rodilla = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark tobillo = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (cadera == null || rodilla == null || tobillo == null) {
            return;
        }

        double angulo = calcularAngulo(cadera, rodilla, tobillo);
        long tiempoActual = System.currentTimeMillis();

        if (angulo > 160) {
            if (enPosicionBaja) {
                repeticiones++;
                listener.onRepeticion(repeticiones);
                listener.onFeedback("¡Excelente!", false);
                enPosicionBaja = false;
            }
        } else if (angulo < 90) {
            enPosicionBaja = true;
        } else if (angulo > 100 && angulo < 130 && !enPosicionBaja) {
            if (tiempoActual - ultimoFeedback > 3000) {
                listener.onFeedback("Baja un poco más la cadera", true);
                ultimoFeedback = tiempoActual;
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