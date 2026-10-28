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
     * Construye la tabla de historial de rutinas en caso de que no exista.
     * Utiliza las constantes del contrato para armar la sentencia de forma segura.
     *
     * @param context Contexto de la aplicación para instanciar DatabaseHelper.
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
     * Registra una nueva sesión completada en la base de datos asociada al usuario actual.
     * Captura la fecha exacta del sistema operativo al momento de finalizar el ejercicio.
     *
     * @param context    Contexto de la vista que invoca el guardado.
     * @param tipoRutina Nombre o categoría de la rutina ejecutada.
     * @return boolean true si la inserción en SQLite fue exitosa y false si la sesión es nula o falla el guardado.
     */
    public static boolean guardarRutinaCompletada(Context context, String tipoRutina) {
        inicializarTabla(context);
        Usuario u = UsuarioController.getUsuarioActual();
        if (u == null) return false;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        ContentValues values = new ContentValues();
        values.put(RutinaEntry.COLUMN_CORREO, u.getCorreo());
        values.put(RutinaEntry.COLUMN_FECHA, fechaHoy);
        values.put(RutinaEntry.COLUMN_TIPO, tipoRutina);

        long result = db.insert(RutinaEntry.TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Calcula la cantidad de días únicos en los que el usuario ha realizado al menos una rutina.
     * Utiliza la cláusula SQL 'COUNT DISTINCT' sobre la columna fecha.
     *
     * @param context Contexto de la aplicación.
     * @return int Número total de días de racha acumulados por el paciente.
     */
    public static int obtenerDiasDeRacha(Context context) {
        inicializarTabla(context);
        Usuario u = UsuarioController.getUsuarioActual();
        if (u == null) return 0;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(DISTINCT " + RutinaEntry.COLUMN_FECHA + ") FROM " +
                RutinaEntry.TABLE_NAME + " WHERE " + RutinaEntry.COLUMN_CORREO + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{u.getCorreo()});

        int racha = 0;
        if (cursor.moveToFirst()) {
            racha = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return racha;
    }

    /**
     * Obtiene el volumen total histórico de sesiones de kinesiología completadas por el paciente.
     *
     * @param context Contexto de la aplicación.
     * @return int Sumatoria total de todas las rutinas registradas sin importar la fecha.
     */
    public static int obtenerSesionesTotales(Context context) {
        Usuario u = UsuarioController.getUsuarioActual();
        if (u == null) return 0;
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + RutinaEntry.TABLE_NAME +
                " WHERE " + RutinaEntry.COLUMN_CORREO + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{u.getCorreo()});

        int total = 0;
        if (cursor.moveToFirst()) total = cursor.getInt(0);
        cursor.close();
        db.close();
        return total;
    }

    /**
     * Filtra y contabiliza específicamente el progreso histórico en ejercicios de 'Sentadillas'.
     *
     * @param context Contexto de la aplicación.
     * @return int Cantidad total de rutinas de tipo "Sentadillas" completadas.
     */
    public static int obtenerProgresoSentadillas(Context context) {
        Usuario u = UsuarioController.getUsuarioActual();
        if (u == null) return 0;
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + RutinaEntry.TABLE_NAME +
                " WHERE " + RutinaEntry.COLUMN_CORREO + " = ? AND " +
                RutinaEntry.COLUMN_TIPO + " = 'Sentadillas'";
        Cursor cursor = db.rawQuery(query, new String[]{u.getCorreo()});

        int sentadillas = 0;
        if (cursor.moveToFirst()) sentadillas = cursor.getInt(0);
        cursor.close();
        db.close();
        return sentadillas;
    }

    /**
     * Ejecuta el algoritmo de cálculo de adherencia al tratamiento comparando
     * los días de racha del paciente contra el total de días naturales del mes en curso.
     *
     * @param context Contexto de la aplicación.
     * @return int Porcentaje de cumplimiento mensual (0 a 100).
     */
    public static int obtenerPorcentajeAdherenciaMensual(Context context) {
        int diasConectados = obtenerDiasDeRacha(context);
        if (diasConectados == 0) return 0;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int diasEnElMes = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

        return (int) (((float) diasConectados / diasEnElMes) * 100);
    }
}