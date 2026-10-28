package com.hotel.modelo;

/**
 * Representa a un cliente (huésped) del hotel.
 * Cada objeto de esta clase equivale a una fila de la tabla "clientes".
 */
public class Cliente {

    // Atributos privados (aplicamos encapsulamiento)
    private int id;
    private String nombre;
    private String dpi;
    private String telefono;
    private String correo;

    // Constructor para clientes NUEVOS (aún no tienen id, lo genera la Base de Datos)
    public Cliente(String nombre, String dpi, String telefono, String correo) {
        this.nombre = nombre;
        this.dpi = dpi;
        this.telefono = telefono;
        this.correo = correo;
    }

    // Constructor para clientes LEÍDOS de la Base de Datos (ya tienen id)
    public Cliente(int id, String nombre, String dpi, String telefono, String correo) {
        this(nombre, dpi, telefono, correo); // reutiliza el constructor anterior
        this.id = id;
    }

    // Getters: permiten leer los atributos desde otras clases
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDpi() { return dpi; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }

    // Setters: permiten modificar datos (se usarán al actualizar un cliente)
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setCorreo(String correo) { this.correo = correo; }

    
        /** Texto que se muestra en las listas desplegables. */
    @Override
    public String toString() {
        return nombre + " (" + dpi + ")";
    }

}

