package service;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class LoggerService {

    public static void log(String username, String action) {
        if (username == null || username.isEmpty()) {
            username = "System";
        }
        
        String sql = "INSERT INTO activity_logs (username, action) VALUES (?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, action);
            ps.executeUpdate();
            
            System.out.println("[AUDIT LOG] User: " + username + " | Action: " + action);
            
        } catch (Exception e) {
            System.err.println("Failed to write audit log!");
            e.printStackTrace();
        }
    }
}
