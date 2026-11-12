package com.example.kinapp.controlador;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kinapp.modelo.RutinaContract.RutinaEntry;
import com.example.kinapp.modelo.UsuarioContract.UsuarioEntry;

/**
 * Clase auxiliar encargada de la gestión, creación y actualización de la base de datos SQLite.
 * Extiende de SQLiteOpenHelper y utiliza las clases contrato para construir
 * las sentencias SQL de forma segura, evitando cadenas quemadas.
 */
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

    private static final String SQL_CREATE_HISTORIAL =
            "CREATE TABLE IF NOT EXISTS " + RutinaEntry.TABLE_NAME + " (" +
                    RutinaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RutinaEntry.COLUMN_CORREO + " TEXT, " +
                    RutinaEntry.COLUMN_FECHA + " TEXT, " +
                    RutinaEntry.COLUMN_TIPO + " TEXT)";

    private static final String SQL_DELETE_HISTORIAL =
            "DROP TABLE IF EXISTS " + RutinaEntry.TABLE_NAME;

    /**
     * Constructor principal para el gestor de base de datos.
     * Inicializa la conexión con el archivo local "KinApp.db".
     *
     * @param context Contexto de la aplicación o actividad que solicita el acceso a SQLite.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Se ejecuta automáticamente la primera vez que la aplicación intenta acceder a la base de datos y esta no existe.
     * Responsable de estructurar el esquema inicial construyendo las tablas necesarias.
     *
     * @param db Instancia en modo escritura de la base de datos recién creada.
     * @throws android.database.SQLException Si ocurre un error de sintaxis al ejecutar las sentencias CREATE TABLE.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_HISTORIAL);
    }

    /**
     * Se activa automáticamente cuando se detecta un incremento en la constante DATABASE_VERSION.
     * Gestiona la migración eliminando las tablas antiguas y volviendo a ejecutar onCreate.
     *
     * @param db         Instancia de la base de datos que requiere la actualización.
     * @param oldVersion Número de versión antigua almacenada actualmente en el dispositivo físico.
     * @param newVersion Número de versión nueva definida en el código fuente.
     * @throws android.database.SQLException Si falla la sentencia DROP TABLE durante la migración.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_HISTORIAL);
        onCreate(db);
    }
}