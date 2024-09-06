import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class CustomerDashboardFrame extends DashboardFrame {

    private JComboBox<String> locationComboBox;
    private JComboBox<String> kitchenComboBox;
    private JComboBox<String> priceComboBox;
    private JButton searchBtn;
    private JPanel resultsPanel;
    private GridBagConstraints gbc;
    private int userId;

    public CustomerDashboardFrame(int userId) {
        super("Customer");
        this.userId = userId;
        System.out.println("CustomerDashboardFrame initialized with userId: " + this.userId);

        this.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Initialize Components
        locationComboBox = new JComboBox<>();
        kitchenComboBox = new JComboBox<>();
        priceComboBox = new JComboBox<>();
        searchBtn = new JButton("Search");
        new JPanel(new GridBagLayout());
        resultsPanel = new JPanel(new GridBagLayout());

        // Filter Panel
        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints filterGbc = new GridBagConstraints();
        filterGbc.insets = new Insets(5, 5, 5, 5);
        filterGbc.fill = GridBagConstraints.HORIZONTAL;
        filterGbc.weightx = 0;

        // Location Label
        JLabel locationLabel = new JLabel("Location:");
        filterGbc.gridx = 0;
        filterGbc.gridy = 0;
        filterGbc.weightx = 1;
        filterPanel.add(locationLabel, filterGbc);

        // Location ComboBox
        filterGbc.gridx = 0;
        filterGbc.gridy = 1;
        filterGbc.weightx = 1;
        filterPanel.add(locationComboBox, filterGbc);

        // Category Label
        JLabel kitchenLabel = new JLabel("Category:");
        filterGbc.gridx = 0;
        filterGbc.gridy = 2;
        filterGbc.weightx = 1;
        filterPanel.add(kitchenLabel, filterGbc);

        // Category ComboBox
        filterGbc.gridx = 0;
        filterGbc.gridy = 3;
        filterGbc.weightx = 1;
        filterPanel.add(kitchenComboBox, filterGbc);

        // Price Label
        JLabel priceLabel = new JLabel("Price:");
        filterGbc.gridx = 0;
        filterGbc.gridy = 4;
        filterGbc.weightx = 1;
        filterPanel.add(priceLabel, filterGbc);

        // Price ComboBox
        filterGbc.gridx = 0;
        filterGbc.gridy = 5;
        filterGbc.weightx = 1;
        filterPanel.add(priceComboBox, filterGbc);

        // Vertical Filler
        filterGbc.gridx = 0;
        filterGbc.gridy = 6;
        filterGbc.weighty = 0.5; // Filler should take up available space
        filterGbc.fill = GridBagConstraints.BOTH;
        filterPanel.add(new JPanel(), filterGbc); // Adding an empty panel as a fille

        // Search Button
        filterGbc.gridx = 0;
        filterGbc.gridy = 7;
        filterGbc.weighty = 0;
        filterGbc.fill = GridBagConstraints.HORIZONTAL;
        filterPanel.add(searchBtn, filterGbc);

        // Profile button
        JButton profileBtn = new JButton("My Profile");
        filterGbc.gridx = 0;
        filterGbc.gridy = 8;
        filterGbc.weighty = 0;
        filterGbc.fill = GridBagConstraints.HORIZONTAL;
        filterPanel.add(profileBtn, filterGbc);

        profileBtn.addActionListener((ActionEvent e) -> {
            openProfileFrame(userId);
        });

        // Results Panel with ScrollPane
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);

        // Add Filter Panel, Results Panel and Profile Panel to Frame
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(filterPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        gbc.weighty = 0.95;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(scrollPane, gbc);

        // Populate ComboBoxes
        populateLocationComboBox();
        populateKitchenComboBox();
        populatePriceComboBox();

        // ActionListener for the Search Button
        searchBtn.addActionListener((ActionEvent e) -> {
            searchStores();
        });

        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setVisible(true);
    }

    private void populateLocationComboBox() {
        locationComboBox.addItem("Any"); // Add "Any" as the first option
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error. Cannot load locations.");
                return;
            }

            String query = "SELECT DISTINCT location FROM Stores";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String location = rs.getString("location");
                locationComboBox.addItem(location);
            }

            if (locationComboBox.getItemCount() > 1) {
                locationComboBox.setSelectedIndex(0); // Set "Any" as the default selected item
            } else {
                JOptionPane.showMessageDialog(this, "No locations found in the database.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void populateKitchenComboBox() {
        kitchenComboBox.addItem("Any"); // Add "Any" as the first option
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error. Cannot load kitchen categories.");
                return;
            }

            String query = "SELECT DISTINCT kitchenCategory FROM Stores";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String kitchen = rs.getString("kitchenCategory");
                kitchenComboBox.addItem(kitchen);
            }

            if (kitchenComboBox.getItemCount() > 1) {
                kitchenComboBox.setSelectedIndex(0); // Set "Any" as the default selected item
            } else {
                JOptionPane.showMessageDialog(this, "No kitchen categories found in the database.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void populatePriceComboBox() {
        priceComboBox.addItem("Any"); // Add "Any" as the first option
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error. Cannot load price categories.");
                return;
            }

            String query = "SELECT DISTINCT priceCategory FROM Stores";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String price = rs.getString("priceCategory");
                priceComboBox.addItem(price);
            }

            if (priceComboBox.getItemCount() > 1) {
                priceComboBox.setSelectedIndex(0); // Set "Any" as the default selected item
            } else {
                JOptionPane.showMessageDialog(this, "No price categories found in the database.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void searchStores() {
        String selectedLocation = (String) locationComboBox.getSelectedItem();
        String selectedKitchen = (String) kitchenComboBox.getSelectedItem();
        String selectedPrice = (String) priceComboBox.getSelectedItem();

        StringBuilder queryBuilder = new StringBuilder("SELECT name, location FROM Stores WHERE 1=1");

        if (!"Any".equals(selectedLocation)) {
            queryBuilder.append(" AND location = ?");
        }
        if (!"Any".equals(selectedKitchen)) {
            queryBuilder.append(" AND kitchenCategory = ?");
        }
        if (!"Any".equals(selectedPrice)) {
            queryBuilder.append(" AND priceCategory = ?");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString());

            int paramIndex = 1;
            if (!"Any".equals(selectedLocation)) {
                pstmt.setString(paramIndex++, selectedLocation);
            }
            if (!"Any".equals(selectedKitchen)) {
                pstmt.setString(paramIndex++, selectedKitchen);
            }
            if (!"Any".equals(selectedPrice)) {
                pstmt.setString(paramIndex++, selectedPrice);
            }

            ResultSet rs = pstmt.executeQuery();

            resultsPanel.removeAll(); // Clear previous results

            int row = 0;
            while (rs.next()) {
                String name = rs.getString("name");

                JButton storeButton = new JButton(name); // Create button for each store
                storeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        openStoreDetailsFrame(name, userId);
                    }
                });

                GridBagConstraints buttonGbc = new GridBagConstraints();
                buttonGbc.insets = new Insets(5, 5, 5, 5);
                buttonGbc.anchor = GridBagConstraints.NORTHWEST;
                buttonGbc.gridx = 0;
                buttonGbc.gridy = row++;
                buttonGbc.gridwidth = 1;
                buttonGbc.fill = GridBagConstraints.HORIZONTAL;
                buttonGbc.weightx = 0.25;
                buttonGbc.weighty = 0.1;
                resultsPanel.add(storeButton, buttonGbc);

                GridBagConstraints fillerGbc = new GridBagConstraints();
                fillerGbc.gridx = 1;
                fillerGbc.gridy = row - 1;
                fillerGbc.weightx = 0.75;
                fillerGbc.fill = GridBagConstraints.HORIZONTAL;
                resultsPanel.add(new JPanel(), fillerGbc);
            }

            resultsPanel.revalidate();
            resultsPanel.repaint();

            if (row == 0) {
                JOptionPane.showMessageDialog(this, "No restaurants found for the selected criteria.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error querying the database.");
        }
    }

    // Method to open a new frame with store details
    public void openStoreDetailsFrame(String storeName, int userId) {
        new storeFrame(storeName, userId); // Open store frame with storeName and userId
    }

    public void openProfileFrame(int userId) {
        new profileFrame(userId); // Open profile frame with userId
    }
}