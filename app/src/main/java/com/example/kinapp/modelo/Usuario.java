package com.example.kinapp.modelo;

public class Usuario {
    private String correo;
    private String contrasena;
    private String nombre;

    public Usuario(String correo, String contrasena, String nombre) {
        this.correo = correo;
        this.contrasena = contrasena;
        this.nombre = nombre;
    }

    public String getCorreo() { return correo; }
    public String getContrasena() { return contrasena; }
    public String getNombre() { return nombre; }
}