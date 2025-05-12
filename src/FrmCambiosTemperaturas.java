import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.category.DefaultCategoryDataset; 
import datechooser.beans.DateChooserCombo;
import entidades.CambioGrado;
import servicios.CambioGradoServicio;

public class FrmCambiosTemperaturas extends JFrame {

    private JComboBox cmbCiudad;
    private DateChooserCombo dccDesde, dccHasta;
    private JTabbedPane tpCiudades;
    private JPanel pnlGrafica;
    private JPanel pnlEstadisticas;

    private List<String> ciudades;
    private List<CambioGrado> datos;

    public FrmCambiosTemperaturas() {
        setTitle("Temperaturas por Ciudad");
        setSize(700, 400);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JToolBar tb = new JToolBar();

        JButton btnGraficar = new JButton();
        btnGraficar.setIcon(new ImageIcon(getClass().getResource("/iconos/Grafica.png")));
        btnGraficar.setToolTipText("Ciudad vs Fecha");
        btnGraficar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnGraficarClick();
            }
        });
        tb.add(btnGraficar);

        JButton btnCalcularEstadisticas = new JButton();
        btnCalcularEstadisticas.setIcon(new ImageIcon(getClass().getResource("/iconos/Datos.png")));
        btnCalcularEstadisticas.setToolTipText("Estadísticas de la Ciudad seleccionada");
        btnCalcularEstadisticas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnCalcularEstadisticasClick();
            }
        });
        tb.add(btnCalcularEstadisticas);

        JButton btnExtremosCalor = new JButton();
        btnExtremosCalor.setIcon(new ImageIcon(getClass().getResource("/iconos/Calor.png"))); // Usa un ícono adecuado
        btnExtremosCalor.setToolTipText("Ciudad más calurosa y menos calurosa para una fecha");
        btnExtremosCalor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                btnExtremosCalorClick();
            }
        });
        tb.add(btnExtremosCalor);

        JPanel pnlCiudades = new JPanel();
        pnlCiudades.setLayout(new BoxLayout(pnlCiudades, BoxLayout.Y_AXIS));

        JPanel pnlDatosProceso = new JPanel();
        pnlDatosProceso.setPreferredSize(new Dimension(pnlDatosProceso.getWidth(), 50));
        pnlDatosProceso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlDatosProceso.setLayout(null);

        JLabel lblCiudad = new JLabel("Ciudad");
        lblCiudad.setBounds(10, 10, 100, 25);
        pnlDatosProceso.add(lblCiudad);

        cmbCiudad = new JComboBox();
        cmbCiudad.setBounds(110, 10, 100, 25);
        pnlDatosProceso.add(cmbCiudad);

        dccDesde = new DateChooserCombo();
        dccDesde.setBounds(220, 10, 100, 25);
        pnlDatosProceso.add(dccDesde);

        dccHasta = new DateChooserCombo();
        dccHasta.setBounds(330, 10, 100, 25);
        pnlDatosProceso.add(dccHasta);

        pnlGrafica = new JPanel();
        JScrollPane spGrafica = new JScrollPane(pnlGrafica);

        pnlEstadisticas = new JPanel();

        tpCiudades = new JTabbedPane();
        tpCiudades.addTab("Gráfica", spGrafica);
        tpCiudades.addTab("Estadísticas", pnlEstadisticas);

        pnlCiudades.add(pnlDatosProceso);
        pnlCiudades.add(tpCiudades);
           
        getContentPane().add(tb, BorderLayout.NORTH);
        getContentPane().add(pnlCiudades, BorderLayout.CENTER);

        cargarDatos();

        setLocationRelativeTo(null);
    }

    private void cargarDatos() {
        String nombreArchivo = System.getProperty("user.dir") + "/src/datos/Temperaturas.csv";
        datos = CambioGradoServicio.getDatos(nombreArchivo);
        ciudades = CambioGradoServicio.getCiudades(datos);

        DefaultComboBoxModel dcm = new DefaultComboBoxModel(ciudades.toArray());
        cmbCiudad.setModel(dcm);
        }



    private void btnGraficarClick() {
        if (cmbCiudad.getSelectedIndex() >= 0) {
            String ciudad = (String) cmbCiudad.getSelectedItem();
            LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            tpCiudades.setSelectedIndex(0);

            var datosFiltrados = CambioGradoServicio.filtrar(ciudad, desde, hasta, datos);
            var cambiosPorFecha = CambioGradoServicio.extraer(datosFiltrados);

            var fechas = cambiosPorFecha.getX();
            var cambios = cambiosPorFecha.getY();

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < fechas.size(); i++) {
                String fechaStr = fechas.get(i).toString(); 
                dataset.addValue(cambios.get(i), "Cambio", fechaStr);
            }
            JFreeChart graficador = ChartFactory.createBarChart("Cambio de Temperatura de "+ ciudad  + " por fecha ", "Fecha",  "Cambio", dataset)     ;      
            
            ChartPanel pnlGraficador = new ChartPanel(graficador);
            pnlGraficador.setPreferredSize(new Dimension(600, 400));

            pnlGrafica.removeAll();
            pnlGrafica.setLayout(new BorderLayout());
            pnlGrafica.add(pnlGraficador, BorderLayout.CENTER);
            pnlGrafica.revalidate();

            pack();
        }
    }

    private void btnCalcularEstadisticasClick() {
        if (cmbCiudad.getSelectedIndex() >= 0) {
            String ciudad = (String) cmbCiudad.getSelectedItem();
            LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            tpCiudades.setSelectedIndex(1);
            pnlEstadisticas.removeAll();
            pnlEstadisticas.setLayout(new GridBagLayout());

            int fila = 0;
            var estadisticas = CambioGradoServicio.getEstadisticas(ciudad, desde, hasta, datos);
            for (var estadistica : estadisticas.entrySet()) {
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = fila;
                pnlEstadisticas.add(new JLabel(estadistica.getKey()), gbc);
                gbc.gridx = 1;
                pnlEstadisticas.add(new JLabel(String.format("%.2f", estadistica.getValue())), gbc);
                fila++;
            }
            pnlEstadisticas.revalidate();
        }
    }

private void btnExtremosCalorClick() {
    LocalDate fechaSeleccionada = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

    var datosFiltrados = datos.stream()
            .filter(dato -> dato.getFecha().equals(fechaSeleccionada))
            .collect(Collectors.toList());

    if (datosFiltrados.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No hay datos para la fecha seleccionada.", "Información", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    var ciudadMasCalurosa = datosFiltrados.stream()
            .max(Comparator.comparing(CambioGrado::getCambio))
            .orElse(null);

    var ciudadMenosCalurosa = datosFiltrados.stream()
            .min(Comparator.comparing(CambioGrado::getCambio))
            .orElse(null);

    String mensaje = String.format(
            "Fecha: %s\nCiudad más calurosa: %s (%.2f°C)\nCiudad menos calurosa: %s (%.2f°C)",
            fechaSeleccionada,
            ciudadMasCalurosa.getCiudad(), ciudadMasCalurosa.getCambio(),
            ciudadMenosCalurosa.getCiudad(), ciudadMenosCalurosa.getCambio()
    );

    JOptionPane.showMessageDialog(this, mensaje, "Resultados", JOptionPane.INFORMATION_MESSAGE);
}
}