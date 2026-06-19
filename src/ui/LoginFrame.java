package ui;

import database.userDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import com.formdev.flatlaf.FlatLightLaf;
import service.SessionManager;

public final class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextField userField;
    private JPasswordField passField;
    private final transient userDAO dao = new userDAO();

    @SuppressWarnings("this-escape")
    public LoginFrame() {
        // Set Light look and feel as default on start
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("ERP Inventory System - Login");
        setSize(480, 430);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Custom Gradient Background Panel
        JPanel bgPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Deep Indigo to Slate Navy Gradient
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(49, 46, 129), 
                    0, getHeight(), new Color(15, 23, 42)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(bgPanel);

        // Card Panel (Glassmorphism layout)
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Translucent panel background
                g2.setColor(new Color(255, 255, 255, 16));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
                
                // White inner glow border
                g2.setColor(new Color(255, 255, 255, 45));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 24, 24));
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(380, 330));
        cardPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);

        // 1. LOGO & HEADER
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        
        JLabel logoLabel = new JLabel("📦", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 42));
        cardPanel.add(logoLabel, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 20, 15, 20);
        JLabel titleLabel = new JLabel("INVENTORY ERP SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        cardPanel.add(titleLabel, gbc);

        // 2. INPUT FORM FIELDS
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 20, 6, 20);

        // Username row
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel userLabel = new JLabel("👤 Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(226, 232, 240));
        cardPanel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        userField = new JTextField();
        userField.setPreferredSize(new Dimension(180, 28));
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cardPanel.add(userField, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel passLabel = new JLabel("🔑 Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(new Color(226, 232, 240));
        cardPanel.add(passLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(180, 28));
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cardPanel.add(passField, gbc);

        // 3. ACTION BUTTONS
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 10, 20);
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        btnPanel.setOpaque(false);

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        registerBtn.setPreferredSize(new Dimension(0, 32));

        JButton loginBtn = new JButton("Login 🔑");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginBtn.setBackground(new Color(99, 102, 241)); // Sleek Indigo accent
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(0, 32));

        btnPanel.add(registerBtn);
        btnPanel.add(loginBtn);
        cardPanel.add(btnPanel, gbc);

        // Add Card to frame content
        bgPanel.add(cardPanel);

        // Action Listeners
        loginBtn.addActionListener(e -> login());
        registerBtn.addActionListener(e -> register());

        // Press Enter to Login
        getRootPane().setDefaultButton(loginBtn);

        setVisible(true);
    }

    private void login() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty.");
            return;
        }

        if (dao.login(u, p)) {
            JOptionPane.showMessageDialog(this, "Welcome back, " + u + " (" + SessionManager.getRole() + ")!");
            dispose();
            new MainFrame(); // open main ERP window
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password.");
        }
    }

    private void register() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty.");
            return;
        }

        // Dropdown to choose role
        String[] roles = {"employee", "admin"};
        String role = (String) JOptionPane.showInputDialog(
            this,
            "Select registration role for user " + u + ":",
            "User Role Selection",
            JOptionPane.PLAIN_MESSAGE,
            null,
            roles,
            roles[0]
        );

        if (role == null) return;

        if (dao.register(u, p, role)) {
            JOptionPane.showMessageDialog(this, "User registered successfully as " + role + "!");
        } else {
            JOptionPane.showMessageDialog(this, "Error registering user. Username might be taken.");
        }
    }
}