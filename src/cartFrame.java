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
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        totalAmountLabel = new JLabel("Total Amount: $0.00");
        totalAmountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new BorderLayout());
        totalPanel.add(totalAmountLabel, BorderLayout.CENTER);
        mainPanel.add(totalPanel, BorderLayout.SOUTH);

        loadOrderItems();

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
            }
        });

        buttonsPanel.add(confirmButton);
        buttonsPanel.add(discardButton);
        buttonsPanel.add(continueButton);

        mainPanel.add(buttonsPanel, BorderLayout.NORTH);

        add(mainPanel);

        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void loadOrderItems() {
        itemsPanel.removeAll();
        itemLabelsMap.clear();
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
                int itemId = rs.getInt("item_id");

                JLabel itemLabel = new JLabel(quantity + " " + itemName + "  $" + totalPrice);
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.gridwidth = 2;
                itemsPanel.add(itemLabel, gbc);
                itemLabelsMap.put(itemId, itemLabel);

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
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

            String checkQuantityQuery = "SELECT quantity FROM OrderItems WHERE order_id = ? AND item_id = ?";
            PreparedStatement checkQuantityStmt = conn.prepareStatement(checkQuantityQuery);
            checkQuantityStmt.setInt(1, orderId);
            checkQuantityStmt.setInt(2, itemId);
            ResultSet rs = checkQuantityStmt.executeQuery();

            if (rs.next()) {
                int quantity = rs.getInt("quantity");

                if (quantity > 1) {

                    String updateItemQuery = "UPDATE OrderItems SET quantity = quantity - 1, price = price - (SELECT price FROM MenuItems WHERE item_id = ?) WHERE order_id = ? AND item_id = ?";
                    PreparedStatement updateItemStmt = conn.prepareStatement(updateItemQuery);
                    updateItemStmt.setInt(1, itemId);
                    updateItemStmt.setInt(2, orderId);
                    updateItemStmt.setInt(3, itemId);
                    updateItemStmt.executeUpdate();
                } else {
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

                loadOrderItems();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addItem(int itemId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Database connection error.");
                return;
            }

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

                loadOrderItems();
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

            String lastOrderQuery = "SELECT order_id FROM Orders ORDER BY order_id DESC LIMIT 1";
            PreparedStatement lastOrderStmt = conn.prepareStatement(lastOrderQuery);
            ResultSet rs = lastOrderStmt.executeQuery();

            if (rs.next()) {
                int lastOrderId = rs.getInt("order_id");

                String updateOrderQuery = "UPDATE Orders SET user_id = (SELECT user_id FROM Orders WHERE order_id = ?), store_id = (SELECT store_id FROM Orders WHERE order_id = ?), order_date = NOW(), status = 'Pending', total_amount = (SELECT SUM(price) FROM OrderItems WHERE order_id = ?), service_type = (SELECT service_type FROM Orders WHERE order_id = ?), timer_start = NOW() WHERE order_id = ?";
                PreparedStatement updateOrderStmt = conn.prepareStatement(updateOrderQuery);
                updateOrderStmt.setInt(1, orderId);
                updateOrderStmt.setInt(2, orderId);
                updateOrderStmt.setInt(3, orderId);
                updateOrderStmt.setInt(4, orderId);
                updateOrderStmt.setInt(5, lastOrderId);

                int rowsAffected = updateOrderStmt.executeUpdate();

                if (rowsAffected > 0) {
                    String promoCode = getPromoCodeFromUser();
                    if (promoCode != null && !promoCode.isEmpty()) {

                        // Query to check the coupon's validity, including min_order_value, store_id,
                        // and expiry_date
                        String promoQuery = "SELECT discount_amount, discount_type, min_order_value, store_id, usage_limit "
                                + "FROM Coupons "
                                + "WHERE coupon_code = ? AND usage_limit = 1";
                        PreparedStatement promoStmt = conn.prepareStatement(promoQuery);
                        promoStmt.setString(1, promoCode);
                        ResultSet promoRs = promoStmt.executeQuery();

                        if (promoRs.next()) {
                            double discountAmount = promoRs.getDouble("discount_amount");
                            String discountType = promoRs.getString("discount_type");
                            double minOrderValue = promoRs.getDouble("min_order_value");
                            int couponStoreId = promoRs.getInt("store_id");

                            String totalQuery = "SELECT total_amount, store_id FROM Orders WHERE order_id = ?";
                            PreparedStatement totalStmt = conn.prepareStatement(totalQuery);
                            totalStmt.setInt(1, lastOrderId);
                            ResultSet totalRs = totalStmt.executeQuery();

                            // Check if the order meets the coupon's conditions
                            if (totalRs.next()) {
                                double totalAmount = totalRs.getDouble("total_amount");
                                int orderStoreId = totalRs.getInt("store_id");

                                if (totalAmount >= minOrderValue && (couponStoreId == orderStoreId)) {
                                    double newTotalAmount = totalAmount;

                                    if (discountType.equals("Percentage")) {
                                        newTotalAmount = totalAmount - (totalAmount * discountAmount / 100);
                                    } else if (discountType.equals("Flat")) {
                                        newTotalAmount = totalAmount - discountAmount;
                                    }

                                    // Update the total amount in the Orders table
                                    String updateTotalQuery = "UPDATE Orders SET total_amount = ? WHERE order_id = ?";
                                    PreparedStatement updateTotalStmt = conn.prepareStatement(updateTotalQuery);
                                    updateTotalStmt.setDouble(1, newTotalAmount);
                                    updateTotalStmt.setInt(2, lastOrderId);
                                    updateTotalStmt.executeUpdate();

                                    // Update the usage limit of the coupon
                                    String updateUsageQuery = "UPDATE Coupons SET usage_limit = 0 WHERE coupon_code = ?";
                                    PreparedStatement updateUsageStmt = conn.prepareStatement(updateUsageQuery);
                                    updateUsageStmt.setString(1, promoCode);
                                    updateUsageStmt.executeUpdate();

                                    JOptionPane.showMessageDialog(this,
                                            "Promo code applied successfully and your total amount has been reduced to $"
                                                    + newTotalAmount);

                                } else {
                                    JOptionPane.showMessageDialog(this, "Order does not meet the coupon's conditions.");
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid, expired, or fully used coupon.");
                        }
                    }

                    EmailPromo.checkAndSendPromoEmail(lastOrderId, userId);
                    OrderNotificationScheduler.checkOrdersAndSendNotifications(orderId);
                    JOptionPane.showMessageDialog(this,
                            "Your order is pending confirmation. You will receive an email notification shortly.");
                    storeFrame.dispose();
                    dispose();
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

    private String getPromoCodeFromUser() {

        String promoCode = JOptionPane.showInputDialog(this, "Enter Promo Code (if any):", "Promo Code",
                JOptionPane.QUESTION_MESSAGE);

        if (promoCode != null && !promoCode.trim().isEmpty()) {
            return promoCode.trim();
        } else {
            return null;
        }
    }

    private void discardItems() {
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
            loadOrderItems();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}