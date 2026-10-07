package com.example.kinapp.controlador;

import com.example.kinapp.modelo.Usuario;
import java.util.ArrayList;

public class UsuarioController {

    private static ArrayList<Usuario> listaUsuarios = new ArrayList<>();
    private static Usuario usuarioActual = null;

    static {
        listaUsuarios.add(new Usuario("admin@kinapp.cl", "123456", "Paciente Demo"));
    }

    public static boolean registrarUsuario(String correo, String contrasena, String nombre) {
        for (Usuario u : listaUsuarios) {
            if (u.getCorreo().equals(correo)) {
                return false;
            }
        }
        listaUsuarios.add(new Usuario(correo, contrasena, nombre));
        return true;
    }

    public static boolean validarLogin(String correo, String contrasena) {
        for (Usuario u : listaUsuarios) {
            if (u.getCorreo().equals(correo) && u.getContrasena().equals(contrasena)) {
                usuarioActual = u;
                return true;
            }
        }
        return false;
    }

    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}