import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

@SuppressWarnings("serial")
public class AllStreamsStatisticsDialog extends JDialog {

    private final String[] tables = {"STREAM1", "STREAM2", "STREAM3", "STREAM4"};

    public AllStreamsStatisticsDialog(JFrame parent) {
        super(parent, "Στατιστικά Όλων των Ροών", true);
        setSize(1400, 900);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel label = new JLabel("Στατιστικά Όλων των Ροών");
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        topPanel.add(label, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);

        // Panels
        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        centerPanel.add(createBoxPlotPanel());
        centerPanel.add(createPieChartPanel());
        centerPanel.add(createHorizontalHistogramPanel());
        
        add(centerPanel, BorderLayout.CENTER);
    }

    // Pie Chart of top 10 cities with most departments
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private ChartPanel createPieChartPanel() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Integer> cityCounts = new HashMap<>();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            for (String table : tables) {
                PreparedStatement ps = conn.prepareStatement("SELECT city FROM " + table);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String city = rs.getString("city");
                    if (city != null && !city.trim().isEmpty()) {
                        cityCounts.put(city, cityCounts.getOrDefault(city, 0) + 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            dataset.setValue("Σφάλμα", 1);
        }

        // Sort and take top 10
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(cityCounts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int othersCount = 0;
        for (int i = 0; i < sorted.size(); i++) {
            if (i < 10) {
                dataset.setValue(sorted.get(i).getKey(), sorted.get(i).getValue());
            } else {
                othersCount += sorted.get(i).getValue();
            }
        }

        if (othersCount > 0) {
            dataset.setValue("Άλλες", othersCount);
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Κατανομή Σχολών ανά Πόλη (Top 10)",
            dataset,
            true, true, false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setNoDataMessage("Δεν υπάρχουν δεδομένα");
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));

        return new ChartPanel(chart);
    }

    // Box-and-Whisker Plot for entry_scores per stream
    private ChartPanel createBoxPlotPanel() {
        DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            for (int i = 0; i < tables.length; i++) {
                String streamLabel = (i + 1) + "η";
                List<Double> scores = new ArrayList<>();
                PreparedStatement ps = conn.prepareStatement("SELECT entry_score FROM " + tables[i]);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    double score = rs.getDouble("entry_score");
                    if (!rs.wasNull()) scores.add(score);
                }
                if (!scores.isEmpty()) {
                    dataset.add(scores, "Μόρια", streamLabel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
            "Κατανομή Βάσεων ανά Κατεύθυνση",
            "Κατεύθυνση",
            "Μόρια Εισαγωγής",
            dataset,
            false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        BoxAndWhiskerRenderer renderer = (BoxAndWhiskerRenderer) plot.getRenderer();
        renderer.setMeanVisible(false); // Removes centers black circles
        renderer.setSeriesPaint(0, new Color(100, 150, 255));
        
        renderer.setDefaultToolTipGenerator(new BoxAndWhiskerToolTipGenerator() {
            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                if (dataset instanceof BoxAndWhiskerCategoryDataset) {
                    BoxAndWhiskerCategoryDataset bwDataset = (BoxAndWhiskerCategoryDataset) dataset;

                    Number min = bwDataset.getMinRegularValue(row, column);
                    Number max = bwDataset.getMaxRegularValue(row, column);
                    Number q1 = bwDataset.getQ1Value(row, column);
                    Number q3 = bwDataset.getQ3Value(row, column);
                    Number median = bwDataset.getMedianValue(row, column);

                    StringBuilder tooltip = new StringBuilder();
                    tooltip.append("Median: ").append(median.floatValue()).append(", ");
                    tooltip.append("Q1: ").append(q1.floatValue()).append(", ");
                    tooltip.append("Q3: ").append(q3.floatValue()).append(", ");;
                    tooltip.append("Min: ").append(min.floatValue()).append(", ");
                    tooltip.append("Max: ").append(max.floatValue());

                    return tooltip.toString();
                }
                return super.generateToolTip(dataset, row, column);
            }
        });

        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        return new ChartPanel(chart);
    }


    // Horizontal Bar Chart: count of university_name
    private ChartPanel createHorizontalHistogramPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> universityCounts = new HashMap<>();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            for (String table : tables) {
                PreparedStatement ps = conn.prepareStatement("SELECT university_name FROM " + table);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String name = rs.getString("university_name");
                    if (name != null && !name.trim().isEmpty()) {
                        universityCounts.put(name, universityCounts.getOrDefault(name, 0) + 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Sort descending
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(universityCounts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<String, Integer> entry : sorted) {
            dataset.addValue(entry.getValue(), "Πλήθος", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Ιστόγραμμα Σχολών σε Πανεπιστήμια",
            "Πανεπιστήμιο",
            "Πλήθος",
            dataset,
            PlotOrientation.HORIZONTAL,
            false, true, false
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);

        // Dynamic color palette
        for (int i = 0; i < sorted.size(); i++) {
            float hue = (float) i / sorted.size();
            renderer.setSeriesPaint(0, Color.getHSBColor(hue, 0.6f, 0.9f));
        }

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(500, 0));
        return panel;
    }
}