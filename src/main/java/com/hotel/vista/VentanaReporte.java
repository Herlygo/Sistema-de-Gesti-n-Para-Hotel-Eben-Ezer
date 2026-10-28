package com.hotel.vista;

import com.hotel.dao.ReporteDAO;
import com.hotel.util.Sesion;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Reporte diario del hotel. Solo el administrador puede abrirlo. */
public class VentanaReporte extends JFrame {

    // Rango de fechas del filtro: por defecto, los últimos 7 días
    private JTextField txtDesde = new JTextField(LocalDate.now().minusDays(6).toString(), 10);
    private JTextField txtHasta = new JTextField(LocalDate.now().toString(), 10);

    // Etiqueta donde se muestran los totales del período
    private JLabel lblTotales = new JLabel(" ");

    // Modelo de la tabla (solo lectura): una fila por cada día del rango
    private DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"Fecha", "Ingresos", "Personas hospedadas",
                         "Personas retiradas", "Habitaciones ocupadas"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private JTable tabla = new JTable(modeloTabla);

    // Objeto que ejecuta la consulta del reporte en Supabase
    private ReporteDAO dao = new ReporteDAO();

    public VentanaReporte() {
        // Barrera de seguridad: si no es administrador, la ventana no se abre
        if (!Sesion.esAdministrador()) {
            JOptionPane.showMessageDialog(null, "Solo el administrador puede ver los reportes.");
            dispose();
            return;
        }

        setTitle("HOTEL EBEN-EZER - Reportes");
        setSize(900, 550);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // cierra solo esta ventana
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Filtro de fechas (parte superior) ---
        JPanel panelFiltro = new JPanel(new FlowLayout());
        panelFiltro.add(new JLabel("Desde (aaaa-mm-dd):"));
        panelFiltro.add(txtDesde);
        panelFiltro.add(new JLabel("Hasta:"));
        panelFiltro.add(txtHasta);
        JButton btnConsultar = new JButton("Consultar");
        panelFiltro.add(btnConsultar);
        add(panelFiltro, BorderLayout.NORTH);

        // --- Tabla con scroll (centro) ---
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- Totales y notas explicativas (parte inferior) ---
        JPanel panelSur = new JPanel(new GridLayout(2, 1));
        panelSur.add(lblTotales);
        panelSur.add(new JLabel("<html><small>Ingresos: reservaciones con check-out ese día. "
            + "Hospedadas/retiradas: personas con check-in/check-out ese día. "
            + "Ocupadas: habitaciones con huésped al cierre del día.</small></html>"));
        add(panelSur, BorderLayout.SOUTH);

        btnConsultar.addActionListener(e -> consultar());
        consultar(); // al abrir, muestra los últimos 7 días
        EstiloHotel.aplicar(this); // mismo diseño que Reservaciones (siempre al final)
    }

    /** Valida el rango de fechas, consulta el reporte y llena la tabla. */
    private void consultar() {
        try {
            // Convierte el texto de los campos en fechas (formato aaaa-mm-dd)
            LocalDate desde = LocalDate.parse(txtDesde.getText().trim());
            LocalDate hasta = LocalDate.parse(txtHasta.getText().trim());

            if (desde.isAfter(hasta)) {
                JOptionPane.showMessageDialog(this, "La fecha inicial no puede ser posterior a la final.");
                return;
            }
            if (hasta.isAfter(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "La fecha final no puede ser posterior a hoy.");
                return;
            }
            if (ChronoUnit.DAYS.between(desde, hasta) > 366) {
                JOptionPane.showMessageDialog(this, "El rango máximo es de un año.");
                return;
            }

            // Cada fila: [0] fecha, [1] ingresos, [2] hospedadas, [3] retiradas, [4] ocupadas
            List<Object[]> filas = dao.reportePorDia(desde, hasta);

            modeloTabla.setRowCount(0); // vacía la tabla antes de llenarla
            double totalIngresos = 0;
            long totalHospedadas = 0, totalRetiradas = 0;

            for (Object[] f : filas) {
                totalIngresos += (double) f[1];
                totalHospedadas += (long) f[2];
                totalRetiradas += (long) f[3];
                modeloTabla.addRow(new Object[]{
                    f[0],
                    String.format(Locale.US, "Q%,.2f", (double) f[1]), // ingresos con formato
                    f[2], f[3], f[4]});
            }

            lblTotales.setText(String.format(Locale.US,
                "  Totales del período:  Ingresos Q%,.2f   |   Personas hospedadas: %d   |   Personas retiradas: %d",
                totalIngresos, totalHospedadas, totalRetiradas));

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Fecha inválida. Usa el formato aaaa-mm-dd (ej. 2026-10-28).");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo generar el reporte: " + ex.getMessage());
        }
    }
}