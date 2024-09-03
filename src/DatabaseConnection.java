import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static Connection connection;

    // Method to initialize the connection
    public static void initializeConnection() {
        String url = "jdbc:mysql://localhost:3306/foodorderdb?zeroDateTimeBehavior=CONVERT_TO_NULL";
        String username = "root";
        String password = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load the MySQL JDBC driver
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connection successful.");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }
    }

    // Method to get the connection
    public static Connection getConnection() {
        if (connection == null) {
            System.out.println("Connection is null. Please initialize the connection first.");
        } else {
            try {
                if (connection.isClosed()) {
                    System.out.println("Connection is closed.");
                    connection = null; // Reset the connection to null
                    DatabaseConnection.initializeConnection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }
}
