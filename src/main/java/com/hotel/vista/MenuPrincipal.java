package com.hotel.vista;

import com.hotel.dao.ClienteDAO;
import com.hotel.dao.HabitacionDAO;
import com.hotel.dao.ReservaDAO;
import com.hotel.modelo.Cliente;
import com.hotel.modelo.Habitacion;
import com.hotel.modelo.Reserva;
import com.hotel.util.Sesion;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.FlowLayout;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Panel principal de HOTEL EBEN-EZER, con resumen conectado a los datos disponibles. */
public class MenuPrincipal extends JFrame {
    // DAOs: cada uno consulta una tabla de Supabase
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    // Etiquetas que se actualizan con los datos de las tarjetas
    private final JLabel lblClientes = new JLabel("—");
    private final JLabel lblHabitaciones = new JLabel("—");
    private final JLabel lblReservas = new JLabel("—");
    private final JLabel lblIngresos = new JLabel("—");
    private final JLabel lblDisponibles = new JLabel("Disponibles: —");
    private final JLabel lblOcupadas = new JLabel("Ocupadas: —");
    private final JLabel lblMantenimiento = new JLabel("Mantenimiento: —");

    // Modelos de las tablas del panel (solo lectura)
    private final DefaultTableModel modeloReservas = new DefaultTableModel(
            new String[]{"Cliente", "Habitación", "Entrada", "Salida", "Estado"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final DefaultTableModel modeloActividad = new DefaultTableModel(
            new String[]{"Fecha", "Movimiento", "Detalle"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private DonutPanel donut;

    public MenuPrincipal() {
        setTitle("HOTEL EBEN-EZER | Panel principal");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 700));

        // CAMBIO: el tamaño se ajusta al área útil de la pantalla (sin la barra de tareas)
        java.awt.Rectangle pantalla = java.awt.GraphicsEnvironment
                .getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1280, pantalla.width), Math.min(820, pantalla.height));

        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(EstiloHotel.FONDO);
        add(crearBarraLateral(), BorderLayout.WEST);
        add(crearCuerpo(), BorderLayout.CENTER);
        cargarResumen(); // al abrir, carga los datos desde Supabase
    }

    /** Barra lateral azul marino con el logo y la navegación. */
    private JPanel crearBarraLateral() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(EstiloHotel.NAVY);
        sidebar.setPreferredSize(new Dimension(220, 0));
        JPanel marca = new JPanel(new BorderLayout(0, 7));
        marca.setBackground(EstiloHotel.NAVY);
        marca.setBorder(new EmptyBorder(22, 15, 20, 15));
        JLabel logo = new JLabel("", SwingConstants.CENTER);
        URL url = getClass().getResource("/com/hotel/vista/logo-eben-ezer.png");
        if (url != null) logo.setIcon(new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(175, 82, java.awt.Image.SCALE_SMOOTH)));
        else { logo.setText("HOTEL EBEN-EZER"); logo.setForeground(EstiloHotel.AZUL_CLARO); }
        marca.add(logo, BorderLayout.CENTER);
        JLabel tagline = new JLabel("SISTEMA DE GESTIÓN HOTELERA", SwingConstants.CENTER);
        tagline.setForeground(EstiloHotel.AZUL_CLARO);
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 9));
        marca.add(tagline, BorderLayout.SOUTH);
        sidebar.add(marca, BorderLayout.NORTH);

        // Botones de navegación: cada uno abre su ventana
        JPanel nav = new JPanel(new GridLayout(0, 1, 0, 5));
        nav.setBackground(EstiloHotel.NAVY);
        nav.setBorder(new EmptyBorder(6, 10, 8, 10));
        nav.add(navButton("⌂   Inicio", true, () -> cargarResumen()));
        nav.add(navButton("♙   Clientes", false, () -> new VentanaClientes().setVisible(true)));
        nav.add(navButton("▣   Habitaciones", false, () -> new VentanaHabitaciones().setVisible(true)));
        nav.add(navButton("▦   Reservas", false, () -> new VentanaReservas().setVisible(true)));
        nav.add(navButton("♟   Usuarios", false, () -> new VentanaUsuarios().setVisible(true)));
        nav.add(navButton("▥   Reportes", false, () -> new VentanaReporte().setVisible(true)));
        // CAMBIO: Configuración ahora abre su ventana (antes mostraba "no implementado")
        nav.add(navButton("⚙   Configuración", false, () -> new VentanaConfiguracion().setVisible(true)));
        sidebar.add(nav, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 12));
        pie.setBackground(EstiloHotel.NAVY);
        pie.setBorder(new EmptyBorder(15, 18, 20, 18));
        JLabel fecha = new JLabel("▦  " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        fecha.setForeground(EstiloHotel.AZUL_CLARO);
        fecha.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JLabel usuario = new JLabel(nombreActual() + " · " + rolActual());
        usuario.setForeground(Color.WHITE);
        usuario.setFont(new Font("SansSerif", Font.BOLD, 11));
        JButton salir = navButton("↪   Cerrar sesión", false, () -> {
            Sesion.cerrar(); new VentanaLogin().setVisible(true); dispose();
        });
        pie.add(fecha, BorderLayout.NORTH);
        pie.add(usuario, BorderLayout.CENTER);
        pie.add(salir, BorderLayout.SOUTH);
        sidebar.add(pie, BorderLayout.SOUTH);
        return sidebar;
    }

    /** Cuerpo del panel: tarjetas, disponibilidad, reservas recientes, actividad y accesos. */
    private JPanel crearCuerpo() {
        JPanel base = new JPanel(new BorderLayout(0, 16));
        base.setBackground(EstiloHotel.FONDO);
        base.setBorder(new EmptyBorder(20, 22, 20, 22));
        base.add(crearEncabezado(), BorderLayout.NORTH);

        // CAMBIO: panel especial que se puede desplazar hacia abajo (clase al final del archivo)
        PanelDesplazable contenido = new PanelDesplazable(new BorderLayout(0, 15));
        contenido.setOpaque(false);
        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 12, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(kpi("♙", "Clientes", lblClientes, "Clientes registrados", EstiloHotel.AZUL_CLARO, EstiloHotel.AZUL));
        tarjetas.add(kpi("▣", "Habitaciones", lblHabitaciones, "Total de habitaciones", EstiloHotel.AZUL, Color.WHITE));
        tarjetas.add(kpi("▦", "Reservas", lblReservas, "Reservaciones registradas", new Color(0xE8,0xD9,0xEA), EstiloHotel.NAVY));
        tarjetas.add(kpi("Ｑ", "Ingresos estimados", lblIngresos, "Reservas no canceladas", new Color(0xF0,0xE9,0xF2), EstiloHotel.NAVY));
        contenido.add(tarjetas, BorderLayout.NORTH);

        JPanel medio = new JPanel(new GridLayout(1, 2, 14, 0));
        medio.setOpaque(false);
        medio.add(crearDisponibilidad());
        medio.add(crearReservas());
        medio.setPreferredSize(new Dimension(0, 300)); // CAMBIO: altura propia para que se vean 5 filas
        contenido.add(medio, BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout(0, 8));
        inferior.setOpaque(false);
        JLabel actividadTitulo = tituloSeccion("Actividad reciente");
        JTable actividad = crearTabla(modeloActividad);
        JScrollPane scrollActividad = new JScrollPane(actividad);
        scrollActividad.setPreferredSize(new Dimension(500, 160)); // CAMBIO: antes 135, ahora caben 5 filas
        JPanel actividadCard = tarjetaBlanca();
        actividadCard.add(actividadTitulo, BorderLayout.NORTH);
        actividadCard.add(scrollActividad, BorderLayout.CENTER);
        inferior.add(actividadCard, BorderLayout.CENTER);
        JPanel accesos = crearAccesos();
        inferior.add(accesos, BorderLayout.SOUTH);
        contenido.add(inferior, BorderLayout.SOUTH);

        // CAMBIO: todo el contenido va dentro de un scroll vertical (aparece solo si no cabe)
        JScrollPane scrollContenido = new JScrollPane(contenido,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollContenido.setBorder(null);
        scrollContenido.setOpaque(false);
        scrollContenido.getViewport().setOpaque(false);
        scrollContenido.getVerticalScrollBar().setUnitIncrement(16);
        base.add(scrollContenido, BorderLayout.CENTER);
        return base;
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 4)); textos.setOpaque(false);
        JLabel t = new JLabel("Menú principal"); t.setForeground(EstiloHotel.NAVY); t.setFont(new Font("SansSerif", Font.BOLD, 27));
        JLabel s = new JLabel("Bienvenido/a, " + nombreActual() + ". Administra las operaciones de tu hotel."); s.setForeground(EstiloHotel.MUTED); s.setFont(new Font("SansSerif", Font.PLAIN, 12));
        textos.add(t); textos.add(s); p.add(textos, BorderLayout.CENTER);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5)); derecha.setOpaque(false);
        JLabel campana = new JLabel("●  Sistema activo"); campana.setForeground(EstiloHotel.AZUL); campana.setFont(new Font("SansSerif", Font.BOLD, 11));
        JLabel usuario = new JLabel("  ◉  " + nombreActual() + "  "); usuario.setOpaque(true); usuario.setBackground(Color.WHITE); usuario.setForeground(EstiloHotel.TEXTO); usuario.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloHotel.BORDE), new EmptyBorder(8, 10, 8, 10)));
        derecha.add(campana); derecha.add(usuario); p.add(derecha, BorderLayout.EAST);
        return p;
    }

    private JPanel kpi(String icono, String titulo, JLabel valor, String detalle, Color fondo, Color texto) {
        JPanel p = new JPanel(new BorderLayout(0, 8)); p.setBackground(fondo);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloHotel.BORDE), new EmptyBorder(14, 15, 13, 15)));
        JLabel i = new JLabel(icono + "   " + titulo); i.setForeground(texto); i.setFont(new Font("SansSerif", Font.BOLD, 12));
        valor.setForeground(texto); valor.setFont(new Font("SansSerif", Font.BOLD, 27));
        JLabel d = new JLabel(detalle); d.setForeground(texto); d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        p.add(i, BorderLayout.NORTH); p.add(valor, BorderLayout.CENTER); p.add(d, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearDisponibilidad() {
        JPanel p = tarjetaBlanca();
        p.add(tituloSeccion("Disponibilidad de habitaciones"), BorderLayout.NORTH);
        JPanel cuerpo = new JPanel(new BorderLayout(12, 0)); cuerpo.setOpaque(false);
        donut = new DonutPanel(); cuerpo.add(donut, BorderLayout.CENTER);
        JPanel leyenda = new JPanel(new GridLayout(3, 1, 0, 7)); leyenda.setOpaque(false);
        leyenda.add(lblDisponibles); leyenda.add(lblOcupadas); leyenda.add(lblMantenimiento);
        for (java.awt.Component c : leyenda.getComponents()) { c.setFont(new Font("SansSerif", Font.PLAIN, 11)); c.setForeground(EstiloHotel.TEXTO); }
        cuerpo.add(leyenda, BorderLayout.SOUTH);
        p.add(cuerpo, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearReservas() {
        JPanel p = tarjetaBlanca();
        p.add(tituloSeccion("Reservas recientes"), BorderLayout.NORTH);
        JTable tabla = crearTabla(modeloReservas);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JButton actualizar = new JButton("Actualizar datos");
        actualizar.setBackground(EstiloHotel.AZUL); actualizar.setForeground(Color.WHITE);
        actualizar.addActionListener(e -> cargarResumen());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); pie.setOpaque(false); pie.add(actualizar);
        p.add(pie, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearAccesos() {
        JPanel p = tarjetaBlanca();
        p.add(tituloSeccion("Accesos rápidos"), BorderLayout.NORTH);
        JPanel fila = new JPanel(new GridLayout(1, 3, 10, 0)); fila.setOpaque(false);
        fila.add(botonAcceso("＋  Nuevo cliente", () -> new VentanaClientes().setVisible(true)));
        fila.add(botonAcceso("▣  Ver habitaciones", () -> new VentanaHabitaciones().setVisible(true)));
        fila.add(botonAcceso("▦  Nueva reserva", () -> new VentanaReservas().setVisible(true)));
        p.add(fila, BorderLayout.CENTER);
        return p;
    }

    private JButton botonAcceso(String texto, Runnable accion) {
        JButton b = new JButton(texto); b.setHorizontalAlignment(SwingConstants.CENTER); b.setBackground(Color.WHITE); b.setForeground(EstiloHotel.AZUL); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloHotel.BORDE), new EmptyBorder(12, 8, 12, 8))); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.addActionListener(e -> accion.run()); return b;
    }
    private JPanel tarjetaBlanca() { JPanel p = new JPanel(new BorderLayout(0, 10)); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(EstiloHotel.BORDE), new EmptyBorder(13, 14, 13, 14))); return p; }
    private JLabel tituloSeccion(String texto) { JLabel l = new JLabel(texto); l.setForeground(EstiloHotel.NAVY); l.setFont(new Font("SansSerif", Font.BOLD, 14)); return l; }
    private JTable crearTabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo); t.setFont(new Font("SansSerif", Font.PLAIN, 11)); t.setForeground(EstiloHotel.TEXTO); t.setBackground(Color.WHITE); t.setRowHeight(25); t.setGridColor(EstiloHotel.BORDE); t.setSelectionBackground(EstiloHotel.AZUL_CLARO); t.setSelectionForeground(EstiloHotel.NAVY); t.setFillsViewportHeight(true); t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11)); t.getTableHeader().setBackground(EstiloHotel.AZUL_CLARO); t.getTableHeader().setForeground(EstiloHotel.NAVY); t.getTableHeader().setReorderingAllowed(false); return t;
    }
    private JButton navButton(String texto, boolean seleccionado, Runnable accion) {
        JButton b = new JButton(texto); b.setHorizontalAlignment(SwingConstants.LEFT); b.setFont(new Font("SansSerif", Font.BOLD, 12)); b.setForeground(seleccionado ? EstiloHotel.NAVY : new Color(220, 232, 244)); b.setBackground(seleccionado ? EstiloHotel.AZUL_CLARO : EstiloHotel.NAVY); b.setBorder(new EmptyBorder(12, 12, 12, 8)); b.setFocusPainted(false); b.setOpaque(true); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.addActionListener(e -> accion.run()); return b;
    }

    /** Lee los datos de Supabase y actualiza tarjetas, gráfica y tablas del panel. */
    private void cargarResumen() {
        int clientes = 0, habitaciones = 0, reservas = 0, disponibles = 0, ocupadas = 0, mantenimiento = 0;
        double total = 0;
        List<Reserva> listaReservas = new ArrayList<>();
        try { List<Cliente> lista = clienteDAO.listar(); clientes = lista.size(); } catch (Exception ex) { mostrarAvisoDatos("clientes", ex); }
        try {
            List<Habitacion> lista = habitacionDAO.listar(); habitaciones = lista.size();
            for (Habitacion h : lista) {
                if ("Disponible".equalsIgnoreCase(h.getEstado())) disponibles++;
                else if ("Ocupada".equalsIgnoreCase(h.getEstado())) ocupadas++;
                else if ("Mantenimiento".equalsIgnoreCase(h.getEstado())) mantenimiento++;
            }
        } catch (Exception ex) { mostrarAvisoDatos("habitaciones", ex); }
        try {
            listaReservas = reservaDAO.listar(); reservas = listaReservas.size();
            for (Reserva r : listaReservas) if (!"Cancelada".equalsIgnoreCase(r.getEstado())) total += r.getTotal();
        } catch (Exception ex) { mostrarAvisoDatos("reservas", ex); }
        lblClientes.setText(String.valueOf(clientes)); lblHabitaciones.setText(String.valueOf(habitaciones)); lblReservas.setText(String.valueOf(reservas));
        lblIngresos.setText(NumberFormat.getCurrencyInstance(new Locale("es", "GT")).format(total));
        lblDisponibles.setText("●  Disponibles: " + disponibles); lblOcupadas.setText("●  Ocupadas: " + ocupadas); lblMantenimiento.setText("●  Mantenimiento: " + mantenimiento);
        lblDisponibles.setForeground(new Color(0x2A, 0x8A, 0x6A)); lblOcupadas.setForeground(new Color(0x1A, 0x3D, 0x63)); lblMantenimiento.setForeground(EstiloHotel.MALVA);
        if (donut != null) { donut.setValores(disponibles, ocupadas, mantenimiento); }
        modeloReservas.setRowCount(0); modeloActividad.setRowCount(0);
        listaReservas.sort(Comparator.comparing(Reserva::getFechaEntrada, Comparator.nullsLast(Comparator.reverseOrder())));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 0; i < Math.min(5, listaReservas.size()); i++) {
            Reserva r = listaReservas.get(i);
            String entrada = r.getFechaEntrada() == null ? "—" : r.getFechaEntrada().format(fmt);
            String salida = r.getFechaSalida() == null ? "—" : r.getFechaSalida().format(fmt);
            modeloReservas.addRow(new Object[]{r.getNombreCliente(), r.getNumeroHabitacion(), entrada, salida, r.getEstado()});
            modeloActividad.addRow(new Object[]{entrada, "Reservación", r.getNombreCliente() + " · Hab. " + r.getNumeroHabitacion() + " · " + r.getEstado()});
        }
        if (listaReservas.isEmpty()) {
            modeloReservas.addRow(new Object[]{"Sin datos", "—", "—", "—", "—"});
            modeloActividad.addRow(new Object[]{LocalDate.now().format(fmt), "Sistema", "No hay reservaciones registradas todavía."});
        }
    }
    private void mostrarAvisoDatos(String modulo, Exception ex) {
        // Evita cuadros de diálogo repetidos al abrir el panel; los valores quedan como cero si no hay conexión.
        System.err.println("No se pudieron cargar " + modulo + ": " + ex.getMessage());
    }
    private String nombreActual() { return Sesion.getUsuario() == null ? "Usuario" : Sesion.getUsuario().getNombre(); }
    private String rolActual() { return Sesion.getUsuario() == null ? "Sesión activa" : Sesion.getUsuario().getRol(); }

    /** CAMBIO: panel que se ajusta al ancho de la ventana y solo hace scroll vertical si no cabe. */
    private static class PanelDesplazable extends JPanel implements javax.swing.Scrollable {
        PanelDesplazable(java.awt.LayoutManager layout) { super(layout); }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(java.awt.Rectangle r, int orientacion, int direccion) { return 16; }
        @Override public int getScrollableBlockIncrement(java.awt.Rectangle r, int orientacion, int direccion) { return r.height; }
        // Siempre ocupa el ancho disponible (así no aparece scroll horizontal)
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        // Si la ventana es más alta que el contenido, lo estira; si es más baja, deja el scroll
        @Override public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof javax.swing.JViewport vp
                    && vp.getHeight() > getPreferredSize().height;
        }
    }

    /** Gráfica de dona con la disponibilidad de habitaciones. */
    private static class DonutPanel extends JPanel {
        private int disponibles, ocupadas, mantenimiento;
        DonutPanel() { setOpaque(false); setPreferredSize(new Dimension(190, 145)); }
        void setValores(int d, int o, int m) { disponibles = d; ocupadas = o; mantenimiento = m; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int total = disponibles + ocupadas + mantenimiento; int x = 18, y = 15, size = Math.min(getHeight() - 28, getWidth() - 30); if (size < 70) size = 70;
            if (total == 0) { g2.setColor(EstiloHotel.BORDE); g2.setStroke(new BasicStroke(18)); g2.drawOval(x, y, size, size); }
            else {
                int start = 90; int[] values = {disponibles, ocupadas, mantenimiento}; Color[] colors = {new Color(0x4A,0x9B,0xD6), EstiloHotel.AZUL, EstiloHotel.MALVA};
                for (int i = 0; i < values.length; i++) { int angle = (int)Math.round(360.0 * values[i] / total); g2.setColor(colors[i]); g2.setStroke(new BasicStroke(18, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)); g2.drawArc(x, y, size, size, start, -angle); start -= angle; }
            }
            g2.setColor(EstiloHotel.NAVY); g2.setFont(new Font("SansSerif", Font.BOLD, 19)); String totalText = String.valueOf(total); int tw = g2.getFontMetrics().stringWidth(totalText); g2.drawString(totalText, x + (size - tw) / 2, y + size / 2 + 2);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10)); String label = "habitaciones"; int lw = g2.getFontMetrics().stringWidth(label); g2.setColor(EstiloHotel.MUTED); g2.drawString(label, x + (size - lw) / 2, y + size / 2 + 18); g2.dispose();
        }
    }
}