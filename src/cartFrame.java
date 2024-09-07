import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class cartFrame extends JFrame {

    private Map<Integer, JLabel> itemLabelsMap = new HashMap<>();
    private int orderId;
    private JPanel itemsPanel;
    private JLabel totalAmountLabel;
    private int userId;
    private storeFrame storeFrame;

    public cartFrame(int orderId, int userId, storeFrame storeFrame) {
        this.orderId = orderId;
        this.userId = userId;
        this.storeFrame = storeFrame;

        setTitle("My Cart");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Items Panel
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Total amount label
        totalAmountLabel = new JLabel("Total Amount: $0.00");
        totalAmountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create a panel for total amount
        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new BorderLayout());
        totalPanel.add(totalAmountLabel, BorderLayout.CENTER);
        mainPanel.add(totalPanel, BorderLayout.SOUTH);

        // Add order items
        loadOrderItems();

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());

        JButton confirmButton = new JButton("Confirm Order");
        JButton discardButton = new JButton("Discard Items");
        JButton continueButton = new JButton("Continue Shopping");

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmOrder();
            }
        });

        discardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                discardItems();
            }
        });

        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                // Continue shopping functionality here
            }
        });

        buttonsPanel.add(confirmButton);
        buttonsPanel.add(discardButton);
        buttonsPanel.add(continueButton);

        // Add buttons panel to the top or center of the frame
        mainPanel.add(buttonsPanel, BorderLayout.NORTH);

        add(mainPanel);
        setVisible(true);
    }

    private void loadOrderItems() {
        itemsPanel.removeAll();
        itemLabelsMap.clear(); // Clear the map when loading items
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            String query = "SELECT mi.item_name, oi.item_id, SUM(oi.quantity) AS total_quantity, SUM(oi.price) AS total_price "
                    + "FROM OrderItems oi "
                    + "JOIN MenuItems mi ON oi.item_id = mi.item_id "
                    + "WHERE oi.order_id = ? "
                    + "GROUP BY mi.item_name, oi.item_id";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            int row = 1;
            while (rs.next()) {
                String itemName = rs.getString("item_name");
                int quantity = rs.getInt("total_quantity");
                double totalPrice = rs.getDouble("total_price");
                int itemId = rs.getInt("item_id"); // Store item_id

                // Create and store the label
                JLabel itemLabel = new JLabel(quantity + " " + itemName + "  $" + totalPrice);
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.gridwidth = 2;
                itemsPanel.add(itemLabel, gbc);
                itemLabelsMap.put(itemId, itemLabel); // Store the label in the map

                // Create the "+" and "-" buttons
                JButton removeButton = new JButton("-");
                removeButton.addActionListener(e -> {
                    removeItem(itemId);
                });
                JButton addButton = new JButton("+");
                addButton.addActionListener(e -> {
                    addItem(itemId);
                });

                JPanel buttonPanel = new JPanel();
                buttonPanel.add(removeButton);
                buttonPanel.add(addButton);

                gbc.gridx = 2;
                gbc.gridy = row;
                gbc.gridwidth = 1;
                itemsPanel.add(buttonPanel, gbc);

                row++;
            }

            // Update total amount
            updateTotalAmount(conn);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private void updateTotalAmount(Connection conn) throws SQLException {
        String query = "SELECT SUM(price) AS total_amount FROM OrderItems WHERE order_id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, orderId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            double totalAmount = rs.getDouble("total_amount");
            this.totalAmountLabel.setText("Total Amount: $" + String.format("%.2f", totalAmount));
        }
    }

    private void removeItem(int itemId) {
        // Implement remove item functionality
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            // Check the current quantity of the item in the order
            String checkQuantityQuery = "SELECT quantity FROM OrderItems WHERE order_id = ? AND item_id = ?";
            PreparedStatement checkQuantityStmt = conn.prepareStatement(checkQuantityQuery);
            checkQuantityStmt.setInt(1, orderId);
            checkQuantityStmt.setInt(2, itemId);
            ResultSet rs = checkQuantityStmt.executeQuery();

            if (rs.next()) {
                int quantity = rs.getInt("quantity");

                if (quantity > 1) {
                    // If the quantity is more than 1, decrease it by 1 and adjust the price
                    String updateItemQuery = "UPDATE OrderItems SET quantity = quantity - 1, price = price - (SELECT price FROM MenuItems WHERE item_id = ?) WHERE order_id = ? AND item_id = ?";
                    PreparedStatement updateItemStmt = conn.prepareStatement(updateItemQuery);
                    updateItemStmt.setInt(1, itemId);
                    updateItemStmt.setInt(2, orderId);
                    updateItemStmt.setInt(3, itemId);
                    updateItemStmt.executeUpdate();
                } else {
                    // If the quantity is 1, delete the item using the NOT IN condition
                    String deleteItemQuery = "DELETE FROM OrderItems WHERE order_item_id IN ("
                            + "SELECT MIN(order_item_id) "
                            + "FROM OrderItems "
                            + "WHERE order_id = ? AND item_id = ? "
                            + "GROUP BY order_id, item_id)";
                    PreparedStatement deleteItemStmt = conn.prepareStatement(deleteItemQuery);
                    deleteItemStmt.setInt(1, orderId);
                    deleteItemStmt.setInt(2, itemId);
                    deleteItemStmt.executeUpdate();
                }

                // Refresh the order items display
                loadOrderItems();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addItem(int itemId) {
        // Implement add item functionality
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            // Assuming default quantity of 1 for addition
            String fetchItemQuery = "SELECT price FROM MenuItems WHERE item_id = ?";
            PreparedStatement fetchItemStmt = conn.prepareStatement(fetchItemQuery);
            fetchItemStmt.setInt(1, itemId);
            ResultSet rs = fetchItemStmt.executeQuery();
            if (rs.next()) {
                double price = rs.getDouble("price");

                String insertItemQuery = "INSERT INTO OrderItems(order_id, item_id, user_id, quantity, price) "
                        + "VALUES (?, ?, ?, 1, ?)";
                PreparedStatement insertItemStmt = conn.prepareStatement(insertItemQuery);
                insertItemStmt.setInt(1, orderId);
                insertItemStmt.setInt(2, itemId);
                insertItemStmt.setInt(3, userId);
                insertItemStmt.setDouble(4, price);
                insertItemStmt.executeUpdate();

                loadOrderItems(); // Refresh the order items display
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void confirmOrder() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            // Step 1: Identify the last order in the Orders table
            String lastOrderQuery = "SELECT order_id FROM Orders ORDER BY order_id DESC LIMIT 1";
            PreparedStatement lastOrderStmt = conn.prepareStatement(lastOrderQuery);
            ResultSet rs = lastOrderStmt.executeQuery();

            if (rs.next()) {
                int lastOrderId = rs.getInt("order_id");

                // Step 2: Update the last order with the current orderId details
                String updateOrderQuery = "UPDATE Orders SET user_id = (SELECT user_id FROM Orders WHERE order_id = ?), store_id = (SELECT store_id FROM Orders WHERE order_id = ?), order_date = NOW(), status = 'Completed', total_amount = (SELECT SUM(price) FROM OrderItems WHERE order_id = ?), service_type = (SELECT service_type FROM Orders WHERE order_id = ?) WHERE order_id = ?";
                PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderQuery);
                updateOrderStmt.setInt(1, orderId); // Get user_id from current orderId
                updateOrderStmt.setInt(2, orderId); // Get store_id from current orderId
                updateOrderStmt.setInt(3, orderId); // Calculate total_amount from current orderId
                updateOrderStmt.setInt(4, orderId); // Get service_type from current orderId
                updateOrderStmt.setInt(5, lastOrderId); // Update the last order in the Orders table

                int rowsAffected = updateOrderStmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Order confirmed and updated successfully.");
                    storeFrame.dispose(); // Close the store frame
                    dispose(); // Close the cart frame
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to confirm the order. Please try again.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "No previous order found to update.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while confirming the order.");
        }
    }

    private void discardItems() {
        // Implement discard items functionality
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            String deleteItemsQuery = "DELETE FROM OrderItems WHERE order_id = ?";
            PreparedStatement deleteItemsStmt = conn.prepareStatement(deleteItemsQuery);
            deleteItemsStmt.setInt(1, orderId);
            deleteItemsStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Items discarded successfully.");
            loadOrderItems(); // Refresh the order items display
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}