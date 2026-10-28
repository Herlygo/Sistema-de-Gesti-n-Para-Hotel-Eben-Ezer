package com.hotel.vista;

import com.hotel.dao.UsuarioDAO;
import com.hotel.modelo.Usuario;
import com.hotel.util.Sesion;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 * Configuración de la cuenta: muestra los datos del usuario que inició sesión
 * y le permite cambiar su propia contraseña.
 */
public class VentanaConfiguracion extends JFrame {

    // Campos para cambiar la contraseña (JPasswordField oculta lo escrito)
    private JPasswordField txtActual = new JPasswordField();
    private JPasswordField txtNueva = new JPasswordField();
    private JPasswordField txtConfirmar = new JPasswordField();

    // Objeto que habla con la base de datos (Supabase)
    private UsuarioDAO dao = new UsuarioDAO();

    public VentanaConfiguracion() {
        setTitle("HOTEL EBEN-EZER - Configuración");
        setSize(600, 430);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // cierra solo esta ventana
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Usuario que inició sesión (guardado en Sesion al hacer login)
        Usuario u = Sesion.getUsuario();

        // --- Datos de la cuenta (solo lectura) ---
        JPanel panelDatos = new JPanel(new GridLayout(3, 2, 5, 5));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Mi cuenta"));
        panelDatos.add(new JLabel("Nombre:"));   panelDatos.add(new JLabel(u.getNombre()));
        panelDatos.add(new JLabel("Usuario:"));  panelDatos.add(new JLabel(u.getUsuario()));
        panelDatos.add(new JLabel("Rol:"));      panelDatos.add(new JLabel(u.getRol()));
        add(panelDatos, BorderLayout.NORTH);

        // --- Formulario para cambiar la contraseña ---
        JPanel panelClave = new JPanel(new GridLayout(4, 2, 5, 5));
        panelClave.setBorder(BorderFactory.createTitledBorder("Cambiar mi contraseña"));
        panelClave.add(new JLabel("Contraseña actual:"));           panelClave.add(txtActual);
        panelClave.add(new JLabel("Nueva contraseña (mín. 6):"));   panelClave.add(txtNueva);
        panelClave.add(new JLabel("Confirmar nueva contraseña:"));  panelClave.add(txtConfirmar);

        JButton btnCambiar = new JButton("Cambiar contraseña");
        panelClave.add(new JLabel(""));   // celda vacía para alinear el botón
        panelClave.add(btnCambiar);

        // Se mete en otro panel para que no se estire hasta el fondo de la ventana
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(panelClave, BorderLayout.NORTH);
        add(contenedor, BorderLayout.CENTER);

        btnCambiar.addActionListener(e -> cambiarClave());

        EstiloHotel.aplicar(this); // mismo diseño que las demás ventanas (siempre al final)
    }

    /** Valida los campos y cambia la contraseña del usuario que inició sesión. */
    private void cambiarClave() {
        String actual = new String(txtActual.getPassword());
        String nueva = new String(txtNueva.getPassword());
        String confirmar = new String(txtConfirmar.getPassword());

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa los tres campos.");
            return;
        }
        if (nueva.length() < 6) {
            JOptionPane.showMessageDialog(this, "La nueva contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!nueva.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "La confirmación no coincide con la nueva contraseña.");
            return;
        }
        if (nueva.equals(actual)) {
            JOptionPane.showMessageDialog(this, "La nueva contraseña debe ser distinta de la actual.");
            return;
        }
        try {
            Usuario u = Sesion.getUsuario();

            // Se comprueba la contraseña actual con el mismo método que usa el login
            if (dao.autenticar(u.getUsuario(), actual) == null) {
                JOptionPane.showMessageDialog(this, "La contraseña actual es incorrecta.");
                return;
            }
            dao.cambiarClave(u.getId(), nueva); // se guarda como hash
            txtActual.setText("");
            txtNueva.setText("");
            txtConfirmar.setText("");
            JOptionPane.showMessageDialog(this, "Contraseña actualizada correctamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}