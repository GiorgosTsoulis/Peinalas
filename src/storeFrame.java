import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JSlider;

public class storeFrame extends JFrame {

    private Integer orderId = null;

    private JPanel detailsPanel;
    private JLabel storeLocationLabel;
    private JLabel storeKitchenCategoryLabel;
    private JLabel storePriceCategoryLabel;
    private JLabel storePhoneLabel;
    private JLabel storeWebsiteLabel;
    private JLabel storeHoursLabel;
    private JLabel storeLabel;

    private JComboBox<String> menuComboBox;
    private JButton searchBtn;
    private JButton cartBtn;
    private JSlider quantitySlider; // Make it an instance variable
    private ButtonGroup itemGroup;

    private JPanel menuSearchPanel;
    private JPanel menuDisplayPanel;
    private int userId;

    public storeFrame(String storeName, int userId) {

        this.userId = userId;
        this.setTitle(storeName + " Menu");
        this.setLocationRelativeTo(null);

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Top panel for Store Details
        detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints detailsGbc = new GridBagConstraints();
        detailsGbc.insets = new Insets(5, 5, 5, 5);
        detailsGbc.fill = GridBagConstraints.HORIZONTAL;

        // Store Label
        storeLabel = new JLabel("Store: " + storeName);
        detailsGbc.gridx = 0;
        detailsGbc.gridy = 0;
        detailsGbc.weightx = 1;
        detailsPanel.add(storeLabel, detailsGbc);

        // Store Location Label
        storeLocationLabel = new JLabel("Location: ");
        detailsGbc.gridx = 0;
        detailsGbc.gridy++;
        detailsGbc.weightx = 1;
        detailsPanel.add(storeLocationLabel, detailsGbc);

        // Kitchen Category Label
        storeKitchenCategoryLabel = new JLabel("Category: ");
        detailsGbc.gridx = 0;
        detailsGbc.gridy++;
        detailsGbc.weightx = 1;
        detailsPanel.add(storeKitchenCategoryLabel, detailsGbc);

        // Price Category Label
        storePriceCategoryLabel = new JLabel("Price: ");
        detailsGbc.gridx = 0;
        detailsGbc.gridy++;
        detailsGbc.weightx = 1;
        detailsPanel.add(storePriceCategoryLabel, detailsGbc);

        // Phone Label
        storePhoneLabel = new JLabel("Phone: ");
        detailsGbc.gridx = 0;
        detailsGbc.gridy++;
        detailsGbc.weightx = 1;
        detailsPanel.add(storePhoneLabel, detailsGbc);

        // Website Label
        storeWebsiteLabel = new JLabel("Website: ");
        detailsGbc.gridx = 0;
        detailsGbc.gridy++;
        detailsPanel.add(storeWebsiteLabel, detailsGbc);

        // Openning hours Label
        storeHoursLabel = new JLabel("Opening Hours: ");
        detailsGbc.gridx = 0;
        detailsGbc.gridy++;
        detailsGbc.weightx = 1;
        detailsPanel.add(storeHoursLabel, detailsGbc);

        // Vertical Filler
        detailsGbc.gridx = 0;
        detailsGbc.gridy = 7;
        detailsGbc.weighty = 0.5; // Filler should take up available space
        detailsGbc.fill = GridBagConstraints.BOTH;
        detailsPanel.add(new JPanel(), detailsGbc);

        // Panel for Menu Items Search
        menuSearchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints menuGbc = new GridBagConstraints();
        menuGbc.insets = new Insets(5, 5, 5, 5);
        menuGbc.fill = GridBagConstraints.HORIZONTAL;

        // Menu Label
        JLabel menuLabel = new JLabel("Menu: ");
        menuGbc.gridx = 0;
        menuGbc.gridy = 0;
        menuSearchPanel.add(menuLabel, menuGbc);

        // Menu ComboBox
        menuComboBox = new JComboBox<>();
        menuGbc.gridx = 1;
        menuGbc.gridy = 0;
        menuSearchPanel.add(menuComboBox, menuGbc);

        // Search Button
        searchBtn = new JButton("Search");
        menuGbc.gridx = 2;
        menuGbc.gridy = 0;
        menuSearchPanel.add(searchBtn, menuGbc);

        // Your Cart Button
        cartBtn = new JButton("My Cart");
        menuGbc.gridx = 3;
        menuGbc.gridy = 0;
        menuSearchPanel.add(cartBtn, menuGbc);

        // Panel for Menu Display
        menuDisplayPanel = new JPanel(new GridBagLayout());
        GridBagConstraints displayGbc = new GridBagConstraints();
        displayGbc.insets = new Insets(5, 5, 5, 5);
        displayGbc.fill = GridBagConstraints.HORIZONTAL;

        // Add details panel to the top of the frame
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(detailsPanel, gbc);

        // Add vertical filler panel between the left and right sides
        JPanel verticalFillerPanel = new JPanel();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(verticalFillerPanel, gbc);

        // Add Menu search panel to the middle of the frame
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.6;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.add(menuSearchPanel, gbc);

        // Add Menu Display to the frame
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.6;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(menuDisplayPanel, gbc);

        // Getting the store details
        getStoreDetails();

        // Populate menuComboBox
        populateMenuComboBox();

        searchBtn.addActionListener(e -> {
            String selectedCategory = (String) menuComboBox.getSelectedItem();
            if (selectedCategory != null) {
                displayMenuItems(selectedCategory);
            } else {
                System.out.println("Please select a category.");
            }
        });

        cartBtn.addActionListener(e -> {
            new cartFrame(this.orderId, this.userId, this);
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                rollbackOrder();
                dispose(); // Close the frame
            }
        });

        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setVisible(true);
    }

    private void getStoreDetails() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            String query = "SELECT location, kitchenCategory, priceCategory, phone_number, website, opening_hours "
                    + "FROM Stores WHERE name = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, this.getTitle().replace(" Menu", "")); // Extract store name from frame title
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Extract details from the result set
                String storeLocation = rs.getString("location");
                String storeKitchenCategory = rs.getString("kitchenCategory");
                String storePriceCategory = rs.getString("priceCategory");
                String storePhone = rs.getString("phone_number");
                String storeWebsite = rs.getString("website");
                String storeHours = rs.getString("opening_hours");

                // Update labels with the retrieved details
                storeLocationLabel.setText("Location: " + storeLocation);
                storeKitchenCategoryLabel.setText("Category: " + storeKitchenCategory);
                storePriceCategoryLabel.setText("Price: " + storePriceCategory);
                storePhoneLabel.setText("Phone: " + storePhone);
                storeWebsiteLabel.setText("Website: " + storeWebsite);
                storeHoursLabel.setText("Opening Hours: " + storeHours);
            } else {
                System.out.println("Store details not found.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void populateMenuComboBox() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            // Query to fetch menu items based on the store name
            String query = "SELECT DISTINCT item_category FROM MenuItems mi "
                    + "JOIN Stores s ON mi.store_id = s.store_id "
                    + "WHERE s.name = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, this.getTitle().replace(" Menu", "")); // Extract store name from frame title
            ResultSet rs = pstmt.executeQuery();

            // Populate combo box with menu items
            while (rs.next()) {
                String itemCategory = rs.getString("item_category");
                menuComboBox.addItem(itemCategory);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void displayMenuItems(String selectedCategory) {
        menuDisplayPanel.removeAll(); // Clear previous items

        // Create and add the category label
        GridBagConstraints displayGbc = new GridBagConstraints();
        displayGbc.insets = new Insets(5, 5, 5, 5);
        displayGbc.gridx = 0;
        displayGbc.gridy = 0;
        displayGbc.gridwidth = 1;
        JLabel categoryLabel = new JLabel("Category: " + selectedCategory);
        menuDisplayPanel.add(categoryLabel, displayGbc);

        displayGbc.gridwidth = 1; // Reset grid width for subsequent components

        itemGroup = new ButtonGroup(); // Group to allow only one selection
        quantitySlider = new JSlider(1, 20); // Create a single quantity slider
        quantitySlider.setMajorTickSpacing(5);
        quantitySlider.setMinorTickSpacing(1);
        quantitySlider.setValue(1);
        quantitySlider.setPaintTicks(true);
        quantitySlider.setPaintLabels(true);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            String query = "SELECT item_name FROM MenuItems mi "
                    + "JOIN Stores s ON mi.store_id = s.store_id "
                    + "WHERE s.name = ? AND mi.item_category = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, this.getTitle().replace(" Menu", "")); // Extract store name from frame title
            pstmt.setString(2, selectedCategory);
            ResultSet rs = pstmt.executeQuery();

            int row = 1;

            while (rs.next()) {
                String itemName = rs.getString("item_name");

                // Create and add the radio button for the item
                displayGbc.gridx = 0;
                displayGbc.gridy = row;
                JRadioButton itemRadioButton = new JRadioButton(itemName);
                itemGroup.add(itemRadioButton);
                menuDisplayPanel.add(itemRadioButton, displayGbc);

                row++;
            }

            // Add the "Quantity" label above the JSlider
            displayGbc.gridx = 1;
            displayGbc.gridy = 0;
            JLabel quantityLabel = new JLabel("Quantity:");
            menuDisplayPanel.add(quantityLabel, displayGbc);

            // Add the single quantity slider below the quantity label
            displayGbc.gridx = 1;
            displayGbc.gridy = 2;
            menuDisplayPanel.add(quantitySlider, displayGbc);

            // Add button at the bottom of the frame
            displayGbc.gridx = 0;
            displayGbc.gridy = row + 1;
            displayGbc.gridwidth = 2;
            displayGbc.anchor = GridBagConstraints.CENTER;
            displayGbc.fill = GridBagConstraints.NONE;
            JButton addToCartButton = new JButton("Add to Cart");
            menuDisplayPanel.add(addToCartButton, displayGbc);

            // Vertical Filler
            displayGbc.gridx = 0;
            displayGbc.gridy = row + 2;
            displayGbc.gridwidth = 2;
            displayGbc.weighty = 1;
            displayGbc.fill = GridBagConstraints.BOTH;
            menuDisplayPanel.add(new JPanel(), displayGbc);

            // Add ActionListener to the Add to Cart button
            addToCartButton.addActionListener(e -> addItem());

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Refresh the panel to show new components
        menuDisplayPanel.revalidate();
        menuDisplayPanel.repaint();

    }

    private void addItem() {
        JRadioButton selectedItemButton = null;

        // Iterate over the elements in the ButtonGroup to find the selected item
        var buttons = itemGroup.getElements();
        while (buttons.hasMoreElements()) {
            JRadioButton button = (JRadioButton) buttons.nextElement();
            if (button.isSelected()) {
                selectedItemButton = button;
                break;
            }
        }

        if (selectedItemButton == null) {
            System.out.println("No item selected.");
            return;
        }

        // Get the selected item name and quantity
        String selectedItem = selectedItemButton.getText();
        int quantity = quantitySlider.getValue();

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            // Set auto-commit to false to manage transactions
            conn.setAutoCommit(false);

            // Create a new order if this is the first item being added
            if (orderId == null) {
                String insertOrderQuery = "INSERT INTO Orders (user_id, store_id, status) VALUES (?, ?, 'In Progress')";
                PreparedStatement insertOrderStmt = conn.prepareStatement(insertOrderQuery,
                        PreparedStatement.RETURN_GENERATED_KEYS);
                insertOrderStmt.setInt(1, userId); // Assuming user_id is 1; adjust as necessary
                insertOrderStmt.setInt(2, getStoreId(conn)); // Fetch store ID based on the store name
                insertOrderStmt.executeUpdate();

                ResultSet generatedKeys = insertOrderStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1); // Store the orderId for future use
                    System.out.println("New order created with ID: " + orderId);
                } else {
                    System.out.println("Failed to create a new order.");
                    conn.rollback(); // Rollback transaction if order creation fails
                    return;
                }
            }

            // Fetch the item_id and price for the selected item
            String fetchItemQuery = "SELECT mi.item_id, mi.price FROM MenuItems mi "
                    + "JOIN Stores s ON mi.store_id = s.store_id "
                    + "WHERE s.name = ? AND mi.item_name = ?";
            PreparedStatement fetchItemStmt = conn.prepareStatement(fetchItemQuery);
            fetchItemStmt.setString(1, this.getTitle().replace(" Menu", "")); // Extract store name from frame title
            fetchItemStmt.setString(2, selectedItem);
            ResultSet itemRs = fetchItemStmt.executeQuery();

            if (itemRs.next()) {
                int itemId = itemRs.getInt("item_id");
                double price = itemRs.getDouble("price");

                // Insert into OrderItems table using the stored orderId
                String insertItemQuery = "INSERT INTO OrderItems(order_id, item_id, user_id, quantity, price) "
                        + "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertItemStmt = conn.prepareStatement(insertItemQuery);
                insertItemStmt.setInt(1, orderId);
                insertItemStmt.setInt(2, itemId);
                insertItemStmt.setInt(3, userId);
                insertItemStmt.setInt(4, quantity);
                insertItemStmt.setDouble(5, price * quantity);

                int rowsAffected = insertItemStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Item added to cart successfully.");
                } else {
                    System.out.println("Failed to add item to cart.");
                    conn.rollback(); // Rollback transaction if item addition fails
                    return;
                }

            } else {
                System.out.println("Item details not found in the database.");
                conn.rollback(); // Rollback transaction if item details are not found
                return;
            }

            // Commit the transaction
            conn.commit();

        } catch (SQLException ex) {
            ex.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction in case of an exception
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore default auto-commit behavior
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getStoreId(Connection conn) throws SQLException {
        String fetchStoreIdQuery = "SELECT store_id FROM Stores WHERE name = ?";
        PreparedStatement fetchStoreIdStmt = conn.prepareStatement(fetchStoreIdQuery);
        fetchStoreIdStmt.setString(1, this.getTitle().replace(" Menu", "")); // Extract store name from frame title
        ResultSet storeRs = fetchStoreIdStmt.executeQuery();

        if (storeRs.next()) {
            return storeRs.getInt("store_id");
        } else {
            throw new SQLException("Store ID not found for store: " + this.getTitle().replace(" Menu", ""));
        }
    }

    private void rollbackOrder() {
        if (orderId != null) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    System.out.println("Database connection error.");
                    return;
                }

                // Start transaction
                conn.setAutoCommit(false);

                // Check if the order status is "In Progress"
                String checkStatusQuery = "SELECT status FROM Orders WHERE order_id = ?";
                PreparedStatement checkStatusStmt = conn.prepareStatement(checkStatusQuery);
                checkStatusStmt.setInt(1, orderId);
                ResultSet rs = checkStatusStmt.executeQuery();

                if (rs.next()) {
                    String status = rs.getString("status");
                    if ("In Progress".equals(status)) {
                        // Delete OrderItems associated with the orderId
                        String deleteItemsQuery = "DELETE FROM OrderItems WHERE order_id = ?";
                        PreparedStatement deleteItemsStmt = conn.prepareStatement(deleteItemsQuery);
                        deleteItemsStmt.setInt(1, orderId);
                        deleteItemsStmt.executeUpdate();

                        // Delete the order
                        String deleteOrderQuery = "DELETE FROM Orders WHERE order_id = ?";
                        PreparedStatement deleteOrderStmt = conn.prepareStatement(deleteOrderQuery);
                        deleteOrderStmt.setInt(1, orderId);
                        deleteOrderStmt.executeUpdate();

                        // Commit the transaction
                        conn.commit();
                        System.out.println("Order rolled back successfully.");
                    } else {
                        System.out.println("Order has already been confirmed. No rollback needed.");
                    }
                } else {
                    System.out.println("Order not found.");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn != null) {
                        conn.rollback(); // Rollback in case of an error
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
