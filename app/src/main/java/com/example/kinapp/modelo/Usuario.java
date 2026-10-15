package com.example.kinapp.modelo;

public class Usuario {
    private String correo;
    private String contrasena;
    private String nombre;
    private String fechaNac;
    private String telefono;

    public Usuario(String correo, String contrasena, String nombre, String fechaNac, String telefono) {
        this.correo = correo;
        this.contrasena = contrasena;
        this.nombre = nombre;
        this.fechaNac = fechaNac;
        this.telefono = telefono;
    }

    public String getCorreo() { return correo; }
    public String getContrasena() { return contrasena; }
    public String getNombre() { return nombre; }
    public String getFechaNac() { return fechaNac; }
    public String getTelefono() { return telefono; }
}