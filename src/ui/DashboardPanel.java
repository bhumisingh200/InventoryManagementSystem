package ui;

import database.ProductDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import model.Product;

public final class DashboardPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JLabel productsCountLabel;
    private JLabel categoriesCountLabel;
    private JLabel lowStockLabel;
    private JLabel outOfStockLabel;
    private JLabel totalValueLabel;

    private JLabel highestPriceLabel;
    private JLabel lowestStockLabel;
    private JLabel mostSoldLabel;

    private JPanel recentLogsPanel;

    private CustomCharts.RoundedBarChart barChart;
    private CustomCharts.GlowingLineGraph lineGraph;
    private final transient ProductDAO dao = new ProductDAO();

    @SuppressWarnings("this-escape")
    public DashboardPanel(MainFrame mainFrame) {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("ERP Inventory Dashboard", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh Dashboard 🔄");
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. METRIC CARDS GRID
        JPanel cardsPanel = new JPanel(new GridLayout(1, 5, 12, 12));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(0, 100));

        JPanel productsCard = createClickableMetricCard("Total Products 📦", "0", new Color(52, 152, 219), () -> mainFrame.showProducts());
        productsCountLabel = (JLabel) productsCard.getClientProperty("valueLabel");

        JPanel categoriesCard = createClickableMetricCard("Categories 📦", "0", new Color(155, 89, 182), () -> mainFrame.showProducts());
        categoriesCountLabel = (JLabel) categoriesCard.getClientProperty("valueLabel");

        JPanel lowStockCard = createClickableMetricCard("Low Stock ⚠️", "0", new Color(230, 126, 34), () -> mainFrame.showProductsTabWithLowStock());
        lowStockLabel = (JLabel) lowStockCard.getClientProperty("valueLabel");

        JPanel outOfStockCard = createClickableMetricCard("Out of Stock 🚫", "0", new Color(231, 76, 60), () -> mainFrame.showProducts());
        outOfStockLabel = (JLabel) outOfStockCard.getClientProperty("valueLabel");

        JPanel valueCard = createMetricCard("Net Value 💰", "Rs. 0.00", new Color(46, 204, 113));
        totalValueLabel = (JLabel) valueCard.getClientProperty("valueLabel");

        cardsPanel.add(productsCard);
        cardsPanel.add(categoriesCard);
        cardsPanel.add(lowStockCard);
        cardsPanel.add(outOfStockCard);
        cardsPanel.add(valueCard);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);

        // 3. CHARTS AND INSIGHTS SPLIT AREA
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(550);
        splitPane.setOpaque(false);

        // Left component: Category Bar Chart and Sales Trend Line Chart (JTabbedPane)
        JTabbedPane chartTabs = new JTabbedPane();
        barChart = new CustomCharts.RoundedBarChart("Category-wise Stock Levels", new HashMap<>());
        lineGraph = new CustomCharts.GlowingLineGraph("Recent Sales Revenue Trends", new java.util.ArrayList<>(), new java.util.ArrayList<>());
        
        chartTabs.addTab("Stock Levels 📊", barChart);
        chartTabs.addTab("Sales Trends 📈", lineGraph);
        splitPane.setLeftComponent(chartTabs);

        // Right component: Insights Panel (Highest price, lowest stock, recent logs)
        JPanel insightsPanel = new JPanel();
        insightsPanel.setLayout(new BoxLayout(insightsPanel, BoxLayout.Y_AXIS));
        insightsPanel.setBorder(BorderFactory.createTitledBorder("Key Business Insights"));

        // Panel for stats
        JPanel statsSubPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        statsSubPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        highestPriceLabel = new JLabel("Highest Priced: Loading...");
        highestPriceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lowestStockLabel = new JLabel("Lowest Stock: Loading...");
        lowestStockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mostSoldLabel = new JLabel("Most Sold Product: Loading...");
        mostSoldLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        statsSubPanel.add(highestPriceLabel);
        statsSubPanel.add(lowestStockLabel);
        statsSubPanel.add(mostSoldLabel);
        insightsPanel.add(statsSubPanel);

        // Panel for recent logs
        recentLogsPanel = new JPanel();
        recentLogsPanel.setLayout(new BoxLayout(recentLogsPanel, BoxLayout.Y_AXIS));
        recentLogsPanel.setBorder(BorderFactory.createTitledBorder("Recent Activity Logs"));
        
        insightsPanel.add(recentLogsPanel);

        splitPane.setRightComponent(insightsPanel);
        centerPanel.add(splitPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Action Listener
        refreshBtn.addActionListener(e -> loadStats());
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
                
                g2.setColor(color);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                
                g2.setColor(new Color(255, 255, 255, 30));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Double(3, 3, getWidth() - 6, getHeight() - 6, 12, 12));
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        panel.setOpaque(false);

        JLabel tLabel = new JLabel(title);
        tLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tLabel.setForeground(Color.WHITE);

        JLabel vLabel = new JLabel(value);
        vLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        vLabel.setForeground(Color.WHITE);

        panel.add(tLabel, BorderLayout.NORTH);
        panel.add(vLabel, BorderLayout.CENTER);
        panel.putClientProperty("valueLabel", vLabel);

        return panel;
    }

    private JPanel createClickableMetricCard(String title, String value, Color color, Runnable onClick) {
        JPanel panel = createMetricCard(title, value, color);
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        return panel;
    }

    public void loadStats() {
        // Query database
        int totalProducts = dao.getTotalProductsCount();
        int totalCategories = dao.getTotalCategoriesCount();
        int lowStock = dao.getLowStockCount();
        int outOfStock = dao.getOutOfStockCount();
        double totalValue = dao.getTotalInventoryValue();

        productsCountLabel.setText(String.valueOf(totalProducts));
        categoriesCountLabel.setText(String.valueOf(totalCategories));
        lowStockLabel.setText(String.valueOf(lowStock));
        outOfStockLabel.setText(String.valueOf(outOfStock));
        totalValueLabel.setText(String.format("Rs. %.2f", totalValue));

        // Load highest/lowest priced
        Product hp = dao.getHighestPricedProduct();
        if (hp != null) {
            highestPriceLabel.setText("💎 Highest Priced: " + hp.getName() + " (Rs. " + String.format("%.2f", hp.getPrice()) + ")");
        } else {
            highestPriceLabel.setText("💎 Highest Priced: -");
        }

        Product ls = dao.getLowestStockProduct();
        if (ls != null) {
            lowestStockLabel.setText("⚠️ Lowest Stock: " + ls.getName() + " (" + ls.getQuantity() + " units left)");
        } else {
            lowestStockLabel.setText("⚠️ Lowest Stock: -");
        }

        Object[] ms = dao.getMostSoldProduct();
        if (ms != null && ms.length > 0) {
            mostSoldLabel.setText("🏆 Most Sold: " + ms[0] + " (" + ms[1] + " sold)");
        } else {
            mostSoldLabel.setText("🏆 Most Sold: -");
        }

        // Load Category Chart
        Map<String, Integer> categoryStock = dao.getCategoryStockMap();
        barChart.setData(categoryStock);

        // Load Sales Trend Line Graph
        List<Double> recentSales = dao.getRecentSalesValues();
        List<String> recentLabels = dao.getRecentSalesLabels();
        lineGraph.setData(recentSales, recentLabels);

        // Load Recent Logs
        recentLogsPanel.removeAll();
        List<Object[]> logs = dao.getActivityLogs();
        int maxLogs = Math.min(3, logs.size());
        for (int i = 0; i < maxLogs; i++) {
            Object[] log = logs.get(i);
            String username = (String) log[0];
            String action = (String) log[1];
            Timestamp time = (Timestamp) log[2];
            String formattedTime = new SimpleDateFormat("HH:mm:ss").format(time);

            JLabel logLabel = new JLabel("<html><b>" + username + "</b>: " + action + " <font color='gray'>[" + formattedTime + "]</font></html>");
            logLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            logLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            recentLogsPanel.add(logLabel);
        }
        if (maxLogs == 0) {
            recentLogsPanel.add(new JLabel("No recent activity logs."));
        }
        recentLogsPanel.revalidate();
        recentLogsPanel.repaint();
    }
}