import java.awt.*;
import javax.swing.*;

public class AdminDashboardFrame extends DashboardFrame {

    private JPanel headingPanel, controlPanel;
    private JLabel headingLabel;
    private JButton acceptDeliveryButton, logoutButton;

    private int userId;

    public AdminDashboardFrame(int userId) {
        super("Admin");
        this.userId = userId;

        initializeLayout();

        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initializeLayout() {
        this.setLayout(new BorderLayout());

        headingPanel = new JPanel();
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));

        headingLabel = new JLabel("Peinalas Control");
        headingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headingPanel.add(headingLabel, BorderLayout.CENTER);

        controlPanel = new JPanel();
        controlPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Accept Delivery Button
        acceptDeliveryButton = new JButton("Accept/Reject Delivery");
        acceptDeliveryButton.addActionListener(e -> {
            new AcceptDeliveryFrame();
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        controlPanel.add(acceptDeliveryButton, gbc);

        // Logoout Button
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            AdminDashboardFrame.this.dispose();
            new LoginFrame();
        });

        gbc.gridx = 0;
        gbc.gridy = 1;
        controlPanel.add(logoutButton, gbc);

        this.add(headingPanel, BorderLayout.NORTH);
        this.add(controlPanel, BorderLayout.CENTER);

    }

}
