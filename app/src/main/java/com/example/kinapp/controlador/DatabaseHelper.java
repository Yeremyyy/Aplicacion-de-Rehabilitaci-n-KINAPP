package com.example.kinapp.controlador;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kinapp.modelo.UsuarioContract.UsuarioEntry;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "KinApp.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + UsuarioEntry.TABLE_NAME + " (" +
                    UsuarioEntry._ID + " INTEGER PRIMARY KEY," +
                    UsuarioEntry.COLUMN_CORREO + " TEXT UNIQUE," +
                    UsuarioEntry.COLUMN_CONTRASENA + " TEXT," +
                    UsuarioEntry.COLUMN_NOMBRE + " TEXT," +
                    UsuarioEntry.COLUMN_FECHA + " TEXT," +
                    UsuarioEntry.COLUMN_TELEFONO + " TEXT," +
                    UsuarioEntry.COLUMN_UBICACION + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UsuarioEntry.TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}