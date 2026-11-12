package com.example.kinapp.controlador;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kinapp.modelo.RutinaContract.RutinaEntry;
import com.example.kinapp.modelo.Usuario;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Controlador encargado de gestionar las métricas, rachas y adherencia de los pacientes.
 * Centraliza las consultas de lectura y escritura hacia la tabla de historial de rutinas,
 * utilizando la clase RutinaContract para mantener el desacoplamiento de cadenas.
 */
public class ProgresoController {

    /**
     * Interfaz de callback para retornar resultados booleanos de operaciones asíncronas.
     */
    public interface OnResultCallback {
        void onResult(boolean success);
    }

    /**
     * Interfaz de callback para retornar resultados numéricos de operaciones asíncronas.
     */
    public interface OnIntCallback {
        void onResult(int value);
    }

    /**
     * Construye la tabla de historial de rutinas en caso de que no exista.
     * Utiliza las constantes del contrato para armar la sentencia de forma segura.
     *
     * @param context Contexto de la aplicación para instanciar DatabaseHelper.
     * @throws android.database.SQLException Si falla la ejecución de la sentencia SQL de creación.
     */
    public static void inicializarTabla(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String createTable = "CREATE TABLE IF NOT EXISTS " + RutinaEntry.TABLE_NAME + " (" +
                RutinaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RutinaEntry.COLUMN_CORREO + " TEXT, " +
                RutinaEntry.COLUMN_FECHA + " TEXT, " +
                RutinaEntry.COLUMN_TIPO + " TEXT)";

        db.execSQL(createTable);
        db.close();
    }

    /**
     * Registra una nueva sesión completada en la base de datos asociada al usuario actual en segundo plano.
     * Captura la fecha exacta del sistema operativo al momento de finalizar el ejercicio.
     *
     * @param context    Contexto de la vista que invoca el guardado.
     * @param tipoRutina Nombre o categoría de la rutina ejecutada.
     * @param callback   Callback para retornar el resultado de la inserción en el hilo principal.
     * @throws android.database.SQLException Si falla la inserción en SQLite.
     */
    public static void guardarRutinaCompletada(Context context, String tipoRutina, OnResultCallback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            inicializarTabla(context);
            Usuario u = UsuarioController.getUsuarioActual();
            if (u == null) {
                AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(false));
                return;
            }

            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            ContentValues values = new ContentValues();
            values.put(RutinaEntry.COLUMN_CORREO, u.getCorreo());
            values.put(RutinaEntry.COLUMN_FECHA, fechaHoy);
            values.put(RutinaEntry.COLUMN_TIPO, tipoRutina);

            long result = db.insert(RutinaEntry.TABLE_NAME, null, values);
            db.close();

            boolean exito = (result != -1);
            AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(exito));
        });
    }

    /**
     * Calcula la cantidad de días únicos en los que el usuario ha realizado al menos una rutina en segundo plano.
     * Utiliza la cláusula SQL 'COUNT DISTINCT' sobre la columna fecha.
     *
     * @param context  Contexto de la aplicación.
     * @param callback Callback para retornar el valor numérico de la racha en el hilo principal.
     * @throws android.database.SQLException Si falla la consulta al cursor de SQLite.
     */
    public static void obtenerDiasDeRacha(Context context, OnIntCallback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            inicializarTabla(context);
            Usuario u = UsuarioController.getUsuarioActual();
            if (u == null) {
                AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(0));
                return;
            }

            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String query = "SELECT COUNT(DISTINCT " + RutinaEntry.COLUMN_FECHA + ") FROM " +
                    RutinaEntry.TABLE_NAME + " WHERE " + RutinaEntry.COLUMN_CORREO + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{u.getCorreo()});

            int rachaCalculada = 0;
            if (cursor.moveToFirst()) {
                rachaCalculada = cursor.getInt(0);
            }
            cursor.close();
            db.close();

            final int rachaFinal = rachaCalculada;
            AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(rachaFinal));
        });
    }

    /**
     * Obtiene el volumen total histórico de sesiones de kinesiología completadas por el paciente en segundo plano.
     *
     * @param context  Contexto de la aplicación.
     * @param callback Callback para retornar el total de sesiones en el hilo principal.
     * @throws android.database.SQLException Si falla la consulta al cursor de SQLite.
     */
    public static void obtenerSesionesTotales(Context context, OnIntCallback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Usuario u = UsuarioController.getUsuarioActual();
            if (u == null) {
                AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(0));
                return;
            }
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String query = "SELECT COUNT(*) FROM " + RutinaEntry.TABLE_NAME +
                    " WHERE " + RutinaEntry.COLUMN_CORREO + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{u.getCorreo()});

            int totalCalculado = 0;
            if (cursor.moveToFirst()) {
                totalCalculado = cursor.getInt(0);
            }
            cursor.close();
            db.close();

            final int totalFinal = totalCalculado;
            AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(totalFinal));
        });
    }

    /**
     * Filtra y contabiliza específicamente el progreso histórico en ejercicios de 'Sentadillas' en segundo plano.
     *
     * @param context  Contexto de la aplicación.
     * @param callback Callback para retornar el total de sentadillas en el hilo principal.
     * @throws android.database.SQLException Si falla la consulta al cursor de SQLite.
     */
    public static void obtenerProgresoSentadillas(Context context, OnIntCallback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Usuario u = UsuarioController.getUsuarioActual();
            if (u == null) {
                AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(0));
                return;
            }
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            String query = "SELECT COUNT(*) FROM " + RutinaEntry.TABLE_NAME +
                    " WHERE " + RutinaEntry.COLUMN_CORREO + " = ? AND " +
                    RutinaEntry.COLUMN_TIPO + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{u.getCorreo(), "Sentadillas"});

            int sentadillasCalculadas = 0;
            if (cursor.moveToFirst()) {
                sentadillasCalculadas = cursor.getInt(0);
            }
            cursor.close();
            db.close();

            final int sentadillasFinal = sentadillasCalculadas;
            AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(sentadillasFinal));
        });
    }

    /**
     * Ejecuta el algoritmo de cálculo de adherencia al tratamiento comparando
     * los días de racha del paciente contra el total de días naturales del mes en curso.
     *
     * @param context  Contexto de la aplicación.
     * @param callback Callback para retornar el porcentaje de adherencia en el hilo principal.
     */
    public static void obtenerPorcentajeAdherenciaMensual(Context context, OnIntCallback callback) {
        obtenerDiasDeRacha(context, diasConectados -> {
            if (diasConectados == 0) {
                callback.onResult(0);
                return;
            }

            java.util.Calendar cal = java.util.Calendar.getInstance();
            int diasEnElMes = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

            int porcentaje = Math.min(100, (int) (((float) diasConectados / diasEnElMes) * 100));
            callback.onResult(porcentaje);
        });
    }
}