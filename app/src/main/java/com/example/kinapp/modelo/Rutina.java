package com.example.kinapp.modelo;

public class Rutina {
    private String titulo;
    private String descripcion;

    public Rutina(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}