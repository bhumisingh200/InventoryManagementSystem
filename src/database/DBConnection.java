package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/inventory_db";

    private static final String USER = "root";

    private static final String PASSWORD = "Bhumi@2006";

    public static Connection getConnection() {

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn =DriverManager.getConnection(URL,USER,PASSWORD);

            System.out.println( "Database Connected Successfully!");

            return conn;

        }
        catch(Exception e) {

            System.out.println("Connection Failed!");

            e.printStackTrace();

            return null;
        }
    }
}