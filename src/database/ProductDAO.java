package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Product;
import service.LoggerService;
import service.SessionManager;

public class ProductDAO {

    // AddProduct
    public boolean addProduct(Product product) {
        if (productExists(product.getId())) {
            System.out.println("Product ID Already Exists!");
            return false;
        }

        if (!validateProduct(product)) {
            return false;
        }

        String sql = "INSERT INTO products(id, name, category, price, quantity, image_path) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, product.getId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getCategory());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getQuantity());
            ps.setString(6, product.getImagePath());

            ps.executeUpdate();
            
            LoggerService.log(SessionManager.getUsername(), "Added product: " + product.getName() + " (ID: " + product.getId() + ")");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ViewProducts (Console representation, keeping existing contract)
    public void viewProducts() {
        String sql = "SELECT * FROM products";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n----- PRODUCTS -----");
            boolean found = false;
            while (rs.next()) {
                found = true;
                double value = rs.getDouble("price") * rs.getInt("quantity");
                System.out.println(
                    "ID: " + rs.getInt("id")
                    + " | Name: " + rs.getString("name")
                    + " | Category: " + rs.getString("category")
                    + " | Price: " + rs.getDouble("price")
                    + " | Quantity: " + rs.getInt("quantity")
                    + " | value: ₹" + value
                );
            }
            if (!found) {
                System.out.println("No Products Found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // SearchProduct (Console representation)
    public void searchProduct(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println(
                        "ID: " + rs.getInt("id")
                        + " | Name: " + rs.getString("name")
                        + " | Category: " + rs.getString("category")
                        + " | Price: " + rs.getDouble("price")
                        + " | Quantity: " + rs.getInt("quantity")
                    );
                } else {
                    System.out.println("Product Not Found!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UpdateProduct with image
    public boolean updateProduct(int id, String name, String category, double price, int quantity, String imagePath) {
        String sql = "UPDATE products SET name=?, category=?, price=?, quantity=?, image_path=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, category);
            ps.setDouble(3, price);
            ps.setInt(4, quantity);
            ps.setString(5, imagePath);
            ps.setInt(6, id);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                LoggerService.log(SessionManager.getUsername(), "Updated product: " + name + " (ID: " + id + ")");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Keep original updateProduct signature (for compatibility)
    public void updateProduct(int id, String name, String category, double price, int quantity) {
        updateProduct(id, name, category, price, quantity, getProductImagePath(id));
    }

    private String getProductImagePath(int id) {
        String sql = "SELECT image_path FROM products WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("image_path");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // DeleteProduct
    public boolean deleteProduct(int id) {
        String name = "";
        String sqlSelect = "SELECT name FROM products WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psSelect = conn.prepareStatement(sqlSelect)) {
            psSelect.setInt(1, id);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sqlDelete = "DELETE FROM products WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlDelete)) {
            
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                LoggerService.log(SessionManager.getUsername(), "Deleted product: " + name + " (ID: " + id + ")");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Duplicate Product ID Validation
    public boolean productExists(int id) {
        String sql = "SELECT 1 FROM products WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Input Validation
    public boolean validateProduct(Product product) {
        if (product.getPrice() <= 0) {
            System.out.println("Price must be greater than 0");
            return false;
        }
        if (product.getQuantity() < 0) {
            System.out.println("Quantity cannot be negative");
            return false;
        }
        return true;
    }

    // Get All Products
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(
                    new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getString("image_path")
                    )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    // Legacy method signatures (kept for compatibility)
    public void inventoryDashboard() {}
    public void lowStockProducts() {}
    public void searchByCategory(String category) {}
    public void exportReport() {}

    // --- NEW STATS AND sales METHODS ---

    public int getTotalProductsCount() {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalCategoriesCount() {
        String sql = "SELECT COUNT(DISTINCT category) FROM products";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM products WHERE quantity < 10";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalInventoryValue() {
        String sql = "SELECT SUM(price * quantity) FROM products";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Product getHighestPricedProduct() {
        String sql = "SELECT * FROM products ORDER BY price DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getString("image_path")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Product getLowestStockProduct() {
        String sql = "SELECT * FROM products ORDER BY quantity ASC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getString("image_path")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Integer> getCategoryStockMap() {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT category, SUM(quantity) FROM products GROUP BY category";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // --- sales RECORDING & TRACKING ---

    public boolean recordSale(int productId, String productName, String category, int quantitySold, double totalPrice, String soldBy) {
        return recordSale(productId, productName, category, quantitySold, totalPrice, soldBy, "Central");
    }

    public boolean recordSale(int productId, String productName, String category, int quantitySold, double totalPrice, String soldBy, String region) {
        // First deduct stock
        String updateSql = "UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
            
            updatePs.setInt(1, quantitySold);
            updatePs.setInt(2, productId);
            updatePs.setInt(3, quantitySold);
            
            int rowsAffected = updatePs.executeUpdate();
            if (rowsAffected == 0) {
                return false; // Insufficient stock
            }
            
            // Insert sale record
            String insertSql = "INSERT INTO sales (product_id, product_name, category, quantity_sold, total_price, sold_by, region) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setInt(1, productId);
                insertPs.setString(2, productName);
                insertPs.setString(3, category);
                insertPs.setInt(4, quantitySold);
                insertPs.setDouble(5, totalPrice);
                insertPs.setString(6, soldBy);
                insertPs.setString(7, region);
                insertPs.executeUpdate();
            }

            LoggerService.log(soldBy, "Sold " + quantitySold + " units of " + productName + " in " + region + " (Revenue: ₹" + totalPrice + ")");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(total_price) FROM sales";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Object[] getMostSoldProduct() {
        String sql = "SELECT product_name, SUM(quantity_sold) as total_sold FROM sales GROUP BY product_name ORDER BY total_sold DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new Object[]{ rs.getString("product_name"), rs.getInt("total_sold") };
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Object[]{ "None", 0 };
    }

    public Map<String, Integer> getProductSalesMap() {
        Map<String, Integer> map = new HashMap<>();
        String sql = "SELECT product_name, SUM(quantity_sold) FROM sales GROUP BY product_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<Object[]> getSalesHistory() {
        List<Object[]> sales = new ArrayList<>();
        String sql = "SELECT product_id, product_name, category, quantity_sold, total_price, sold_by, sold_at FROM sales ORDER BY sold_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                sales.add(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getInt("quantity_sold"),
                    rs.getDouble("total_price"),
                    rs.getString("sold_by"),
                    rs.getTimestamp("sold_at")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sales;
    }

    public List<Object[]> getActivityLogs() {
        List<Object[]> logs = new ArrayList<>();
        String sql = "SELECT username, action, timestamp FROM activity_logs ORDER BY timestamp DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new Object[]{
                    rs.getString("username"),
                    rs.getString("action"),
                    rs.getTimestamp("timestamp")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    public Map<String, Double> getRegionalSalesMap() {
        Map<String, Double> map = new HashMap<>();
        String sql = "SELECT region, SUM(total_price) FROM sales GROUP BY region";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getDouble(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public List<Double> getRecentSalesValues() {
        List<Double> values = new ArrayList<>();
        String sql = "SELECT total_price FROM sales ORDER BY sold_at DESC LIMIT 7";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                values.add(0, rs.getDouble(1)); // prepend to preserve chronological order
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    public List<String> getRecentSalesLabels() {
        List<String> labels = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(sold_at, '%d-%b %H:%i') FROM sales ORDER BY sold_at DESC LIMIT 7";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                labels.add(0, rs.getString(1)); // prepend to preserve chronological order
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return labels;
    }

    // --- CATEGORY CRUD ---
    public List<String> getCategories() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM categories ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addCategory(String name) {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
            LoggerService.log(SessionManager.getUsername(), "Added category: " + name);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCategory(String oldName, String newName) {
        String sqlCat = "UPDATE categories SET name = ? WHERE name = ?";
        String sqlProd = "UPDATE products SET category = ? WHERE category = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psCat = conn.prepareStatement(sqlCat);
                 PreparedStatement psProd = conn.prepareStatement(sqlProd)) {
                psCat.setString(1, newName);
                psCat.setString(2, oldName);
                psCat.executeUpdate();

                psProd.setString(1, newName);
                psProd.setString(2, oldName);
                psProd.executeUpdate();

                conn.commit();
                LoggerService.log(SessionManager.getUsername(), "Updated category from " + oldName + " to " + newName);
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCategory(String name) {
        String sql = "DELETE FROM categories WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                LoggerService.log(SessionManager.getUsername(), "Deleted category: " + name);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- RESTOCK REQUESTS ---
    public boolean addRestockRequest(int productId, String productName, int quantity, String requestedBy) {
        String sql = "INSERT INTO restock_requests (product_id, product_name, requested_quantity, requested_by) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setString(2, productName);
            ps.setInt(3, quantity);
            ps.setString(4, requestedBy);
            ps.executeUpdate();
            LoggerService.log(requestedBy, "Requested restock: " + quantity + " units of " + productName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> getAllRestockRequests() {
        List<Object[]> requests = new ArrayList<>();
        String sql = "SELECT id, product_id, product_name, requested_quantity, status, requested_by, requested_at FROM restock_requests ORDER BY requested_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requests.add(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getInt("requested_quantity"),
                    rs.getString("status"),
                    rs.getString("requested_by"),
                    rs.getTimestamp("requested_at")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requests;
    }

    public boolean approveRestockRequest(int requestId) {
        String sqlSelect = "SELECT product_id, product_name, requested_quantity, requested_by FROM restock_requests WHERE id = ?";
        String sqlUpdateProd = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
        String sqlUpdateRequest = "UPDATE restock_requests SET status = 'APPROVED' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psSelect = conn.prepareStatement(sqlSelect)) {
                psSelect.setInt(1, requestId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        int productId = rs.getInt("product_id");
                        String prodName = rs.getString("product_name");
                        int qty = rs.getInt("requested_quantity");
                        String reqBy = rs.getString("requested_by");

                        // Update stock
                        try (PreparedStatement psUpProd = conn.prepareStatement(sqlUpdateProd)) {
                            psUpProd.setInt(1, qty);
                            psUpProd.setInt(2, productId);
                            psUpProd.executeUpdate();
                        }

                        // Update status
                        try (PreparedStatement psUpReq = conn.prepareStatement(sqlUpdateRequest)) {
                            psUpReq.setInt(1, requestId);
                            psUpReq.executeUpdate();
                        }

                        conn.commit();
                        LoggerService.log(SessionManager.getUsername(), "Approved restock of " + qty + " units for " + prodName + " (requested by " + reqBy + ")");
                        return true;
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean rejectRestockRequest(int requestId) {
        String sqlSelect = "SELECT product_name, requested_quantity, requested_by FROM restock_requests WHERE id = ?";
        String sqlUpdateRequest = "UPDATE restock_requests SET status = 'REJECTED' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psSelect = conn.prepareStatement(sqlSelect)) {
                psSelect.setInt(1, requestId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        String prodName = rs.getString("product_name");
                        int qty = rs.getInt("requested_quantity");
                        String reqBy = rs.getString("requested_by");

                        // Update status
                        try (PreparedStatement psUpReq = conn.prepareStatement(sqlUpdateRequest)) {
                            psUpReq.setInt(1, requestId);
                            psUpReq.executeUpdate();
                        }

                        conn.commit();
                        LoggerService.log(SessionManager.getUsername(), "Rejected restock of " + qty + " units for " + prodName + " (requested by " + reqBy + ")");
                        return true;
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- MANUALLY RESTOCK PRODUCT ---
    public boolean restockProduct(int productId, int quantity) {
        String sqlSelect = "SELECT name FROM products WHERE id = ?";
        String sqlUpdate = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psSelect = conn.prepareStatement(sqlSelect);
             PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
            
            psSelect.setInt(1, productId);
            String name = "";
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("name");
                } else {
                    return false; // Product doesn't exist
                }
            }

            psUpdate.setInt(1, quantity);
            psUpdate.setInt(2, productId);
            int rows = psUpdate.executeUpdate();
            if (rows > 0) {
                LoggerService.log(SessionManager.getUsername(), "Restocked " + quantity + " units of " + name + " (ID: " + productId + ")");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- ADDITIONAL STATS ---
    public int getOutOfStockCount() {
        String sql = "SELECT COUNT(*) FROM products WHERE quantity = 0";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}