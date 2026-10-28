package com.hotel.modelo;

/**
 * Representa una habitación del hotel.
 * Cada objeto equivale a una fila de la tabla "habitaciones".
 */
public class Habitacion {

    // Atributos privados (encapsulamiento)
    private int id;
    private String numero;       // número visible de la habitación (ej. "101")
    private String tipo;         // Sencilla, Doble o Suite
    private int capacidad;       // cantidad máxima de personas
    private double tarifaNoche;  // precio por noche
    private String estado;       // Disponible, Ocupada o Mantenimiento

    // Constructor para habitaciones NUEVAS (el id lo genera la BD)
    public Habitacion(String numero, String tipo, int capacidad,
                      double tarifaNoche, String estado) {
        this.numero = numero;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.tarifaNoche = tarifaNoche;
        this.estado = estado;
    }

    // Constructor para habitaciones LEÍDAS de la BD (ya tienen id)
    public Habitacion(int id, String numero, String tipo, int capacidad,
                      double tarifaNoche, String estado) {
        this(numero, tipo, capacidad, tarifaNoche, estado); // reutiliza el anterior
        this.id = id;
    }

    // Getters: leer los atributos desde otras clases
    public int getId() { return id; }
    public String getNumero() { return numero; }
    public String getTipo() { return tipo; }
    public int getCapacidad() { return capacidad; }
    public double getTarifaNoche() { return tarifaNoche; }
    public String getEstado() { return estado; }

    // Setters: modificar datos que pueden cambiar con el tiempo
    public void setTarifaNoche(double tarifaNoche) { this.tarifaNoche = tarifaNoche; }
    public void setEstado(String estado) { this.estado = estado; }

        /** Texto que se muestra en las listas desplegables. */
    @Override
    public String toString() {
        return numero + " - " + tipo + " (Q" + tarifaNoche + "/noche)";
    }

}