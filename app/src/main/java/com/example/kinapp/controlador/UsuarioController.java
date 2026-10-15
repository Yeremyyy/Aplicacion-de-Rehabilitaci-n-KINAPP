package com.example.kinapp.controlador;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kinapp.modelo.Usuario;
import com.example.kinapp.modelo.UsuarioContract.UsuarioEntry;

public class UsuarioController {

    private static Usuario usuarioActual = null;

    public static boolean registrarUsuario(Context context, String correo, String contrasena, String nombre) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UsuarioEntry.COLUMN_CORREO, correo);
        values.put(UsuarioEntry.COLUMN_CONTRASENA, contrasena);
        values.put(UsuarioEntry.COLUMN_NOMBRE, nombre);

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
                UsuarioEntry.COLUMN_NOMBRE
        };

        String selection = UsuarioEntry.COLUMN_CORREO + " = ? AND " + UsuarioEntry.COLUMN_CONTRASENA + " = ?";
        String[] selectionArgs = {correo, contrasena};

        Cursor cursor = db.query(UsuarioEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        boolean isValid = false;

        if (cursor.moveToFirst()) {
            isValid = true;
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_NOMBRE));
            usuarioActual = new Usuario(correo, contrasena, nombre);
        }

        cursor.close();
        db.close();
        return isValid;
    }

    public static void restaurarSesion(Context context, String correo) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                UsuarioEntry.COLUMN_CORREO,
                UsuarioEntry.COLUMN_CONTRASENA,
                UsuarioEntry.COLUMN_NOMBRE
        };

        String selection = UsuarioEntry.COLUMN_CORREO + " = ?";
        String[] selectionArgs = {correo};

        Cursor cursor = db.query(UsuarioEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_NOMBRE));
            String contrasena = cursor.getString(cursor.getColumnIndexOrThrow(UsuarioEntry.COLUMN_CONTRASENA));
            usuarioActual = new Usuario(correo, contrasena, nombre);
        }

        cursor.close();
        db.close();
    }

    public static boolean actualizarUsuario(Context context, String correoViejo, String nuevoNombre) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UsuarioEntry.COLUMN_NOMBRE, nuevoNombre);

        String selection = UsuarioEntry.COLUMN_CORREO + " = ?";
        String[] selectionArgs = {correoViejo};

        int count = db.update(UsuarioEntry.TABLE_NAME, values, selection, selectionArgs);
        db.close();

        if(count > 0 && usuarioActual != null) {
            usuarioActual = new Usuario(usuarioActual.getCorreo(), usuarioActual.getContrasena(), nuevoNombre);
        }
        return count > 0;
    }

    public static boolean eliminarUsuario(Context context, String correo) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = UsuarioEntry.COLUMN_CORREO + " = ?";
        String[] selectionArgs = {correo};

        int deletedRows = db.delete(UsuarioEntry.TABLE_NAME, selection, selectionArgs);
        db.close();

        if(deletedRows > 0) {
            cerrarSesion();
        }
        return deletedRows > 0;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}