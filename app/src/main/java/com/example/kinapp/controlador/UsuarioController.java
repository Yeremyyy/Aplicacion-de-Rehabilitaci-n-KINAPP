package com.example.kinapp.controlador;

import com.example.kinapp.modelo.Usuario;
import java.util.ArrayList;

public class UsuarioController {

    private static ArrayList<Usuario> listaUsuarios = new ArrayList<>();

    static {
        listaUsuarios.add(new Usuario("admin@gmail.cl", "123456", "Paciente pepito"));
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
                return true;
            }
        }
        return false;
    }
}