package database;
import java.sql.ResultSet;
import java.io.FileWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;

import model.Product;

public class ProductDAO {

    //AddProduct
    public void addProduct(Product product) {
        //to cancel dublicate product
        if(productExists(product.getId())){
            System.out.println( "Product ID Already Exists!");
            return;
        }

        //To check if the input is in possitive no. only
        if(!validateProduct(product)){
            return;
        }

        String sql =
        "INSERT INTO products(id,name,category,price,quantity) VALUES(?,?,?,?,?)";

        try {

            Connection conn =
                    DBConnection.getConnection();

            PreparedStatement ps =
                    conn.prepareStatement(sql);

            ps.setInt(1, product.getId());
            ps.setString(2, product.getName());
            ps.setString(3, product.getCategory());
            ps.setDouble(4, product.getPrice());
            ps.setInt(5, product.getQuantity());

            ps.executeUpdate();

            System.out.println(
                    "Product Added To Database!");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //ViewProduct
    public void viewProducts() {
        String sql = "SELECT * FROM products";
        try {
            Connection conn = DBConnection.getConnection();

            PreparedStatement ps =
                conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            System.out.println("\n----- PRODUCTS -----");

            boolean found = false;
            while(rs.next()) {
                found = true;

                double value =rs.getDouble("price")*rs.getInt("quantity");

                System.out.println(
                "ID: " + rs.getInt("id")
                + " | Name: " + rs.getString("name")
                + " | Category: " + rs.getString("category")
                + " | Price: " + rs.getDouble("price")
                + " | Quantity: " + rs.getInt("quantity")
                + " | value: ₹" + value);
            }
            if(!found){
                System.out.println("No Products Found!");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    //SearchProduct
    public void searchProduct(int id){
        String sql =
            "SELECT * FROM products WHERE id = ?";
        try{
            Connection conn =
                DBConnection.getConnection();
                PreparedStatement ps =conn.prepareStatement(sql);
                ps.setInt(1,id);
                ResultSet rs =ps.executeQuery();
                
                if(rs.next()){
                    System.out.println(
                "ID: " + rs.getInt("id")
                + " | Name: " + rs.getString("name")
                + " | Category: " + rs.getString("category")
                + " | Price: " + rs.getDouble("price")
                + " | Quantity: " + rs.getInt("quantity"));
            }else{
                System.out.println(
                    "Product Not Found!");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //UpdateProduct
    public void updateProduct(int id,String name,String category,double price,int quantity) {String sql =
        "UPDATE products SET name=?, category=?, price=?, quantity=? WHERE id=?";
        try {
            Connection conn =DBConnection.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql);
            
                ps.setString(1, name);
                ps.setString(2, category);
                ps.setDouble(3, price);
                ps.setInt(4, quantity);
                ps.setInt(5, id);

                int rows = ps.executeUpdate();

                if(rows > 0) {
                    System.out.println("Product Updated Successfully!");
                } else {
                    System.out.println("Product Not Found!");
                }
        } catch(Exception e) {
                e.printStackTrace();
        }
    }

    //DeleteProduct
    public void deleteProduct(int id) {
        String sql ="DELETE FROM products WHERE id=?";
        
        try {
            Connection conn =DBConnection.getConnection();

            PreparedStatement ps =conn.prepareStatement(sql);
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if(rows > 0) {
                System.out.println("Product Deleted Successfully!");
            }else{
                System.out.println("Product Not Found!");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    //InventoryDashboard
    public void inventoryDashboard() {
        String sql = "SELECT * FROM products";
        try {
            Connection conn =
                DBConnection.getConnection();
                
            PreparedStatement ps =
                conn.prepareStatement(sql);
                
            ResultSet rs =
                ps.executeQuery();
                
            int totalProducts = 0;
            int totalQuantity = 0;
            double totalValue = 0;
            
            while(rs.next()) {
                totalProducts++;
                int quantity =
                    rs.getInt("quantity");
                double price =
                    rs.getDouble("price");
                totalQuantity += quantity;
                    
                totalValue +=
                    (price * quantity);
            }
            
            System.out.println("\n===== INVENTORY DASHBOARD =====");
            
            System.out.println(
                "Total Products: "
                + totalProducts);
                
            System.out.println(
                "Total Quantity: "
                + totalQuantity);
                
            System.out.println(
                "Total Inventory Value: ₹"
                + totalValue);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    //LowStock
    public void lowStockProducts() {
        String sql =
            "SELECT * FROM products WHERE quantity < ?";
        try {
            Connection conn =
                DBConnection.getConnection();
                
            PreparedStatement ps =
                conn.prepareStatement(sql);
                
            ps.setInt(1, 5);
                
            ResultSet rs =
                ps.executeQuery();
                
            boolean found = false;
                
            System.out.println(
                "\n===== LOW STOCK PRODUCTS =====");
                
            while(rs.next()) {
                    
                found = true;
                    
                System.out.println(
                    "ID: " + rs.getInt("id")
                    + " | Name: " + rs.getString("name")
                    + " | Quantity: " + rs.getInt("quantity"));
            }
            if(!found) {
                System.out.println(
                    "No Low Stock Products Found!");
            }
        }catch(Exception e) {
                e.printStackTrace();
        }
    }

    //Search By Category
    public void searchByCategory(String category) {
        String sql =
            "SELECT * FROM products WHERE category = ?";
        try {
            Connection conn =
                DBConnection.getConnection();
                
            PreparedStatement ps =
                conn.prepareStatement(sql);
                
            ps.setString(1, category);
            
            ResultSet rs =
                ps.executeQuery();
                
            boolean found = false;
            
            System.out.println(
                "\n===== CATEGORY RESULTS =====");
                
            while(rs.next()) {
                found = true;
                
                System.out.println(
                    "ID: " + rs.getInt("id")
                    + " | Name: " + rs.getString("name")
                    + " | Category: " + rs.getString("category")
                    + " | Price: " + rs.getDouble("price")
                    + " | Quantity: " + rs.getInt("quantity"));
            }
            if(!found){
                System.out.println(
                    "No Products Found In This Category!");
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Duplicate Product ID Validation
    public boolean productExists(int id){
        String sql = "SELECT * FROM products WHERE id=?";
        
        try{
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql);
            ps.setInt(1,id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //Input Validation
    public boolean validateProduct(Product product){
        if(product.getPrice() <= 0){
            System.out.println(
                "Price must be greater than 0");
            return false;
        }
        if(product.getQuantity() < 0){
            System.out.println(
                "Quantity cannot be negative");
            return false;
        }
        return true;
    }

    //Export Report
    public void exportReport(){
        String sql =
            "SELECT * FROM products";
        try{
            Connection conn =
                DBConnection.getConnection();
                
            PreparedStatement ps =
                conn.prepareStatement(sql);
                
            ResultSet rs =
                ps.executeQuery();
                
            FileWriter writer =
                new FileWriter(
                        "inventory_report.txt");
                        
            writer.write(
                "===== INVENTORY REPORT =====\n\n");
                
            while(rs.next()){
                
                writer.write(
                "ID: " + rs.getInt("id")
                + "\nName: "
                + rs.getString("name")
                + "\nCategory: "
                + rs.getString("category")
                + "\nPrice: "
                + rs.getDouble("price")
                + "\nQuantity: "
                + rs.getInt("quantity")
                + "\n\n");
            }
            writer.close();
            
            System.out.println(
                "Report Exported Successfully!");
        }catch(Exception e){
                e.printStackTrace();
        }
    }

    //

}