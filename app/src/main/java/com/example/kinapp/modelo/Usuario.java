package com.example.kinapp.modelo;

/**
 * Entidad que representa a un paciente o a un especialista en el sistema.
 * Pertenece a la capa Modelo y mantiene en memoria los datos del usuario con sesión activa,
 * cargados desde su nodo de Firebase Realtime Database.
 */
public class Usuario {

    public static final String ROL_PACIENTE = "paciente";
    public static final String ROL_ESPECIALISTA = "especialista";

    private final String uid;
    private final String correo;
    private final String nombre;
    private final String fechaNac;
    private final String telefono;
    private final String rol;
    private final String especialistaId;

    /**
     * Construye e inicializa una nueva instancia de la entidad Usuario.
     *
     * @param uid            Identificador único asignado por Firebase Authentication.
     * @param correo         Dirección de correo electrónico del usuario.
     * @param nombre         Nombre completo del paciente o especialista.
     * @param fechaNac       Fecha de nacimiento almacenada en formato de texto.
     * @param telefono       Número de contacto telefónico del usuario.
     * @param rol            Rol del usuario: {@link #ROL_PACIENTE} o {@link #ROL_ESPECIALISTA}.
     * @param especialistaId Identificador del especialista asignado al paciente, o null si no tiene.
     */
    public Usuario(String uid, String correo, String nombre, String fechaNac,
                   String telefono, String rol, String especialistaId) {
        this.uid = uid;
        this.correo = correo;
        this.nombre = nombre;
        this.fechaNac = fechaNac;
        this.telefono = telefono;
        this.rol = rol;
        this.especialistaId = especialistaId;
    }

    /** @return String con el identificador único del usuario en Firebase. */
    public String getUid() { return uid; }

    /** @return String correspondiente al correo electrónico registrado. */
    public String getCorreo() { return correo; }

    /** @return String correspondiente al nombre completo. */
    public String getNombre() { return nombre; }

    /** @return String correspondiente a la fecha de nacimiento. */
    public String getFechaNac() { return fechaNac; }

    /** @return String correspondiente al número de teléfono. */
    public String getTelefono() { return telefono; }

    /** @return String con el rol del usuario. */
    public String getRol() { return rol; }

    /** @return String con el identificador del especialista asignado, o null si no tiene. */
    public String getEspecialistaId() { return especialistaId; }

    /** @return true si el usuario tiene rol de especialista. */
    public boolean esEspecialista() { return ROL_ESPECIALISTA.equals(rol); }
}