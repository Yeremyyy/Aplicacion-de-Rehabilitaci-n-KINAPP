package com.example.kinapp.controlador;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kinapp.modelo.Usuario;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProgresoController {

    public static void inicializarTabla(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String createTable = "CREATE TABLE IF NOT EXISTS historial_rutinas (id INTEGER PRIMARY KEY AUTOINCREMENT, correo TEXT, fecha TEXT, tipo TEXT)";
        db.execSQL(createTable);
        db.close();
    }

    public static boolean guardarRutinaCompletada(Context context, String tipoRutina) {
        inicializarTabla(context);
        Usuario u = UsuarioController.getUsuarioActual();
        if (u == null) return false;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        ContentValues values = new ContentValues();
        values.put("correo", u.getCorreo());
        values.put("fecha", fechaHoy);
        values.put("tipo", tipoRutina);

        long result = db.insert("historial_rutinas", null, values);
        db.close();
        return result != -1;
    }

    public static int obtenerDiasDeRacha(Context context) {
        inicializarTabla(context);
        Usuario u = UsuarioController.getUsuarioActual();
        if (u == null) return 0;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(DISTINCT fecha) FROM historial_rutinas WHERE correo = ?";
        Cursor cursor = db.rawQuery(query, new String[]{u.getCorreo()});

        int racha = 0;
        if (cursor.moveToFirst()) {
            racha = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return racha;
    }
}