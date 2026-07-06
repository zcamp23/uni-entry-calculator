import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class MainMenuFrame extends JFrame {
    private JLabel welcomeLabel;

    public MainMenuFrame(String email) {
        setTitle("Κεντρικό Μενού");
        setSize(700, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Extract "username", the text before @
        String username = email.contains("@") ? email.split("@")[0] : email;

        // --- Top Panel (Welcome + Logout) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);
        welcomeLabel = new JLabel("Καλώς ήρθες, " + username);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton logoutButton = new JButton("Αποσύνδεση");
        logoutButton.setFocusPainted(false);
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBorderPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setPreferredSize(new Dimension(120, 30));
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginRegisterFrame();
        });

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);

        // --- Content Panel (White margin under top) ---
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0)); // Margin between boxes
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Left Panel (Βάσεις 2024) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS)); // Left panel's buttons are aligned one below the other
        leftPanel.setBackground(Color.LIGHT_GRAY);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        leftPanel.setPreferredSize(new Dimension(250, getHeight()));

        JLabel basesLabel = new JLabel("Βάσεις 2024");
        basesLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        basesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(basesLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        String[] baseButtons = {"Θεωρητική", "Θετική", "Υγείας", "Οικονομίας και Πληροφορικής", "Αγαπημένες"};

        for (int i = 0; i < baseButtons.length; i++) {

            String btnText = baseButtons[i];
            int btnNumber = i + 1; // 1-based index (1,2,3,4)

            JButton btn;

            if (btnText.equals("Αγαπημένες")) {
            	//"Αγαπημένες" is the only Red Button
                btn = new JButton(btnText);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setPreferredSize(new Dimension(120, 30));
                btn.setMaximumSize(new Dimension(200, 40));
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                btn.setBackground(new Color(220, 53, 69));
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("SansSerif", Font.PLAIN, 14));


                btn.addActionListener(e -> {
                    dispose(); // Close main menu
                    new FavoritesFrame(email);
                });

            } else {
                // Use DarkButton for all the other buttons
                btn = new DarkButton(btnText);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                btn.setPreferredSize(new Dimension(120, 30));
                btn.setMaximumSize(new Dimension(200, 40));
                btn.setFont(new Font("SansSerif", Font.PLAIN, 14)); // override font

                btn.addActionListener(e -> {
                    dispose();
                    new StreamXDataFrame(btnNumber, email);
                });
            }

            leftPanel.add(btn);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }


        // --- Right Panel (Gray box + Στατιστικά button) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        // --- Right Gray Box ---
        JPanel rightBox = new JPanel();
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.Y_AXIS));
        rightBox.setBackground(Color.LIGHT_GRAY);
        rightBox.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel calcLabel = new JLabel("Υπολογισμός Μορίων");
        calcLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        calcLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightBox.add(calcLabel);
        rightBox.add(Box.createRigidArea(new Dimension(0, 20)));

        // Grid 2x2
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        gridPanel.setOpaque(false);

        String[] fields = {"1ο πεδίο", "2ο πεδίο", "3ο πεδίο", "4ο πεδίο"};
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            int fieldNumber = i + 1; // 1-based index (1,2,3,4)
            DarkButton fieldBtn = new DarkButton(field);

            fieldBtn.addActionListener(e -> {
                dispose();
                new MoriaCalculationFrame(fieldNumber, email);
            });

            gridPanel.add(fieldBtn);
        }


        rightBox.add(gridPanel); //gridPanel is inside the Right Gray Box panel
        rightPanel.add(rightBox);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Στατιστικά button
        JButton statsButton = new DarkButton("Στατιστικά");
        statsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsButton.setMaximumSize(new Dimension(200, 40));
        statsButton.addActionListener(e -> {
            new AllStreamsStatisticsDialog(this).setVisible(true);
        });
        rightPanel.add(statsButton);

        // --- Add left and right panels to content panel ---
        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(rightPanel, BorderLayout.CENTER);

        // --- Final Frame Layout ---
        add(topPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        setLocationRelativeTo(null); // Center window on screen
        setVisible(true);
    }

}
