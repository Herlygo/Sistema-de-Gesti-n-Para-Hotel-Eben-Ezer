package com.hotel.dao;

import com.hotel.bd.Conexion;
import com.hotel.modelo.Habitacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;

/**
 * DAO de habitaciones: todas las consultas SQL de la tabla "habitaciones".
 */
public class HabitacionDAO {

    /** Inserta una habitación nueva. */
    public void insertar(Habitacion h) throws SQLException {
        String sql = "INSERT INTO habitaciones (numero, tipo, capacidad, tarifa_noche, estado) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, h.getNumero());
            ps.setString(2, h.getTipo());
            ps.setInt(3, h.getCapacidad());
            ps.setDouble(4, h.getTarifaNoche());
            ps.setString(5, h.getEstado());

            ps.executeUpdate(); // ejecuta el INSERT
        }
    }

    /** Devuelve todas las habitaciones ordenadas por número. */
    public List<Habitacion> listar() throws SQLException {
        List<Habitacion> lista = new ArrayList<>();
        String sql = "SELECT id, numero, tipo, capacidad, tarifa_noche, estado "
                   + "FROM habitaciones ORDER BY numero";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) { // cada vuelta es una fila
                lista.add(new Habitacion(
                    rs.getInt("id"),
                    rs.getString("numero"),
                    rs.getString("tipo"),
                    rs.getInt("capacidad"),
                    rs.getDouble("tarifa_noche"),
                    rs.getString("estado")
                ));
            }
        }
        return lista;
    }

    /** Cambia el estado de una habitación (Disponible, Ocupada, Mantenimiento). */
    public void actualizarEstado(int id, String nuevoEstado) throws SQLException {
        String sql = "UPDATE habitaciones SET estado = ? WHERE id = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, id); // el WHERE evita modificar todas las filas
            ps.executeUpdate();
        }
    }
    
        /**
     * Devuelve las habitaciones libres en un rango de fechas.
     * Excluye las que están en mantenimiento y las que ya tienen una
     * reservación activa que se cruza con el rango pedido.
     */
    public List<Habitacion> listarDisponibles(LocalDate entrada, LocalDate salida)
            throws SQLException {
        List<Habitacion> lista = new ArrayList<>();

        // NOT EXISTS: la habitación se incluye solo si NO hay reservas que se crucen
        String sql = "SELECT h.id, h.numero, h.tipo, h.capacidad, h.tarifa_noche, h.estado "
                   + "FROM habitaciones h "
                   + "WHERE h.estado <> 'Mantenimiento' "
                   + "AND NOT EXISTS ("
                   + "  SELECT 1 FROM reservas r "
                   + "  WHERE r.id_habitacion = h.id "
                   + "  AND r.estado IN ('Pendiente','Confirmada','En curso') "
                   + "  AND r.fecha_entrada < ? "   // la reserva existente empieza antes de que termine la nueva
                   + "  AND r.fecha_salida > ?"     // y termina después de que empiece la nueva
                   + ") ORDER BY h.numero";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(salida));
            ps.setDate(2, java.sql.Date.valueOf(entrada));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Habitacion(
                        rs.getInt("id"), rs.getString("numero"), rs.getString("tipo"),
                        rs.getInt("capacidad"), rs.getDouble("tarifa_noche"),
                        rs.getString("estado")));
                }
            }
        }
        return lista;
    }
    
}