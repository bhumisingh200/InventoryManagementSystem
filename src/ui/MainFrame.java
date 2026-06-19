package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import service.DatabaseBackupService;
import service.LoggerService;
import service.SessionManager;

public final class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Theme state
    private boolean isDarkMode = false;
    private JButton themeToggleBtn;

    // View panels
    private transient DashboardPanel dashboardPanel;
    private transient ProductPanel productPanel;
    private transient BillingPanel billingPanel;
    private transient SalesTrackingPanel salesTrackingPanel;
    private transient ActivityLogsPanel activityLogsPanel;

    private transient CategoryPanel categoryPanel;
    private transient EmployeePanel employeePanel;
    private transient MonitoringPanel monitoringPanel;
    private transient EmployeeActivityPanel employeeActivityPanel;
    private transient ProfilePanel profilePanel;

    private String activeCardName = "dashboard";

    @SuppressWarnings("this-escape")
    public MainFrame() {
        // Set active card based on role
        activeCardName = SessionManager.isAdmin() ? "dashboard" : "products";

        setTitle("Inventory Management System (ERP)");
        setSize(1250, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initialize view panels
        dashboardPanel = new DashboardPanel(this);
        productPanel = new ProductPanel();
        billingPanel = new BillingPanel(this);
        salesTrackingPanel = new SalesTrackingPanel();
        activityLogsPanel = new ActivityLogsPanel();

        categoryPanel = new CategoryPanel();
        employeePanel = new EmployeePanel();
        monitoringPanel = new MonitoringPanel();
        employeeActivityPanel = new EmployeeActivityPanel();
        profilePanel = new ProfilePanel();

        // Build UI
        createHeader();
        createSidebar();
        createContentArea();

        // Initial Stats Load
        refreshAllPanels();

        setVisible(true);
    }

    private void createHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), getWidth(), 0, new Color(30, 41, 59));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // App Logo/Title with Quick Add (+) button for Admin
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("🚀 INVENTORY ERP SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        titlePanel.add(title);

        if (SessionManager.isAdmin()) {
            JButton quickAddHeaderBtn = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (getModel().isPressed()) {
                        g2.setColor(new Color(67, 56, 202));
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(79, 70, 229));
                    } else {
                        g2.setColor(new Color(99, 102, 241));
                    }
                    g2.fillOval(0, 0, getWidth(), getHeight());

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth("+")) / 2;
                    int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent() - 1;
                    g2.drawString("+", x, y);
                }
            };
            quickAddHeaderBtn.setPreferredSize(new Dimension(28, 28));
            quickAddHeaderBtn.setBorderPainted(false);
            quickAddHeaderBtn.setContentAreaFilled(false);
            quickAddHeaderBtn.setFocusPainted(false);
            quickAddHeaderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            quickAddHeaderBtn.setToolTipText("Quick Add New Product");
            quickAddHeaderBtn.addActionListener(e -> {
                new ProductFormDialog(productPanel);
                refreshAllPanels();
            });
            titlePanel.add(quickAddHeaderBtn);
        }
        header.add(titlePanel, BorderLayout.WEST);

        // Right side: User card, theme switch, logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        // User info
        String username = SessionManager.getUsername() != null ? SessionManager.getUsername() : "Guest";
        String role = SessionManager.getRole() != null ? SessionManager.getRole().toUpperCase() : "VISITOR";
        JLabel userLabel = new JLabel("👤 Logged in: " + username + " (" + role + ")");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(Color.WHITE);
        rightPanel.add(userLabel);

        // Theme Toggle
        themeToggleBtn = new JButton("🌙 Dark Mode");
        themeToggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        themeToggleBtn.setBackground(new Color(52, 73, 94));
        themeToggleBtn.setForeground(Color.WHITE);
        themeToggleBtn.addActionListener(e -> toggleTheme());
        rightPanel.add(themeToggleBtn);

        // Logout
        JButton logoutBtn = new JButton("Logout 🚪");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(e -> logout());
        rightPanel.add(logoutBtn);

        header.add(rightPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(15, 23, 42)); // Deep Slate Navy
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 12, 6, 12);

        boolean isAdmin = SessionManager.isAdmin();

        if (isAdmin) {
            JButton dashBtn = createSidebarBtn("📊 Dashboard", "dashboard");
            JButton prodBtn = createSidebarBtn("📦 Products List", "products");
            JButton catBtn = createSidebarBtn("📁 Categories", "categories");
            JButton monitorBtn = createSidebarBtn("⚠️ Stock Monitoring", "monitoring");
            JButton empBtn = createSidebarBtn("👥 Employees", "employees");
            JButton billBtn = createSidebarBtn("🧾 Billing & POS", "billing");
            JButton salesBtn = createSidebarBtn("💰 Sales & Revenue", "sales");
            JButton logsBtn = createSidebarBtn("📜 Activity Logs", "logs");
            JButton profileBtn = createSidebarBtn("👤 My Profile", "profile");

            sidebar.add(dashBtn, gbc);
            gbc.gridy++;
            sidebar.add(prodBtn, gbc);
            gbc.gridy++;
            sidebar.add(catBtn, gbc);
            gbc.gridy++;
            sidebar.add(monitorBtn, gbc);
            gbc.gridy++;
            sidebar.add(empBtn, gbc);
            gbc.gridy++;
            sidebar.add(billBtn, gbc);
            gbc.gridy++;
            sidebar.add(salesBtn, gbc);
            gbc.gridy++;
            sidebar.add(logsBtn, gbc);
            gbc.gridy++;
            sidebar.add(profileBtn, gbc);
            gbc.gridy++;
        } else {
            JButton prodBtn = createSidebarBtn("📦 Product Catalog", "products");
            JButton billBtn = createSidebarBtn("🧾 Billing & POS", "billing");
            JButton reqBtn = createSidebarBtn("🚀 My Activity & Reqs", "employee_activity");
            JButton profileBtn = createSidebarBtn("👤 My Profile", "profile");

            sidebar.add(prodBtn, gbc);
            gbc.gridy++;
            sidebar.add(billBtn, gbc);
            gbc.gridy++;
            sidebar.add(reqBtn, gbc);
            gbc.gridy++;
            sidebar.add(profileBtn, gbc);
            gbc.gridy++;
        }

        // Add spacer
        gbc.weighty = 1.0;
        sidebar.add(Box.createVerticalGlue(), gbc);
        gbc.weighty = 0.0;
        gbc.gridy++;

        // DB Backup & Restore Panel (Admin only)
        if (isAdmin) {
            JPanel dbControls = new JPanel(new GridLayout(2, 1, 0, 5));
            dbControls.setOpaque(false);

            JButton backupBtn = new JButton("💾 Backup Database");
            backupBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            backupBtn.setBackground(new Color(39, 174, 96));
            backupBtn.setForeground(Color.WHITE);
            backupBtn.setPreferredSize(new Dimension(0, 30));
            backupBtn.addActionListener(e -> backupDatabase());

            JButton restoreBtn = new JButton("🔄 Restore Database");
            restoreBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            restoreBtn.setBackground(new Color(230, 126, 34));
            restoreBtn.setForeground(Color.WHITE);
            restoreBtn.setPreferredSize(new Dimension(0, 30));
            restoreBtn.addActionListener(e -> restoreDatabase());

            dbControls.add(backupBtn);
            dbControls.add(restoreBtn);

            sidebar.add(dbControls, gbc);
        }

        add(sidebar, BorderLayout.WEST);
    }

    private JButton createSidebarBtn(String text, String cardName) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                boolean isActive = activeCardName.equals(cardName);

                if (isActive) {
                    g2.setColor(new Color(99, 102, 241, 220)); // Glowing Indigo capsule
                    g2.fill(new java.awt.geom.RoundRectangle2D.Double(8, 4, getWidth() - 16, getHeight() - 8, 8, 8));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 18)); // Soft hover highlight
                    g2.fill(new java.awt.geom.RoundRectangle2D.Double(8, 4, getWidth() - 16, getHeight() - 8, 8, 8));
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setPreferredSize(new Dimension(0, 38));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            activeCardName = cardName;
            cardLayout.show(contentPanel, cardName);
            refreshAllPanels();
            btn.getParent().repaint();
        });
        return btn;
    }

    private void createContentArea() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(productPanel, "products");
        contentPanel.add(billingPanel, "billing");
        contentPanel.add(salesTrackingPanel, "sales");
        contentPanel.add(activityLogsPanel, "logs");

        contentPanel.add(categoryPanel, "categories");
        contentPanel.add(employeePanel, "employees");
        contentPanel.add(monitoringPanel, "monitoring");
        contentPanel.add(employeeActivityPanel, "employee_activity");
        contentPanel.add(profilePanel, "profile");

        add(contentPanel, BorderLayout.CENTER);
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        try {
            if (isDarkMode) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                themeToggleBtn.setText("☀️ Light Mode");
                themeToggleBtn.setBackground(new Color(241, 196, 15));
                themeToggleBtn.setForeground(Color.BLACK);
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
                themeToggleBtn.setText("🌙 Dark Mode");
                themeToggleBtn.setBackground(new Color(52, 73, 94));
                themeToggleBtn.setForeground(Color.WHITE);
            }
            SwingUtilities.updateComponentTreeUI(this);

            // Re-render chart components to adapt to new backgrounds
            refreshAllPanels();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void backupDatabase() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Database Backup File Destination");
        chooser.setSelectedFile(new File("inventory_db_backup_" + (System.currentTimeMillis() / 1000) + ".sql"));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File dest = chooser.getSelectedFile();
            if (DatabaseBackupService.backupDatabase(dest)) {
                JOptionPane.showMessageDialog(this,
                        "Database backup created successfully!\nPath: " + dest.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(this, "Error: Failed to back up the database.");
            }
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            LoggerService.log(SessionManager.getUsername(), "Logged out");
            SessionManager.logout();
            dispose();
            new LoginFrame(); // open login screen
        }
    }

    public void showProducts() {
        activeCardName = "products";
        cardLayout.show(contentPanel, "products");
        productPanel.load();
        repaint();
    }

    public void showProductsTabWithLowStock() {
        activeCardName = "products";
        cardLayout.show(contentPanel, "products");
        productPanel.filterLowStock();
        repaint();
    }

    public void refreshProductPanel() {
        productPanel.load();
    }

    private void restoreDatabase() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Database Backup File to Restore");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File srcFile = chooser.getSelectedFile();
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Restoring the database will overwrite current tables and records.\nAre you sure you want to restore from:\n"
                            + srcFile.getName() + "?",
                    "Confirm Database Restore",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (DatabaseBackupService.restoreDatabase(srcFile)) {
                    JOptionPane.showMessageDialog(this, "Database restored successfully!");
                    refreshAllPanels();
                } else {
                    JOptionPane.showMessageDialog(this, "Error: Failed to restore the database.");
                }
            }
        }
    }

    public void refreshAllPanels() {
        if (SessionManager.isAdmin()) {
            dashboardPanel.loadStats();
            productPanel.load();
            billingPanel.loadProducts();
            salesTrackingPanel.loadStats();
            activityLogsPanel.loadLogs();
            categoryPanel.loadCategories();
            employeePanel.loadEmployees();
            monitoringPanel.refreshAll();
            profilePanel.updateLabels();
        } else {
            productPanel.load();
            billingPanel.loadProducts();
            employeeActivityPanel.refreshAll();
            profilePanel.updateLabels();
        }
    }
}