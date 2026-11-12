package com.example.kinapp.vista;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.kinapp.R;
import com.example.kinapp.controlador.UsuarioController;
import com.example.kinapp.modelo.Usuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Actividad principal del panel de control de la aplicación.
 * Gestiona la navegación inferior, la visualización de rutinas asignadas
 * y el sistema de activación de notificaciones locales.
 */
public class PanelActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    private BottomNavigationView bottomNav;
    private TextView txtNombreUsuario;
    private TextView txtTituloRutina;
    private TextView txtInfoRutina;
    private ImageView btnNotificaciones;
    private Button btnComenzarTerapia;
    private LinearLayout btnCatalogo;
    private LinearLayout btnProgreso;
    private LinearLayout btnCamara;
    private LinearLayout btnMensajes;

    /**
     * Inicializa los componentes visuales, recupera la sesión del usuario actual
     * y configura los escuchadores de eventos para la navegación y las alertas.
     *
     * @param savedInstanceState Estado guardado previo de la actividad.
     * @throws android.view.InflateException Si ocurre un error al cargar el diseño XML.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);

        txtNombreUsuario = findViewById(R.id.txt_nombreUsuario);
        txtTituloRutina = findViewById(R.id.txt_tituloRutina);
        txtInfoRutina = findViewById(R.id.txt_infoRutina);
        btnNotificaciones = findViewById(R.id.btn_notificaciones);
        btnComenzarTerapia = findViewById(R.id.btn_comenzarTerapia);
        btnCatalogo = findViewById(R.id.btn_catalogo_grid);
        btnProgreso = findViewById(R.id.btn_progreso_grid);
        btnCamara = findViewById(R.id.btn_camara_grid);
        btnMensajes = findViewById(R.id.btn_mensajes_grid);

        txtTituloRutina.setText("Sentadillas");
        txtInfoRutina.setText("Cámara guiada · 12 reps");

        Usuario u = UsuarioController.getUsuarioActual();
        if (u != null) {
            txtNombreUsuario.setText(u.getNombre());
        }

        btnNotificaciones.setOnClickListener(v -> verificarYDispararNotificacion());
        btnCatalogo.setOnClickListener(v -> mostrarToastEnDesarrollo());
        btnMensajes.setOnClickListener(v -> mostrarToastEnDesarrollo());

        btnCamara.setOnClickListener(v -> {
            startActivity(new Intent(PanelActivity.this, CalibracionSensorActivity.class));
        });

        btnComenzarTerapia.setOnClickListener(v -> {
            startActivity(new Intent(PanelActivity.this, CalibracionSensorActivity.class));
        });

        btnProgreso.setOnClickListener(v -> {
            startActivity(new Intent(PanelActivity.this, ProgresoActivity.class));
            overridePendingTransition(0, 0);
        });

        bottomNav = findViewById(R.id.barra_navegacion_panel);
        bottomNav.setSelectedItemId(R.id.nav_inicio);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inicio) {
                return true;
            } else if (itemId == R.id.nav_rutinas) {
                startActivity(new Intent(PanelActivity.this, RutinasActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_progreso) {
                startActivity(new Intent(PanelActivity.this, ProgresoActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_perfil) {
                startActivity(new Intent(PanelActivity.this, PerfilActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    /**
     * Verifica los permisos de notificación en tiempo de ejecución y comprueba
     * las preferencias del usuario antes de emitir la alerta local.
     */
    private void verificarYDispararNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
                return;
            }
        }

        SharedPreferences prefs = getSharedPreferences("KinAppPrefs", MODE_PRIVATE);
        boolean notifActivas = prefs.getBoolean("notificaciones_activas", true);

        if (!notifActivas) {
            Toast.makeText(this, "Las notificaciones están desactivadas en tu perfil", Toast.LENGTH_SHORT).show();
            return;
        }

        dispararNotificacionLocal("Recordatorio KinApp", "Tienes pendiente tu rutina de rehabilitación para hoy.");
    }

    /**
     * Construye y emite una notificación local en el dispositivo utilizando canales configurados.
     *
     * @param titulo Título descriptivo de la notificación.
     * @param mensaje Contenido o cuerpo de la notificación.
     */
    private void dispararNotificacionLocal(String titulo, String mensaje) {
        String CHANNEL_ID = "kinapp_canal_rutinas";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Rutinas KinApp";
            String description = "Canal para recordatorios de rehabilitación kinésica";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notificaciones)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            notificationManager.notify(1, builder.build());
            Toast.makeText(this, "Notificación de prueba enviada", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Muestra un mensaje temporal indicando que una característica se encuentra en desarrollo.
     */
    private void mostrarToastEnDesarrollo() {
        Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show();
    }
}