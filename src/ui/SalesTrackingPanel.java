package ui;

import database.ProductDAO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class SalesTrackingPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JLabel totalRevenueLabel;
    private JLabel mostSoldLabel;
    private JTable salesTable;
    private DefaultTableModel salesModel;

    private CustomCharts.DonutPieChart pieChart;
    private CustomCharts.RoundedBarChart barChart;
    private CustomCharts.InteractiveSalesMap salesMap;

    private final transient ProductDAO dao = new ProductDAO();

    @SuppressWarnings("this-escape")
    public SalesTrackingPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. TOP HEADER & METRICS
        JPanel topArea = new JPanel(new BorderLayout(10, 10));
        topArea.setOpaque(false);

        JLabel titleLabel = new JLabel("Sales & Revenue Tracking", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        topArea.add(titleLabel, BorderLayout.WEST);

        // Refresh & Export buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh Stats 🔄");
        JButton exportBtn = new JButton("Export Sales Logs 📄");
        controlPanel.add(refreshBtn);
        controlPanel.add(exportBtn);
        topArea.add(controlPanel, BorderLayout.EAST);

        // Cards Grid
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 20, 20));
        cardsGrid.setOpaque(false);
        cardsGrid.setPreferredSize(new Dimension(0, 100));

        JPanel revenueCard = createMetricCard("Total Sales Revenue 💰", "Rs. 0.00", new Color(46, 204, 113));
        totalRevenueLabel = (JLabel) revenueCard.getClientProperty("valueLabel");

        JPanel productCard = createMetricCard("Most Popular Product 🏆", "None (0 sold)", new Color(52, 152, 219));
        mostSoldLabel = (JLabel) productCard.getClientProperty("valueLabel");

        cardsGrid.add(revenueCard);
        cardsGrid.add(productCard);
        topArea.add(cardsGrid, BorderLayout.SOUTH);

        add(topArea, BorderLayout.NORTH);

        // 2. CENTER AREA: Charts (Left) & Sales Table (Right)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.5);
        splitPane.setOpaque(false);

        // Tabbed Pane for Charts
        JTabbedPane chartsTabbedPane = new JTabbedPane();

        JPanel chartsContainer = new JPanel(new GridLayout(2, 1, 15, 15));
        chartsContainer.setOpaque(false);

        pieChart = new CustomCharts.DonutPieChart("Product Sales Contribution", new HashMap<>());
        barChart = new CustomCharts.RoundedBarChart("Category-wise Revenue (₹)", new HashMap<>());

        chartsContainer.add(pieChart);
        chartsContainer.add(barChart);

        salesMap = new CustomCharts.InteractiveSalesMap("Geographic Regional Sales Distribution", new HashMap<>());

        chartsTabbedPane.addTab("Revenue Analytics 📈", chartsContainer);
        chartsTabbedPane.addTab("Regional Sales Map 🗺️", salesMap);

        splitPane.setLeftComponent(chartsTabbedPane);

        // Sales Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Sales History"));

        salesModel = new DefaultTableModel(
                new String[] { "ID", "Product Name", "Category", "Qty Sold", "Total Value", "Biller", "Timestamp" }, 0);
        salesTable = new JTable(salesModel);
        salesTable.setRowHeight(22);
        salesTable.setAutoCreateRowSorter(true);
        tablePanel.add(new JScrollPane(salesTable), BorderLayout.CENTER);

        splitPane.setRightComponent(tablePanel);
        add(splitPane, BorderLayout.CENTER);

        // Action Listeners
        refreshBtn.addActionListener(e -> loadStats());
        exportBtn.addActionListener(e -> exportSalesLogs());

        loadStats();
    }

    private JPanel createMetricCard(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Draw rounded background block
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));

                // Light inner glow
                g2.setColor(new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Double(3, 3, getWidth() - 6, getHeight() - 6, 12, 12));
            }
        };
        panel.setPreferredSize(new Dimension(220, 90));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        panel.setOpaque(false);

        JLabel tLabel = new JLabel(title);
        tLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tLabel.setForeground(Color.WHITE);

        JLabel vLabel = new JLabel(value);
        vLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        vLabel.setForeground(Color.WHITE);

        panel.add(tLabel, BorderLayout.NORTH);
        panel.add(vLabel, BorderLayout.CENTER);
        panel.putClientProperty("valueLabel", vLabel);

        return panel;
    }

    public void loadStats() {
        // 1. Load Labels
        double totalRevenue = dao.getTotalRevenue();
        totalRevenueLabel.setText(String.format("Rs. %.2f", totalRevenue));

        Object[] mostSold = dao.getMostSoldProduct();
        mostSoldLabel.setText(mostSold[0] + " (" + mostSold[1] + " units)");

        // 2. Load Charts data
        Map<String, Integer> productSales = dao.getProductSalesMap();
        pieChart.setData(productSales);

        // Load Category Revenue for Bar Chart
        Map<String, Integer> categoryRevenue = new HashMap<>();
        List<Object[]> salesHistory = dao.getSalesHistory();
        for (Object[] row : salesHistory) {
            String category = (String) row[2];
            double price = (double) row[4];
            categoryRevenue.put(category, categoryRevenue.getOrDefault(category, 0) + (int) price);
        }
        barChart.setData(categoryRevenue);

        // Load Regional Sales Map
        Map<String, Double> regionalSales = dao.getRegionalSalesMap();
        salesMap.setData(regionalSales);

        // 3. Load Table
        salesModel.setRowCount(0);
        int serial = 1;
        for (Object[] row : salesHistory) {
            // Row contents: product_id, product_name, category, quantity_sold, total_price,
            // sold_by, sold_at
            salesModel.addRow(new Object[] {
                    serial++,
                    row[1],
                    row[2],
                    row[3],
                    String.format("Rs. %.2f", (double) row[4]),
                    row[5],
                    new SimpleDateFormat("dd-MM-yyyy HH:mm").format((Timestamp) row[6])
            });
        }
    }

    private void exportSalesLogs() {
        if (salesModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No sales logs available to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Sales History");
        chooser.setSelectedFile(new File("Sales_Report_" + System.currentTimeMillis() + ".csv"));

        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (FileWriter writer = new FileWriter(file)) {
            // Write Headers
            writer.write("Serial,Product Name,Category,Quantity Sold,Total Revenue,Biller,Timestamp\n");

            List<Object[]> salesHistory = dao.getSalesHistory();
            int serial = 1;
            for (Object[] row : salesHistory) {
                writer.write(
                        serial++ + ","
                                + escapeCSV(row[1].toString()) + ","
                                + escapeCSV(row[2].toString()) + ","
                                + row[3] + ","
                                + row[4] + ","
                                + escapeCSV(row[5].toString()) + ","
                                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Timestamp) row[6]) + "\n");
            }

            JOptionPane.showMessageDialog(this, "Sales history exported successfully to:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error exporting sales history: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
