package com.example.kinapp.controlador;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kinapp.modelo.Usuario;
import com.example.kinapp.modelo.UsuarioContract.UsuarioEntry;

public class UsuarioController {

    private static Usuario usuarioActual = null;

    public static boolean registrarUsuario(Context context, String correo, String contrasena, String nombre, String fechaNac) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UsuarioEntry.COLUMN_CORREO, correo);
        values.put(UsuarioEntry.COLUMN_CONTRASENA, contrasena);
        values.put(UsuarioEntry.COLUMN_NOMBRE, nombre);
        values.put(UsuarioEntry.COLUMN_FECHA, fechaNac);
        values.put(UsuarioEntry.COLUMN_TELEFONO, "");

        long newRowId = db.insert(UsuarioEntry.TABLE_NAME, null, values);
        db.close();

        return newRowId != -1;
    }

    public static boolean validarLogin(Context context, String correo, String contrasena) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                UsuarioEntry.COLUMN_CORREO,
                UsuarioEntry.COLUMN_CONTRASENA,
                UsuarioEntry.COLUMN_NOMBRE,
                UsuarioEntry.COLUMN_FECHA,
                UsuarioEntry.COLUMN_TELEFONO
        };

        String selection = UsuarioEntry.COLUMN_CORREO + " = ? AND " + UsuarioEntry.COLUMN_CONTRASENA + " = ?";
        String[] selectionArgs = {correo, contrasena};

        Cursor cursor = db.query(UsuarioEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        boolean isValid = false;

        if (cursor.moveToFirst()) {
            isValid = true;
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_NOMBRE));
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_FECHA));
            String tel = cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_TELEFONO));
            usuarioActual = new Usuario(correo, contrasena, nombre, fecha, tel);
        }

        cursor.close();
        db.close();
        return isValid;
    }

    public static boolean restaurarSesion(Context context, String correo) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                UsuarioEntry.COLUMN_CORREO,
                UsuarioEntry.COLUMN_CONTRASENA,
                UsuarioEntry.COLUMN_NOMBRE,
                UsuarioEntry.COLUMN_FECHA,
                UsuarioEntry.COLUMN_TELEFONO
        };

        String selection = UsuarioEntry.COLUMN_CORREO + " = ?";
        String[] selectionArgs = {correo};

        Cursor cursor = db.query(UsuarioEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        boolean exito = false;

        if (cursor.moveToFirst()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_NOMBRE));
            String contrasena = cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_CONTRASENA));
            String fecha = cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_FECHA));
            String tel = cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_TELEFONO));
            usuarioActual = new Usuario(correo, contrasena, nombre, fecha, tel);
            exito = true;
        }

        cursor.close();
        db.close();
        return exito;
    }

    public static boolean actualizarTelefono(Context context, String nuevoTelefono) {
        if (usuarioActual == null) return false;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UsuarioEntry.COLUMN_TELEFONO, nuevoTelefono);

        String selection = UsuarioEntry.COLUMN_CORREO + " = ?";
        String[] selectionArgs = {usuarioActual.getCorreo()};

        int count = db.update(UsuarioEntry.TABLE_NAME, values, selection, selectionArgs);
        db.close();

        if(count > 0) {
            usuarioActual = new Usuario(usuarioActual.getCorreo(), usuarioActual.getContrasena(), usuarioActual.getNombre(), usuarioActual.getFechaNac(), nuevoTelefono);
        }
        return count > 0;
    }

    public static boolean actualizarContrasena(Context context, String nuevaContrasena) {
        if (usuarioActual == null) return false;

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UsuarioEntry.COLUMN_CONTRASENA, nuevaContrasena);

        String selection = UsuarioEntry.COLUMN_CORREO + " = ?";
        String[] selectionArgs = {usuarioActual.getCorreo()};

        int count = db.update(UsuarioEntry.TABLE_NAME, values, selection, selectionArgs);
        db.close();

        if(count > 0) {
            usuarioActual = new Usuario(usuarioActual.getCorreo(), nuevaContrasena, usuarioActual.getNombre(), usuarioActual.getFechaNac(), usuarioActual.getTelefono());
        }
        return count > 0;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}