package com.hotel.dao;

import com.hotel.bd.Conexion;
import com.hotel.modelo.Reserva;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de reservaciones: consultas SQL de la tabla "reservas" y
 * la lógica de cambio de estado (confirmar, cancelar, check-in, check-out).
 */
public class ReservaDAO {

    /** Verifica si una habitación está libre en un rango de fechas. */
    public boolean estaDisponible(int idHabitacion, LocalDate entrada, LocalDate salida)
            throws SQLException {
        // Cuenta reservaciones activas que se cruzan con el rango pedido
        String sql = "SELECT COUNT(*) FROM reservas "
                   + "WHERE id_habitacion = ? "
                   + "AND estado IN ('Pendiente','Confirmada','En curso') "
                   + "AND fecha_entrada < ? "
                   + "AND fecha_salida > ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idHabitacion);
            ps.setDate(2, java.sql.Date.valueOf(salida));
            ps.setDate(3, java.sql.Date.valueOf(entrada));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) == 0; // 0 cruces = disponible
            }
        }
    }

    /** Guarda una reservación nueva, solo si la habitación está libre (RF-05 y RF-06). */
    public void insertar(Reserva r) throws SQLException {
        // Validación en Java: mensaje claro para el usuario
        if (!estaDisponible(r.getIdHabitacion(), r.getFechaEntrada(), r.getFechaSalida())) {
            throw new SQLException("La habitación ya está reservada en ese rango de fechas.");
        }

        // fecha_salida NO se inserta: la calcula la base de datos sola
        String sql = "INSERT INTO reservas (id_cliente, id_habitacion, fecha_entrada, dias, total) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getIdCliente());
            ps.setInt(2, r.getIdHabitacion());
            ps.setDate(3, java.sql.Date.valueOf(r.getFechaEntrada()));
            ps.setInt(4, r.getDias());
            ps.setDouble(5, r.getTotal());
            ps.executeUpdate();
        }
        // Si Java no detectara el cruce, la restricción "sin_solapamiento"
        // de la BD rechazaría igualmente el INSERT (doble seguridad).
    }

    /** Lista las reservaciones con el nombre del cliente y el número de habitación (RF-09). */
    public List<Reserva> listar() throws SQLException {
        List<Reserva> lista = new ArrayList<>();

        // JOIN: une reservas con clientes y habitaciones para mostrar nombres, no solo ids
        String sql = "SELECT r.id, c.nombre, h.numero, r.fecha_entrada, r.dias, "
                   + "r.fecha_salida, r.total, r.estado "
                   + "FROM reservas r "
                   + "JOIN clientes c ON c.id = r.id_cliente "
                   + "JOIN habitaciones h ON h.id = r.id_habitacion "
                   + "ORDER BY r.id DESC";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Reserva(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("numero"),
                    rs.getDate("fecha_entrada").toLocalDate(),
                    rs.getInt("dias"),
                    rs.getDate("fecha_salida").toLocalDate(),
                    rs.getDouble("total"),
                    rs.getString("estado")));
            }
        }
        return lista;
    }

    /** Pendiente -> Confirmada. */
    public void confirmar(int id) throws SQLException {
        cambiarEstadoSimple(id,
            "UPDATE reservas SET estado = 'Confirmada' WHERE id = ? AND estado = 'Pendiente'",
            "Solo se pueden confirmar reservaciones en estado Pendiente.");
    }

    /** Pendiente o Confirmada -> Cancelada. */
    public void cancelar(int id) throws SQLException {
        cambiarEstadoSimple(id,
            "UPDATE reservas SET estado = 'Cancelada' "
          + "WHERE id = ? AND estado IN ('Pendiente','Confirmada')",
            "Solo se pueden cancelar reservaciones Pendientes o Confirmadas.");
    }

    /** Check-in: Confirmada -> En curso, y la habitación pasa a Ocupada. */
    public void registrarEntrada(int id) throws SQLException {
        ejecutarEnTransaccion(id,
            "UPDATE reservas SET estado = 'En curso', entrada_real = now() "
          + "WHERE id = ? AND estado = 'Confirmada'",
            "UPDATE habitaciones SET estado = 'Ocupada' "
          + "WHERE id = (SELECT id_habitacion FROM reservas WHERE id = ?)",
            "El check-in solo se puede hacer en reservaciones Confirmadas.");
    }

    /** Check-out: En curso -> Finalizada, y la habitación vuelve a Disponible. */
    public void registrarSalida(int id) throws SQLException {
        ejecutarEnTransaccion(id,
            "UPDATE reservas SET estado = 'Finalizada', salida_real = now() "
          + "WHERE id = ? AND estado = 'En curso'",
            "UPDATE habitaciones SET estado = 'Disponible' "
          + "WHERE id = (SELECT id_habitacion FROM reservas WHERE id = ?)",
            "El check-out solo se puede hacer en reservaciones En curso.");
    }

    /** Ejecuta un UPDATE de estado; si no afectó ninguna fila, el estado actual no lo permite. */
    private void cambiarEstadoSimple(int id, String sql, String mensajeError)
            throws SQLException {
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) { // 0 filas = la condición del WHERE no se cumplió
                throw new SQLException(mensajeError);
            }
        }
    }

    /**
     * Ejecuta DOS updates (reserva y habitación) como una sola operación.
     * Si uno falla, se deshace todo (rollback) para no dejar datos inconsistentes.
     */
    private void ejecutarEnTransaccion(int id, String sqlReserva, String sqlHabitacion,
                                       String mensajeError) throws SQLException {
        try (Connection con = Conexion.obtenerConexion()) {
            con.setAutoCommit(false); // inicia la transacción
            try (PreparedStatement ps1 = con.prepareStatement(sqlReserva);
                 PreparedStatement ps2 = con.prepareStatement(sqlHabitacion)) {

                ps1.setInt(1, id);
                if (ps1.executeUpdate() == 0) {
                    throw new SQLException(mensajeError);
                }
                ps2.setInt(1, id);
                ps2.executeUpdate();

                con.commit(); // confirma los dos cambios juntos
            } catch (SQLException ex) {
                con.rollback(); // deshace todo si algo falló
                throw ex;
            }
        }
    }
}
