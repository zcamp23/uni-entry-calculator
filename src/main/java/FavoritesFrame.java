import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.sql.*;


@SuppressWarnings("serial")
public class FavoritesFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private int userId;

    public FavoritesFrame(String email) {

    	this.userId = fetchUserIdByEmail(email);
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Αποτυχία εύρεσης χρήστη.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
    	
    	setTitle("Αγαπημένα");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Αγαπημένα");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        
        JButton homeButton = new DarkButton("Αρχική");
        homeButton.setPreferredSize(new Dimension(100, 30));
        homeButton.addActionListener(e -> {
            dispose();
            new MainMenuFrame(email);
        });

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(homeButton, BorderLayout.EAST);
        
        
        
        String[] columnNames = {"Τμήμα", "Πανεπιστήμιο", "Πόλη", "Μόρια Εισαγωγής", "ΕΒΕ","Ισχύουν επιπλέον προϋποθέσεις", "Αγαπημένο",
        		"uni_id", "uni_table"}; // Hidden visually columns
        
        model = new DefaultTableModel(null, columnNames) {
        	@Override
            public boolean isCellEditable(int row, int column) {
        		return column == 6; // Only favorite checkbox editable
        	}

            @Override
            public Class<?> getColumnClass(int columnIndex) {
            	if (columnIndex == 6) return Boolean.class;
                	return String.class;
                }
        };

        table = new JTable(model);
        table.removeColumn(table.getColumnModel().getColumn(8)); // uni_table: hidden only visually
        table.removeColumn(table.getColumnModel().getColumn(7)); // uni_id: hidden only visually
        
        // Custom renderer for the "Αγαπημένο" column to ensure checkbox appears correctly
        table.getColumnModel().getColumn(6).setCellRenderer(table.getDefaultRenderer(Boolean.class));
        table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);

        loadFavorites();

        // Listen for checkbox changes
        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 6) {
                int viewRow = e.getFirstRow();
                int modelRow = table.convertRowIndexToModel(viewRow); // Safely convert

                Boolean isChecked = (Boolean) model.getValueAt(modelRow, 6);
                if (!isChecked) {
                    int uniIdToDelete = (int) model.getValueAt(modelRow, 7);
                    String uniTableToDelete = (String) model.getValueAt(modelRow, 8);

                    removeFavorite(userId, uniIdToDelete, uniTableToDelete);
                    model.removeRow(modelRow); // Use model row here too
                }
            }
        });
             
        add(topPanel, BorderLayout.NORTH);
        // --- NEW: Panel for Spacing around the JScrollPane ---
        JPanel scrollPaneContainer = new JPanel(new BorderLayout());
        // Set the background to white for the padding color
        scrollPaneContainer.setBackground(Color.WHITE); 
        // Apply an EmptyBorder for 10px padding on all sides
        // Top, Left, Bottom, Right
        scrollPaneContainer.setBorder(new EmptyBorder(10, 0, 0, 0)); 
        scrollPaneContainer.add(scrollPane, BorderLayout.CENTER); // Add scrollPane to its container

        // Add the container panel to the center of the frame
        add(scrollPaneContainer, BorderLayout.CENTER);         
        setVisible(true);
    }
    
    private int fetchUserIdByEmail(String email) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM LOGIN WHERE email=?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void loadFavorites() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            // Get all favorites for the user
            PreparedStatement favStmt = conn.prepareStatement(
                "SELECT uni_id, uni_table FROM FAVORITES WHERE user_id = ?"
            );
            favStmt.setInt(1, userId);
            ResultSet favRs = favStmt.executeQuery();

            while (favRs.next()) {
                int uniId = favRs.getInt("uni_id");
                String uniTable = favRs.getString("uni_table");

                // Query the STREAM table based on uni_table
                PreparedStatement streamStmt = conn.prepareStatement(
                    "SELECT department_name, university_name, city, entry_score, cutoff_score, extra " +
                    "FROM " + uniTable + " WHERE uni_id = ?"
                );
                streamStmt.setInt(1, uniId);
                ResultSet streamRs = streamStmt.executeQuery();

                if (streamRs.next()) {
                    String dep = streamRs.getString("department_name");
                    String uni = streamRs.getString("university_name");
                    String city = streamRs.getString("city");
                    float entry = streamRs.getFloat("entry_score");
                    float cutoff = streamRs.getFloat("cutoff_score");
                    String extra = streamRs.getBoolean("extra") ? "Ναι" : "Όχι";
                    boolean fav = true;

                    model.addRow(new Object[]{
                            dep, uni, city,
                            String.format("%.2f", entry),
                            String.format("%.2f", cutoff),
                            extra, fav,
                            uniId, uniTable
                    });
                }
                streamRs.close();
                streamStmt.close();
            }
            favRs.close();
            favStmt.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Σφάλμα φόρτωσης αγαπημένων: " + ex.getMessage(), "Σφάλμα Βάσης Δεδομένων", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeFavorite(int userId, int uniId, String uniTable) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM FAVORITES WHERE user_id = ? AND uni_id = ? AND uni_table = ?"
            );
            stmt.setInt(1, userId);
            stmt.setInt(2, uniId);
            stmt.setString(3, uniTable);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Σφάλμα κατά την αφαίρεση από τα αγαπημένα: " + ex.getMessage(), "Σφάλμα Βάσης Δεδομένων", JOptionPane.ERROR_MESSAGE);
        }
    }
}
