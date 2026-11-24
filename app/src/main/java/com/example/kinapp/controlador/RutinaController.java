package com.example.kinapp.controlador;

import androidx.annotation.NonNull;

import com.example.kinapp.modelo.RutinaAsignada;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Controlador de las rutinas asignadas y de las sesiones completadas en la nube.
 * Usa Firebase Realtime Database con una estructura jerárquica por paciente:
 * "rutinasAsignadas/{pacienteUid}/{rutinaId}" y "sesiones/{pacienteUid}/{sesionId}".
 */
public class RutinaController {

    private static final String NODO_RUTINAS = "rutinasAsignadas";
    private static final String NODO_SESIONES = "sesiones";

    /**
     * Callback que recibe la rutina pendiente más reciente del paciente cada vez que cambia.
     */
    public interface OnRutinaListener {
        void onRutinaActiva(RutinaAsignada rutina);
    }

    /**
     * Callback opcional que informa si una escritura en la nube terminó con éxito.
     */
    public interface OnResultCallback {
        void onResult(boolean success);
    }

    /**
     * Registra un ValueEventListener que entrega en tiempo real la rutina pendiente más reciente
     * del paciente, o null si no tiene ninguna. Debe liberarse con {@link #dejarDeEscuchar}.
     *
     * @param pacienteUid Identificador del paciente en Firebase Authentication.
     * @param oyente      Callback que recibe la rutina activa cada vez que los datos cambian.
     * @return El escuchador registrado, necesario para liberarlo después.
     */
    public static ValueEventListener escucharRutinaActiva(String pacienteUid, OnRutinaListener oyente) {
        ValueEventListener escuchador = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                RutinaAsignada activa = null;
                for (DataSnapshot hijo : snapshot.getChildren()) {
                    RutinaAsignada candidata = desdeSnapshot(hijo);
                    if (candidata.esPendiente()
                            && (activa == null || candidata.getCreadaEn() > activa.getCreadaEn())) {
                        activa = candidata;
                    }
                }
                oyente.onRutinaActiva(activa);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                oyente.onRutinaActiva(null);
            }
        };
        referenciaRutinas(pacienteUid).addValueEventListener(escuchador);
        return escuchador;
    }

    /**
     * Elimina un escuchador registrado con {@link #escucharRutinaActiva}.
     *
     * @param pacienteUid Identificador del paciente cuyo nodo se escuchaba.
     * @param escuchador  Escuchador devuelto al registrarlo.
     */
    public static void dejarDeEscuchar(String pacienteUid, ValueEventListener escuchador) {
        referenciaRutinas(pacienteUid).removeEventListener(escuchador);
    }

    /**
     * Marca una rutina asignada como completada.
     *
     * @param pacienteUid Identificador del paciente.
     * @param rutinaId    Identificador de la rutina a completar.
     * @param callback    Callback opcional con el resultado; puede ser null.
     */
    public static void completarRutina(String pacienteUid, String rutinaId, OnResultCallback callback) {
        referenciaRutinas(pacienteUid)
                .child(rutinaId)
                .child("estado")
                .setValue(RutinaAsignada.ESTADO_COMPLETADA)
                .addOnSuccessListener(sinUso -> notificar(callback, true))
                .addOnFailureListener(error -> notificar(callback, false));
    }

    /**
     * Guarda una sesión completada en la nube usando una clave generada con push().getKey().
     * La escritura se sincroniza en segundo plano; la app no necesita esperar la respuesta.
     *
     * @param pacienteUid  Identificador del paciente.
     * @param rutinaId     Identificador de la rutina asignada, o null si fue una rutina libre.
     * @param repeticiones Repeticiones realizadas.
     * @param meta         Repeticiones objetivo de la rutina.
     * @param duracionSeg  Duración de la sesión en segundos.
     * @param correcciones Cantidad de avisos de profundidad emitidos.
     * @param callback     Callback opcional con el resultado; puede ser null.
     */
    public static void guardarSesionNube(String pacienteUid, String rutinaId, int repeticiones, int meta,
                                         long duracionSeg, int correcciones, OnResultCallback callback) {
        DatabaseReference nodo = FirebaseDatabase.getInstance().getReference(NODO_SESIONES).child(pacienteUid);
        String sesionId = nodo.push().getKey();
        if (sesionId == null) {
            notificar(callback, false);
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("rutinaId", rutinaId == null ? "" : rutinaId);
        datos.put("ejercicio", "Sentadillas");
        datos.put("repeticiones", repeticiones);
        datos.put("meta", meta);
        datos.put("duracionSeg", duracionSeg);
        datos.put("correcciones", correcciones);
        datos.put("fecha", new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        datos.put("creadaEn", ServerValue.TIMESTAMP);

        nodo.child(sesionId)
                .setValue(datos)
                .addOnSuccessListener(sinUso -> notificar(callback, true))
                .addOnFailureListener(error -> notificar(callback, false));
    }

    /**
     * Obtiene la referencia al nodo de rutinas de un paciente.
     *
     * @param pacienteUid Identificador del paciente.
     * @return Referencia a "rutinasAsignadas/{pacienteUid}".
     */
    private static DatabaseReference referenciaRutinas(String pacienteUid) {
        return FirebaseDatabase.getInstance().getReference(NODO_RUTINAS).child(pacienteUid);
    }

    /**
     * Convierte un nodo de Realtime Database en una rutina asignada, con valores por defecto
     * para los campos ausentes.
     *
     * @param snapshot Datos del nodo de la rutina.
     * @return RutinaAsignada con los datos del nodo.
     */
    private static RutinaAsignada desdeSnapshot(DataSnapshot snapshot) {
        String ejercicio = snapshot.child("ejercicio").getValue(String.class);
        Integer repeticiones = snapshot.child("repeticiones").getValue(Integer.class);
        String estado = snapshot.child("estado").getValue(String.class);
        Long creadaEn = snapshot.child("creadaEn").getValue(Long.class);
        return new RutinaAsignada(
                snapshot.getKey(),
                ejercicio != null ? ejercicio : "Sentadillas",
                repeticiones != null && repeticiones > 0 ? repeticiones : RutinaAsignada.REPETICIONES_POR_DEFECTO,
                estado != null ? estado : RutinaAsignada.ESTADO_PENDIENTE,
                creadaEn != null ? creadaEn : 0L);
    }

    /**
     * Invoca un callback opcional de forma segura.
     *
     * @param callback Callback que puede ser null.
     * @param exito    Resultado a informar.
     */
    private static void notificar(OnResultCallback callback, boolean exito) {
        if (callback != null) {
            callback.onResult(exito);
        }
    }
}