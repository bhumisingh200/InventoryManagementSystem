import database.DBConnection;
import database.ProductDAO;
import database.userDAO;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.Product;
import service.DatabaseBackupService;

import service.SessionManager;
import ui.PDFWriter;

public class VerificationTests {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   RUNNING AUTOMATED VERIFICATION TESTS         ");
        System.out.println("==================================================");

        try {
            // 1. Connection & Schema Verification
            System.out.print("1. Verifying Database Connection and Schemas... ");
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                throw new Exception("Connection failed! Check DBConnection and MySQL service.");
            }
            System.out.println("PASSED");

            // Check tables exist
            System.out.print("2. Checking Tables Existence... ");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString(1).toLowerCase());
            }

            assertContains(tables, "users", "Users table");
            assertContains(tables, "products", "Products table");
            assertContains(tables, "sales", "Sales table");
            assertContains(tables, "activity_logs", "Activity logs table");
            System.out.println("PASSED");

            // 2. Authentication & RBAC Checks
            System.out.print("3. Testing Role-Based Login & Sessions... ");
            userDAO uDao = new userDAO();

            // Login as admin
            boolean adminLogin = uDao.login("admin", "admin123");
            if (!adminLogin || !SessionManager.isLoggedIn() || !SessionManager.isAdmin()) {
                throw new Exception("Admin login or session failed! Role: " + SessionManager.getRole());
            }

            // Login as employee
            boolean empLogin = uDao.login("employee", "emp123");
            if (!empLogin || !SessionManager.isLoggedIn() || SessionManager.isAdmin()) {
                throw new Exception("Employee login or session failed! Role: " + SessionManager.getRole());
            }
            System.out.println("PASSED");

            // Restore admin session for modification tasks
            uDao.login("admin", "admin123");

            // 3. Product CRUD Checks
            System.out.print("4. Testing Product CRUD Operations... ");
            ProductDAO pDao = new ProductDAO();

            // Delete product with ID 999 if it exists
            pDao.deleteProduct(999);

            Product testProd = new Product(999, "Verification Laptop", "Electronics", 1200.0, 15,
                    "uploads/test_laptop.png");
            boolean added = pDao.addProduct(testProd);
            if (!added) {
                throw new Exception("Failed to add product!");
            }

            // Read product
            List<Product> allProds = pDao.getAllProducts();
            Product found = null;
            for (Product p : allProds) {
                if (p.getId() == 999) {
                    found = p;
                    break;
                }
            }
            if (found == null) {
                throw new Exception("Product 999 not found in database!");
            }

            if (!"Verification Laptop".equals(found.getName()) || 1200.0 != found.getPrice()
                    || 15 != found.getQuantity()) {
                throw new Exception("Product attributes mismatch!");
            }

            // Update product
            boolean updated = pDao.updateProduct(999, "Verification Laptop Pro", "Electronics", 1350.0, 8,
                    "uploads/test_laptop_pro.png");
            if (!updated) {
                throw new Exception("Failed to update product!");
            }

            // Read again & verify updates + image
            allProds = pDao.getAllProducts();
            for (Product p : allProds) {
                if (p.getId() == 999) {
                    found = p;
                    break;
                }
            }
            if (found.getQuantity() != 8 || !"uploads/test_laptop_pro.png".equals(found.getImagePath())) {
                throw new Exception("Product update values or image path mismatch! Qty: " + found.getQuantity()
                        + ", Img: " + found.getImagePath());
            }
            System.out.println("PASSED");

            // 4. Low Stock Metrics Check
            System.out.print("5. Checking Low Stock Status Calculations... ");
            int lowStockCount = pDao.getLowStockCount();
            if (lowStockCount <= 0) {
                throw new Exception(
                        "Low stock count check failed. Laptop Pro has quantity 8, which is < 10, so count should be >= 1.");
            }
            System.out.println("PASSED (Low stock items count: " + lowStockCount + ")");

            // 5. Billing, Stock Deduction & Sales Recording Check
            System.out.print("6. Testing POS Billing & Stock Deduction... ");
            int qtyToSell = 3;
            double totalPrice = found.getPrice() * qtyToSell;

            // Record sale (reduces quantity from 8 to 5)
            boolean saleLogged = pDao.recordSale(found.getId(), found.getName(), found.getCategory(), qtyToSell,
                    totalPrice, "admin");
            if (!saleLogged) {
                throw new Exception("POS sale failed!");
            }

            // Check quantity decreased in database
            allProds = pDao.getAllProducts();
            Product soldProd = null;
            for (Product p : allProds) {
                if (p.getId() == 999) {
                    soldProd = p;
                    break;
                }
            }
            if (soldProd.getQuantity() != 5) {
                throw new Exception(
                        "Stock was not deducted correctly! Remaining: " + soldProd.getQuantity() + " (expected 5)");
            }

            // Verify sales revenue
            double totalRev = pDao.getTotalRevenue();
            if (totalRev < totalPrice) {
                throw new Exception("Sales revenue tracking calculation incorrect! Rev: Rs. " + totalRev);
            }
            System.out.println("PASSED");

            // 6. PDF Invoice Generation Check
            System.out.print("7. Testing PDF Invoice Writer... ");
            File pdfFile = new File("test_verification_invoice.pdf");
            if (pdfFile.exists()) {
                pdfFile.delete();
            }

            List<Object[]> cart = new ArrayList<>();
            cart.add(new Object[] { soldProd, qtyToSell, totalPrice });

            double tax = totalPrice * 0.18;
            double subtotal = totalPrice - tax;

            boolean pdfGenerated = PDFWriter.generateInvoice(
                    pdfFile, "INV-VERIFY", "19-06-2026 12:00:00", "admin", cart, subtotal, tax, totalPrice);

            if (!pdfGenerated || !pdfFile.exists() || pdfFile.length() == 0) {
                throw new Exception("PDF Invoice file was not created!");
            }
            System.out.println("PASSED (Generated PDF Size: " + pdfFile.length() + " bytes)");
            pdfFile.delete(); // cleanup

            // 7. Database Backup Check
            System.out.print("8. Testing SQL Database Backup Writer... ");
            File backupFile = new File("test_db_backup.sql");
            if (backupFile.exists()) {
                backupFile.delete();
            }

            boolean backupCreated = DatabaseBackupService.backupDatabase(backupFile);
            if (!backupCreated || !backupFile.exists() || backupFile.length() == 0) {
                throw new Exception("Database SQL backup script was not created!");
            }
            System.out.println("PASSED (Generated SQL Size: " + backupFile.length() + " bytes)");
            backupFile.delete(); // cleanup

            // 8. Custom Graphics Charts Mock Check
            System.out.print("9. Verifying Graphic Charts Mapping Metrics... ");
            Map<String, Integer> categoryStock = pDao.getCategoryStockMap();
            if (categoryStock == null || categoryStock.isEmpty()) {
                throw new Exception("Category stock mapping empty!");
            }
            System.out.println("PASSED (Categories mapped: " + categoryStock.size() + ")");

            // 9. Activity Logs Audit Check
            System.out.print("10. Testing Audit Trail Logs retrieval... ");
            List<Object[]> logs = pDao.getActivityLogs();
            if (logs == null || logs.isEmpty()) {
                throw new Exception("No audit logs found!");
            }
            System.out.println("PASSED (Total logged audit records: " + logs.size() + ")");

            // Cleanup test product
            pDao.deleteProduct(999);
            conn.close();

            System.out.println("==================================================");
            System.out.println("   ALL BACKEND VERIFICATION TESTS PASSED SUCCESSFULLY! ");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.out.println("\nTEST SUITE FAILED:");
            e.printStackTrace();
            System.out.println("==================================================");
        }
    }

    private static void assertContains(List<String> list, String value, String desc) throws Exception {
        if (!list.contains(value)) {
            throw new Exception("Assertion Failed: " + desc + " ('" + value + "') not found in database.");
        }
    }
}
