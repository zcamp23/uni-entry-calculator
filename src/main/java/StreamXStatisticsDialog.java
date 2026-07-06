import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.RectangleInsets;

@SuppressWarnings("serial")
public class StreamXStatisticsDialog extends JDialog {

    private final String tableName;
    private String[] subjects;

    
    public StreamXStatisticsDialog (JFrame parent, String email, int stream, String streamLabel) {
        super(parent, "Στατιστικά - " + streamLabel, true);
        this.tableName = "STREAM" + stream;

        switch (stream) { 
        case 1:
            subjects = new String[]{
                "Νεοελληνική Γλώσσα και Λογοτεχνία",
                "Αρχαία Ελληνικά",
                "Ιστορία",
                "Λατινικά"
            };
            break;
        case 2:
            subjects = new String[]{
                "Νεοελληνική Γλώσσα και Λογοτεχνία",
                "Φυσική",
                "Χημεία",
                "Μαθηματικά"
            };
            break;
        case 3:
            subjects = new String[]{
                "Νεοελληνική Γλώσσα και Λογοτεχνία",
                "Φυσική",
                "Χημεία",
                "Βιολογία"
            };
            break;
        case 4:
            subjects = new String[]{
                "Νεοελληνική Γλώσσα και Λογοτεχνία",
                "Μαθηματικά",
                "Πληροφορική",
                "Οικονομία"
            };
            break;
        }


        setSize(1600, 950);
        setMinimumSize(new Dimension(1000, 800));
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel("Στατιστικά - " + streamLabel);
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        topPanel.add(infoLabel, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Main content panel for charts
        JPanel chartsPanel = new JPanel();
        chartsPanel.setLayout(new GridLayout(2, 2, 10, 10)); // 2x2 grid for charts
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartsPanel.setBackground(Color.WHITE);

        chartsPanel.add(createSubjectCoefficientPieChartPanel()); // Pie Chart
        chartsPanel.add(createEntryScoreHistogramPanel());     // Histogram
        chartsPanel.add(createUniversityCountBarChartPanel()); // Bar Chart
        chartsPanel.add(createScatterPlotPanel());             // Scatter Plot

        add(chartsPanel, BorderLayout.CENTER);
    }

    // Pie Chart: Percentage of Subject Coefficients
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private ChartPanel createSubjectCoefficientPieChartPanel() {
		DefaultPieDataset dataset = new DefaultPieDataset();
        
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            String query = "SELECT subject1_score, subject2_score, subject3_score, subject4_score FROM " + tableName;
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            Map<String, Double> totalCoefficients = new LinkedHashMap<>();
            for (String subject : subjects) {
                totalCoefficients.put(subject, 0.0);
            }


            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                totalCoefficients.put(subjects[0], totalCoefficients.get(subjects[0]) + rs.getDouble("subject1_score"));
                totalCoefficients.put(subjects[1], totalCoefficients.get(subjects[1]) + rs.getDouble("subject2_score"));
                totalCoefficients.put(subjects[2], totalCoefficients.get(subjects[2]) + rs.getDouble("subject3_score"));
                totalCoefficients.put(subjects[3], totalCoefficients.get(subjects[3]) + rs.getDouble("subject4_score"));
            }

            if (rowCount > 0) {
                for (Map.Entry<String, Double> entry : totalCoefficients.entrySet()) {
                    dataset.setValue(entry.getKey(), entry.getValue() / rowCount);
                }
            } else {
                dataset.setValue("Δεν υπάρχουν δεδομένα", 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            dataset.setValue("Σφάλμα φόρτωσης", 1);
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Μέσος Όρος Συντελεστών Μαθημάτων",
            dataset,
            true, // include legend
            true,
            false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setNoDataMessage("Δεν υπάρχουν δεδομένα");
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));

        return new ChartPanel(chart);
    }

    // Histogram: count of departments per entry score
    private ChartPanel createEntryScoreHistogramPanel() {
        HistogramDataset dataset = new HistogramDataset();
        List<Double> scores = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            String query = "SELECT entry_score FROM " + tableName;
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                scores.add(rs.getDouble("entry_score"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!scores.isEmpty()) {
            // Define the range and number of bins for the histogram
            double minScore = 7.0; // Start the histogram from 7
            double maxScore = 20.0; // End the histogram at 20
            
            // This will give a bar for each integer range, with numbers on the axis.
            int numberOfBins = (int) (maxScore - minScore); 
            
            // Convert List<Double> to double[] array for HistogramDataset
            double[] scoreArray = scores.stream().mapToDouble(Double::doubleValue).toArray();
            
            // Add the series to the HistogramDataset
            dataset.addSeries("Μόρια Εισαγωγής", scoreArray, numberOfBins, minScore, maxScore);
        }
        
        JFreeChart chart = ChartFactory.createHistogram(
            "Ιστόγραμμα Μορίων Εισαγωγής", // Chart title
            "Μόρια",                     // X-axis label
            "Πλήθος Σχολών",             // Y-axis label
            dataset,
            org.jfree.chart.plot.PlotOrientation.VERTICAL, // Vertical bars
            false, // don't include legend
            true, // tooltips
            false // urls
        );

        // Customize the plot
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));

        return new ChartPanel(chart);
    }
    
    // Bar Chart: count of departments per university
    private ChartPanel createUniversityCountBarChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> universityCounts = new HashMap<>();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            String query = "SELECT university_name FROM " + tableName;
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String uniName = rs.getString("university_name");
                if (uniName != null && !uniName.trim().isEmpty()) {
                    universityCounts.put(uniName, universityCounts.getOrDefault(uniName, 0) + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching university counts for bar chart: " + e.getMessage());
        }

        // Sort universities by count in descending order
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(universityCounts.entrySet());
        Collections.sort(sortedEntries, (e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        for (Map.Entry<String, Integer> entry : sortedEntries) {
            dataset.addValue(entry.getValue(), "Πλήθος Εγγραφών", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Κατανομή Πανεπιστημίων", 	// Chart Title
            "Πανεπιστήμιο",       		// Y-axis label (cause it's for horizontal bars, this is the category axis)
            "Πλήθος Εγγραφών",          // X-axis label (value axis)
            dataset,
            PlotOrientation.HORIZONTAL, // Horizontal bars
            false, // do not include legend
            true, // tooltips
            false // urls
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);

        CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis(); // Get the domain axis (which is CategoryAxis for horizontal bar chart)
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10)); // Change 10 to your desired font size
        
        // Custom renderer for tooltips
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false); // remove bar outlines

        // Custom Tooltip Generator
        renderer.setDefaultToolTipGenerator(new CategoryToolTipGenerator() {
            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                String universityName = (String) dataset.getColumnKey(column);
                Number count = dataset.getValue(row, column);
                return "<html><b>" + universityName + "</b><br>Πλήθος: " + count + "</html>";
            }
        });

        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        return new ChartPanel(chart);
    }


    // Scatter Plot (Y: entry_score, X: cutoff)
    private ChartPanel createScatterPlotPanel() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Σχολές");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            String query = "SELECT entry_score, cutoff_score FROM " + tableName;
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double entryScore = rs.getDouble("entry_score");
                double cutoffScore = rs.getDouble("cutoff_score");
                series.add(cutoffScore, entryScore);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createScatterPlot(
            "Σχέση Μορίων Εισαγωγής (Y) και ΕΒΕ (X)",
            "ΕΒΕ",
            "Μόρια Εισαγωγής",
            dataset
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        // Add reference line (Y=X)
        XYSeries lineSeries = new XYSeries("Γραμμή Αναφοράς (Y=X)");
        double minX = plot.getDomainAxis().getLowerBound();
        double maxX = plot.getDomainAxis().getUpperBound();
        lineSeries.add(minX, minX);
        lineSeries.add(maxX, maxX);
        dataset.addSeries(lineSeries);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4, 4)); // Points circle shape
        renderer.setSeriesPaint(0, Color.BLUE); // Scatter plot points color
        

        renderer.setSeriesLinesVisible(1, true); // Make the reference line visible
        renderer.setSeriesShapesVisible(1, false); // No shapes for the reference line
        renderer.setSeriesPaint(1, Color.RED); // Reference line color
        renderer.setSeriesStroke(1, new BasicStroke(2.0f)); // Thicker line

        plot.setRenderer(renderer);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));

        return new ChartPanel(chart);
    }
}