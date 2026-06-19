package ui;

import database.userDAO;
import java.awt.*;
import javax.swing.*;
import service.SessionManager;

public final class ProfilePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JLabel userValLabel;
    private JLabel roleValLabel;

    private JPasswordField oldPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;

    private final transient userDAO dao = new userDAO();

    @SuppressWarnings("this-escape")
    public ProfilePanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("My Account Profile", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center card container
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        // Card Panel (similar glassmorphic style)
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                boolean isDark = com.formdev.flatlaf.FlatLaf.isLafDark();
                if (isDark) {
                    g2.setColor(new Color(255, 255, 255, 10));
                } else {
                    g2.setColor(new Color(0, 0, 0, 10));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                g2.setColor(new Color(150, 150, 150, 40));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(450, 380));
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1.0;

        // User Avatar Emoji
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel avatarLabel = new JLabel("👤", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        card.add(avatarLabel, gbc);

        // Username
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.3;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(userLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        userValLabel = new JLabel(SessionManager.getUsername() != null ? SessionManager.getUsername() : "Guest");
        userValLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(userValLabel, gbc);

        // Role
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel roleLabel = new JLabel("Account Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(roleLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        roleValLabel = new JLabel(SessionManager.getRole() != null ? SessionManager.getRole().toUpperCase() : "VISITOR");
        roleValLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(roleValLabel, gbc);

        // Separator line
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        card.add(sep, gbc);

        // Change Password Form
        gbc.gridwidth = 1;
        
        // Old Password
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.4;
        JLabel oldPassLabel = new JLabel("Old Password:");
        oldPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        card.add(oldPassLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        oldPassField = new JPasswordField();
        card.add(oldPassField, gbc);

        // New Password
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.4;
        JLabel newPassLabel = new JLabel("New Password:");
        newPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        card.add(newPassLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        newPassField = new JPasswordField();
        card.add(newPassField, gbc);

        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.4;
        JLabel confirmPassLabel = new JLabel("Confirm New:");
        confirmPassLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        card.add(confirmPassLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        confirmPassField = new JPasswordField();
        card.add(confirmPassField, gbc);

        // Submit Button
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        JButton updateBtn = new JButton("Update Password 🔐");
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        updateBtn.setBackground(new Color(99, 102, 241));
        updateBtn.setForeground(Color.WHITE);
        card.add(updateBtn, gbc);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        updateBtn.addActionListener(e -> updatePassword());
    }

    public void updateLabels() {
        userValLabel.setText(SessionManager.getUsername() != null ? SessionManager.getUsername() : "Guest");
        roleValLabel.setText(SessionManager.getRole() != null ? SessionManager.getRole().toUpperCase() : "VISITOR");
    }

    private void updatePassword() {
        String username = SessionManager.getUsername();
        if (username == null) {
            JOptionPane.showMessageDialog(this, "No active user session.");
            return;
        }

        String oldPass = new String(oldPassField.getPassword()).trim();
        String newPass = new String(newPassField.getPassword()).trim();
        String confirmPass = new String(confirmPassField.getPassword()).trim();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All password fields must be filled.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.");
            return;
        }

        if (dao.changePassword(username, oldPass, newPass)) {
            JOptionPane.showMessageDialog(this, "Password updated successfully!");
            oldPassField.setText("");
            newPassField.setText("");
            confirmPassField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Incorrect old password. Please try again.");
        }
    }
}
