package ui;

import controller.ProductController;
import database.ProductDAO;
import java.awt.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import model.Product;
import service.LoggerService;
import service.SessionManager;

public final class ProductPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel model;
    private transient TableRowSorter<DefaultTableModel> sorter;

    // Search elements
    private JTextField searchField;
    private JComboBox<String> searchColumnCombo;
    private JComboBox<String> categoryFilterCombo;

    // Image/Detail Panel
    private JPanel detailPanel;
    private JLabel detailImageLabel;
    private JLabel detailNameLabel;
    private JLabel detailCategoryLabel;
    private JLabel detailPriceLabel;
    private JLabel detailQtyLabel;
    private JLabel detailStatusLabel;

    private JButton addBtn;
    private JButton updateBtn;
    private JButton deleteBtn;
    private JButton requestRestockBtn;

    private final transient ProductController controller = new ProductController();
    private final transient ProductDAO dao = new ProductDAO();

    @SuppressWarnings("this-escape")
    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. TOP PANEL: Search, Filter, and Controls
        JPanel controlPanel = new JPanel(new BorderLayout(10, 5));

        // Search & Filter Bar (North-West)
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchBar.add(searchField);

        searchColumnCombo = new JComboBox<>(
                new String[] { "All Columns", "Product ID", "Name", "Category", "Price", "Quantity" });
        searchBar.add(searchColumnCombo);

        searchBar.add(new JLabel("Category:"));
        categoryFilterCombo = new JComboBox<>();
        categoryFilterCombo.addItem("All Categories");
        searchBar.add(categoryFilterCombo);

        controlPanel.add(searchBar, BorderLayout.WEST);

        // CRUD & Export Buttons (North-East)
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));

        addBtn = new JButton("Add Product ➕");
        updateBtn = new JButton("Update ✏️");
        deleteBtn = new JButton("Delete ❌");
        requestRestockBtn = new JButton("Request Restock ⚠️");
        JButton refreshBtn = new JButton("Refresh 🔄");
        JButton exportCsvBtn = new JButton("Export CSV 📄");
        JButton exportExcelBtn = new JButton("Export Excel 📊");

        actionsBar.add(addBtn);
        actionsBar.add(updateBtn);
        actionsBar.add(deleteBtn);
        actionsBar.add(requestRestockBtn);
        actionsBar.add(refreshBtn);
        actionsBar.add(exportCsvBtn);
        actionsBar.add(exportExcelBtn);

        controlPanel.add(actionsBar, BorderLayout.EAST);
        add(controlPanel, BorderLayout.NORTH);

        // 2. CENTER PANEL: Split pane (JTable + Image Detail Panel)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(850);
        splitPane.setResizeWeight(0.85);

        // JTable Setup
        model = new DefaultTableModel(new String[] { "ID", "Name", "Category", "Price", "Quantity" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only cells
            }
        };

        // Custom JTable with Theme-Aware Low-Stock Highlighting
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                int modelRow = convertRowIndexToModel(row);
                int qty = (int) model.getValueAt(modelRow, 4);

                if (!isRowSelected(row)) {
                    if (qty < 10) {
                        boolean isDark = com.formdev.flatlaf.FlatLaf.isLafDark();
                        if (isDark) {
                            c.setBackground(new Color(110, 30, 30));
                            c.setForeground(new Color(255, 180, 180));
                        } else {
                            c.setBackground(new Color(255, 225, 225));
                            c.setForeground(new Color(180, 20, 20));
                        }
                    } else {
                        c.setBackground(getBackground());
                        c.setForeground(getForeground());
                    }
                }
                return c;
            }
        };

        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        splitPane.setLeftComponent(new JScrollPane(table));

        // Right Detail/Image Panel
        createDetailPanel();
        splitPane.setRightComponent(detailPanel);

        add(splitPane, BorderLayout.CENTER);

        // 3. EVENT LISTENERS
        addBtn.addActionListener(e -> new ProductFormDialog(this));
        updateBtn.addActionListener(e -> updateSelectedProduct());
        deleteBtn.addActionListener(e -> deleteSelectedProduct());
        requestRestockBtn.addActionListener(e -> requestRestockForSelected());
        refreshBtn.addActionListener(e -> load());
        exportCsvBtn.addActionListener(e -> exportToCSV());
        exportExcelBtn.addActionListener(e -> exportToExcel());

        // Search listeners
        searchField.addCaretListener(e -> applyFilters());
        searchColumnCombo.addActionListener(e -> applyFilters());
        categoryFilterCombo.addActionListener(e -> applyFilters());

        // Table selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailPanel();
            }
        });

        // Set Role Permissions
        applyRolePermissions();
        load();
    }

    private void createDetailPanel() {
        detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(BorderFactory.createTitledBorder("Product Preview"));
        detailPanel.setPreferredSize(new Dimension(250, 0));

        // Image label
        detailImageLabel = new JLabel();
        detailImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        detailImageLabel.setPreferredSize(new Dimension(200, 150));
        detailImageLabel.setMaximumSize(new Dimension(200, 150));
        detailImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Metadata Labels
        detailNameLabel = createBoldDetailLabel("Name: -");
        detailCategoryLabel = createDetailLabel("Category: -");
        detailPriceLabel = createDetailLabel("Price: -");
        detailQtyLabel = createDetailLabel("Quantity: -");
        detailStatusLabel = createBoldDetailLabel("Status: -");

        detailPanel.add(Box.createVerticalStrut(15));
        detailPanel.add(detailImageLabel);
        detailPanel.add(Box.createVerticalStrut(15));
        detailPanel.add(detailNameLabel);
        detailPanel.add(Box.createVerticalStrut(8));
        detailPanel.add(detailCategoryLabel);
        detailPanel.add(Box.createVerticalStrut(8));
        detailPanel.add(detailPriceLabel);
        detailPanel.add(Box.createVerticalStrut(8));
        detailPanel.add(detailQtyLabel);
        detailPanel.add(Box.createVerticalStrut(12));
        detailPanel.add(detailStatusLabel);
        detailPanel.add(Box.createVerticalGlue());
    }

    private JLabel createDetailLabel(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private JLabel createBoldDetailLabel(String text) {
        JLabel l = new JLabel(text);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return l;
    }

    private void applyRolePermissions() {
        boolean isAdmin = SessionManager.isAdmin();
        addBtn.setVisible(isAdmin);
        updateBtn.setVisible(isAdmin);
        deleteBtn.setVisible(isAdmin);
        requestRestockBtn.setVisible(!isAdmin);
    }

    public void load() {
        // Load category filter dynamically
        String currentFilter = (String) categoryFilterCombo.getSelectedItem();
        categoryFilterCombo.removeAllItems();
        categoryFilterCombo.addItem("All Categories");
        for (String cat : dao.getCategories()) {
            categoryFilterCombo.addItem(cat);
        }
        if (currentFilter != null) {
            categoryFilterCombo.setSelectedItem(currentFilter);
        }

        model.setRowCount(0);
        for (Product p : controller.getAllProducts()) {
            model.addRow(new Object[] {
                    p.getId(),
                    p.getName(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getQuantity()
            });
        }
        updateDetailPanel();
    }

    private void applyFilters() {
        String searchText = searchField.getText().trim();
        int searchColIdx = searchColumnCombo.getSelectedIndex();
        String catFilter = (String) categoryFilterCombo.getSelectedItem();

        List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();

        // 1. Text Search Filter
        if (!searchText.isEmpty()) {
            if (searchColIdx == 0) { // All columns
                filters.add(RowFilter.regexFilter("(?i)" + searchText));
            } else { // Specific Column (Subtract 1 to match table model index)
                filters.add(RowFilter.regexFilter("(?i)" + searchText, searchColIdx - 1));
            }
        }

        // 2. Category Dropdown Filter
        if (catFilter != null && !catFilter.equalsIgnoreCase("All Categories")) {
            filters.add(RowFilter.regexFilter("(?i)^" + catFilter + "$", 2));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void updateDetailPanel() {
        int row = table.getSelectedRow();
        if (row == -1) {
            detailImageLabel.setIcon(null);
            drawPlaceholderImage("None", "?");
            detailNameLabel.setText("No selection");
            detailCategoryLabel.setText("Category: -");
            detailPriceLabel.setText("Price: -");
            detailQtyLabel.setText("Quantity: -");
            detailStatusLabel.setText("Status: -");
            detailStatusLabel.setForeground(getForeground());
            return;
        }

        // Get actual indices from model in case of filtering
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);

        // Fetch complete product with image_path from database
        Product product = null;
        for (Product p : controller.getAllProducts()) {
            if (p.getId() == id) {
                product = p;
                break;
            }
        }

        if (product != null) {
            detailNameLabel.setText(product.getName());
            detailCategoryLabel.setText("Category: " + product.getCategory());
            detailPriceLabel.setText(String.format("Price: Rs. %.2f", product.getPrice()));
            detailQtyLabel.setText("Quantity: " + product.getQuantity() + " units");

            if (product.getQuantity() < 10) {
                detailStatusLabel.setText("⚠️ LOW STOCK ALERT");
                detailStatusLabel.setForeground(new Color(231, 76, 60)); // Red
            } else {
                detailStatusLabel.setText("✓ In Stock");
                detailStatusLabel.setForeground(new Color(46, 204, 113)); // Green
            }

            // Render Product Image
            String imgPath = product.getImagePath();
            if (imgPath != null && !imgPath.isEmpty()) {
                File imgFile = new File(imgPath);
                if (imgFile.exists()) {
                    try {
                        ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
                        // Scale image
                        Image img = icon.getImage().getScaledInstance(200, 150, Image.SCALE_SMOOTH);
                        detailImageLabel.setIcon(new ImageIcon(img));
                        detailImageLabel.revalidate();
                        detailImageLabel.repaint();
                        return;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

            // Draw placeholder image on the label dynamically
            drawPlaceholderImage(product.getName(), product.getCategory());
        }
    }

    private void drawPlaceholderImage(String name, String category) {
        int w = 200;
        int h = 150;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw elegant gradient background
        GradientPaint gp = new GradientPaint(0, 0, new Color(52, 73, 94), w, h, new Color(44, 62, 80));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        // Draw circular badge
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillOval(w / 2 - 35, h / 2 - 45, 70, 70);

        // Initials text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
        String initials = "";
        if (name.length() > 0)
            initials += Character.toUpperCase(name.charAt(0));
        if (category.length() > 0)
            initials += Character.toUpperCase(category.charAt(0));
        if (initials.isEmpty())
            initials = "??";

        g2.drawString(initials, w / 2 - (initials.length() * 8), h / 2 - 2);

        // Label underneath initials
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.setColor(new Color(255, 255, 255, 180));
        String labelText = "NO IMAGE UPLOADED";
        g2.drawString(labelText, w / 2 - 50, h / 2 + 25);

        g2.dispose();
        detailImageLabel.setIcon(new ImageIcon(img));
        detailImageLabel.revalidate();
        detailImageLabel.repaint();
    }

    private void deleteSelectedProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this product?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);

        if (dao.deleteProduct(id)) {
            JOptionPane.showMessageDialog(this, "Product deleted successfully!");
            load();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete product.");
        }
    }

    private void updateSelectedProduct() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);

        // Retrieve existing details
        Product product = null;
        for (Product p : controller.getAllProducts()) {
            if (p.getId() == id) {
                product = p;
                break;
            }
        }

        if (product == null)
            return;

        // Create fields
        JTextField nameField = new JTextField(product.getName());
        JComboBox<String> categoryCombo = new JComboBox<>();
        for (String cat : dao.getCategories()) {
            categoryCombo.addItem(cat);
        }
        categoryCombo.setSelectedItem(product.getCategory());
        JTextField priceField = new JTextField(String.valueOf(product.getPrice()));
        JTextField qtyField = new JTextField(String.valueOf(product.getQuantity()));

        JLabel fileLabel = new JLabel(
                product.getImagePath() != null ? new File(product.getImagePath()).getName() : "No image");
        JButton fileBtn = new JButton("Change... 🖼");

        final String[] updatedImgPath = { product.getImagePath() };

        fileBtn.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Product Image");
            chooser.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
            int res = chooser.showOpenDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                try {
                    File destDir = new File("uploads");
                    if (!destDir.exists())
                        destDir.mkdirs();
                    String extension = selected.getName().substring(selected.getName().lastIndexOf('.'));
                    String destName = "img_" + System.currentTimeMillis() + extension;
                    File dest = new File(destDir, destName);
                    Files.copy(selected.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    updatedImgPath[0] = "uploads/" + destName;
                    fileLabel.setText(selected.getName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JPanel fileWrapper = new JPanel(new BorderLayout(5, 5));
        fileWrapper.add(fileLabel, BorderLayout.CENTER);
        fileWrapper.add(fileBtn, BorderLayout.EAST);

        Object[] fields = {
                "ID (Not Editable)", new JLabel(String.valueOf(id)),
                "Name", nameField,
                "Category", categoryCombo,
                "Price", priceField,
                "Quantity", qtyField,
                "Image File", fileWrapper
        };

        int result = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Update Product",
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                String category = (String) categoryCombo.getSelectedItem();
                double price = Double.parseDouble(priceField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name cannot be empty.");
                    return;
                }

                if (dao.updateProduct(id, name, category, price, qty, updatedImgPath[0])) {
                    JOptionPane.showMessageDialog(this, "Product updated successfully!");
                    load();
                } else {
                    JOptionPane.showMessageDialog(this, "Error updating product.");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numerical values.");
            }
        }
    }

    private void exportToCSV() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export to CSV");
        chooser.setSelectedFile(new File("Inventory_Report.csv"));

        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Product ID,Name,Category,Price,Quantity\n");
            for (int i = 0; i < table.getRowCount(); i++) {
                int modelIdx = table.convertRowIndexToModel(i);
                writer.write(
                        model.getValueAt(modelIdx, 0) + ","
                                + escapeCSV(model.getValueAt(modelIdx, 1).toString()) + ","
                                + escapeCSV(model.getValueAt(modelIdx, 2).toString()) + ","
                                + model.getValueAt(modelIdx, 3) + ","
                                + model.getValueAt(modelIdx, 4) + "\n");
            }
            LoggerService.log(SessionManager.getUsername(), "Exported product table to CSV: " + file.getName());
            JOptionPane.showMessageDialog(this, "Inventory exported successfully to CSV!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void exportToExcel() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export to Excel (.xls)");
        chooser.setSelectedFile(new File("Inventory_Report.xls"));

        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".xls")) {
            file = new File(file.getAbsolutePath() + ".xls");
        }

        // Export as HTML table format - which MS Excel opens perfectly and supports
        // basic CSS styling!
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(
                    "<html><head><style>th{background-color:#4CAF50;color:white;font-weight:bold;} td,th{border:1px solid black;padding:5px;font-family:Arial;}</style></head><body>");
            writer.write("<h2>INVENTORY STATUS REPORT</h2>");
            writer.write("<table>");
            writer.write(
                    "<tr><th>Product ID</th><th>Product Name</th><th>Category</th><th>Unit Price</th><th>Quantity</th></tr>");

            for (int i = 0; i < table.getRowCount(); i++) {
                int modelIdx = table.convertRowIndexToModel(i);
                writer.write("<tr>");
                writer.write("<td>" + model.getValueAt(modelIdx, 0) + "</td>");
                writer.write("<td>" + model.getValueAt(modelIdx, 1) + "</td>");
                writer.write("<td>" + model.getValueAt(modelIdx, 2) + "</td>");
                writer.write("<td>Rs. " + model.getValueAt(modelIdx, 3) + "</td>");
                writer.write("<td>" + model.getValueAt(modelIdx, 4) + "</td>");
                writer.write("</tr>");
            }

            writer.write("</table></body></html>");

            LoggerService.log(SessionManager.getUsername(),
                    "Exported product table to Excel (.xls): " + file.getName());
            JOptionPane.showMessageDialog(this, "Inventory exported successfully as an Excel-compatible (.xls) file!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private String escapeCSV(String val) {
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    public void filterLowStock() {
        searchField.setText("");
        searchColumnCombo.setSelectedIndex(0); // All
        categoryFilterCombo.setSelectedIndex(0); // All
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Object>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                int qty = Integer.parseInt(entry.getStringValue(4));
                return qty < 10;
            }
        });
    }

    private void requestRestockForSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) model.getValueAt(modelRow, 0);
        String name = (String) model.getValueAt(modelRow, 1);

        String qtyStr = JOptionPane.showInputDialog(this, "Enter Restock Quantity Request for \"" + name + "\":", "50");
        if (qtyStr == null || qtyStr.trim().isEmpty())
            return;

        try {
            int qty = Integer.parseInt(qtyStr.trim());
            if (qty <= 0)
                throw new NumberFormatException();

            if (dao.addRestockRequest(id, name, qty, SessionManager.getUsername())) {
                JOptionPane.showMessageDialog(this, "Restock request submitted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit request.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive integer quantity.");
        }
    }
}
