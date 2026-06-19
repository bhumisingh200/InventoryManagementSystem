package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import service.SessionManager;
import service.LoggerService;

public class userDAO {

    // LOGIN CHECK
    public boolean login(String username, String password) {
        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT role FROM users WHERE username=? AND password=?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                SessionManager.login(username, role);
                LoggerService.log(username, "Logged in successfully");
                return true;
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // REGISTER USER (Default to employee)
    public boolean register(String username, String password) {
        return register(username, password, "employee");
    }

    // REGISTER USER WITH ROLE
    public boolean register(String username, String password, String role) {
        try {
            Connection conn = DBConnection.getConnection();

            String sql = "INSERT INTO users(username,password,role) VALUES(?,?,?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            ps.executeUpdate();
            
            LoggerService.log("System", "Registered new user: " + username + " (" + role + ")");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // GET ALL EMPLOYEES
    public java.util.List<Object[]> getAllEmployees() {
        java.util.List<Object[]> list = new java.util.ArrayList<>();
        String sql = "SELECT id, username, role FROM users ORDER BY username ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // REMOVE EMPLOYEE
    public boolean removeEmployee(String username) {
        if ("admin".equals(username)) return false; // Prevent removing primary admin
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                LoggerService.log(SessionManager.getUsername(), "Removed user: " + username);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // RESET PASSWORD
    public boolean resetPassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                LoggerService.log(SessionManager.getUsername(), "Reset password for user: " + username);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // CHANGE PASSWORD
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            ps.setString(3, oldPassword);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                LoggerService.log(username, "Updated their own password");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}