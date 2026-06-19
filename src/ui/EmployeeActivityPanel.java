package ui;

import database.ProductDAO;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Product;
import service.SessionManager;

public final class EmployeeActivityPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JLabel soldTodayLabel;
    private JLabel revenueTodayLabel;
    private JLabel pendingReqLabel;

    private JTable requestsTable;
    private DefaultTableModel requestsModel;

    private JComboBox<String> productCombo;
    private JTextField qtyField;

    private final transient ProductDAO dao = new ProductDAO();

    @SuppressWarnings("this-escape")
    public EmployeeActivityPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("My Requests & Sales Activity", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh Activity 🔄");
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // TOP METRICS PANEL
        JPanel metricsPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        metricsPanel.setOpaque(false);
        metricsPanel.setPreferredSize(new Dimension(0, 80));

        JPanel soldCard = createMetricCard("My Units Sold Today", "0 units", new Color(52, 152, 219));
        soldTodayLabel = (JLabel) soldCard.getClientProperty("valueLabel");

        JPanel revenueCard = createMetricCard("My Sales Revenue Today", "Rs. 0.00", new Color(46, 204, 113));
        revenueTodayLabel = (JLabel) revenueCard.getClientProperty("valueLabel");

        JPanel reqCard = createMetricCard("My Pending Requests", "0 pending", new Color(241, 196, 15));
        pendingReqLabel = (JLabel) reqCard.getClientProperty("valueLabel");

        metricsPanel.add(soldCard);
        metricsPanel.add(revenueCard);
        metricsPanel.add(reqCard);

        // CENTER SPLIT: Left is requests history table, Right is submission form
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Panel: Request History
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createTitledBorder("My Restock Request History"));
        
        requestsModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Qty Requested", "Status", "Requested At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        requestsTable = new JTable(requestsModel);
        requestsTable.setRowHeight(24);
        leftPanel.add(new JScrollPane(requestsTable), BorderLayout.CENTER);

        // Right Panel: Form
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Request New Restock"));
        rightPanel.setPreferredSize(new Dimension(300, 0));

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 15));
        form.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        form.add(new JLabel("Select Product:"));
        productCombo = new JComboBox<>();
        form.add(productCombo);

        form.add(new JLabel("Quantity to Request:"));
        qtyField = new JTextField("50");
        form.add(qtyField);

        JButton submitBtn = new JButton("Submit Restock Request 🚀");
        submitBtn.setBackground(new Color(99, 102, 241));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        rightPanel.add(form);
        rightPanel.add(Box.createVerticalStrut(10));
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.add(submitBtn);
        rightPanel.add(btnWrapper);
        rightPanel.add(Box.createVerticalGlue());

        // Assemble Grid
        gbc.weightx = 0.65;
        gbc.gridx = 0;
        centerPanel.add(leftPanel, gbc);

        gbc.weightx = 0.35;
        gbc.gridx = 1;
        centerPanel.add(rightPanel, gbc);

        JPanel mainLayout = new JPanel(new BorderLayout(15, 15));
        mainLayout.setOpaque(false);
        mainLayout.add(metricsPanel, BorderLayout.NORTH);
        mainLayout.add(centerPanel, BorderLayout.CENTER);

        add(mainLayout, BorderLayout.CENTER);

        // Event Listeners
        refreshBtn.addActionListener(e -> refreshAll());
        submitBtn.addActionListener(e -> submitRequest());

        refreshAll();
    }

    public void refreshAll() {
        loadProducts();
        loadActivityStats();
        loadRequestHistory();
    }

    private JPanel createMetricCard(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panel.setOpaque(false);

        JLabel tLabel = new JLabel(title);
        tLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tLabel.setForeground(Color.WHITE);

        JLabel vLabel = new JLabel(value);
        vLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        vLabel.setForeground(Color.WHITE);

        panel.add(tLabel, BorderLayout.NORTH);
        panel.add(vLabel, BorderLayout.CENTER);
        panel.putClientProperty("valueLabel", vLabel);

        return panel;
    }

    private void loadProducts() {
        productCombo.removeAllItems();
        List<Product> products = dao.getAllProducts();
        for (Product p : products) {
            productCombo.addItem(p.getName() + " (ID: " + p.getId() + ")");
        }
    }

    private void loadActivityStats() {
        String me = SessionManager.getUsername();
        List<Object[]> sales = dao.getSalesHistory();

        int unitsSoldToday = 0;
        double revenueToday = 0.0;

        Calendar calToday = Calendar.getInstance();
        calToday.set(Calendar.HOUR_OF_DAY, 0);
        calToday.set(Calendar.MINUTE, 0);
        calToday.set(Calendar.SECOND, 0);
        calToday.set(Calendar.MILLISECOND, 0);
        Date todayMidnight = calToday.getTime();

        for (Object[] sale : sales) {
            // sale: product_id, product_name, category, quantity_sold, total_price, sold_by, sold_at
            String soldBy = (String) sale[5];
            Timestamp soldAt = (Timestamp) sale[6];
            int qty = (int) sale[3];
            double total = (double) sale[4];

            if (me != null && me.equalsIgnoreCase(soldBy) && soldAt.after(todayMidnight)) {
                unitsSoldToday += qty;
                revenueToday += total;
            }
        }

        soldTodayLabel.setText(unitsSoldToday + " units");
        revenueTodayLabel.setText(String.format("Rs. %.2f", revenueToday));
    }

    private void loadRequestHistory() {
        requestsModel.setRowCount(0);
        String me = SessionManager.getUsername();
        List<Object[]> allReqs = dao.getAllRestockRequests();
        
        int pendingCount = 0;
        for (Object[] req : allReqs) {
            // req: id, product_id, product_name, requested_quantity, status, requested_by, requested_at
            String requestedBy = (String) req[5];
            if (me != null && me.equalsIgnoreCase(requestedBy)) {
                String status = (String) req[4];
                if ("PENDING".equalsIgnoreCase(status)) {
                    pendingCount++;
                }

                requestsModel.addRow(new Object[]{
                    req[0],
                    req[2],
                    req[3],
                    status,
                    new SimpleDateFormat("dd-MM-yyyy HH:mm").format((Timestamp) req[6])
                });
            }
        }
        pendingReqLabel.setText(pendingCount + " pending");
    }

    private void submitRequest() {
        int index = productCombo.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        String item = (String) productCombo.getSelectedItem();
        int productId = -1;
        String productName = "";
        try {
            int startIdx = item.lastIndexOf("(ID: ") + 5;
            int endIdx = item.lastIndexOf(")");
            productId = Integer.parseInt(item.substring(startIdx, endIdx));
            productName = item.substring(0, item.lastIndexOf(" (ID: "));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to parse product ID.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();

            if (dao.addRestockRequest(productId, productName, qty, SessionManager.getUsername())) {
                JOptionPane.showMessageDialog(this, "Restock request submitted successfully!");
                qtyField.setText("50");
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit request.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive request quantity.");
        }
    }
}
