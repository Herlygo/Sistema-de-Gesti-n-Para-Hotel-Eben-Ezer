package com.hotel.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase responsable de abrir la conexión con la base de datos de Supabase.
 * Todas las demás clases le piden la conexión a ella.
 */
public class Conexion {

    // Parte 1: dirección. Unimos host + puerto + base de datos, con SSL obligatorio.
    //   host:     aws-0-us-east-1.pooler.supabase.com
    //   port:     5432
    //   database: postgres
    private static final String URL =
        "jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require";

    // Parte 2: usuario 
    private static final String USUARIO = "postgres.ubytiydbkfsabvqopdvv";

    // Parte 3: la contraseña que obtuvimos al crear el proyecto en supabase
    private static final String CLAVE = "Ysgq7bLh96jqZVAy";

    /** Abre y devuelve una conexión nueva a la base de datos. */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}