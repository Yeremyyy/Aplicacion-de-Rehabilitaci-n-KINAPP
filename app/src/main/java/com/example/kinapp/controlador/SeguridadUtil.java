package com.example.kinapp.controlador;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilidades criptográficas para el almacenamiento seguro de contraseñas.
 * Usa PBKDF2WithHmacSHA256 con sal aleatoria por usuario.
 */
public final class SeguridadUtil {

    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_CLAVE_BITS = 256;
    private static final int LONGITUD_SAL_BYTES = 16;

    private SeguridadUtil() {}

    /**
     * Genera el hash de una contraseña con una sal aleatoria nueva.
     *
     * @param contrasena Contraseña en texto plano.
     * @return String con formato "sal:hash" codificado en Base64.
     * @throws IllegalStateException Si el algoritmo PBKDF2 no está disponible.
     */
    public static String hashContrasena(String contrasena) {
        byte[] sal = new byte[LONGITUD_SAL_BYTES];
        new SecureRandom().nextBytes(sal);
        byte[] hash = derivar(contrasena, sal);
        Base64.Encoder enc = Base64.getEncoder();
        return enc.encodeToString(sal) + ":" + enc.encodeToString(hash);
    }

    /**
     * Verifica una contraseña contra el valor almacenado, con comparación en tiempo constante.
     *
     * @param contrasena Contraseña ingresada por el usuario.
     * @param almacenado Valor "sal:hash" guardado en la base de datos.
     * @return true si coincide; false si no coincide o el formato es inválido.
     */
    public static boolean verificarContrasena(String contrasena, String almacenado) {
        if (almacenado == null) return false;
        try {
            String[] partes = almacenado.split(":");
            if (partes.length != 2) return false;
            byte[] sal = Base64.getDecoder().decode(partes[0]);
            byte[] esperado = Base64.getDecoder().decode(partes[1]);
            return MessageDigest.isEqual(esperado, derivar(contrasena, sal));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] derivar(String contrasena, byte[] sal) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, ITERACIONES, LONGITUD_CLAVE_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo derivar el hash de la contraseña", e);
        }
    }
}