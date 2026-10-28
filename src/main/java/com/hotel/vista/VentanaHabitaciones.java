package com.hotel.vista;

import com.hotel.dao.HabitacionDAO;
import com.hotel.modelo.Habitacion;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana para registrar habitaciones, consultarlas y cambiar su estado.
 */
public class VentanaHabitaciones extends JFrame {

    // Campos del formulario
    private JTextField txtNumero = new JTextField();
    // JComboBox = lista desplegable; evita que el usuario escriba valores inválidos
    private JComboBox<String> cmbTipo =
        new JComboBox<>(new String[]{"Sencilla", "Doble", "Suite"});
    private JTextField txtCapacidad = new JTextField();
    private JTextField txtTarifa = new JTextField();
    private JComboBox<String> cmbEstado =
        new JComboBox<>(new String[]{"Disponible", "Ocupada", "Mantenimiento"});

    // Modelo de la tabla; se bloquea la edición directa de celdas
    private DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Número", "Tipo", "Capacidad", "Tarifa/noche", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false; // la tabla es solo de lectura
        }
    };
    private JTable tabla = new JTable(modeloTabla);

    // Objeto que habla con la base de datos
    private HabitacionDAO dao = new HabitacionDAO();

    /** Constructor: arma la interfaz. */
    public VentanaHabitaciones() {
        setTitle("HOTEL EBEN-EZER - Habitaciones");
        setSize(750, 500);
        // DISPOSE cierra solo esta ventana (EXIT cerraría todo el programa)
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Formulario (norte): 6 filas x 2 columnas ---
        JPanel panelForm = new JPanel(new GridLayout(6, 2, 5, 5));
        panelForm.add(new JLabel("Número:"));        panelForm.add(txtNumero);
        panelForm.add(new JLabel("Tipo:"));          panelForm.add(cmbTipo);
        panelForm.add(new JLabel("Capacidad:"));     panelForm.add(txtCapacidad);
        panelForm.add(new JLabel("Tarifa/noche:"));  panelForm.add(txtTarifa);
        panelForm.add(new JLabel("Estado:"));        panelForm.add(cmbEstado);

        JButton btnGuardar = new JButton("Guardar habitación");
        JButton btnEstado = new JButton("Cambiar estado de la fila seleccionada");
        panelForm.add(btnGuardar);
        panelForm.add(btnEstado);
        add(panelForm, BorderLayout.NORTH);

        // --- Tabla (centro) ---
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Eventos de los botones
        btnGuardar.addActionListener(e -> guardar());
        btnEstado.addActionListener(e -> cambiarEstado());

        // Control de permisos: solo el administrador puede crear o editar habitaciones
        if (!com.hotel.util.Sesion.esAdministrador()) {
            btnGuardar.setEnabled(false);   // no puede registrar habitaciones
            btnEstado.setEnabled(false);    // no puede cambiar su estado manualmente
            txtNumero.setEnabled(false);
            txtCapacidad.setEnabled(false);
            txtTarifa.setEnabled(false);
            cmbTipo.setEnabled(false);
            cmbEstado.setEnabled(false);
        }

        cargarTabla(); // al abrir, muestra las habitaciones existentes
        EstiloHotel.aplicar(this);
    }

    /** Valida el formulario y guarda la habitación en la BD. */
    private void guardar() {
        if (txtNumero.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "El número de habitación es obligatorio.");
            return;
        }
        try {
            // Convierte el texto a número; falla si el usuario escribió letras
            int capacidad = Integer.parseInt(txtCapacidad.getText().trim());
            double tarifa = Double.parseDouble(txtTarifa.getText().trim());

            if (capacidad <= 0 || tarifa <= 0) {
                JOptionPane.showMessageDialog(this, "Capacidad y tarifa deben ser mayores que 0.");
                return;
            }

            Habitacion h = new Habitacion(
                txtNumero.getText().trim(),
                (String) cmbTipo.getSelectedItem(),   // valor elegido en la lista
                capacidad,
                tarifa,
                (String) cmbEstado.getSelectedItem());

            dao.insertar(h);
            limpiarFormulario();
            cargarTabla();
            JOptionPane.showMessageDialog(this, "Habitación guardada correctamente.");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Capacidad debe ser un número entero y tarifa un número (ej. 250.50).");
        } catch (Exception ex) {
            // Por ejemplo: número de habitación repetido o fallo de conexión
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    /** Cambia el estado de la fila seleccionada usando el valor del combo "Estado". */
    private void cambiarEstado() {
        int fila = tabla.getSelectedRow(); // -1 si no hay fila seleccionada
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero una habitación de la tabla.");
            return;
        }
        try {
            int id = (int) modeloTabla.getValueAt(fila, 0); // columna 0 = ID
            dao.actualizarEstado(id, (String) cmbEstado.getSelectedItem());
            cargarTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    /** Lee las habitaciones de la BD y las muestra en la tabla. */
    private void cargarTabla() {
        try {
            modeloTabla.setRowCount(0); // vacía la tabla antes de llenarla
            for (Habitacion h : dao.listar()) {
                modeloTabla.addRow(new Object[]{
                    h.getId(), h.getNumero(), h.getTipo(),
                    h.getCapacidad(), h.getTarifaNoche(), h.getEstado()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar: " + ex.getMessage());
        }
    }

    /** Deja los campos de texto vacíos. */
    private void limpiarFormulario() {
        txtNumero.setText("");
        txtCapacidad.setText("");
        txtTarifa.setText("");
    }
}