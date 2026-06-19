package ui;

import database.ProductDAO;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Product;

public final class MonitoringPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable alertTable;
    private DefaultTableModel alertModel;

    private JTable requestTable;
    private DefaultTableModel requestModel;

    private final transient ProductDAO dao = new ProductDAO();

    @SuppressWarnings("this-escape")
    public MonitoringPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Inventory Monitoring & Alerts", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh Status 🔄");
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Panel
        JTabbedPane tabbedPane = new JTabbedPane();

        // TAB 1: Stock Alert Board
        JPanel alertPanel = new JPanel(new BorderLayout(10, 10));
        alertModel = new DefaultTableModel(new String[]{"Product ID", "Product Name", "Category", "Current Stock", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        alertTable = new JTable(alertModel);
        alertTable.setRowHeight(26);
        alertPanel.add(new JScrollPane(alertTable), BorderLayout.CENTER);

        JPanel alertControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton manualRestockBtn = new JButton("Restock Selected Product 📥");
        manualRestockBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        manualRestockBtn.setBackground(new Color(46, 204, 113));
        manualRestockBtn.setForeground(Color.WHITE);
        alertControls.add(manualRestockBtn);
        alertPanel.add(alertControls, BorderLayout.SOUTH);

        tabbedPane.addTab("Low & Out of Stock Alerts ⚠️", alertPanel);

        // TAB 2: Employee Restock Requests
        JPanel requestPanel = new JPanel(new BorderLayout(10, 10));
        requestModel = new DefaultTableModel(new String[]{"Request ID", "Product ID", "Product Name", "Requested Qty", "Status", "Requested By", "Date/Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        requestTable = new JTable(requestModel);
        requestTable.setRowHeight(26);
        requestPanel.add(new JScrollPane(requestTable), BorderLayout.CENTER);

        JPanel requestControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton approveBtn = new JButton("Approve Request ✓");
        approveBtn.setBackground(new Color(46, 204, 113));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton rejectBtn = new JButton("Reject Request ❌");
        rejectBtn.setBackground(new Color(231, 76, 60));
        rejectBtn.setForeground(Color.WHITE);
        rejectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        requestControls.add(approveBtn);
        requestControls.add(rejectBtn);
        requestPanel.add(requestControls, BorderLayout.SOUTH);

        tabbedPane.addTab("Employee Restock Requests 📜", requestPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Listeners
        refreshBtn.addActionListener(e -> refreshAll());
        manualRestockBtn.addActionListener(e -> manualRestock());
        approveBtn.addActionListener(e -> approveRequest());
        rejectBtn.addActionListener(e -> rejectRequest());

        refreshAll();
    }

    public void refreshAll() {
        loadStockAlerts();
        loadRestockRequests();
    }

    private void loadStockAlerts() {
        alertModel.setRowCount(0);
        List<Product> products = dao.getAllProducts();
        for (Product p : products) {
            if (p.getQuantity() < 10) {
                String status = p.getQuantity() == 0 ? "OUT OF STOCK 🚫" : "LOW STOCK ⚠️";
                alertModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getCategory(), p.getQuantity(), status
                });
            }
        }
    }

    private void loadRestockRequests() {
        requestModel.setRowCount(0);
        List<Object[]> requests = dao.getAllRestockRequests();
        for (Object[] req : requests) {
            // req: id, product_id, product_name, requested_quantity, status, requested_by, requested_at
            requestModel.addRow(new Object[]{
                req[0],
                req[1],
                req[2],
                req[3],
                req[4],
                req[5],
                new SimpleDateFormat("dd-MM-yyyy HH:mm").format((Timestamp) req[6])
            });
        }
    }

    private void manualRestock() {
        int selectedRow = alertTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an alert item first.");
            return;
        }

        int productId = (int) alertModel.getValueAt(selectedRow, 0);
        String name = (String) alertModel.getValueAt(selectedRow, 1);

        String qtyStr = JOptionPane.showInputDialog(this, "Enter Restock Quantity for \"" + name + "\":");
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        try {
            int qty = Integer.parseInt(qtyStr.trim());
            if (qty <= 0) throw new NumberFormatException();

            if (dao.restockProduct(productId, qty)) {
                JOptionPane.showMessageDialog(this, "Product restocked successfully!");
                refreshAll();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to restock product.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity. Please enter a positive integer.");
        }
    }

    private void approveRequest() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request first.");
            return;
        }

        int requestId = (int) requestModel.getValueAt(selectedRow, 0);
        String status = (String) requestModel.getValueAt(selectedRow, 4);

        if (!"PENDING".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Request has already been processed (Status: " + status + ").");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, "Approve this restock request?", "Confirm Approval", JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        if (dao.approveRestockRequest(requestId)) {
            JOptionPane.showMessageDialog(this, "Request approved successfully!");
            refreshAll();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to approve request. Ensure the product still exists.");
        }
    }

    private void rejectRequest() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request first.");
            return;
        }

        int requestId = (int) requestModel.getValueAt(selectedRow, 0);
        String status = (String) requestModel.getValueAt(selectedRow, 4);

        if (!"PENDING".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Request has already been processed (Status: " + status + ").");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, "Reject this restock request?", "Confirm Rejection", JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        if (dao.rejectRestockRequest(requestId)) {
            JOptionPane.showMessageDialog(this, "Request rejected successfully.");
            refreshAll();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to reject request.");
        }
    }
}
