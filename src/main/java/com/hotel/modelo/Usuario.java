package com.hotel.modelo;

/** Representa a un usuario del sistema (administrador o recepcionista). */
public class Usuario {

    private int id;
    private String nombre;
    private String usuario;  // nombre de inicio de sesión
    private String rol;      // "Administrador" o "Recepcionista"

    public Usuario(int id, String nombre, String usuario, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUsuario() { return usuario; }
    public String getRol() { return rol; }
}