import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.*;

@SuppressWarnings("serial")
public class LoginRegisterFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    
    public LoginRegisterFrame() {
        setTitle("Σύστημα Υπολογισμού Μορίων");
        setSize(700, 400);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(30, 50, 30, 50));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel);

        // Title label
        JLabel titleLabel = new JLabel("Σύστημα Υπολογισμού Μορίων");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Email label
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        JPanel emailLabelPanel = new JPanel(new BorderLayout());
        emailLabelPanel.setMaximumSize(new Dimension(350, 30));
        emailLabelPanel.add(emailLabel, BorderLayout.WEST);
        mainPanel.add(emailLabelPanel);

        // Email field
        emailField = new JTextField(20);
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        emailField.setMaximumSize(new Dimension(350, 35));
        addPlaceholder(emailField, "email@domain.com");
        mainPanel.add(emailField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Password label
        JLabel passwordLabel = new JLabel("Κωδικός:");
        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        JPanel passwordLabelPanel = new JPanel(new BorderLayout());
        passwordLabelPanel.setMaximumSize(new Dimension(350, 30));
        passwordLabelPanel.add(passwordLabel, BorderLayout.WEST);
        mainPanel.add(passwordLabelPanel);

        // Password field
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        passwordField.setMaximumSize(new Dimension(350, 35));
        addPlaceholder(passwordField, "******");
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));

        // Login Button
        DarkButton loginButton = new DarkButton("Σύνδεση");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 16));  // override default font size
        loginButton.setPreferredSize(new Dimension(140, 45));
        loginButton.addActionListener(e -> login());

        // Register Button
        DarkButton registerButton = new DarkButton("Εγγραφή");
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 16));  // override default font size
        registerButton.setPreferredSize(new Dimension(140, 45));
        registerButton.addActionListener(e -> register());

        
        buttonsPanel.add(loginButton);
        buttonsPanel.add(registerButton);

        mainPanel.add(buttonsPanel);

        setLocationRelativeTo(null); // Center window on screen
        setVisible(true);
    }

    private void addPlaceholder(JTextField textField, String placeholder) {
        textField.setForeground(Color.GRAY);
        textField.setText(placeholder);
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
            	if (textField.getText().equals(placeholder)) {
                    textField.setText("");
	            	textField.setForeground(Color.BLACK);
	                if (textField instanceof JPasswordField) {
	                	((JPasswordField) textField).setEchoChar('•');
	                }
            	}    
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty() || textField.getText().equals(placeholder)) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                    if (textField instanceof JPasswordField) {
                        ((JPasswordField) textField).setEchoChar((char) 0);
                    }
                }
            }
        });

        if (textField instanceof JPasswordField) {
            ((JPasswordField) textField).setEchoChar((char) 0);
        }
    }
    
    // Compile email RegEx pattern once to avoid expensive recompilations on every validation call
    private Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9]+([._+-]?[a-zA-Z0-9]+)*@([\\w-]+\\.)+[a-zA-Z]{2,}$");

    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email.toLowerCase()).matches();
    }

    private void login() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Μη έγκυρο email.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT password FROM LOGIN WHERE email=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                
                // Verify the entered password against the stored hashed password.
                // If the hashes match, the login is successful.
                
                if (PasswordUtils.verifyPassword(password, storedHash)) {
                    dispose();
                    new MainMenuFrame(email);
                } else {
                    JOptionPane.showMessageDialog(this, "Λανθασμένος κωδικός.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Δεν βρέθηκε λογαριασμός με αυτό το email.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private void register() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Μη έγκυρο email.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String hashedPassword = PasswordUtils.hashPassword(password, PasswordUtils.generateSalt());

            String query = "INSERT INTO LOGIN (email, password) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, email);
            ps.setString(2, hashedPassword);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Επιτυχής Εγγραφή! Μπορείτε να συνδεθείτε.");
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Αυτό το email χρησιμοποιείται ήδη.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
