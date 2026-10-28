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
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param context    Es el contexto de la interfaz gráfica que invoca el registro.
     * @param correo     Correo electrónico único ingresado por el usuario.
     * @param contrasena Contraseña de acceso en texto plano.
     * @param nombre     Nombre completo del usuario.
     * @param fechaNac   Fecha de nacimiento con formato texto.
     * @param ubicacion  Ciudad detectada automáticamente mediante LocationManager y Geocoder.
     * @return boolean true si la inserción fue exitosa y false si hubo un error de restricción.
     * @throws android.database.SQLException Si falla la validación de SQLite al invocar un INSERT se acciona.
     */
    public static boolean registrarUsuario(Context context, String correo, String contrasena, String nombre, String fechaNac, String ubicacion) {
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

        return newRowId != -1;
    }

    /**
     * Aqui se verifica las credenciales de acceso del usuario consultando la base de datos.
     * Si las credenciales son válidas, instancia el objeto 'usuarioActual' para mantener la sesión activa.
     *
     * @param context    Es el contexto de la aplicación utilizado para instanciar DatabaseHelper.
     * @param correo     Correo electrónico ingresado en el formulario de login.
     * @param contrasena Contraseña ingresada en el formulario de login.
     * @return boolean true si el usuario existe y la contraseña coincide y false en caso contrario.
     * @throws IllegalArgumentException Si getColumnIndexOrThrow no encuentra una de las columnas solicitadas en el cursor, se acciona.
     */
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

    /**
     * En esto se recupera los datos completos de un usuario previamente autenticado a partir de su correo electrónico.
     * Reconstruye el objeto 'usuarioActual' en memoria, útil tras el cierre inesperado de la aplicación.
     *
     * @param context Contexto de la aplicación.
     * @param correo  Correo electrónico almacenado.
     * @return boolean true si el usuario fue encontrado y la sesión restaurada y false si el registro no existe.
     */
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

    /**
     * Esto es opcional pero el usuario modifica el número telefónico actualmente logueado en la base de datos.
     * Requiere que exista una sesión activa.
     *
     * @param context       Contexto de la vista que solicita la modificación.
     * @param nuevoTelefono El nuevo número de contacto a guardar.
     * @return boolean true si al menos un registro fue actualizado en SQLite y false si la sesión es nula o falló el UPDATE.
     */
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

        if (count > 0) {
            usuarioActual = new Usuario(usuarioActual.getCorreo(), usuarioActual.getContrasena(), usuarioActual.getNombre(), usuarioActual.getFechaNac(), nuevoTelefono);
        }
        return count > 0;
    }

    /**
     * Se reemplaza la contraseña del usuario actualmente logueado en la base de datos.
     * Valida internamente la existencia de una sesión activa antes de abrir la base de datos.
     *
     * @param context         Contexto de la aplicación.
     * @param nuevaContrasena La nueva cadena de texto que reemplazará a la contraseña actual.
     * @return boolean true si la actualización en SQLite tuvo éxito y false en caso de fallo o sesión nula.
     */
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