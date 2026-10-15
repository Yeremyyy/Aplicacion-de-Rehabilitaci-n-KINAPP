package com.example.kinapp.vista;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

public class PoseOverlayView extends View {
    private Pose pose;
    private Paint paintPunto;
    private Paint paintLinea;
    private int imageWidth;
    private int imageHeight;

    public PoseOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paintPunto = new Paint();
        paintPunto.setColor(Color.RED);
        paintPunto.setStyle(Paint.Style.FILL);

        paintLinea = new Paint();
        paintLinea.setColor(Color.GREEN);
        paintLinea.setStrokeWidth(10f);
    }

    public void setPose(Pose pose, int imageWidth, int imageHeight) {
        this.pose = pose;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        invalidate();
    }

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

        if (hombro != null && cadera != null && rodilla != null && tobillo != null) {
            float hX = hombro.getPosition().x * scale + offsetX;
            float hY = hombro.getPosition().y * scale + offsetY;
            float cX = cadera.getPosition().x * scale + offsetX;
            float cY = cadera.getPosition().y * scale + offsetY;
            float rX = rodilla.getPosition().x * scale + offsetX;
            float rY = rodilla.getPosition().y * scale + offsetY;
            float tX = tobillo.getPosition().x * scale + offsetX;
            float tY = tobillo.getPosition().y * scale + offsetY;

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