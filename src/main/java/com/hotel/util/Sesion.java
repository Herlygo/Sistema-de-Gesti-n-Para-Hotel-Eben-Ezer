package com.hotel.util;

import com.hotel.modelo.Usuario;

/** Guarda el usuario que inició sesión mientras el programa está abierto. */
public class Sesion {

    // static: pertenece a la clase, hay una sola sesión activa a la vez
    private static Usuario usuarioActual;

    public static void iniciar(Usuario u) { usuarioActual = u; }
    public static void cerrar() { usuarioActual = null; }
    public static Usuario getUsuario() { return usuarioActual; }

    /** Atajo para preguntar si el usuario actual es administrador. */
    public static boolean esAdministrador() {
        return usuarioActual != null && "Administrador".equals(usuarioActual.getRol());
    }
}
