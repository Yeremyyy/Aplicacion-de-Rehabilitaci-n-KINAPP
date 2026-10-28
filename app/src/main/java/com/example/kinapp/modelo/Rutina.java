package com.example.kinapp.modelo;


/**
 * Clase modelo que representa una rutina de kinesiología o ejercicio.
 * Pertenece estrictamente a la Capa Modelo en la arquitectura MVC.
 * Su propósito es encapsular y transportar los datos en memoria para ser consumidos por
 * la capa visual, específicamente a través del adaptador RutinaAdapter para el RecyclerView.
 */
public class Rutina {
    private String titulo;
    private String descripcion;

    /**
     * Construye e inicializa una nueva instancia de una Rutina en la memoria RAM.
     *
     * @param titulo      Nombre o identificador principal del ejercicio, ej. "Sentadillas".
     * @param descripcion Texto explicativo detallando las instrucciones o beneficios de la rutina.
     */
    public Rutina(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    /**
     * Obtiene el título asignado a esta rutina.
     *
     * @return String correspondiente al nombre del ejercicio.
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Obtiene la descripción o instrucciones asignadas a esta rutina.
     *
     * @return String correspondiente al detalle del ejercicio.
     */
    public String getDescripcion() {
        return descripcion;
    }
}