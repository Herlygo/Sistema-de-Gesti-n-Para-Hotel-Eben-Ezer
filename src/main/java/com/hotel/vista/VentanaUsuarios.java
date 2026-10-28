package com.hotel.vista;

import com.hotel.dao.UsuarioDAO;
import com.hotel.modelo.Usuario;
import com.hotel.util.Sesion;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana para administrar los usuarios del sistema.
 * Solo el administrador puede abrirla.
 */
public class VentanaUsuarios extends JFrame {

    // Campos del formulario (donde se escriben los datos)
    private JTextField txtNombre = new JTextField();
    private JTextField txtUsuario = new JTextField();
    private JPasswordField txtClave = new JPasswordField(); // oculta lo escrito
    private JComboBox<String> cmbRol =
        new JComboBox<>(new String[]{"Recepcionista", "Administrador"});

    // Modelo de la tabla: guarda las filas que se ven en pantalla.
    // isCellEditable devuelve false para que la tabla sea de solo lectura.
    private DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[]{"ID", "Nombre", "Usuario", "Rol"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private JTable tabla = new JTable(modeloTabla);

    // Objeto que habla con la base de datos (Supabase)
    private UsuarioDAO dao = new UsuarioDAO();

    public VentanaUsuarios() {
        // Barrera de seguridad: si no es administrador, la ventana no se abre
        if (!Sesion.esAdministrador()) {
            JOptionPane.showMessageDialog(null, "Solo el administrador puede gestionar usuarios.");
            dispose();
            return;
        }

        setTitle("HOTEL EBEN-EZER - Usuarios");
        setSize(900, 550);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // cierra solo esta ventana
        setLocationRelativeTo(null);                // centra la ventana
        setLayout(new BorderLayout(10, 10));

        // --- Formulario (norte): 5 filas x 2 columnas ---
        JPanel panelForm = new JPanel(new GridLayout(5, 2, 5, 5));
        panelForm.add(new JLabel("Nombre completo:"));                panelForm.add(txtNombre);
        panelForm.add(new JLabel("Usuario (para iniciar sesión):"));  panelForm.add(txtUsuario);
        panelForm.add(new JLabel("Contraseña (mín. 6 caracteres):")); panelForm.add(txtClave);
        panelForm.add(new JLabel("Rol:"));                            panelForm.add(cmbRol);

        JButton btnCrear = new JButton("Crear usuario");
        JButton btnClave = new JButton("Cambiar contraseña del seleccionado");
        panelForm.add(btnCrear);
        panelForm.add(btnClave);
        add(panelForm, BorderLayout.NORTH);

        // --- Tabla con scroll (centro): aquí se muestran los usuarios ---
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- Botón eliminar (sur), en un panel igual al de Reservaciones ---
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnEliminar = new JButton("Eliminar usuario seleccionado");
        panelBotones.add(btnEliminar);
        add(panelBotones, BorderLayout.SOUTH);

        // Cada botón ejecuta su método al hacer clic
        btnCrear.addActionListener(e -> crear());
        btnClave.addActionListener(e -> cambiarClave());
        btnEliminar.addActionListener(e -> eliminar());

        cargarTabla(); // al abrir, muestra los usuarios que ya existen
        EstiloHotel.aplicar(this); // mismo diseño que Reservaciones (siempre al final)
    }

    /** Valida el formulario y crea el usuario. */
    private void crear() {
        String nombre = txtNombre.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String clave = new String(txtClave.getPassword());

        if (nombre.isEmpty() || usuario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y usuario son obligatorios.");
            return;
        }
        if (usuario.contains(" ")) { // el usuario de login no debe llevar espacios
            JOptionPane.showMessageDialog(this, "El usuario no puede contener espacios.");
            return;
        }
        if (clave.length() < 6) {
            JOptionPane.showMessageDialog(this, "La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        try {
            dao.insertar(nombre, usuario, clave, (String) cmbRol.getSelectedItem());
            limpiarFormulario();
            cargarTabla(); // refresca la tabla con el usuario nuevo
            JOptionPane.showMessageDialog(this, "Usuario creado correctamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    /** Cambia la contraseña del usuario seleccionado, usando la escrita en el formulario. */
    private void cambiarClave() {
        int fila = tabla.getSelectedRow(); // -1 si no hay fila seleccionada
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un usuario de la tabla.");
            return;
        }
        String clave = new String(txtClave.getPassword());
        if (clave.length() < 6) {
            JOptionPane.showMessageDialog(this,
                "Escribe la nueva contraseña (mín. 6 caracteres) en el campo Contraseña.");
            return;
        }
        try {
            int id = (int) modeloTabla.getValueAt(fila, 0); // columna 0 = ID
            dao.cambiarClave(id, clave);
            limpiarFormulario();
            JOptionPane.showMessageDialog(this, "Contraseña actualizada.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    /** Elimina el usuario seleccionado, con confirmación y sin permitir auto-eliminación. */
    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona primero un usuario de la tabla.");
            return;
        }
        int id = (int) modeloTabla.getValueAt(fila, 0);

        // Protección: el administrador no puede borrarse a sí mismo
        if (id == Sesion.getUsuario().getId()) {
            JOptionPane.showMessageDialog(this, "No puedes eliminar tu propio usuario.");
            return;
        }
        // Pide confirmación porque borrar no se puede deshacer
        int respuesta = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al usuario \"" + modeloTabla.getValueAt(fila, 2) + "\"?",
            "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) return;

        try {
            dao.eliminar(id);
            cargarTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    /** Lee los usuarios de la base de datos y los muestra en la tabla (nunca las contraseñas). */
    private void cargarTabla() {
        try {
            modeloTabla.setRowCount(0); // vacía la tabla antes de llenarla
            for (Usuario u : dao.listar()) {
                modeloTabla.addRow(new Object[]{
                    u.getId(), u.getNombre(), u.getUsuario(), u.getRol()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar: " + ex.getMessage());
        }
    }

    /** Deja los campos del formulario vacíos. */
    private void limpiarFormulario() {
        txtNombre.setText("");
        txtUsuario.setText("");
        txtClave.setText("");
    }
}