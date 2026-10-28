package com.hotel.vista;

import com.hotel.dao.ClienteDAO;
import com.hotel.dao.HabitacionDAO;
import com.hotel.dao.ReservaDAO;
import com.hotel.modelo.Cliente;
import com.hotel.modelo.Habitacion;
import com.hotel.modelo.Reserva;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
 * Ventana para crear reservaciones, consultarlas y gestionar su ciclo de vida:
 * confirmar, cancelar, check-in y check-out.
 */
public class VentanaReservas extends JFrame {

    // Listas desplegables que contienen objetos completos (no solo texto)
    private JComboBox<Cliente> cmbCliente = new JComboBox<>();
    private JComboBox<Habitacion> cmbHabitacion = new JComboBox<>();

    // Campos de texto
    private JTextField txtEntrada = new JTextField(LocalDate.now().toString()); // hoy por defecto
    private JTextField txtDias = new JTextField("1");

    // Tabla de reservaciones (solo lectura)
    private DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Cliente", "Habitación", "Entrada", "Días",
                         "Salida", "Total", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private JTable tabla = new JTable(modeloTabla);

    // DAOs: cada uno se encarga de una tabla
    private ReservaDAO reservaDAO = new ReservaDAO();
    private HabitacionDAO habitacionDAO = new HabitacionDAO();
    private ClienteDAO clienteDAO = new ClienteDAO();

    public VentanaReservas() {
        setTitle("HOTEL EBEN-EZER - Reservaciones");
        setSize(900, 550);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Formulario (norte) ---
        JPanel panelForm = new JPanel(new GridLayout(5, 2, 5, 5));
        panelForm.add(new JLabel("Cliente:"));                       panelForm.add(cmbCliente);
        panelForm.add(new JLabel("Fecha de entrada (aaaa-mm-dd):")); panelForm.add(txtEntrada);
        panelForm.add(new JLabel("Cantidad de días:"));              panelForm.add(txtDias);

        JButton btnBuscar = new JButton("1. Buscar habitaciones disponibles");
        panelForm.add(btnBuscar);
        panelForm.add(new JLabel("")); // celda vacía para alinear

        panelForm.add(new JLabel("Habitación disponible:"));         panelForm.add(cmbHabitacion);
        add(panelForm, BorderLayout.NORTH);

        // --- Tabla (centro) ---
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- Botones de acción (sur) ---
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnReservar = new JButton("2. Reservar");
        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnEntrada = new JButton("Check-in");
        JButton btnSalida = new JButton("Check-out");
        JButton btnCancelar = new JButton("Cancelar reserva");
        panelBotones.add(btnReservar);
        panelBotones.add(btnConfirmar);
        panelBotones.add(btnEntrada);
        panelBotones.add(btnSalida);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);

        // --- Eventos ---
        btnBuscar.addActionListener(e -> buscarDisponibles());
        btnReservar.addActionListener(e -> reservar());
        // Los cuatro botones de estado reutilizan un mismo método con una "acción"
        btnConfirmar.addActionListener(e -> accionSobreSeleccion("confirmar"));
        btnEntrada.addActionListener(e -> accionSobreSeleccion("entrada"));
        btnSalida.addActionListener(e -> accionSobreSeleccion("salida"));
        btnCancelar.addActionListener(e -> accionSobreSeleccion("cancelar"));

        cargarClientes();
        cargarTabla();
        EstiloHotel.aplicar(this);
    }

    /** Llena la lista de clientes desde la base de datos. */
    private void cargarClientes() {
        try {
            cmbCliente.removeAllItems();
            for (Cliente c : clienteDAO.listar()) {
                cmbCliente.addItem(c); // se muestra con el toString() de Cliente
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los clientes: " + ex.getMessage());
        }
    }

    /** Convierte el texto de la fecha en LocalDate; devuelve null si es inválido. */
    private LocalDate leerFechaEntrada() {
        try {
            return LocalDate.parse(txtEntrada.getText().trim()); // formato aaaa-mm-dd
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Fecha inválida. Usa el formato aaaa-mm-dd (ej. 2026-11-15).");
            return null;
        }
    }

    /** Convierte el texto de días en entero; devuelve -1 si es inválido. */
    private int leerDias() {
        try {
            int dias = Integer.parseInt(txtDias.getText().trim());
            if (dias <= 0) throw new NumberFormatException();
            return dias;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Los días deben ser un número entero mayor que 0.");
            return -1;
        }
    }

    /** RF-04: muestra solo las habitaciones libres para las fechas indicadas. */
    private void buscarDisponibles() {
        LocalDate entrada = leerFechaEntrada();
        int dias = leerDias();
        if (entrada == null || dias == -1) return; // datos inválidos: se detiene

        try {
            cmbHabitacion.removeAllItems();
            LocalDate salida = entrada.plusDays(dias); // fecha de salida calculada
            for (Habitacion h : habitacionDAO.listarDisponibles(entrada, salida)) {
                cmbHabitacion.addItem(h);
            }
            if (cmbHabitacion.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay habitaciones disponibles en esas fechas.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    /** RF-05: crea la reservación con los datos del formulario. */
    private void reservar() {
        Cliente cliente = (Cliente) cmbCliente.getSelectedItem();
        Habitacion habitacion = (Habitacion) cmbHabitacion.getSelectedItem();
        LocalDate entrada = leerFechaEntrada();
        int dias = leerDias();

        if (cliente == null || habitacion == null) {
            JOptionPane.showMessageDialog(this, "Elige un cliente y busca una habitación disponible primero.");
            return;
        }
        if (entrada == null || dias == -1) return;
        if (entrada.isBefore(LocalDate.now())) { // no se reserva en el pasado
            JOptionPane.showMessageDialog(this, "La fecha de entrada no puede ser anterior a hoy.");
            return;
        }

        try {
            // El constructor calcula fecha de salida y total automáticamente
            Reserva r = new Reserva(cliente.getId(), habitacion.getId(),
                                    entrada, dias, habitacion.getTarifaNoche());
            reservaDAO.insertar(r);
            cargarTabla();
            cmbHabitacion.removeAllItems(); // obliga a buscar de nuevo (la lista cambió)
            JOptionPane.showMessageDialog(this,
                "Reservación creada.\nSalida: " + r.getFechaSalida() + "\nTotal: Q" + r.getTotal());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo reservar: " + ex.getMessage());
        }
    }

    /** Aplica confirmar / check-in / check-out / cancelar a la fila seleccionada. */
    private void accionSobreSeleccion(String accion) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero una reservación de la tabla.");
            return;
        }
        try {
            int id = (int) modeloTabla.getValueAt(fila, 0); // columna 0 = ID
            switch (accion) {
                case "confirmar": reservaDAO.confirmar(id); break;
                case "entrada":   reservaDAO.registrarEntrada(id); break;
                case "salida":    reservaDAO.registrarSalida(id); break;
                case "cancelar":  reservaDAO.cancelar(id); break;
            }
            cargarTabla(); // refresca para ver el nuevo estado
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /** Lee las reservaciones de la BD y las muestra en la tabla. */
    private void cargarTabla() {
        try {
            modeloTabla.setRowCount(0);
            for (Reserva r : reservaDAO.listar()) {
                modeloTabla.addRow(new Object[]{
                    r.getId(), r.getNombreCliente(), r.getNumeroHabitacion(),
                    r.getFechaEntrada(), r.getDias(), r.getFechaSalida(),
                    r.getTotal(), r.getEstado()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar: " + ex.getMessage());
        }
    }
}