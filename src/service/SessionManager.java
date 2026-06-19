package service;

public class SessionManager {
    private static String currentUsername = null;
    private static String currentUserRole = null; // "admin" or "employee"

    public static void login(String username, String role) {
        currentUsername = username;
        currentUserRole = role;
    }

    public static void logout() {
        currentUsername = null;
        currentUserRole = null;
    }

    public static String getUsername() {
        return currentUsername;
    }

    public static String getRole() {
        return currentUserRole;
    }

    public static boolean isAdmin() {
        return "admin".equalsIgnoreCase(currentUserRole);
    }

    public static boolean isLoggedIn() {
        return currentUsername != null;
    }
}
