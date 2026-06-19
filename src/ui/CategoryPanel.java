package ui;

import database.ProductDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public final class CategoryPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel model;
    private final transient ProductDAO dao = new ProductDAO();

    @SuppressWarnings("this-escape")
    public CategoryPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("Category Management", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);
        JButton addBtn = new JButton("Add Category ➕");
        JButton editBtn = new JButton("Edit Category ✏️");
        JButton deleteBtn = new JButton("Delete Category ❌");
        JButton refreshBtn = new JButton("Refresh 🔄");

        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addBtn.setBackground(new Color(52, 152, 219));
        addBtn.setForeground(Color.WHITE);

        controls.add(addBtn);
        controls.add(editBtn);
        controls.add(deleteBtn);
        controls.add(refreshBtn);
        headerPanel.add(controls, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Table
        model = new DefaultTableModel(new String[] { "Serial No.", "Category Name" }, 0) {
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
        addBtn.addActionListener(e -> addCategory());
        editBtn.addActionListener(e -> editCategory());
        deleteBtn.addActionListener(e -> deleteCategory());
        refreshBtn.addActionListener(e -> loadCategories());

        loadCategories();
    }

    public void loadCategories() {
        model.setRowCount(0);
        List<String> categories = dao.getCategories();
        int serial = 1;
        for (String cat : categories) {
            model.addRow(new Object[] { serial++, cat });
        }
    }

    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "Enter New Category Name:", "Add Category",
                JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty())
            return;
        name = name.trim();

        if (dao.addCategory(name)) {
            JOptionPane.showMessageDialog(this, "Category added successfully!");
            loadCategories();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add category. It may already exist.");
        }
    }

    private void editCategory() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category first.");
            return;
        }

        String oldName = (String) model.getValueAt(selectedRow, 1);
        String newName = JOptionPane.showInputDialog(this, "Edit Category Name:", oldName);
        if (newName == null || newName.trim().isEmpty())
            return;
        newName = newName.trim();

        if (oldName.equalsIgnoreCase(newName))
            return;

        if (dao.updateCategory(oldName, newName)) {
            JOptionPane.showMessageDialog(this, "Category updated successfully!");
            loadCategories();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update category.");
        }
    }

    private void deleteCategory() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category first.");
            return;
        }

        String name = (String) model.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete category: \"" + name
                        + "\"?\nNote: Products in this category will remain, but category association in the filter might be affected.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        if (dao.deleteCategory(name)) {
            JOptionPane.showMessageDialog(this, "Category deleted successfully!");
            loadCategories();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete category.");
        }
    }
}
