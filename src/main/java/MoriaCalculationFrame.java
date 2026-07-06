import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class MoriaCalculationFrame extends JFrame {
    private String[] subjects;

    public MoriaCalculationFrame(int field, String email) {
        setTitle("Υπολογισμός Μορίων");
        setSize(700, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top bar with title and "Αρχική"
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Υπολογισμός Μορίων 2024 - " + field + "ο Πεδίο");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton homeButton = new DarkButton("Αρχική");
        homeButton.setPreferredSize(new Dimension(100, 30));
        homeButton.addActionListener(e -> {
            dispose();
            new MainMenuFrame(email);
        });

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(homeButton, BorderLayout.EAST);

        // Subjects based on field
        switch (field) {
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

        // Center panel for table
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Table headers
        JPanel headerRow = new JPanel(new GridLayout(1, 2)); //2 Columns
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        headerRow.setBackground(Color.LIGHT_GRAY);

        JLabel subjectHeader = new JLabel("Μάθημα", SwingConstants.CENTER);
        subjectHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        subjectHeader.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel gradeHeader = new JLabel("Βαθμός (0-20)", SwingConstants.CENTER);
        gradeHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        gradeHeader.setHorizontalAlignment(SwingConstants.CENTER);

        headerRow.add(subjectHeader);
        headerRow.add(gradeHeader);

        centerPanel.add(headerRow);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Rows for each subject
        JTextField[] gradeFields = new JTextField[subjects.length];
        for (int i = 0; i < subjects.length; i++) {
            JPanel row = new JPanel(new GridLayout(1, 2, 10, 0)); //2 Columns with height gap
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            row.setBackground(Color.WHITE);

            JLabel subjectLabel = new JLabel(subjects[i]);
            JTextField gradeField = new JTextField();
            gradeField.setPreferredSize(new Dimension(50, 30));
            gradeField.setHorizontalAlignment(JTextField.CENTER);

            gradeFields[i] = gradeField;

            row.add(subjectLabel);
            row.add(gradeField);
            centerPanel.add(row);
            centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JButton calculateButton = new DarkButton("Υπολογισμός");
        calculateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        calculateButton.setMaximumSize(new Dimension(150, 35));
        calculateButton.addActionListener(e -> {
            float[] grades = new float[4];
            try {
                for (int i = 0; i < 4; i++) {
                    grades[i] = Float.parseFloat(gradeFields[i].getText());
                    if (grades[i] < 0 || grades[i] > 20) {
                        throw new NumberFormatException("Invalid grade range");
                    }
                }
                dispose();
                new MoriaResultsFrame(field, grades, email); // Open MoriaResultsFrame when grades are valid
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Παρακαλώ εισάγετε έγκυρους βαθμούς από 0 έως 20.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        });

        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(calculateButton);

        // Final layout
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
}