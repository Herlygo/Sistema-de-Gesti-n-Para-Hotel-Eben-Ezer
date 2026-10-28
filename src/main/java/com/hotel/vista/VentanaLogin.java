package com.hotel.vista;

import com.hotel.dao.UsuarioDAO;
import com.hotel.modelo.Usuario;
import com.hotel.util.Sesion;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/** Inicio de sesión de HOTEL EBEN-EZER. */
public class VentanaLogin extends JFrame {
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();
    private final UsuarioDAO dao = new UsuarioDAO();

    public VentanaLogin() {
        setTitle("HOTEL EBEN-EZER | Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setMinimumSize(new Dimension(760, 500));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(EstiloHotel.FONDO);

        JPanel marca = new JPanel(new BorderLayout(0, 18));
        marca.setBackground(EstiloHotel.NAVY);
        marca.setBorder(new EmptyBorder(38, 30, 34, 30));
        JLabel logo = new JLabel("", SwingConstants.CENTER);
        URL logoUrl = getClass().getResource("/com/hotel/vista/logo-eben-ezer.png");
        if (logoUrl != null) logo.setIcon(new ImageIcon(new ImageIcon(logoUrl).getImage().getScaledInstance(260, 125, java.awt.Image.SCALE_SMOOTH)));
        else {
            logo.setText("HOTEL EBEN-EZER");
            logo.setForeground(EstiloHotel.AZUL_CLARO);
            logo.setFont(new Font("Serif", Font.BOLD, 26));
        }
        marca.add(logo, BorderLayout.CENTER);
        JLabel marcaPie = new JLabel("SISTEMA DE GESTIÓN HOTELERA", SwingConstants.CENTER);
        marcaPie.setForeground(EstiloHotel.AZUL_CLARO);
        marcaPie.setFont(new Font("SansSerif", Font.BOLD, 11));
        marca.add(marcaPie, BorderLayout.SOUTH);
        marca.setPreferredSize(new Dimension(360, 0));
        add(marca, BorderLayout.WEST);

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setBackground(EstiloHotel.FONDO);
        derecha.setBorder(new EmptyBorder(52, 54, 42, 54));
        JPanel formulario = new JPanel(new GridLayout(0, 1, 0, 10));
        formulario.setOpaque(false);
        JLabel titulo = new JLabel("Iniciar sesión");
        titulo.setForeground(EstiloHotel.NAVY);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 29));
        JLabel subtitulo = new JLabel("Ingresa tus credenciales para continuar");
        subtitulo.setForeground(EstiloHotel.MUTED);
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        formulario.add(titulo);
        formulario.add(subtitulo);
        formulario.add(espacio(7));

        JLabel lUsuario = etiqueta("Usuario");
        txtUsuario.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtUsuario.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloHotel.BORDE), new EmptyBorder(10, 12, 10, 12)));
        txtUsuario.setPreferredSize(new Dimension(300, 43));
        JLabel lClave = etiqueta("Contraseña");
        txtClave.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtClave.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloHotel.BORDE), new EmptyBorder(10, 12, 10, 12)));
        formulario.add(lUsuario);
        formulario.add(txtUsuario);
        formulario.add(lClave);
        formulario.add(txtClave);
        JCheckBox mostrar = new JCheckBox("Mostrar contraseña");
        mostrar.setOpaque(false);
        mostrar.setForeground(EstiloHotel.MUTED);
        mostrar.addActionListener(e -> txtClave.setEchoChar(mostrar.isSelected() ? (char) 0 : '•'));
        formulario.add(mostrar);
        formulario.add(espacio(4));

        JButton btnEntrar = new JButton("Ingresar    →");
        btnEntrar.setBackground(EstiloHotel.AZUL);
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnEntrar.setBorder(new EmptyBorder(13, 14, 13, 14));
        btnEntrar.setFocusPainted(false);
        btnEntrar.setOpaque(true);
        btnEntrar.addActionListener(e -> iniciarSesion());
        formulario.add(btnEntrar);
        JLabel pie = new JLabel("Acceso exclusivo para personal autorizado");
        pie.setForeground(EstiloHotel.MUTED);
        pie.setFont(new Font("SansSerif", Font.PLAIN, 11));
        derecha.add(formulario, BorderLayout.CENTER);
        derecha.add(pie, BorderLayout.SOUTH);
        add(derecha, BorderLayout.CENTER);
        getRootPane().setDefaultButton(btnEntrar);
    }

    private JPanel espacio(int alto) {
        JPanel p = new JPanel(); p.setOpaque(false); p.setPreferredSize(new Dimension(1, alto)); return p;
    }
    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto); l.setForeground(EstiloHotel.TEXTO); l.setFont(new Font("SansSerif", Font.BOLD, 12)); return l;
    }
    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        String clave = new String(txtClave.getPassword());
        if (usuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe tu usuario y contraseña.", "Datos incompletos", JOptionPane.WARNING_MESSAGE); return;
        }
        try {
            Usuario u = dao.autenticar(usuario, clave);
            if (u == null) {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "No se pudo iniciar sesión", JOptionPane.ERROR_MESSAGE);
                txtClave.setText(""); return;
            }
            Sesion.iniciar(u);
            new MenuPrincipal().setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new VentanaLogin().setVisible(true)); }
}
