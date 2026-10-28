package com.hotel.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;

/** Colores y estilos compartidos de HOTEL EBEN-EZER. */
public final class EstiloHotel {
    public static final Color NAVY = new Color(0x0A, 0x19, 0x31);
    public static final Color AZUL_CLARO = new Color(0xB3, 0xCF, 0xE5);
    public static final Color MALVA = new Color(0xA4, 0x7F, 0xA7);
    public static final Color AZUL = new Color(0x1A, 0x3D, 0x63);
    public static final Color FONDO = new Color(0xF6, 0xFA, 0xFD);
    public static final Color TEXTO = new Color(0x16, 0x2A, 0x45);
    public static final Color MUTED = new Color(0x5E, 0x71, 0x88);
    public static final Color BORDE = new Color(0xD9, 0xE5, 0xEF);

    private EstiloHotel() {}

    public static void aplicar(JFrame ventana) {
        ventana.getContentPane().setBackground(FONDO);
        recorrer(ventana.getContentPane());
    }

    private static void recorrer(Container contenedor) {
        for (Component c : contenedor.getComponents()) {
            if (c instanceof JPanel panel) {
                Color predeterminado = UIManager.getColor("Panel.background");
                if (predeterminado == null || panel.getBackground().equals(predeterminado)) panel.setBackground(FONDO);
            }
            if (c instanceof JButton b) {
                b.setFont(new Font("SansSerif", Font.BOLD, 12));
                b.setBackground(AZUL);
                b.setForeground(Color.WHITE);
                b.setFocusPainted(false);
                b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
                b.setBorder(new EmptyBorder(9, 13, 9, 13));
                b.setOpaque(true);
            } else if (c instanceof JTextField || c instanceof JPasswordField || c instanceof JComboBox<?>) {
                c.setFont(new Font("SansSerif", Font.PLAIN, 13));
                if (c instanceof javax.swing.JComponent jc) {
                    jc.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BORDE), new EmptyBorder(7, 8, 7, 8)));
                }
            } else if (c instanceof JTable tabla) {
                tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
                tabla.setForeground(TEXTO);
                tabla.setBackground(Color.WHITE);
                tabla.setRowHeight(29);
                tabla.setGridColor(BORDE);
                tabla.setSelectionBackground(AZUL_CLARO);
                tabla.setSelectionForeground(TEXTO);
                JTableHeader header = tabla.getTableHeader();
                header.setFont(new Font("SansSerif", Font.BOLD, 12));
                header.setBackground(NAVY);
                header.setForeground(Color.WHITE);
                header.setReorderingAllowed(false);
            } else if (c instanceof JScrollPane scroll) {
                scroll.setBorder(BorderFactory.createLineBorder(BORDE));
                scroll.getViewport().setBackground(Color.WHITE);
            } else if (c instanceof JLabel label) {
                if (label.getFont().getSize() <= 12) label.setForeground(TEXTO);
            }
            if (c instanceof Container hijo) recorrer(hijo);
        }
    }
}
