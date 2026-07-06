import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

@SuppressWarnings("serial")
public class StreamXDataFrame extends JFrame {
    private final int stream;
    private int userId;

    private static final String[] STREAM_LABELS = {
        "Θεωρητική", "Θετική", "Υγείας", "Οικονομίας και Πληροφορικής"
    };

    private static final String[] COLUMN_NAMES = {
        "Τμήμα", "Πανεπιστήμιο", "Πόλη",
        "Μόρια Εισαγωγής", "ΕΒΕ",
        "Ισχύουν επιπλέον προϋποθέσεις", "Αγαπημένο"
    };

    public StreamXDataFrame(int stream, String email) {
        this.stream = stream;
        this.userId = fetchUserIdByEmail(email);
        if (userId == -1) {
            JOptionPane.showMessageDialog(this, "Αποτυχία εύρεσης χρήστη.", "Σφάλμα", JOptionPane.ERROR_MESSAGE); // the user's row was deleted or modified externally while the app was running
            dispose();
            return;
        }

        setTitle("Βάσεις: " + STREAM_LABELS[stream-1]);
        setSize(1000, 600);
        setMinimumSize(new Dimension(650, 322));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.LIGHT_GRAY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel infoLabel = new JLabel("Κατεύθυνση - " + STREAM_LABELS[stream-1]);
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton homeButton = new DarkButton("Αρχική");
        homeButton.setPreferredSize(new Dimension(100, 30));
        homeButton.addActionListener(e -> {
            dispose();
            new MainMenuFrame(email);
        });

        topPanel.add(infoLabel, BorderLayout.WEST);
        topPanel.add(homeButton, BorderLayout.EAST);

        // Filter Panel
        JPanel filterPanel = new JPanel(new BorderLayout(10, 10));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        filterPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField("Τμήμα ή Πανεπιστήμιο");
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setBackground(Color.DARK_GRAY);
        searchField.setForeground(Color.GRAY);
        searchField.setCaretColor(Color.WHITE);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Τμήμα ή Πανεπιστήμιο")) {
                    searchField.setText("");
                    searchField.setForeground(Color.WHITE);
                }
            }

            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Τμήμα ή Πανεπιστήμιο");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        String[] sortOptions = {
            "Αύξουσα - Τμήμα",
            "Αύξουσα - Πανεπιστήμιο",
            "Αύξουσα - Πόλη",
            "Φθίνουσα - Μόρια Εισαγωγής"
        };
        JComboBox<String> sortBox = new JComboBox<>(sortOptions);
        sortBox.setPreferredSize(new Dimension(190, 30));
        sortBox.setBackground(Color.DARK_GRAY);
        sortBox.setForeground(Color.WHITE);
        sortBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sortBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JButton statsButton = new DarkButton("Στατιστικά");
        statsButton.setPreferredSize(new Dimension(120, 30));
        statsButton.addActionListener(e -> {
            new StreamXStatisticsDialog(this, email, stream, STREAM_LABELS[stream-1]).setVisible(true);
        });

        JPanel rightFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightFilter.setBackground(Color.WHITE);
        rightFilter.add(statsButton);
        rightFilter.add(Box.createRigidArea(new Dimension(30, 0))); // 30px horizontal space
        rightFilter.add(sortBox);

        filterPanel.add(searchField, BorderLayout.WEST);
        filterPanel.add(rightFilter, BorderLayout.EAST);

        DefaultTableModel model = new DefaultTableModel(COLUMN_NAMES, 0) {
            public boolean isCellEditable(int row, int col) {
                return col == 6;
            }

            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 6 ? Boolean.class : String.class;
            }
        };

        JTable table = new JTable(model);
        TableColumnModel colModel = table.getColumnModel();

        colModel.getColumn(0).setPreferredWidth(340);
        colModel.getColumn(1).setPreferredWidth(130);
        colModel.getColumn(2).setPreferredWidth(80);
        colModel.getColumn(3).setPreferredWidth(120);
        colModel.getColumn(4).setPreferredWidth(110);
        colModel.getColumn(5).setPreferredWidth(170);
        colModel.getColumn(6).setPreferredWidth(70);

        // Center align numeric and extra
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        colModel.getColumn(3).setCellRenderer(center);
        colModel.getColumn(4).setCellRenderer(center);
        colModel.getColumn(5).setCellRenderer(center);

        JScrollPane scrollPane = new JScrollPane(table);

        loadStreamData(model, "", (String) sortBox.getSelectedItem());

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                model.setRowCount(0);
                loadStreamData(model, searchField.getText().equals("Τμήμα ή Πανεπιστήμιο") ? "" : searchField.getText(),
                        (String) sortBox.getSelectedItem());
            }
        });

        sortBox.addActionListener(e -> {
            model.setRowCount(0);
            loadStreamData(model, searchField.getText().equals("Τμήμα ή Πανεπιστήμιο") ? "" : searchField.getText(),
                    (String) sortBox.getSelectedItem());
        });

        JPanel north = new JPanel(new BorderLayout());
        north.add(topPanel, BorderLayout.NORTH);
        north.add(filterPanel, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

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

    private void loadStreamData(DefaultTableModel model, String filter, String sortOption) {
        String tableName = "STREAM" + stream;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {

            StringBuilder query = new StringBuilder("SELECT * FROM " + tableName);
            if (!filter.trim().isEmpty()) {
                query.append(" WHERE department_name LIKE ? OR university_name LIKE ?");
            }

            switch (sortOption) {
                case "Αύξουσα - Πανεπιστήμιο":
                    query.append(" ORDER BY university_name ASC");
                    break;
                case "Αύξουσα - Πόλη":
                    query.append(" ORDER BY city ASC");
                    break;
                case "Φθίνουσα - Μόρια Εισαγωγής":
                    query.append(" ORDER BY entry_score DESC");
                    break;
                default:
                    query.append(" ORDER BY department_name ASC");
            }

            PreparedStatement ps = conn.prepareStatement(query.toString());
            if (!filter.trim().isEmpty()) {
                String pattern = "%" + filter + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
            }

            ResultSet rs = ps.executeQuery();

            // Favorites
            PreparedStatement favStmt = conn.prepareStatement(
                    "SELECT uni_id FROM FAVORITES WHERE user_id=? AND uni_table=?");
            favStmt.setInt(1, userId);
            favStmt.setString(2, tableName);
            ResultSet favRs = favStmt.executeQuery();
            Set<Integer> favIds = new HashSet<>();
            while (favRs.next()) favIds.add(favRs.getInt("uni_id"));

            while (rs.next()) {
                int uniId = rs.getInt("uni_id");
                String dep = rs.getString("department_name");
                String uni = rs.getString("university_name");
                String city = rs.getString("city");
                float entry = rs.getFloat("entry_score");
                float cutoff = rs.getFloat("cutoff_score");
                String extra = rs.getBoolean("extra") ? "Ναι" : "Όχι";
                boolean fav = favIds.contains(uniId);

                model.addRow(new Object[]{
                        dep, uni, city,
                        String.format("%.2f", entry),
                        String.format("%.2f", cutoff),
                        extra, fav
                });
            }

            attachFavoriteListener(model, tableName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void attachFavoriteListener(DefaultTableModel model, String tableName) {
        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 6) {
                int row = e.getFirstRow();
                boolean isFav = (boolean) model.getValueAt(row, 6);
                String dep = (String) model.getValueAt(row, 0);
                String uni = (String) model.getValueAt(row, 1);

                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/panell", "root", "")) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT uni_id FROM " + tableName + " WHERE department_name = ? AND university_name = ?");
                    stmt.setString(1, dep);
                    stmt.setString(2, uni);

                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int uniId = rs.getInt("uni_id");
                        if (isFav) {
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
                }
            }
        });
    }
}
