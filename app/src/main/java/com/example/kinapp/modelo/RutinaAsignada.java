package com.example.kinapp.modelo;

/**
 * Entidad que representa una rutina de ejercicio asignada a un paciente.
 * Se almacena en el nodo "rutinasAsignadas/{pacienteUid}/{rutinaId}" de Firebase Realtime Database.
 */
public class RutinaAsignada {

    /** Estado de una rutina que el paciente aún no ha completado. */
    public static final String ESTADO_PENDIENTE = "pendiente";

    /** Estado de una rutina que el paciente ya completó. */
    public static final String ESTADO_COMPLETADA = "completada";

    /** Repeticiones de la rutina libre que se usa cuando no hay una rutina asignada. */
    public static final int REPETICIONES_POR_DEFECTO = 12;

    /** Clave del Intent que transporta la meta de repeticiones entre pantallas. */
    public static final String EXTRA_META = "meta";

    /** Clave del Intent que transporta el identificador de la rutina asignada. */
    public static final String EXTRA_RUTINA_ID = "rutina_id";

    private final String id;
    private final String ejercicio;
    private final int repeticiones;
    private final String estado;
    private final long creadaEn;

    /**
     * Construye una rutina asignada.
     *
     * @param id           Identificador de la rutina generado por Firebase.
     * @param ejercicio    Nombre del ejercicio a realizar.
     * @param repeticiones Meta de repeticiones fijada por el especialista.
     * @param estado       Estado de la rutina: {@link #ESTADO_PENDIENTE} o {@link #ESTADO_COMPLETADA}.
     * @param creadaEn     Marca de tiempo de creación en milisegundos.
     */
    public RutinaAsignada(String id, String ejercicio, int repeticiones, String estado, long creadaEn) {
        this.id = id;
        this.ejercicio = ejercicio;
        this.repeticiones = repeticiones;
        this.estado = estado;
        this.creadaEn = creadaEn;
    }

    /** @return String con el identificador de la rutina. */
    public String getId() { return id; }

    /** @return String con el nombre del ejercicio. */
    public String getEjercicio() { return ejercicio; }

    /** @return int con la meta de repeticiones. */
    public int getRepeticiones() { return repeticiones; }

    /** @return String con el estado de la rutina. */
    public String getEstado() { return estado; }

    /** @return long con la marca de tiempo de creación. */
    public long getCreadaEn() { return creadaEn; }

    /** @return true si la rutina está pendiente de realizar. */
    public boolean esPendiente() { return ESTADO_PENDIENTE.equals(estado); }
}