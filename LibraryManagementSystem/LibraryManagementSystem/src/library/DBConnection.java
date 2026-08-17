package library;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/LibraryDB";
    private static final String USER = "root"; 
    private static final String PASSWORD = "7493010984Sa@"; 

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); 
            
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Java still cannot see your MySQL JAR file. Check your Build Path!");
        } catch (SQLException e) {
            System.out.println("Error: Database Connection Failed! Check your password and ensure MySQL is running.");
            e.printStackTrace();
        }
        return connection;
    }
}