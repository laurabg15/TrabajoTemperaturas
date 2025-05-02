import datechooser.beans.DateChooserCombo;
import entidades.Temperaturas;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import servicios.Par;
import servicios.TemperaturaServicio;

public class FrmTemperatura extends JFrame {

    private JComboBox cmbTemperatura;
    private DateChooserCombo dccDesde;
    private DateChooserCombo dccHasta;
    private JTabbedPane tpTemperatura;
    private JPanel pnlGrafica;
    private JPanel pnlEstadisticas;
    private List<String> ciudades;
    private List<Temperaturas> datos;

    public FrmTemperatura() {
        this.setTitle("Registros de Temperatura");
        this.setSize(700, 400);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        JToolBar tb = new JToolBar();

        JButton btnGraficar = new JButton();
        btnGraficar.setIcon(new ImageIcon(this.getClass().getResource("/iconos/Grafica.png")));
        btnGraficar.setToolTipText("Gráfica temperatura vs fecha");
        btnGraficar.addActionListener(e -> btnGraficarClick());
        tb.add(btnGraficar);

        JButton btnEstadisticas = new JButton();
        btnEstadisticas.setIcon(new ImageIcon(this.getClass().getResource("/iconos/Datos.png")));
        btnEstadisticas.setToolTipText("Estadísticas de temperatura para la ciudad seleccionada");
        btnEstadisticas.addActionListener(e -> btnCalcularEstadisticasClick());
        tb.add(btnEstadisticas);

        JPanel pnlTemperatura = new JPanel();
        pnlTemperatura.setLayout(new BoxLayout(pnlTemperatura, BoxLayout.Y_AXIS));

        JPanel pnlDatosProceso = new JPanel();
        pnlDatosProceso.setPreferredSize(new Dimension(0, 50));
        pnlDatosProceso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlDatosProceso.setLayout(null);

        JLabel lblCiudad = new JLabel("Ciudad:");
        lblCiudad.setBounds(10, 10, 100, 25);
        pnlDatosProceso.add(lblCiudad);

        this.cmbTemperatura = new JComboBox();
        this.cmbTemperatura.setBounds(110, 10, 100, 25);
        pnlDatosProceso.add(this.cmbTemperatura);

        this.dccDesde = new DateChooserCombo();
        this.dccDesde.setBounds(220, 10, 100, 25);
        pnlDatosProceso.add(this.dccDesde);

        this.dccHasta = new DateChooserCombo();
        this.dccHasta.setBounds(330, 10, 100, 25);
        pnlDatosProceso.add(this.dccHasta);

        this.pnlGrafica = new JPanel();
        JScrollPane spGrafica = new JScrollPane(this.pnlGrafica);

        this.pnlEstadisticas = new JPanel();

        this.tpTemperatura = new JTabbedPane();
        this.tpTemperatura.addTab("Gráfica", spGrafica);
        this.tpTemperatura.addTab("Estadísticas", this.pnlEstadisticas);

        pnlTemperatura.add(pnlDatosProceso);
        pnlTemperatura.add(this.tpTemperatura);

        this.getContentPane().add(tb, BorderLayout.NORTH);
        this.getContentPane().add(pnlTemperatura, BorderLayout.CENTER);

        this.cargarDatos();
    }

    private void cargarDatos() {
        this.datos = TemperaturaServicio.getDatos(System.getProperty("user.dir") + "/src/datos/Temperaturas.csv");
        this.ciudades = TemperaturaServicio.getCiudades(this.datos);
        DefaultComboBoxModel dcm = new DefaultComboBoxModel(this.ciudades.toArray());
        this.cmbTemperatura.setModel(dcm);
    }

    private void btnGraficarClick() {
        if (this.cmbTemperatura.getSelectedIndex() >= 0) {
            String ciudad = (String) this.cmbTemperatura.getSelectedItem();
            LocalDate desde = this.dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate hasta = this.dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            this.tpTemperatura.setSelectedIndex(0);

            List<Temperatura> filtrados = TemperaturaServicio.filtrar(ciudad, desde, hasta, this.datos);
            Par<List<LocalDate>, List<Double>> datosSerie = TemperaturaServicio.extraer(filtrados);

            TimeSeries serie = new TimeSeries("Temperatura en " + ciudad);
            for (int i = 0; i < datosSerie.getPrimero().size(); i++) {
                LocalDate fecha = datosSerie.getPrimero().get(i);
                double temperatura = datosSerie.getSegundo().get(i);
                serie.add(new Day(fecha.getDayOfMonth(), fecha.getMonthValue(), fecha.getYear()), temperatura);
            }

            TimeSeriesCollection coleccion = new TimeSeriesCollection();
            coleccion.addSeries(serie);

            JFreeChart grafica = ChartFactory.createTimeSeriesChart(
                    "Temperatura en " + ciudad,
                    "Fecha",
                    "Temperatura (°C)",
                    coleccion,
                    true, true, false);

            ChartPanel pnlGraficar = new ChartPanel(grafica);
            pnlGraficar.setPreferredSize(new Dimension(800, 400));

            this.pnlGrafica.removeAll();
            this.pnlGrafica.setLayout(new BorderLayout());
            this.pnlGrafica.add(pnlGraficar, BorderLayout.CENTER);
            this.pnlGrafica.revalidate();
            this.pnlGrafica.repaint();
        }
    }

    private void btnCalcularEstadisticasClick() {
        if (this.cmbTemperatura.getSelectedIndex() >= 0) {
            String ciudad = (String) this.cmbTemperatura.getSelectedItem();
            LocalDate desde = this.dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate hasta = this.dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            this.tpTemperatura.setSelectedIndex(1);

            Map<String, Double> estadisticas = TemperaturaServicio.getEstadisticas(ciudad, desde, hasta, this.datos);

            this.pnlEstadisticas.removeAll();
            this.pnlEstadisticas.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            int fila = 0;

            for (Map.Entry<String, Double> entrada : estadisticas.entrySet()) {
                gbc.gridx = 0;
                gbc.gridy = fila;
                this.pnlEstadisticas.add(new JLabel(entrada.getKey()), gbc);
                gbc.gridx = 1;
                this.pnlEstadisticas.add(new JLabel(String.format("%.2f", entrada.getValue())), gbc);
                fila++;
            }

            this.pnlEstadisticas.revalidate();
            this.pnlEstadisticas.repaint();
        }
    }
}
