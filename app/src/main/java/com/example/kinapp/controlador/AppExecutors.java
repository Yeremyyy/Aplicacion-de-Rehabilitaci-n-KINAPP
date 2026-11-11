package com.example.kinapp.controlador;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Gestor global de hilos para la aplicación.
 * Permite ejecutar operaciones pesadas (como consultas y escrituras en SQLite en mi caso)
 * en un hilo secundario y retornar los resultados al hilo principal (UI).
 */
public class AppExecutors {

    private static final int THREAD_COUNT = 3;

    private final Executor diskIO;
    private final Executor mainThread;

    private static AppExecutors instance;

    /**
     * Obtiene la instancia única (Singleton) del administrador de hilos.
     *
     * @return AppExecutors Instancia única configurada para la aplicación.
     */
    public static AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors(
                    Executors.newFixedThreadPool(THREAD_COUNT),
                    new MainThreadExecutor()
            );
        }
        return instance;
    }

    /**
     * Constructor privado del gestor de hilos.
     *
     * @param diskIO     Ejecutor para tareas de E/S en disco y bases de datos.
     * @param mainThread Ejecutor para tareas que deben actualizar la interfaz de usuario.
     */
    private AppExecutors(Executor diskIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.mainThread = mainThread;
    }

    /**
     * Proporciona acceso al ejecutor de tareas en segundo plano.
     *
     * @return Executor Hilo dedicado a operaciones pesadas fuera del hilo principal.
     */
    public Executor diskIO() {
        return diskIO;
    }

    /**
     * Proporciona acceso al ejecutor del hilo principal de Android.
     *
     * @return Executor Hilo principal encargado del renderizado de la UI.
     */
    public Executor mainThread() {
        return mainThread;
    }

    /**
     * Clase interna privada para ejecutar tareas en el hilo principal de la interfaz gráfica.
     */
    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        /**
         * Ejecuta un comando en el hilo principal de la aplicación.
         *
         * @param command Tarea Runnable a ejecutar en el hilo principal.
         * @throws NullPointerException Si el comando recibido es nulo.
         */
        @Override
        public void execute(Runnable command) {
            if (command == null) {
                throw new NullPointerException("El comando Runnable no puede ser nulo.");
            }
            mainThreadHandler.post(command);
        }
    }
}