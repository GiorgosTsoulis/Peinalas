import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import org.jdesktop.swingx.JXDatePicker;

public abstract class RegisterFrame extends JFrame {

    protected JTextField firstnameField;
    protected JTextField lastnameField;
    protected JTextField addressField;
    protected JTextField addressNumberField;
    protected JTextField postCodeField;
    protected JComboBox<String> countryComboBox;
    protected JXDatePicker ageDatePicker;
    protected JComboBox<String> genderComboBox;
    protected JTextField numberField;
    protected JTextField emailField;
    protected JTextField usernameField;
    protected JPasswordField passwordField;
    protected JButton registerBtn;
    protected JButton backBtn;
    protected LoginFrame loginFrame;
    protected GridBagConstraints gbc;

    public RegisterFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setLayout(new GridBagLayout());

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Firstname Label
        JLabel firstnameLabel = new JLabel("Firstname");
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(firstnameLabel, gbc);

        // Firstname TextField
        firstnameField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        this.add(firstnameField, gbc);

        // Lastname Label
        JLabel lastnameLabel = new JLabel("Lastname");
        gbc.gridx = 2;
        gbc.gridy = 0;
        this.add(lastnameLabel, gbc);

        // Lastname TextField
        lastnameField = new JTextField();
        gbc.gridx = 3;
        gbc.gridy = 0;
        this.add(lastnameField, gbc);

        // Address Label
        JLabel addressLabel = new JLabel("Address");
        gbc.gridx = 0;
        gbc.gridy = 1;
        this.add(addressLabel, gbc);

        // Address TextField
        addressField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        this.add(addressField, gbc);

        // Address Number Label
        JLabel addressNumberLabel = new JLabel("Address Number");
        gbc.gridx = 2;
        gbc.gridy = 1;
        this.add(addressNumberLabel, gbc);

        // Address Number TextField
        addressNumberField = new JTextField();
        gbc.gridx = 3;
        gbc.gridy = 1;
        this.add(addressNumberField, gbc);

        // Post Code Label
        JLabel postCodeLabel = new JLabel("Post Code");
        gbc.gridx = 0;
        gbc.gridy = 2;
        this.add(postCodeLabel, gbc);

        // Post Code TextField
        postCodeField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        this.add(postCodeField, gbc);

        // Country Label
        JLabel countryLabel = new JLabel("Country");
        gbc.gridx = 2;
        gbc.gridy = 2;
        this.add(countryLabel, gbc);

        // Country ComboBox
        String[] countries = java.util.Locale.getISOCountries();
        countryComboBox = new JComboBox<>(countries);
        gbc.gridx = 3;
        gbc.gridy = 2;
        this.add(countryComboBox, gbc);

        // Age Label
        JLabel ageLabel = new JLabel("Age");
        gbc.gridx = 0;
        gbc.gridy = 3;
        this.add(ageLabel, gbc);

        // Age DatePicker
        ageDatePicker = new JXDatePicker();
        gbc.gridx = 1;
        gbc.gridy = 3;
        this.add(ageDatePicker, gbc);

        // Gender Label
        JLabel genderLabel = new JLabel("Gender");
        gbc.gridx = 2;
        gbc.gridy = 3;
        this.add(genderLabel, gbc);

        // Gender ComboBox
        String[] genders = { "Male", "Female", "Other" };
        genderComboBox = new JComboBox<>(genders);
        gbc.gridx = 3;
        gbc.gridy = 3;
        this.add(genderComboBox, gbc);

        // Number Label
        JLabel numberLabel = new JLabel("Number");
        gbc.gridx = 0;
        gbc.gridy = 4;
        this.add(numberLabel, gbc);

        // Number TextField
        numberField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 4;
        this.add(numberField, gbc);

        // Email Label
        JLabel emailLabel = new JLabel("Email");
        gbc.gridx = 0;
        gbc.gridy = 5;
        this.add(emailLabel, gbc);

        // Email TextField
        emailField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 5;
        this.add(emailField, gbc);

        // Username Label
        JLabel usernameLabel = new JLabel("Username");
        gbc.gridx = 0;
        gbc.gridy = 6;
        this.add(usernameLabel, gbc);

        // Username TextField
        usernameField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 6;
        this.add(usernameField, gbc);

        // Password Label
        JLabel passwordLabel = new JLabel("Password");
        gbc.gridx = 0;
        gbc.gridy = 7;
        this.add(passwordLabel, gbc);

        // Password TextField
        passwordField = new JPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 7;
        this.add(passwordField, gbc);

        // Register Button
        registerBtn = new JButton("Register");
        gbc.gridx = 1;
        gbc.gridy = 8;
        this.add(registerBtn, gbc);

        // Back Button
        backBtn = new JButton("Back");
        gbc.gridx = 2;
        gbc.gridy = 8;
        this.add(backBtn, gbc);

        // ActionListener for the Register Button
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });

        // ActionListener for the Back Button
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegisterFrame.this.dispose();
                loginFrame.setVisible(true);
            }
        });

        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setVisible(true);
    }

    protected abstract void registerUser();

    // Method to clear input fields
    protected void clearFields() {
        firstnameField.setText("");
        lastnameField.setText("");
        addressField.setText("");
        addressNumberField.setText("");
        postCodeField.setText("");
        countryComboBox.setSelectedIndex(0);
        ageDatePicker.setDate(null);
        genderComboBox.setSelectedIndex(0);
        numberField.setText("");
        emailField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }
}
