import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;


@SuppressWarnings("serial")
public class MoriaResultsFrame extends JFrame {
    private final int field;
    private final float[] grades;
    private int userId;

    private static final String[] COLUMN_NAMES = {
        "Τμήμα", "Πανεπιστήμιο", "Πόλη",
        "Μόρια σχολής", "Διαφορά με Βάση",
        "Διαφορά με ΕΒΕ", "Ισχύουν επιπλέον προϋποθέσεις",
        "Αγαπημένο"
    };

    public MoriaResultsFrame(int field, float[] grades, String email) {
        this.field = field;
        this.grades = grades;
        
        int fetchedUserId = fetchUserIdByEmail(email);
        if (fetchedUserId == -1) {
            JOptionPane.showMessageDialog(this, "Αποτυχία εύρεσης χρήστη.", "Σφάλμα", JOptionPane.ERROR_MESSAGE); // the user's row was deleted or modified externally while the app was running
            dispose();
            return;
        }
        this.userId = fetchedUserId;

        setTitle("Αποτελέσματα Υπολογισμού Μορίων");
        setSize(1120, 600);
        setMinimumSize(new Dimension(650, 322));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        float mo = (grades[0] + grades[1] + grades[2] + grades[3]) / 4f;
        JLabel infoLabel = new JLabel(String.format("Μέσος Όρος: %.2f", mo));
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton homeButton = new DarkButton("Αρχική");
        homeButton.setPreferredSize(new Dimension(100, 30));
        homeButton.addActionListener(e -> {
            dispose();
            new MainMenuFrame(email);
        });

        topPanel.add(infoLabel, BorderLayout.WEST);
        topPanel.add(homeButton, BorderLayout.EAST);

        // Filter panel
        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField("Τμήμα ή Πανεπιστήμιο");
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setBackground(Color.DARK_GRAY);
        searchField.setForeground(Color.GRAY);
        searchField.setCaretColor(Color.WHITE);	// blinking cursor
        searchField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Τμήμα ή Πανεπιστήμιο")) {
                    searchField.setText("");
                    searchField.setForeground(Color.WHITE); // Change to actual text color
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Τμήμα ή Πανεπιστήμιο");
                    searchField.setForeground(Color.GRAY); // Change back to placeholder color
                }
            }
        });

        String[] sortOptions = {
            "Αύξουσα - Τμήμα",
            "Αύξουσα - Πανεπιστήμιο",
            "Αύξουσα - Πόλη",
            "Φθίνουσα - Μόρια Σχολής",
            "Φθίνουσα - Διαφορά με Βάση"
        };
        
        JComboBox<String> sortBox = new JComboBox<>(sortOptions); // Drop-down
        sortBox.setPreferredSize(new Dimension(190, 30));
        sortBox.setBackground(Color.DARK_GRAY);
        sortBox.setForeground(Color.WHITE);
        sortBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sortBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        

        filterPanel.add(searchField, BorderLayout.WEST);
        filterPanel.add(sortBox, BorderLayout.EAST);
        filterPanel.setBackground(Color.WHITE);

        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel(COLUMN_NAMES, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 7; // make editable only the favorite-checkboxes column
            }

            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 7) ? Boolean.class : String.class; //the column 7 is boolean (checkboxes), all other columns hold String values
            }
        };

        table.setModel(model);
        TableColumnModel columnModel = table.getColumnModel();

        columnModel.getColumn(0).setPreferredWidth(355);	// Τμήμα
	    columnModel.getColumn(1).setPreferredWidth(115);	// Πανεπιστήμιο
	    columnModel.getColumn(2).setPreferredWidth(75);	// Πόλη
	    columnModel.getColumn(3).setPreferredWidth(95);	// Μόρια σχολής
	    columnModel.getColumn(4).setPreferredWidth(105);	// Διαφορά με Βάση
	    columnModel.getColumn(5).setPreferredWidth(100);	// Διαφορά με ΕΒΕ
	    columnModel.getColumn(6).setPreferredWidth(175);	// Ισχύουν επιπλέον προϋποθέσεις
	    columnModel.getColumn(7).setPreferredWidth(65); 	// Αγαπημένο (checkboxes)

	    // Custom renderer to center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Apply centering renderer to some columns
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Μόρια σχολής
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Ισχύουν επιπλέον προϋποθέσεις	     
	     
        // Custom renderer to color differences (and center-align them)
        DefaultTableCellRenderer diffRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                try {
                    float number = Float.parseFloat(value.toString().replace(',', '.'));
                    if (number >= 0) {
                        c.setForeground(new Color(0, 153, 0)); // green
                    } else {
                        c.setForeground(Color.RED); // red
                    }
                } catch (NumberFormatException e) {
                    c.setForeground(Color.BLACK); // fallback
                }
                // Center-align
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };

        // Apply coloring renderer to "Διαφορά με Βάση" (col 4) and "Διαφορά με ΕΒΕ" (col 5)
        table.getColumnModel().getColumn(4).setCellRenderer(diffRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(diffRenderer);
        
        
        JScrollPane scrollPane = new JScrollPane(table);

        loadUniversityData(model, "", "Αύξουσα - Τμήμα");
        
        // Listens for key releases in the search field to perform live filtering.
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                model.setRowCount(0);
                if (searchField.getText().equals("Τμήμα ή Πανεπιστήμιο")){
                    loadUniversityData(model, "", (String) sortBox.getSelectedItem());
                }
                else {
                	loadUniversityData(model, searchField.getText(), (String) sortBox.getSelectedItem());
                }
            }
        });

        // Listens for changes in the sort order dropdown to re-sort the table.
        sortBox.addActionListener(e -> {
            model.setRowCount(0);
            if (searchField.getText().equals("Τμήμα ή Πανεπιστήμιο")){
                loadUniversityData(model, "", (String) sortBox.getSelectedItem());
            }
            else {
            	loadUniversityData(model, searchField.getText(), (String) sortBox.getSelectedItem());
            }
        });

        
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BorderLayout());
        northContainer.add(topPanel, BorderLayout.NORTH);
        northContainer.add(filterPanel, BorderLayout.SOUTH);

        add(northContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private int fetchUserIdByEmail(String email) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM LOGIN WHERE email = ?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void loadUniversityData(DefaultTableModel model, String filterText, String sortOption) {
    	String tableName = "STREAM" + field;

    	try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
    	    StringBuilder query = new StringBuilder("SELECT * FROM " + tableName);
    	    List<String> conditions = new ArrayList<>();

    	    if (!filterText.trim().isEmpty()) {
    	        conditions.add("(department_name LIKE ? OR university_name LIKE ?)");
    	    }

    	    String orderBy = " ORDER BY department_name ASC"; // default order
    	    switch (sortOption) {
	    	    case "Αύξουσα - Πανεπιστήμιο":
		            orderBy = " ORDER BY university_name ASC";
		            break;
    	    	case "Αύξουσα - Πόλη":
    	            orderBy = " ORDER BY city ASC";
    	            break;
    	        //Manual Sorting the rest
    	    }

    	    if (!conditions.isEmpty()) {
    	        query.append(" WHERE ").append(String.join(" AND ", conditions));
    	    }
    	    query.append(orderBy);

    	    PreparedStatement ps = conn.prepareStatement(query.toString());

    	    if (!filterText.trim().isEmpty()) {
    	        String pattern = "%" + filterText + "%";
    	        ps.setString(1, pattern);
    	        ps.setString(2, pattern);
    	    }

    	    ResultSet rs = ps.executeQuery();

    	    PreparedStatement favStmt = conn.prepareStatement(
    	        "SELECT uni_id FROM FAVORITES WHERE user_id=? AND uni_table=?");
    	    favStmt.setInt(1, userId);
    	    favStmt.setString(2, tableName);

    	    Set<Integer> favoriteIds = new HashSet<>();
    	    ResultSet favRs = favStmt.executeQuery();
    	    while (favRs.next()) {
    	        favoriteIds.add(favRs.getInt("uni_id"));
    	    }

    	    List<Object[]> rowData = new ArrayList<>();

    	    while (rs.next()) {
    	        int uniId = rs.getInt("uni_id");
    	        String dep = rs.getString("department_name");
    	        String uni = rs.getString("university_name");
    	        String city = rs.getString("city");

    	        float score = 0;
    	        score += grades[0] * rs.getFloat("subject1_score");
    	        score += grades[1] * rs.getFloat("subject2_score");
    	        score += grades[2] * rs.getFloat("subject3_score");
    	        score += grades[3] * rs.getFloat("subject4_score");
    	        float moSchool = score / 100f;

    	        float entry = rs.getFloat("entry_score");
    	        float cutoff = rs.getFloat("cutoff_score");
    	        float diffEntry = moSchool - entry;
    	        float diffCutoff = moSchool - cutoff;

    	        String extra = rs.getBoolean("extra") ? "Ναι" : "Όχι";
    	        boolean isFav = favoriteIds.contains(uniId);

    	        rowData.add(new Object[]{
    	            dep, uni, city,
    	            String.format("%.2f", moSchool),
    	            String.format("%+.2f", diffEntry),
    	            String.format("%+.2f", diffCutoff),
    	            extra, isFav
    	        });
    	    }

    	    // Manual Sorting for the calculated fields
    	    if ("Φθίνουσα - Μόρια Σχολής".equals(sortOption)) {
    	        rowData.sort((a, b) -> {
    	            float aVal = Float.parseFloat(a[3].toString().replace(',', '.'));
    	            float bVal = Float.parseFloat(b[3].toString().replace(',', '.'));
    	            return Float.compare(bVal, aVal); // descending
    	        });
    	    } else if ("Φθίνουσα - Διαφορά με Βάση".equals(sortOption)) {
    	        rowData.sort((a, b) -> {
    	            float aVal = Float.parseFloat(a[4].toString().replace(',', '.'));
    	            float bVal = Float.parseFloat(b[4].toString().replace(',', '.'));
    	            return Float.compare(bVal, aVal); // descending
    	        });
    	    }

    	    model.setRowCount(0); // Clear table
    	    for (Object[] row : rowData) {
    	        model.addRow(row);
    	    }

    	    // Attach listener to new model
    	    tableChangeListener(model, tableName);

    	} catch (Exception e) {
    	    e.printStackTrace();
    	    JOptionPane.showMessageDialog(this, "Σφάλμα φόρτωσης δεδομένων.");
    	}
    }

    private void tableChangeListener(DefaultTableModel model, String tableName) {
        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 7) {
                int row = e.getFirstRow();
                boolean checked = (boolean) model.getValueAt(row, 7);
                String dep = (String) model.getValueAt(row, 0);
                String uni = (String) model.getValueAt(row, 1);

                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
                    // Get uni_id by department_name and university_name
                    PreparedStatement idStmt = conn.prepareStatement(
                        "SELECT uni_id FROM " + tableName + " WHERE department_name = ? AND university_name = ?");
                    idStmt.setString(1, dep);
                    idStmt.setString(2, uni);
                    ResultSet rs = idStmt.executeQuery();
                    if (rs.next()) {
                        int uniId = rs.getInt("uni_id");
                        if (checked) {
                            PreparedStatement insert = conn.prepareStatement(
                                "INSERT IGNORE INTO FAVORITES (user_id, uni_id, uni_table) VALUES (?, ?, ?)");
                            insert.setInt(1, userId);
                            insert.setInt(2, uniId);
                            insert.setString(3, tableName);
                            insert.executeUpdate();
                        } else {
                            PreparedStatement delete = conn.prepareStatement(
                                "DELETE FROM FAVORITES WHERE user_id=? AND uni_id=? AND uni_table=?");
                            delete.setInt(1, userId);
                            delete.setInt(2, uniId);
                            delete.setString(3, tableName);
                            delete.executeUpdate();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Αποτυχία αποθήκευσης αγαπημένου.");
                }
            }
        });
    }
}