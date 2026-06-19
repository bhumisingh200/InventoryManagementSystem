package ui;

import database.userDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public final class EmployeePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel model;
    private final transient userDAO dao = new userDAO();

    @SuppressWarnings("this-escape")
    public EmployeePanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Employee & User Management", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);
        JButton addBtn = new JButton("Add User 👤➕");
        JButton removeBtn = new JButton("Remove User ❌");
        JButton resetPassBtn = new JButton("Reset Password 🔑");
        JButton refreshBtn = new JButton("Refresh 🔄");

        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.setBackground(new Color(52, 152, 219));
        addBtn.setForeground(Color.WHITE);

        controls.add(addBtn);
        controls.add(removeBtn);
        controls.add(resetPassBtn);
        controls.add(refreshBtn);
        headerPanel.add(controls, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Table
        model = new DefaultTableModel(new String[]{"ID", "Username", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Listeners
        addBtn.addActionListener(e -> addUser());
        removeBtn.addActionListener(e -> removeUser());
        resetPassBtn.addActionListener(e -> resetPassword());
        refreshBtn.addActionListener(e -> loadEmployees());

        loadEmployees();
    }

    public void loadEmployees() {
        model.setRowCount(0);
        List<Object[]> employees = dao.getAllEmployees();
        for (Object[] emp : employees) {
            model.addRow(emp);
        }
    }

    private void addUser() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"employee", "admin"});

        Object[] fields = {
            "Username:", userField,
            "Password:", passField,
            "Role:", roleCombo
        };

        int result = JOptionPane.showConfirmDialog(
            this, fields, "Add User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword()).trim();
            String r = (String) roleCombo.getSelectedItem();

            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }

            if (dao.register(u, p, r)) {
                JOptionPane.showMessageDialog(this, "User added successfully!");
                loadEmployees();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user. Username might be taken.");
            }
        }
    }

    private void removeUser() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }

        String username = (String) model.getValueAt(selectedRow, 1);
        if ("admin".equals(username)) {
            JOptionPane.showMessageDialog(this, "Cannot remove primary admin user.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, "Are you sure you want to remove user: " + username + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        if (dao.removeEmployee(username)) {
            JOptionPane.showMessageDialog(this, "User removed successfully!");
            loadEmployees();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to remove user.");
        }
    }

    private void resetPassword() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }

        String username = (String) model.getValueAt(selectedRow, 1);
        String newPass = JOptionPane.showInputDialog(this, "Enter New Password for " + username + ":");
        if (newPass == null || newPass.trim().isEmpty()) return;
        newPass = newPass.trim();

        if (dao.resetPassword(username, newPass)) {
            JOptionPane.showMessageDialog(this, "Password reset successfully!");
            loadEmployees();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to reset password.");
        }
    }
}
