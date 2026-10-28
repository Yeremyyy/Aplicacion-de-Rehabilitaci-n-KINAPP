package com.example.kinapp.modelo;

/**
 * Clase modelo que representa la entidad de un paciente o especialista en el sistema.
 * Pertenece a la Capa Modelo de la arquitectura MVC.
 * Su objetivo principal es mantener el estado del usuario en memoria (ej. sesión activa)
 * facilitando el transporte de los datos extraídos de SQLite hacia los controladores y vistas.
 */
public class Usuario {
    private String correo;
    private String contrasena;
    private String nombre;
    private String fechaNac;
    private String telefono;

    /**
     * Construye e inicializa una nueva instancia de la entidad Usuario.
     *
     * @param correo     Dirección de correo electrónico (actúa como identificador único).
     * @param contrasena Clave de acceso del usuario.
     * @param nombre     Nombre completo del paciente o especialista.
     * @param fechaNac   Fecha de nacimiento almacenada en formato de texto.
     * @param telefono   Número de contacto telefónico del usuario.
     */

    public Usuario(String correo, String contrasena, String nombre, String fechaNac, String telefono) {
        this.correo = correo;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.fechaNac = fechaNac;
        this.telefono = telefono;
    }

    /** @return String correspondiente al correo electrónico registrado. */
    public String getCorreo() { return correo; }

    /** @return String correspondiente a la contraseña del usuario. */
    public String getContrasena() { return contrasena; }

    /** @return String correspondiente al nombre completo. */
    public String getNombre() { return nombre; }

    /** @return String correspondiente a la fecha de nacimiento. */
    public String getFechaNac() { return fechaNac; }

    /** @return String correspondiente al número de teléfono. */
    public String getTelefono() { return telefono; }
}