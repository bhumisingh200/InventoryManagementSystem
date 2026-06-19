package ui;

import database.ProductDAO;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Product;
import service.SessionManager;

public final class BillingPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JComboBox<String> productCombo;
    private JTextField qtyField;
    private JTextField billerField;
    private JComboBox<String> regionCombo;
    private JLabel stockLabel;
    private JLabel priceLabel;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;

    private final transient ProductDAO dao = new ProductDAO();
    private transient List<Product> availableProducts;
    private transient Map<Integer, Product> productMap = new HashMap<>();
    
    // Tracks item in cart: Key = Product ID, Value = {Product, Quantity}
    private transient List<Object[]> cartItems = new ArrayList<>(); 
    private double grandTotalVal = 0.0;

    private transient MainFrame mainFrame;

    @SuppressWarnings("this-escape")
    public BillingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title
        JLabel titleLabel = new JLabel("Point of Sale & Billing", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        // Main split pane: Left is cart and checkout, Right is product adding
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // 1. LEFT PANEL: Cart Table and Action buttons
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));
        
        cartModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Category", "Unit Price", "Qty", "Total"}, 0);
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(24);
        leftPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        // Bottom area of left panel: Total and checkout button
        JPanel checkoutPanel = new JPanel(new BorderLayout(10, 10));
        totalLabel = new JLabel("Grand Total: Rs. 0.00", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        checkoutPanel.add(totalLabel, BorderLayout.NORTH);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton removeBtn = new JButton("Remove Selected");
        JButton clearBtn = new JButton("Clear Cart");
        JButton checkoutBtn = new JButton("Checkout & Generate PDF Bill 🧾");
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        checkoutBtn.setBackground(new Color(46, 204, 113));
        checkoutBtn.setForeground(Color.WHITE);

        actionBtns.add(removeBtn);
        actionBtns.add(clearBtn);
        actionBtns.add(checkoutBtn);
        checkoutPanel.add(actionBtns, BorderLayout.SOUTH);
        leftPanel.add(checkoutPanel, BorderLayout.SOUTH);

        // 2. RIGHT PANEL: Adding Product form
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Product Entry"));
        rightPanel.setPreferredSize(new Dimension(300, 0));

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("Select Product:"));
        productCombo = new JComboBox<>();
        form.add(productCombo);

        form.add(new JLabel("Available Stock:"));
        stockLabel = new JLabel("0");
        stockLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        form.add(stockLabel);

        form.add(new JLabel("Unit Price:"));
        priceLabel = new JLabel("Rs. 0.00");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        form.add(priceLabel);

        form.add(new JLabel("Quantity:"));
        qtyField = new JTextField("1");
        form.add(qtyField);

        form.add(new JLabel("Biller Name:"));
        billerField = new JTextField();
        billerField.setText(SessionManager.getUsername() != null ? SessionManager.getUsername() : "Employee");
        form.add(billerField);

        form.add(new JLabel("Sales Region:"));
        regionCombo = new JComboBox<>(new String[]{
            "North (Delhi)", "West (Mumbai)", "South (Bangalore)", "East (Kolkata)", "Central (Hyderabad)"
        });
        regionCombo.setSelectedItem("Central (Hyderabad)");
        form.add(regionCombo);

        JButton addToCartBtn = new JButton("Add To Cart 🛒");
        addToCartBtn.setBackground(new Color(52, 152, 219));
        addToCartBtn.setForeground(Color.WHITE);

        rightPanel.add(form);
        rightPanel.add(Box.createVerticalStrut(10));
        
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrapper.add(addToCartBtn);
        rightPanel.add(btnWrapper);
        rightPanel.add(Box.createVerticalGlue());

        // Assemble grid
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        mainContent.add(leftPanel, gbc);

        gbc.weightx = 0.3;
        gbc.gridx = 1;
        mainContent.add(rightPanel, gbc);

        add(mainContent, BorderLayout.CENTER);

        // Setup Listeners
        loadProducts();

        productCombo.addActionListener(e -> updateProductDetails());
        addToCartBtn.addActionListener(e -> addToCart());
        removeBtn.addActionListener(e -> removeSelected());
        clearBtn.addActionListener(e -> clearCart());
        checkoutBtn.addActionListener(e -> checkout());
    }

    public void loadProducts() {
        productCombo.removeAllItems();
        productMap.clear();
        availableProducts = dao.getAllProducts();
        
        for (Product p : availableProducts) {
            productCombo.addItem(p.getName() + " (ID: " + p.getId() + ")");
            productMap.put(p.getId(), p);
        }
        updateProductDetails();
    }

    private void updateProductDetails() {
        int selectedIndex = productCombo.getSelectedIndex();
        if (selectedIndex == -1) {
            stockLabel.setText("0");
            priceLabel.setText("Rs. 0.00");
            return;
        }

        Product p = getSelectedProduct();
        if (p != null) {
            // Subtract quantity already in cart
            int inCartQty = getCartQty(p.getId());
            stockLabel.setText(String.valueOf(p.getQuantity() - inCartQty));
            priceLabel.setText(String.format("Rs. %.2f", p.getPrice()));
        }
    }

    private Product getSelectedProduct() {
        int index = productCombo.getSelectedIndex();
        if (index == -1) return null;
        
        String item = (String) productCombo.getSelectedItem();
        // Extract ID from name (ID: X)
        try {
            int startIdx = item.lastIndexOf("(ID: ") + 5;
            int endIdx = item.lastIndexOf(")");
            int id = Integer.parseInt(item.substring(startIdx, endIdx));
            return productMap.get(id);
        } catch (Exception e) {
            return null;
        }
    }

    private int getCartQty(int productId) {
        for (Object[] item : cartItems) {
            Product p = (Product) item[0];
            if (p.getId() == productId) {
                return (int) item[1];
            }
        }
        return 0;
    }

    private void addToCart() {
        Product p = getSelectedProduct();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        int qtyToAdd;
        try {
            qtyToAdd = Integer.parseInt(qtyField.getText().trim());
            if (qtyToAdd <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive quantity.");
            return;
        }

        int inCartQty = getCartQty(p.getId());
        int availableStock = p.getQuantity() - inCartQty;

        if (qtyToAdd > availableStock) {
            JOptionPane.showMessageDialog(this, "Insufficient stock! Only " + availableStock + " units left.");
            return;
        }

        // Add or update cart
        boolean exists = false;
        for (Object[] item : cartItems) {
            Product cartProduct = (Product) item[0];
            if (cartProduct.getId() == p.getId()) {
                int oldQty = (int) item[1];
                item[1] = oldQty + qtyToAdd;
                item[2] = (oldQty + qtyToAdd) * p.getPrice();
                exists = true;
                break;
            }
        }

        if (!exists) {
            cartItems.add(new Object[]{p, qtyToAdd, qtyToAdd * p.getPrice()});
        }

        updateCartTable();
        updateProductDetails();
    }

    private void removeSelected() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row in the cart to remove.");
            return;
        }

        int productId = (int) cartTable.getValueAt(selectedRow, 0);
        cartItems.removeIf(item -> ((Product) item[0]).getId() == productId);

        updateCartTable();
        updateProductDetails();
    }

    private void clearCart() {
        cartItems.clear();
        updateCartTable();
        updateProductDetails();
    }

    private void updateCartTable() {
        cartModel.setRowCount(0);
        grandTotalVal = 0.0;

        for (Object[] item : cartItems) {
            Product p = (Product) item[0];
            int qty = (int) item[1];
            double total = (double) item[2];
            grandTotalVal += total;

            cartModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getPrice(),
                qty,
                total
            });
        }
        totalLabel.setText(String.format("Grand Total: Rs. %.2f", grandTotalVal));
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty!");
            return;
        }

        String biller = billerField.getText().trim();
        if (biller.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter biller's name.");
            return;
        }

        // Confirm Checkout
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Confirm sale of " + cartItems.size() + " items for Rs. " + String.format("%.2f", grandTotalVal) + "?",
            "Confirm Checkout",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        // Perform sale in database
        boolean success = true;
        String selectedRegion = (String) regionCombo.getSelectedItem();
        for (Object[] item : cartItems) {
            Product p = (Product) item[0];
            int qty = (int) item[1];
            double total = (double) item[2];
            
            boolean saleLogged = dao.recordSale(p.getId(), p.getName(), p.getCategory(), qty, total, biller, selectedRegion);
            if (!saleLogged) {
                success = false;
                JOptionPane.showMessageDialog(this, "Failed to record sale for " + p.getName() + ". Insufficient stock.");
                break;
            }
        }

        if (success) {
            // Generate PDF Bill
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF Invoice");
            fileChooser.setSelectedFile(new File("Invoice_" + System.currentTimeMillis() + ".pdf"));
            
            int selectResult = fileChooser.showSaveDialog(this);
            if (selectResult == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                
                // Ensure extension is .pdf
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getAbsolutePath() + ".pdf");
                }

                String invoiceNo = "INV-" + (System.currentTimeMillis() / 1000);
                String dateStr = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
                
                double tax = grandTotalVal * 0.18; // 18% GST standard
                double subtotal = grandTotalVal - tax;

                boolean pdfSuccess = PDFWriter.generateInvoice(
                    file, invoiceNo, dateStr, biller, cartItems, subtotal, tax, grandTotalVal
                );

                if (pdfSuccess) {
                    JOptionPane.showMessageDialog(this, "Sale complete! PDF invoice saved to:\n" + file.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(this, "Sale complete, but failed to write PDF invoice file.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sale completed successfully. Invoice PDF not saved.");
            }

            // Reset cart
            clearCart();
            loadProducts();
            
            // Refresh main products panel if it exists
            mainFrame.refreshProductPanel();
        }
    }
}
