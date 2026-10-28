package com.hotel.vista;

import com.hotel.dao.ClienteDAO;
import com.hotel.modelo.Cliente;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana para registrar y consultar clientes del hotel.
 */
public class VentanaClientes extends JFrame {

    // Campos de texto del formulario (donde el usuario escribe)
    private JTextField txtNombre = new JTextField();
    private JTextField txtDpi = new JTextField();
    private JTextField txtTelefono = new JTextField();
    private JTextField txtCorreo = new JTextField();

    // Modelo de la tabla: guarda las filas que se muestran en pantalla.
    // El segundo parámetro (0) indica que inicia sin filas.
    private DefaultTableModel modeloTabla =
        new DefaultTableModel(new String[]{"ID", "Nombre", "DPI", "Teléfono", "Correo"}, 0);

    // Objeto que se encarga de hablar con la base de datos
    private ClienteDAO dao = new ClienteDAO();

    /** Constructor: arma toda la interfaz al crear la ventana. */
    public VentanaClientes() {
        // Configuración básica de la ventana
        setTitle("HOTEL EBEN-EZER - Registro de Clientes");
        setSize(700, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // cierra el programa al cerrar la ventana
        setLocationRelativeTo(null);             // centra la ventana en la pantalla
        setLayout(new BorderLayout(10, 10));     // divide la ventana en norte/centro/etc.

        // --- Panel del formulario (parte superior) ---
        // GridLayout(5, 2): 5 filas y 2 columnas (etiqueta + campo)
        JPanel panelForm = new JPanel(new GridLayout(5, 2, 5, 5));
        panelForm.add(new JLabel("Nombre:"));   panelForm.add(txtNombre);
        panelForm.add(new JLabel("DPI:"));      panelForm.add(txtDpi);
        panelForm.add(new JLabel("Teléfono:")); panelForm.add(txtTelefono);
        panelForm.add(new JLabel("Correo:"));   panelForm.add(txtCorreo);

        JButton btnGuardar = new JButton("Guardar cliente");
        panelForm.add(btnGuardar);
        add(panelForm, BorderLayout.NORTH);

        // --- Tabla con scroll (centro) ---
        add(new JScrollPane(new JTable(modeloTabla)), BorderLayout.CENTER);

        // Al hacer clic en el botón se ejecuta el método guardar()
        btnGuardar.addActionListener(e -> guardar());

        // Al abrir la ventana se cargan los clientes que ya existen en la Base de Datos
        cargarTabla();
        EstiloHotel.aplicar(this);
    }

    /** Toma los datos del formulario y los guarda en la base de datos. */
    private void guardar() {
        // Validación mínima: nombre y DPI son obligatorios
        if (txtNombre.getText().isBlank() || txtDpi.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Nombre y DPI son obligatorios.");
            return; // sale del método sin guardar
        }
        try {
            // Se crea el objeto Cliente con lo que escribió el usuario, en este caso sus datos
            Cliente c = new Cliente(
                txtNombre.getText().trim(),
                txtDpi.getText().trim(),
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim());

            dao.insertar(c);   // guarda los datos en Supabase
            limpiarFormulario();
            cargarTabla();     // refresca la tabla con el nuevo registro
            JOptionPane.showMessageDialog(this, "Cliente guardado correctamente.");
        } catch (Exception ex) {
            // Si algo falla (red, DPI repetido) se avisa al usuario
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    /** Lee los clientes de la Base de Datos y los muestra en la tabla. */
    private void cargarTabla() {
        try {
            modeloTabla.setRowCount(0); // vacía la tabla antes de llenarla
            for (Cliente c : dao.listar()) {
                // Cada cliente se convierte en una fila de la tabla
                modeloTabla.addRow(new Object[]{
                    c.getId(), c.getNombre(), c.getDpi(), c.getTelefono(), c.getCorreo()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar: " + ex.getMessage());
        }
    }

    /** Deja los campos vacíos después de guardar. */
    private void limpiarFormulario() {
        txtNombre.setText("");
        txtDpi.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
    }
}