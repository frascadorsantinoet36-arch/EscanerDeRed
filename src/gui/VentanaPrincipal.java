package gui;

import logica.EscaneadorRed;
import modelo.ResultadoEscanio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JTextField txtIpInicio;
    private JTextField txtIpFin;
    private JTextField txtTimeout;
    private JTextField txtReintentos;
    private JTable tablaResultados;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JProgressBar progressBar;
    private JLabel lblEstado;
    private JLabel lblEquiposActivos;

    private JButton btnIniciar;
    private JButton btnDetener;
    private JButton btnLimpiar;
    private JButton btnGuardar;
    private JButton btnFiltrarActivos;

    private EscaneadorRed escaneador;
    private List<ResultadoEscanio> todosLosResultados;
    private int contadorActivos = 0;
    private boolean filtrandoActivos = false;

    public VentanaPrincipal() {
        setTitle("Escáner de Red");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        escaneador = new EscaneadorRed();
        todosLosResultados = new ArrayList<>();

        // --- PANEL SUPERIOR ---
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelFormulario.setBackground(new Color(230, 245, 255));

        panelFormulario.add(new JLabel("IP de inicio:"));
        txtIpInicio = new JTextField("10.160.7.223");
        panelFormulario.add(txtIpInicio);

        panelFormulario.add(new JLabel("IP de fin:"));
        txtIpFin = new JTextField("10.160.7.233");
        panelFormulario.add(txtIpFin);

        panelFormulario.add(new JLabel("Tiempo de espera (ms):"));
        txtTimeout = new JTextField("1000");
        panelFormulario.add(txtTimeout);

        panelFormulario.add(new JLabel("Número de reintentos:"));
        txtReintentos = new JTextField("1");
        panelFormulario.add(txtReintentos);

        add(panelFormulario, BorderLayout.NORTH);

        // --- TABLA CENTRAL ---
        String[] columnas = {"IP", "Nombre equipo", "Activo", "Tiempo (ms)"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Boolean.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaResultados = new JTable(modeloTabla);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaResultados.setRowSorter(sorter);

        JScrollPane scrollTabla = new JScrollPane(tablaResultados);
        add(scrollTabla, BorderLayout.CENTER);

        // --- PANEL INFERIOR ---
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.Y_AXIS));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        lblEstado = new JLabel("Listo", SwingConstants.CENTER);
        lblEstado.setOpaque(true);
        lblEstado.setBackground(new Color(180, 200, 230));
        lblEstado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JPanel panelInfoActivos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblEquiposActivos = new JLabel("Equipos activos: 0");
        panelInfoActivos.add(lblEquiposActivos);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnIniciar = new JButton("Iniciar escaneo");
        btnDetener = new JButton("Detener escaneo");
        btnLimpiar = new JButton("Limpiar");
        btnGuardar = new JButton("Guardar resultados");
        btnFiltrarActivos = new JButton("Mostrar solo activos");

        btnDetener.setEnabled(false);

        panelBotones.add(btnIniciar);
        panelBotones.add(btnDetener);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnFiltrarActivos);

        panelInferior.add(progressBar);
        panelInferior.add(lblEstado);
        panelInferior.add(panelInfoActivos);
        panelInferior.add(panelBotones);

        add(panelInferior, BorderLayout.SOUTH);

        // --- EVENTOS ---
        btnIniciar.addActionListener(e -> iniciarEscaneo());
        btnDetener.addActionListener(e -> detenerEscaneo());
        btnLimpiar.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardarResultados());
        btnFiltrarActivos.addActionListener(e -> alternarFiltroActivos());
    }

    private void iniciarEscaneo() {
        String ipIni = txtIpInicio.getText().trim();
        String ipFin = txtIpFin.getText().trim();

        if (!EscaneadorRed.esIpValida(ipIni) || !EscaneadorRed.esIpValida(ipFin)) {
            JOptionPane.showMessageDialog(this, "Formato de IP inválido. Ingrese una IP válida (ej: 192.168.1.1).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int timeout;
        try {
            timeout = Integer.parseInt(txtTimeout.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El tiempo de espera debe ser un número.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        limpiar();

        btnIniciar.setEnabled(false);
        btnDetener.setEnabled(true);
        lblEstado.setText("Escaneando...");

        escaneador.escanearRangoAsync(ipIni, ipFin, timeout, new EscaneadorRed.EscanioListener() {
            @Override
            public void onProgreso(ResultadoEscanio res, int actual, int total) {
                SwingUtilities.invokeLater(() -> {
                    todosLosResultados.add(res);
                    if (res.isActivo()) {
                        contadorActivos++;
                        lblEquiposActivos.setText("Equipos activos: " + contadorActivos);
                    }

                    if (!filtrandoActivos || res.isActivo()) {
                        modeloTabla.addRow(new Object[]{
                                res.getIp(),
                                res.getNombreEquipo(),
                                res.isActivo(),
                                res.isActivo() ? res.getTiempoMs() : ""
                        });
                    }

                    progressBar.setMaximum(total);
                    progressBar.setValue(actual);
                });
            }

            @Override
            public void onFinalizado() {
                SwingUtilities.invokeLater(() -> {
                    btnIniciar.setEnabled(true);
                    btnDetener.setEnabled(false);
                    lblEstado.setText("Escaneo finalizado");
                });
            }
        });
    }

    private void detenerEscaneo() {
        escaneador.detener();
        lblEstado.setText("Escaneo cancelado");
        btnIniciar.setEnabled(true);
        btnDetener.setEnabled(false);
    }

    private void limpiar() {
        modeloTabla.setRowCount(0);
        todosLosResultados.clear();
        contadorActivos = 0;
        lblEquiposActivos.setText("Equipos activos: 0");
        progressBar.setValue(0);
        lblEstado.setText("Listo");
    }

    private void alternarFiltroActivos() {
        filtrandoActivos = !filtrandoActivos;
        if (filtrandoActivos) {
            sorter.setRowFilter(RowFilter.regexFilter("true", 2));
            btnFiltrarActivos.setText("Mostrar todos");
        } else {
            sorter.setRowFilter(null);
            btnFiltrarActivos.setText("Mostrar solo activos");
        }
    }

    private void guardarResultados() {
        if (todosLosResultados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay resultados para guardar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("IP\tNombre equipo\tActivo\tTiempo(ms)");
                for (ResultadoEscanio r : todosLosResultados) {
                    writer.println(r.getIp() + "\t" + r.getNombreEquipo() + "\t" + r.isActivo() + "\t" + r.getTiempoMs());
                }
                JOptionPane.showMessageDialog(this, "Resultados guardados con éxito.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }
}