package com.example.kinapp.controlador;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kinapp.modelo.UsuarioContract.UsuarioEntry;


/**
 * Clase auxiliar encargada de la gestión, creación y actualización de la base de datos usada, en este caso, SQLite.
 * Extiende de SQLiteOpenHelper y utiliza la clase contrato UsuarioContract
 * para construir las sentencias SQL de forma segura, evitando cadenas en duro.
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


    /**
     * Constructor principal para el gestor de base de datos.
     * Inicializa la conexión con el archivo local "KinApp.db" utilizando la versión actual definida(Aun falta terminarla).
     *
     * @param context Contexto de la aplicación o de la Activity que solicita el acceso a SQLite, ya sea para ejecutar alguna sentencia o algo.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Se ejecuta automáticamente la primera vez que la aplicación intenta acceder a la base de datos y esta no existe.
     * Responsable de estructurar el esquema inicial construyendo las tablas necesarias.
     *
     * @param db es la instancia en modo escritura o lectura de la base de datos recién creada.
     * @throws android.database.SQLException Si ocurre un error de sintaxis al ejecutar la sentencia para CREATE TABLA esta reacciona.
     */

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }


    /**
     * Se activa automáticamente cuando se detecta un incremento en la constante DATABASE_VERSION, es decir, si se genera una version nueva de la db.
     * Gestiona la migración destructiva por lo que elimina las tablas antiguas y vuelve a ejecutar onCreate
     * para aplicar el nuevo esquema estructural.
     *
     * @param db         Es la instancia de la base de datos que requiere la actualización.
     * @param oldVersion Es el número de versión antigua almacenada actualmente en el dispositivo físico.
     * @param newVersion Es el Número de versión nueva definida en el código fuente.
     * @throws android.database.SQLException Si falla la sentencia DROP TABLE durante la migración, esta acciona.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}