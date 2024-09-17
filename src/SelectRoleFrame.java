import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class SelectRoleFrame extends JFrame {

    private JComboBox<String> roleComboBox;
    private JButton selectRoleBtn;

    public SelectRoleFrame(LoginFrame loginFrame) {
        this.setTitle("Select Role");
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Role Label
        JLabel roleLabel = new JLabel("Role");
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(roleLabel, gbc);

        // Role ComboBox
        String[] roles = { "Customer", "Staff", "Delivery" };
        roleComboBox = new JComboBox<>(roles);
        gbc.gridx = 1;
        gbc.gridy = 0;
        this.add(roleComboBox, gbc);

        // Select Role Button
        selectRoleBtn = new JButton("Select Role");
        gbc.gridx = 2;
        gbc.gridy = 0;
        this.add(selectRoleBtn, gbc);

        // ActionListener for the Select Role Button
        selectRoleBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedRole = (String) roleComboBox.getSelectedItem();
                switch (selectedRole) {
                    case "Customer":
                        // Assuming there is a CustomerRegisterFrame class
                        new CustomerRegisterFrame(loginFrame);
                        break;
                    case "Staff":
                        new StaffRegisterFrame(loginFrame);
                        break;
                    case "Delivery":
                        new DeliveryRegisterFrame(loginFrame);
                        break;
                }
                SelectRoleFrame.this.dispose();
            }
        });

        this.pack();
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }
}
