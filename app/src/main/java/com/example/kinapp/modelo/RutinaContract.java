package com.example.kinapp.modelo;

import android.provider.BaseColumns;

/**
 * Clase contrato para la tabla de historial de rutinas en SQLite.
 * Almacena de forma centralizada y segura en constantes estáticas los nombres de la tabla
 * y sus columnas para evitar errores de inyección SQL o tipeo.
 */
public final class RutinaContract {

    private RutinaContract() {}

    /**
     * Define la estructura de la tabla 'historial_rutinas'.
     * Implementa BaseColumns para heredar automáticamente el campo _ID obligatorio en Android.
     */
    public static class RutinaEntry implements BaseColumns {
        public static final String TABLE_NAME = "historial_rutinas";
        public static final String COLUMN_CORREO = "correo";
        public static final String COLUMN_FECHA = "fecha";
        public static final String COLUMN_TIPO = "tipo";
    }
}