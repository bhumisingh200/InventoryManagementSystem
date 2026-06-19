package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBConnection {

    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "inventory_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Bhumi@2006";

    private static boolean initialized = false;

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // First run, ensure database exists
            if (!initialized) {
                initializeDatabase();
            }

            Connection conn = DriverManager.getConnection(BASE_URL + DB_NAME, USER, PASSWORD);
            return conn;
        } catch (Exception e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
            return null;
        }
    }

    private static synchronized void initializeDatabase() {
        if (initialized) return;
        
        try (Connection conn = DriverManager.getConnection(BASE_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Create Database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            
            // Reconnect to specific DB
            try (Connection dbConn = DriverManager.getConnection(BASE_URL + DB_NAME, USER, PASSWORD);
                 Statement dbStmt = dbConn.createStatement()) {
                
                // 1. Create Users Table
                dbStmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE, " +
                    "password VARCHAR(100), " +
                    "role VARCHAR(20) DEFAULT 'employee')"
                );

                // Check and Add 'role' column if not exists
                try {
                    dbStmt.executeQuery("SELECT role FROM users LIMIT 1").close();
                } catch (Exception e) {
                    dbStmt.executeUpdate("ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'employee'");
                }

                // Seed Default Users
                try (PreparedStatement ps = dbConn.prepareStatement(
                        "INSERT IGNORE INTO users (username, password, role) VALUES (?, ?, ?)")) {
                    // Admin
                    ps.setString(1, "admin");
                    ps.setString(2, "admin123");
                    ps.setString(3, "admin");
                    ps.executeUpdate();
                    
                    // Employee
                    ps.setString(1, "employee");
                    ps.setString(2, "emp123");
                    ps.setString(3, "employee");
                    ps.executeUpdate();
                }

                // Ensure default users have correct roles even if they already existed
                dbStmt.executeUpdate("UPDATE users SET role = 'admin' WHERE username = 'admin'");
                dbStmt.executeUpdate("UPDATE users SET role = 'employee' WHERE username = 'employee'");

                // 2. Create Products Table
                dbStmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS products (" +
                    "id INT PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "category VARCHAR(100), " +
                    "price DOUBLE, " +
                    "quantity INT, " +
                    "image_path VARCHAR(255))"
                );

                // Check and Add 'image_path' column if not exists
                try {
                    dbStmt.executeQuery("SELECT image_path FROM products LIMIT 1").close();
                } catch (Exception e) {
                    dbStmt.executeUpdate("ALTER TABLE products ADD COLUMN image_path VARCHAR(255)");
                }

                // 3. Create Sales Table
                dbStmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS sales (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "product_id INT, " +
                    "product_name VARCHAR(100), " +
                    "category VARCHAR(100), " +
                    "quantity_sold INT, " +
                    "total_price DOUBLE, " +
                    "sold_by VARCHAR(50), " +
                    "region VARCHAR(50) DEFAULT 'Central', " +
                    "sold_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                );

                // Check and Add 'region' column if not exists
                try {
                    dbStmt.executeQuery("SELECT region FROM sales LIMIT 1").close();
                } catch (Exception e) {
                    dbStmt.executeUpdate("ALTER TABLE sales ADD COLUMN region VARCHAR(50) DEFAULT 'Central'");
                }

                // 4. Create Activity Logs Table
                dbStmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS activity_logs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50), " +
                    "action VARCHAR(255), " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                );

                // 5. Create Categories Table
                dbStmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS categories (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50) UNIQUE)"
                );

                // Seed Default Categories if empty
                try (ResultSet rs = dbStmt.executeQuery("SELECT COUNT(*) FROM categories")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        dbStmt.executeUpdate("INSERT INTO categories (name) VALUES " +
                            "('Electronics'), ('Grocery'), ('Stationery'), ('Food'), ('Furniture')");
                    }
                }

                // 6. Create Restock Requests Table
                dbStmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS restock_requests (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "product_id INT, " +
                    "product_name VARCHAR(100), " +
                    "requested_quantity INT, " +
                    "status VARCHAR(20) DEFAULT 'PENDING', " +
                    "requested_by VARCHAR(50), " +
                    "requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
                );
            }
            
            initialized = true;
            System.out.println("Database Initialized/Migrated Successfully!");
        } catch (Exception e) {
            System.err.println("Database Initialization Failed!");
            e.printStackTrace();
        }
    }
}