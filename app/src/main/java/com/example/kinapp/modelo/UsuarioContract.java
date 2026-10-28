package com.example.kinapp.modelo;

import android.provider.BaseColumns;

/**
 * Clase contrato para la tabla de usuarios en SQLite.
 * Almacena de forma centralizada y segura mediante constantes estáticas los nombres
 * de la tabla y sus columnas. Esto evita errores de inyección SQL, previene el 'hardcoding'
 * de cadenas y facilita el mantenimiento de la base de datos.
 */
public final class UsuarioContract {

    /**
     * Constructor privado para evitar que la clase sea instanciada accidentalmente.
     * Una clase contrato solo debe utilizarse de forma estática para acceder a sus constantes.
     */
    private UsuarioContract() {}

    /**
     * Define la estructura interna exacta de la tabla 'usuarios'.
     * Implementa BaseColumns para heredar automáticamente la columna _ID,
     * un requisito estándar para el manejo óptimo de cursores en Android.
     */
    public static class UsuarioEntry implements BaseColumns {
        public static final String TABLE_NAME = "usuarios";
        public static final String COLUMN_CORREO = "correo";
        public static final String COLUMN_CONTRASENA = "contrasena";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_FECHA = "fechaNac";
        public static final String COLUMN_TELEFONO = "telefono";
        public static final String COLUMN_UBICACION = "ubicacion";
    }
}