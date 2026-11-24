package com.example.kinapp.controlador;

import com.example.kinapp.modelo.Usuario;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador principal para la gestión de usuarios y sesiones.
 * Delega la autenticación en Firebase Authentication y almacena el perfil de cada usuario
 * (nombre, rol, teléfono, etc.) en el nodo "usuarios" de Firebase Realtime Database.
 * Mantiene en memoria el usuario de la sesión activa para el resto de la aplicación.
 */
public class UsuarioController {

    private static final String NODO_USUARIOS = "usuarios";

    private static Usuario usuarioActual = null;

    /**
     * Callback para operaciones cuyo resultado es solo éxito o fracaso.
     */
    public interface OnResultCallback {
        void onResult(boolean success);
    }

    /**
     * Callback para operaciones que además devuelven un mensaje de error legible para el usuario.
     */
    public interface OnAuthCallback {
        void onResultado(boolean exito, String mensaje);
    }

    /**
     * Crea una cuenta en Firebase Authentication y guarda su perfil en Realtime Database.
     * Si el perfil no se puede guardar, elimina la cuenta recién creada para no dejarla huérfana.
     * Al terminar con éxito, la sesión queda iniciada.
     *
     * @param correo     Correo electrónico del nuevo usuario.
     * @param contrasena Contraseña de acceso; Firebase exige al menos 6 caracteres.
     * @param nombre     Nombre completo del usuario.
     * @param fechaNac   Fecha de nacimiento en formato de texto.
     * @param ubicacion  Ciudad detectada mediante GPS y Geocoder.
     * @param rol        Rol elegido: {@link Usuario#ROL_PACIENTE} o {@link Usuario#ROL_ESPECIALISTA}.
     * @param callback   Callback que informa el resultado y un mensaje de error cuando falla.
     */
    public static void registrarUsuario(String correo, String contrasena, String nombre,
                                        String fechaNac, String ubicacion, String rol,
                                        OnAuthCallback callback) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(correo, contrasena)
                .addOnSuccessListener(resultado -> {
                    FirebaseUser firebaseUser = resultado.getUser();
                    if (firebaseUser == null) {
                        callback.onResultado(false, "No se pudo crear la cuenta");
                        return;
                    }

                    Map<String, Object> datos = new HashMap<>();
                    datos.put("nombre", nombre);
                    datos.put("correo", correo);
                    datos.put("rol", rol);
                    datos.put("fechaNac", fechaNac);
                    datos.put("telefono", "");
                    datos.put("ubicacion", ubicacion);
                    datos.put("creadoEn", ServerValue.TIMESTAMP);

                    referenciaUsuario(firebaseUser.getUid())
                            .setValue(datos)
                            .addOnSuccessListener(sinUso -> {
                                usuarioActual = new Usuario(firebaseUser.getUid(), correo, nombre,
                                        fechaNac, "", rol, null);
                                callback.onResultado(true, null);
                            })
                            .addOnFailureListener(error -> {
                                firebaseUser.delete();
                                callback.onResultado(false, "No se pudo guardar el perfil. Intenta de nuevo");
                            });
                })
                .addOnFailureListener(error ->
                        callback.onResultado(false, traducirErrorRegistro(error)));
    }

    /**
     * Inicia sesión con correo y contraseña en Firebase Authentication y carga el perfil
     * del usuario desde Realtime Database.
     *
     * @param correo     Correo ingresado en el formulario de acceso.
     * @param contrasena Contraseña ingresada en el formulario de acceso.
     * @param callback   Callback que informa el resultado y un mensaje de error cuando falla.
     */
    public static void validarLogin(String correo, String contrasena, OnAuthCallback callback) {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(correo, contrasena)
                .addOnSuccessListener(resultado -> cargarPerfil(resultado.getUser(), callback))
                .addOnFailureListener(error ->
                        callback.onResultado(false, traducirErrorLogin(error)));
    }

    /**
     * Restaura la sesión si Firebase conserva un usuario autenticado en el dispositivo,
     * cargando su perfil desde Realtime Database.
     *
     * @param callback Callback que indica si la sesión fue restaurada.
     */
    public static void restaurarSesion(OnResultCallback callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onResult(false);
            return;
        }
        cargarPerfil(firebaseUser, (exito, mensaje) -> callback.onResult(exito));
    }

    /**
     * Actualiza el teléfono del usuario con sesión activa en Realtime Database.
     *
     * @param nuevoTelefono Nuevo número de contacto.
     * @param callback      Callback que indica si la actualización fue exitosa.
     */
    public static void actualizarTelefono(String nuevoTelefono, OnResultCallback callback) {
        if (usuarioActual == null) {
            callback.onResult(false);
            return;
        }

        final Usuario actual = usuarioActual;
        referenciaUsuario(actual.getUid())
                .child("telefono")
                .setValue(nuevoTelefono)
                .addOnSuccessListener(sinUso -> {
                    usuarioActual = new Usuario(actual.getUid(), actual.getCorreo(), actual.getNombre(),
                            actual.getFechaNac(), nuevoTelefono, actual.getRol(), actual.getEspecialistaId());
                    callback.onResult(true);
                })
                .addOnFailureListener(error -> callback.onResult(false));
    }

    /**
     * Cambia la contraseña del usuario con sesión activa. Primero reautentica con la contraseña
     * actual, como exige Firebase para operaciones sensibles, y luego aplica la nueva.
     *
     * @param contrasenaActual Contraseña vigente del usuario.
     * @param contrasenaNueva  Nueva contraseña; debe tener al menos 6 caracteres.
     * @param callback         Callback que informa el resultado y un mensaje de error cuando falla.
     */
    public static void actualizarContrasena(String contrasenaActual, String contrasenaNueva,
                                            OnAuthCallback callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null || firebaseUser.getEmail() == null) {
            callback.onResultado(false, "No hay una sesión activa");
            return;
        }

        AuthCredential credencial = EmailAuthProvider.getCredential(firebaseUser.getEmail(), contrasenaActual);
        firebaseUser.reauthenticate(credencial)
                .addOnSuccessListener(sinUso -> firebaseUser.updatePassword(contrasenaNueva)
                        .addOnSuccessListener(otroSinUso -> callback.onResultado(true, null))
                        .addOnFailureListener(error ->
                                callback.onResultado(false, traducirErrorClave(error))))
                .addOnFailureListener(error ->
                        callback.onResultado(false, "La contraseña actual es incorrecta"));
    }

    /**
     * Proporciona acceso global al usuario con sesión activa.
     *
     * @return Usuario actualmente autenticado, o null si no hay sesión activa.
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Cierra la sesión en Firebase Authentication y elimina el usuario en memoria.
     */
    public static void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        usuarioActual = null;
    }

    /**
     * Obtiene la referencia del nodo de un usuario dentro de Realtime Database.
     *
     * @param uid Identificador del usuario en Firebase Authentication.
     * @return Referencia al nodo "usuarios/{uid}".
     */
    private static DatabaseReference referenciaUsuario(String uid) {
        return FirebaseDatabase.getInstance().getReference(NODO_USUARIOS).child(uid);
    }

    /**
     * Descarga el perfil del usuario autenticado y lo guarda como usuario actual.
     * Si la cuenta no tiene perfil, cierra la sesión para evitar un estado inconsistente.
     *
     * @param firebaseUser Usuario autenticado en Firebase.
     * @param callback     Callback que informa el resultado y un mensaje de error cuando falla.
     */
    private static void cargarPerfil(FirebaseUser firebaseUser, OnAuthCallback callback) {
        if (firebaseUser == null) {
            callback.onResultado(false, "No se pudo iniciar sesión");
            return;
        }

        referenciaUsuario(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        usuarioActual = desdeSnapshot(firebaseUser.getUid(), snapshot);
                        callback.onResultado(true, null);
                    } else {
                        FirebaseAuth.getInstance().signOut();
                        callback.onResultado(false, "No se encontró el perfil de esta cuenta");
                    }
                })
                .addOnFailureListener(error ->
                        callback.onResultado(false, "No se pudo cargar el perfil. Revisa tu conexión"));
    }

    /**
     * Convierte un nodo de Realtime Database en una entidad Usuario.
     *
     * @param uid      Identificador del usuario en Firebase Authentication.
     * @param snapshot Datos del nodo "usuarios/{uid}".
     * @return Usuario con los datos del nodo.
     */
    private static Usuario desdeSnapshot(String uid, DataSnapshot snapshot) {
        return new Usuario(
                uid,
                snapshot.child("correo").getValue(String.class),
                snapshot.child("nombre").getValue(String.class),
                snapshot.child("fechaNac").getValue(String.class),
                snapshot.child("telefono").getValue(String.class),
                snapshot.child("rol").getValue(String.class),
                snapshot.child("especialistaId").getValue(String.class));
    }

    /**
     * Traduce un error de registro de Firebase a un mensaje comprensible.
     *
     * @param error Excepción devuelta por Firebase.
     * @return Mensaje en español para mostrar al usuario.
     */
    private static String traducirErrorRegistro(Exception error) {
        if (error instanceof FirebaseAuthWeakPasswordException) {
            return "La contraseña debe tener al menos 6 caracteres";
        }
        if (error instanceof FirebaseAuthUserCollisionException) {
            return "El correo ya está registrado";
        }
        if (error instanceof FirebaseAuthInvalidCredentialsException) {
            return "El correo no tiene un formato válido";
        }
        if (error instanceof FirebaseNetworkException) {
            return "Sin conexión a internet";
        }
        return "No se pudo crear la cuenta";
    }

    /**
     * Traduce un error de inicio de sesión de Firebase a un mensaje comprensible.
     *
     * @param error Excepción devuelta por Firebase.
     * @return Mensaje en español para mostrar al usuario.
     */
    private static String traducirErrorLogin(Exception error) {
        if (error instanceof FirebaseAuthInvalidCredentialsException
                || error instanceof FirebaseAuthInvalidUserException) {
            return "Correo o contraseña incorrectos";
        }
        if (error instanceof FirebaseNetworkException) {
            return "Sin conexión a internet";
        }
        return "No se pudo iniciar sesión";
    }

    /**
     * Traduce un error al cambiar la contraseña a un mensaje comprensible.
     *
     * @param error Excepción devuelta por Firebase.
     * @return Mensaje en español para mostrar al usuario.
     */
    private static String traducirErrorClave(Exception error) {
        if (error instanceof FirebaseAuthWeakPasswordException) {
            return "La nueva contraseña debe tener al menos 6 caracteres";
        }
        if (error instanceof FirebaseNetworkException) {
            return "Sin conexión a internet";
        }
        return "No se pudo actualizar la contraseña";
    }
}