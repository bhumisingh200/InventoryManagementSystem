package ui;

import database.ProductDAO;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public final class ActivityLogsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable logsTable;
    private DefaultTableModel logsModel;
    private JTextField searchField;
    private transient TableRowSorter<DefaultTableModel> sorter;

    private final transient ProductDAO dao = new ProductDAO();

    @SuppressWarnings("this-escape")
    public ActivityLogsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Audit & Activity Logs", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Control Panel with Search & Refresh
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        controls.setOpaque(false);

        controls.add(new JLabel("Filter Logs:"));
        searchField = new JTextField(18);
        controls.add(searchField);

        JButton refreshBtn = new JButton("Refresh 🔄");
        controls.add(refreshBtn);
        headerPanel.add(controls, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table
        logsModel = new DefaultTableModel(new String[]{"Username", "Action Performed", "Log Timestamp"}, 0);
        logsTable = new JTable(logsModel);
        logsTable.setRowHeight(22);
        
        // Sorting and filtering setup
        sorter = new TableRowSorter<>(logsModel);
        logsTable.setRowSorter(sorter);
        
        add(new JScrollPane(logsTable), BorderLayout.CENTER);

        // Listeners
        refreshBtn.addActionListener(e -> loadLogs());
        searchField.addCaretListener(e -> filterLogs());

        loadLogs();
    }

    public void loadLogs() {
        logsModel.setRowCount(0);
        List<Object[]> logs = dao.getActivityLogs();
        
        for (Object[] log : logs) {
            String username = (String) log[0];
            String action = (String) log[1];
            Timestamp time = (Timestamp) log[2];
            String formattedTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(time);

            logsModel.addRow(new Object[]{
                username,
                action,
                formattedTime
            });
        }
    }

    private void filterLogs() {
        String filterText = searchField.getText().trim();
        if (filterText.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Case-insensitive filtering
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText));
        }
    }
}
