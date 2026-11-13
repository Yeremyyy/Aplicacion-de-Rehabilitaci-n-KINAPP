package com.example.kinapp.vista;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

/**
 * Vista personalizada que se superpone a la vista previa de la cámara y dibuja el esqueleto 2D
 * del lado izquierdo del cuerpo (hombro, cadera, rodilla y tobillo) a partir de la pose de ML Kit.
 */
public class PoseOverlayView extends View {
    private Pose pose;
    private Paint paintPunto;
    private Paint paintLinea;
    private int imageWidth;
    private int imageHeight;
    private boolean isFrontCamera = true;

    /**
     * Crea la vista y configura los pinceles: puntos rojos para las articulaciones y líneas verdes
     * para los segmentos.
     *
     * @param context Contexto de la aplicación.
     * @param attrs   Atributos definidos en el layout XML.
     */
    public PoseOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintPunto = new Paint();
        paintPunto.setColor(Color.RED);
        paintPunto.setStyle(Paint.Style.FILL);

        paintLinea = new Paint();
        paintLinea.setColor(Color.GREEN);
        paintLinea.setStrokeWidth(10f);
    }

    /**
     * Actualiza la pose a dibujar y solicita repintar la vista.
     *
     * @param pose          Pose detectada por ML Kit en el último fotograma.
     * @param imageWidth    Ancho del fotograma analizado, ya rotado.
     * @param imageHeight   Alto del fotograma analizado, ya rotado.
     * @param isFrontCamera true si la imagen viene de la cámara frontal (se refleja horizontalmente).
     */
    public void setPose(Pose pose, int imageWidth, int imageHeight, boolean isFrontCamera) {
        this.pose = pose;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.isFrontCamera = isFrontCamera;
        invalidate();
    }

    /**
     * Convierte una coordenada X de la imagen a la de la pantalla, reflejándola si la cámara es frontal.
     *
     * @param x       Coordenada X en el espacio de la imagen.
     * @param scale   Factor de escala imagen-pantalla.
     * @param offsetX Desplazamiento horizontal por el recorte de la vista previa.
     * @return float Coordenada X en píxeles de la vista.
     */
    private float translateX(float x, float scale, float offsetX) {
        float scaledX = x * scale + offsetX;
        return isFrontCamera ? getWidth() - scaledX : scaledX;
    }

    /**
     * Convierte una coordenada Y de la imagen a la de la pantalla.
     *
     * @param y       Coordenada Y en el espacio de la imagen.
     * @param scale   Factor de escala imagen-pantalla.
     * @param offsetY Desplazamiento vertical por el recorte de la vista previa.
     * @return float Coordenada Y en píxeles de la vista.
     */
    private float translateY(float y, float scale, float offsetY) {
        return y * scale + offsetY;
    }

    /**
     * Dibuja el esqueleto sobre el lienzo, escalando las coordenadas para que coincidan con la vista
     * previa (recorte central). Solo dibuja si la confianza de la cadera supera 0,5.
     *
     * @param canvas Lienzo sobre el que se dibujan las líneas y los puntos.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pose == null || pose.getAllPoseLandmarks().isEmpty() || imageWidth == 0) return;

        float scaleX = (float) getWidth() / imageWidth;
        float scaleY = (float) getHeight() / imageHeight;
        float scale = Math.max(scaleX, scaleY);

        float offsetX = (getWidth() - (imageWidth * scale)) / 2.0f;
        float offsetY = (getHeight() - (imageHeight * scale)) / 2.0f;

        PoseLandmark hombro = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark cadera = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rodilla = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark tobillo = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);

        if (hombro != null && cadera != null && rodilla != null && tobillo != null &&
                cadera.getInFrameLikelihood() > 0.5f) {

            float hX = translateX(hombro.getPosition().x, scale, offsetX);
            float hY = translateY(hombro.getPosition().y, scale, offsetY);
            float cX = translateX(cadera.getPosition().x, scale, offsetX);
            float cY = translateY(cadera.getPosition().y, scale, offsetY);
            float rX = translateX(rodilla.getPosition().x, scale, offsetX);
            float rY = translateY(rodilla.getPosition().y, scale, offsetY);
            float tX = translateX(tobillo.getPosition().x, scale, offsetX);
            float tY = translateY(tobillo.getPosition().y, scale, offsetY);

            canvas.drawLine(hX, hY, cX, cY, paintLinea);
            canvas.drawLine(cX, cY, rX, rY, paintLinea);
            canvas.drawLine(rX, rY, tX, tY, paintLinea);

            canvas.drawCircle(hX, hY, 15f, paintPunto);
            canvas.drawCircle(cX, cY, 15f, paintPunto);
            canvas.drawCircle(rX, rY, 15f, paintPunto);
            canvas.drawCircle(tX, tY, 15f, paintPunto);
        }
    }
}