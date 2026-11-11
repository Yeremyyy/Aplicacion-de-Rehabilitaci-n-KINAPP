package com.example.kinapp.controlador;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.kinapp.modelo.Usuario;
import com.example.kinapp.modelo.UsuarioContract.UsuarioEntry;

/**
 * Es el controlador principal para la gestión de usuarios y sesiones en la aplicación.
 * Implementa el patrón MVC aislando la lógica de negocio como el CRUD de las vistas.
 * Utiliza la clase UsuarioContract para garantizar la integridad de las sentencias SQL.
 */
public class UsuarioController {

    private static Usuario usuarioActual = null;

    /**
     * Interfaz de callback para retornar resultados booleanos de operaciones asíncronas de usuario.
     */
    public interface OnResultCallback {
        void onResult(boolean success);
    }

    /**
     * Inserta un nuevo usuario en la base de datos en segundo plano.
     *
     * @param context    Es el contexto de la interfaz gráfica que invoca el registro.
     * @param correo     Correo electrónico único ingresado por el usuario.
     * @param contrasena Contraseña de acceso en texto plano.
     * @param nombre     Nombre completo del usuario.
     * @param fechaNac   Fecha de nacimiento con formato texto.
     * @param ubicacion  Ciudad detectada automáticamente mediante LocationManager y Geocoder.
     * @param callback   Callback para retornar el éxito o fracaso de la inserción en el hilo principal.
     * @throws android.database.SQLException Si falla la validación de SQLite al invocar un INSERT.
     */
    public static void registrarUsuario(Context context, String correo, String contrasena, String nombre, String fechaNac, String ubicacion, OnResultCallback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(UsuarioEntry.COLUMN_CORREO, correo);
            values.put(UsuarioEntry.COLUMN_CONTRASENA, contrasena);
            values.put(UsuarioEntry.COLUMN_NOMBRE, nombre);
            values.put(UsuarioEntry.COLUMN_FECHA, fechaNac);
            values.put(UsuarioEntry.COLUMN_TELEFONO, "");
            values.put(UsuarioEntry.COLUMN_UBICACION, ubicacion);

            long newRowId = db.insert(UsuarioEntry.TABLE_NAME, null, values);
            db.close();

            boolean exito = (newRowId != -1);
            AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(exito));
        });
    }

    /**
     * Verifica las credenciales de acceso del usuario consultando la base de datos en segundo plano.
     * Si las credenciales son válidas, instancia el objeto 'usuarioActual' para mantener la sesión activa.
     *
     * @param context    Es el contexto de la aplicación utilizado para instanciar DatabaseHelper.
     * @param correo     Correo electrónico ingresado en el formulario de login.
     * @param contrasena Contraseña ingresada en el formulario de login.
     * @param callback   Callback para retornar el resultado de la validación en el hilo principal.
     * @throws IllegalArgumentException Si getColumnIndexOrThrow no encuentra una de las columnas solicitadas en el cursor.
     */
    public static void validarLogin(Context context, String correo, String contrasena, OnResultCallback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
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

            AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(isValid));
        });
    }

    /**
     * Recupera los datos completos de un usuario previamente autenticado a partir de su correo electrónico en segundo plano.
     * Reconstruye el objeto 'usuarioActual' en memoria, útil tras el cierre inesperado de la aplicación.
     *
     * @param context  Contexto de la aplicación.
     * @param correo   Correo electrónico almacenado.
     * @param callback Callback para retornar si la sesión fue restaurada exitosamente.
     */
    public static void restaurarSesion(Context context, String correo, OnResultCallback callback) {
        AppExecutors.getInstance().diskIO().execute(() -> {
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

            AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(exito));
        });
    }

    /**
     * Modifica el número telefónico del usuario actualmente logueado en la base de datos en segundo plano.
     * Requiere que exista una sesión activa.
     *
     * @param context       Contexto de la vista que solicita la modificación.
     * @param nuevoTelefono El nuevo número de contacto a guardar.
     * @param callback      Callback para retornar el resultado de la actualización.
     * @return boolean      false de forma inmediata si la sesión es nula, o procesa de forma asíncrona si hay sesión.
     */
    public static void actualizarTelefono(Context context, String nuevoTelefono, OnResultCallback callback) {
        if (usuarioActual == null) {
            callback.onResult(false);
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(UsuarioEntry.COLUMN_TELEFONO, nuevoTelefono);

            String selection = UsuarioEntry.COLUMN_CORREO + " = ?";
            String[] selectionArgs = {usuarioActual.getCorreo()};

            int count = db.update(UsuarioEntry.TABLE_NAME, values, selection, selectionArgs);
            db.close();

            boolean exito = (count > 0);
            if (exito) {
                usuarioActual = new Usuario(usuarioActual.getCorreo(), usuarioActual.getContrasena(), usuarioActual.getNombre(), usuarioActual.getFechaNac(), nuevoTelefono);
            }

            AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(exito));
        });
    }

    /**
     * Reemplaza la contraseña del usuario actualmente logueado en la base de datos en segundo plano.
     * Valida internamente la existencia de una sesión activa.
     *
     * @param context         Contexto de la aplicación.
     * @param nuevaContrasena La nueva cadena de texto que reemplazará a la contraseña actual.
     * @param callback        Callback para retornar el resultado de la actualización de credenciales.
     */
    public static void actualizarContrasena(Context context, String nuevaContrasena, OnResultCallback callback) {
        if (usuarioActual == null) {
            callback.onResult(false);
            return;
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(UsuarioEntry.COLUMN_CONTRASENA, nuevaContrasena);

            String selection = UsuarioEntry.COLUMN_CORREO + " = ?";
            String[] selectionArgs = {usuarioActual.getCorreo()};

            int count = db.update(UsuarioEntry.TABLE_NAME, values, selection, selectionArgs);
            db.close();

            boolean exito = (count > 0);
            if(exito) {
                usuarioActual = new Usuario(usuarioActual.getCorreo(), nuevaContrasena, usuarioActual.getNombre(), usuarioActual.getFechaNac(), usuarioActual.getTelefono());
            }

            AppExecutors.getInstance().mainThread().execute(() -> callback.onResult(exito));
        });
    }

    /**
     * Proporciona acceso global al objeto de sesión del usuario en memoria.
     * @return Usuario Instancia del usuario actualmente logueado, o null si no hay sesión activa.
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Destruye la sesión activa eliminando la referencia en memoria del usuario actual.
     */
    public static void cerrarSesion() {
        usuarioActual = null;
    }
}