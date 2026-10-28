package com.hotel.dao;

import com.hotel.bd.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** DAO del reporte diario: ingresos, personas y habitaciones ocupadas por día. */
public class ReporteDAO {

    /**
     * Devuelve un renglón por cada día del rango, con:
     * [0] fecha, [1] ingresos, [2] personas hospedadas, [3] personas retiradas,
     * [4] habitaciones ocupadas.
     */
    public List<Object[]> reportePorDia(LocalDate desde, LocalDate hasta) throws SQLException {
        List<Object[]> lista = new ArrayList<>();

        String sql =
            // "dias": genera una fila por cada fecha del rango pedido
            "WITH dias AS ("
          + "  SELECT generate_series(CAST(? AS date)::timestamp, CAST(? AS date)::timestamp, "
          + "         interval '1 day')::date AS dia), "
            // "estancias": reservaciones con check-in, con fechas en hora de Guatemala
          + "estancias AS ("
          + "  SELECT id_habitacion, personas, total, "
          + "         (entrada_real AT TIME ZONE 'America/Guatemala')::date AS d_entrada, "
          + "         (salida_real  AT TIME ZONE 'America/Guatemala')::date AS d_salida "
          + "  FROM reservas WHERE entrada_real IS NOT NULL) "
            // Para cada día se calculan los cuatro datos con subconsultas
          + "SELECT d.dia, "
          + "  COALESCE((SELECT SUM(e.total)    FROM estancias e WHERE e.d_salida  = d.dia), 0) AS ingresos, "
          + "  COALESCE((SELECT SUM(e.personas) FROM estancias e WHERE e.d_entrada = d.dia), 0) AS hospedadas, "
          + "  COALESCE((SELECT SUM(e.personas) FROM estancias e WHERE e.d_salida  = d.dia), 0) AS retiradas, "
          + "  (SELECT COUNT(DISTINCT e.id_habitacion) FROM estancias e "
          + "     WHERE e.d_entrada <= d.dia "
          + "       AND (e.d_salida IS NULL OR e.d_salida > d.dia)) AS ocupadas "
          + "FROM dias d ORDER BY d.dia";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(desde));
            ps.setDate(2, java.sql.Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getDate("dia").toLocalDate(),
                        rs.getDouble("ingresos"),
                        rs.getLong("hospedadas"),
                        rs.getLong("retiradas"),
                        rs.getLong("ocupadas")});
                }
            }
        }
        return lista;
    }
}