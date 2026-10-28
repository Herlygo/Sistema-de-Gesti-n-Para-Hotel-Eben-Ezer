package com.hotel.dao;

import com.hotel.bd.Conexion;
import com.hotel.modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object): concentra todas las operaciones SQL
 * relacionadas con clientes. La interfaz gráfica nunca escribe SQL;
 * solo llama a los métodos de esta clase.
 */
public class ClienteDAO {

    /** Inserta un cliente nuevo en la tabla "clientes". */
    public void insertar(Cliente c) throws SQLException {
        // Los signos ? son parámetros: evitan la inyección SQL
        String sql = "INSERT INTO clientes (nombre, dpi, telefono, correo) VALUES (?, ?, ?, ?)";

        // try-with-resources: cierra la conexión automáticamente al terminar
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Se reemplaza cada ? por su valor (el número es la posición)
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDpi());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());

            ps.executeUpdate(); // ejecuta el INSERT
        }
    }

    /** Devuelve todos los clientes registrados. */
    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, dpi, telefono, correo FROM clientes ORDER BY id";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) { // ejecuta el SELECT

            // Cada vuelta del while es una fila del resultado
            while (rs.next()) {
                lista.add(new Cliente(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("dpi"),
                    rs.getString("telefono"),
                    rs.getString("correo")
                ));
            }
        }
        return lista;
    }
}