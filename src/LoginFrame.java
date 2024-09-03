import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginFrame() {
        this.setTitle("Login");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new GridBagLayout());
        this.setPreferredSize(new Dimension(300, 250)); // Set preferred size

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Welcome Label
        JLabel label1 = new JLabel("Welcome to Foody");
        label1.setFont(new Font("Arial", Font.BOLD, 24)); // Set font to bold and larger size
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        this.add(label1, gbc);

        // Username Label
        JLabel usernameLabel = new JLabel("Username");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        this.add(usernameLabel, gbc);

        // Username TextField
        usernameField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        this.add(usernameField, gbc);

        // Add KeyListener to usernameField
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    passwordField.requestFocus();
                }
            }
        });

        // Password Label
        JLabel passwordLabel = new JLabel("Password");
        gbc.gridx = 0;
        gbc.gridy = 2;
        this.add(passwordLabel, gbc);

        // Password TextField
        passwordField = new JPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        this.add(passwordField, gbc);

        // Add KeyListener to passwordField
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    LoginAction();
                }
            }
        });

        // Login Button
        JButton loginBtn = new JButton("Login");
        gbc.gridx = 1;
        gbc.gridy = 3;
        this.add(loginBtn, gbc);

        // ActionListener for the Login Button
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginAction();
            }
        });

        // Register Button
        JButton registerBtn = new JButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 3;
        this.add(registerBtn, gbc);

        // ActionListener for the Register Button
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SelectRoleFrame(LoginFrame.this);
                dispose(); // Close the login frame
            }
        });

        this.pack();
        this.setVisible(true);
    }

    // LoginAction Method
    private void LoginAction() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Updated query to also select user_id
            String query = "SELECT user_id, role FROM Users WHERE username = ? AND password = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id"); // Retrieve user ID
                String role = rs.getString("role");

                // Pass userId to the next frame
                showWelcomeFrame(role, userId);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid username or password.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
        }
    }

    // Show Welcome Frame
    private void showWelcomeFrame(String role, int userId) {
        new WelcomeFrame(role, userId, () -> openDashBoard(role, userId));
    }

    // Open respective dashboard based on the role
    public void openDashBoard(String role, int userId) {
        JFrame dashboard;
        dashboard = switch (role) {
            case "Customer" ->
                new CustomerDashboardFrame(userId);
            case "Staff" ->
                new StaffDashboardFrame(userId);
            case "Admin" ->
                new DashboardFrame("Admin");
            case "Delivery" ->
                new DashboardFrame("Delivery");
            default ->
                null;
        };

        if (dashboard != null) {
            dashboard.setVisible(true);
        }
    }
}
