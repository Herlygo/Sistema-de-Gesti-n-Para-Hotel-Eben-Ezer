package com.hotel.dao;

import com.hotel.bd.Conexion;
import com.hotel.modelo.Usuario;
import com.hotel.util.HashUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.hotel.bd.Conexion;
import com.hotel.modelo.Usuario;
import com.hotel.util.HashUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO de usuarios: valida las credenciales del inicio de sesión. */
public class UsuarioDAO {

    /**
     * Busca un usuario con ese nombre y contraseña.
     * @return el Usuario si las credenciales son correctas; null si no lo son
     */
    public Usuario autenticar(String usuario, String clave) throws SQLException {
        // Se compara el HASH de lo que escribió el usuario con el hash guardado
        String sql = "SELECT id, nombre, usuario, rol FROM usuarios "
                   + "WHERE usuario = ? AND clave = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, HashUtil.sha256(clave));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // encontró una fila: credenciales correctas
                    return new Usuario(rs.getInt("id"), rs.getString("nombre"),
                                       rs.getString("usuario"), rs.getString("rol"));
                }
                return null; // ninguna fila: usuario o contraseña incorrectos
            }
        }
    }
    
        /** Crea un usuario nuevo; la contraseña se guarda como hash, nunca en texto plano. */
    public void insertar(String nombre, String usuario, String clave, String rol)
            throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, usuario, clave, rol) VALUES (?, ?, ?, ?)";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, usuario);
            ps.setString(3, HashUtil.sha256(clave)); // se convierte en hash antes de guardar
            ps.setString(4, rol);
            ps.executeUpdate();

        } catch (SQLException ex) {
            // Código 23505 = violación de unicidad: el nombre de usuario ya existe
            if ("23505".equals(ex.getSQLState())) {
                throw new SQLException("Ese nombre de usuario ya existe.");
            }
            throw ex; // cualquier otro error se propaga tal cual
        }
    }

    /** Lista todos los usuarios. Nunca se devuelve la contraseña. */
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, usuario, rol FROM usuarios ORDER BY id";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) { // cada vuelta es una fila de la tabla
                lista.add(new Usuario(rs.getInt("id"), rs.getString("nombre"),
                                      rs.getString("usuario"), rs.getString("rol")));
            }
        }
        return lista;
    }

    /** Cambia la contraseña de un usuario (guardándola como hash). */
    public void cambiarClave(int id, String nuevaClave) throws SQLException {
        String sql = "UPDATE usuarios SET clave = ? WHERE id = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, HashUtil.sha256(nuevaClave));
            ps.setInt(2, id); // el WHERE evita modificar a todos los usuarios
            ps.executeUpdate();
        }
    }

    /** Elimina un usuario por su id. */
    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}