package com.hotel.modelo;

import java.time.LocalDate;

/**
 * Representa una reservación. Relaciona un cliente con una habitación
 * durante un rango de fechas. Equivale a una fila de la tabla "reservas".
 */
public class Reserva {

    private int id;
    private int idCliente;          // llave foránea hacia clientes
    private int idHabitacion;       // llave foránea hacia habitaciones
    private LocalDate fechaEntrada;
    private int dias;
    private LocalDate fechaSalida;  // se calcula: entrada + días
    private double total;           // tarifa por noche x días
    private String estado;          // Pendiente, Confirmada, En curso, Finalizada, Cancelada

    // Datos extra solo para mostrar en la tabla (vienen de un JOIN)
    private String nombreCliente;
    private String numeroHabitacion;

    /** Constructor para reservaciones NUEVAS (lo usa la ventana al reservar). */
    public Reserva(int idCliente, int idHabitacion, LocalDate fechaEntrada,
                   int dias, double tarifaNoche) {
        this.idCliente = idCliente;
        this.idHabitacion = idHabitacion;
        this.fechaEntrada = fechaEntrada;
        this.dias = dias;
        this.fechaSalida = fechaEntrada.plusDays(dias); // fecha de salida calculada
        this.total = tarifaNoche * dias;                // total calculado
        this.estado = "Pendiente";                      // toda reserva inicia pendiente
    }

    /** Constructor para reservaciones LEÍDAS de la BD (para mostrarlas en la tabla). */
    public Reserva(int id, String nombreCliente, String numeroHabitacion,
                   LocalDate fechaEntrada, int dias, LocalDate fechaSalida,
                   double total, String estado) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.numeroHabitacion = numeroHabitacion;
        this.fechaEntrada = fechaEntrada;
        this.dias = dias;
        this.fechaSalida = fechaSalida;
        this.total = total;
        this.estado = estado;
    }

    // Getters
    public int getId() { return id; }
    public int getIdCliente() { return idCliente; }
    public int getIdHabitacion() { return idHabitacion; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public int getDias() { return dias; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public double getTotal() { return total; }
    public String getEstado() { return estado; }
    public String getNombreCliente() { return nombreCliente; }
    public String getNumeroHabitacion() { return numeroHabitacion; }
}