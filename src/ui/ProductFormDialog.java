package ui;

import database.ProductDAO;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.*;
import model.Product;

public final class ProductFormDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTextField idField;
    private JTextField nameField;
    private JComboBox<String> categoryCombo;
    private JTextField priceField;
    private JTextField qtyField;
    private JLabel imageLabel;
    private String selectedImagePath = "";

    @SuppressWarnings("this-escape")
    public ProductFormDialog(ProductPanel panel) {
        setTitle("Add New Product");
        setModal(true);
        setSize(400, 350);
        setLocationRelativeTo(panel);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        formPanel.add(new JLabel("Product ID:"));
        idField = new JTextField();
        formPanel.add(idField);

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>();
        for (String cat : new ProductDAO().getCategories()) {
            categoryCombo.addItem(cat);
        }
        formPanel.add(categoryCombo);

        formPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        formPanel.add(priceField);

        formPanel.add(new JLabel("Quantity:"));
        qtyField = new JTextField();
        formPanel.add(qtyField);

        formPanel.add(new JLabel("Image:"));
        JPanel imgSelectPanel = new JPanel(new BorderLayout(5, 5));
        imageLabel = new JLabel("No image selected");
        imageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        JButton uploadBtn = new JButton("Browse... 🖼");
        imgSelectPanel.add(imageLabel, BorderLayout.CENTER);
        imgSelectPanel.add(uploadBtn, BorderLayout.EAST);
        formPanel.add(imgSelectPanel);

        add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton saveBtn = new JButton("Save Product");
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.WHITE);
        JButton cancelBtn = new JButton("Cancel");

        actionPanel.add(cancelBtn);
        actionPanel.add(saveBtn);
        add(actionPanel, BorderLayout.SOUTH);

        // Upload Button Action
        uploadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Product Image");
            // Accept images only
            chooser.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                selectedImagePath = copyImageToUploads(selectedFile);
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    imageLabel.setText(selectedFile.getName());
                    imageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                } else {
                    imageLabel.setText("Failed to copy image");
                    selectedImagePath = "";
                }
            }
        });

        // Save Button Action
        saveBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                String category = (String) categoryCombo.getSelectedItem();
                double price = Double.parseDouble(priceField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name cannot be empty.");
                    return;
                }

                Product p = new Product(id, name, category, price, qty, selectedImagePath);

                ProductDAO dao = new ProductDAO();
                if (dao.addProduct(p)) {
                    JOptionPane.showMessageDialog(this, "Product added successfully!");
                    panel.load();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Product ID already exists or invalid data.");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numerical values for ID, Price, and Quantity.");
            }
        });

        cancelBtn.addActionListener(e -> dispose());
        setVisible(true);
    }

    private String copyImageToUploads(File source) {
        try {
            File destDir = new File("uploads");
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String extension = "";
            String name = source.getName();
            int i = name.lastIndexOf('.');
            if (i > 0) {
                extension = name.substring(i);
            }

            // Unique file name to prevent collision
            String destFileName = "img_" + System.currentTimeMillis() + extension;
            File dest = new File(destDir, destFileName);

            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return "uploads/" + destFileName; // return relative path
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}