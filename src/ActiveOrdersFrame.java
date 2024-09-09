import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class ActiveOrdersFrame extends JFrame {

    private int userId;
    private JPanel contentPanel;

    ActiveOrdersFrame(int userId) {
        this.userId = userId;
        this.setTitle("My Active Orders");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a panel for the column names
        JPanel columnNamesPanel = new JPanel(new GridLayout(1, 5));
        columnNamesPanel.add(new JLabel("Order ID"));
        columnNamesPanel.add(new JLabel("Store"));
        columnNamesPanel.add(new JLabel("Amount"));
        columnNamesPanel.add(new JLabel("Service"));
        columnNamesPanel.add(new JLabel("Timer"));

        // Create the content panel for active orders
        contentPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(contentPanel);

        // Add the column names panel and the scroll pane to the frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(columnNamesPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        loadActiveOrders();

        // Back Button
        JButton backBtn = new JButton("Back");
        JPanel backBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backBtnPanel.add(backBtn);
        getContentPane().add(backBtnPanel, BorderLayout.SOUTH);

        // ActionListener for the Back Button
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ActiveOrdersFrame.this.dispose();
            }
        });

        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void loadActiveOrders() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;
            }

            String query = "SELECT o.order_id, s.name, o.total_amount, o.service_type, o.timer_start, s.preparation_time "
                    +
                    "FROM Orders o " +
                    "JOIN Stores s ON o.store_id = s.store_id " +
                    "WHERE o.user_id = ? AND o.status = 'In Progress'";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(2, 2, 2, 2); // Minimal spacing between rows
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.anchor = GridBagConstraints.NORTH;

            while (rs.next()) {
                int orderId = rs.getInt("order_id");
                String storeName = rs.getString("name");
                double totalAmount = rs.getDouble("total_amount");
                String serviceType = rs.getString("service_type");
                Timestamp timerStart = rs.getTimestamp("timer_start");
                int preparationTime = rs.getInt("preparation_time");

                // Create and add labels for each active order
                JLabel orderIdLabel = new JLabel(String.valueOf(orderId));
                JLabel storeNameLabel = new JLabel(storeName);
                JLabel totalAmountLabel = new JLabel(String.format("$%.2f", totalAmount));
                JLabel serviceTypeLabel = new JLabel(serviceType);
                JLabel timerLabel = new JLabel();

                // Add labels to the content panel with constraints
                gbc.gridy++;
                contentPanel.add(orderIdLabel, gbc);
                gbc.gridx++;
                contentPanel.add(storeNameLabel, gbc);
                gbc.gridx++;
                contentPanel.add(totalAmountLabel, gbc);
                gbc.gridx++;
                contentPanel.add(serviceTypeLabel, gbc);
                gbc.gridx++;
                contentPanel.add(timerLabel, gbc);
                gbc.gridx = 0; // Reset to first column for next row

                // Start the timer task
                startTimer(timerStart, preparationTime, timerLabel, orderIdLabel, storeNameLabel, totalAmountLabel,
                        serviceTypeLabel);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + ex.getMessage());
        }
    }

    private void startTimer(Timestamp timerStart, int preparationTime, JLabel timerLabel, JLabel... labels) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long startTime = timerStart.getTime();
                long elapsedTime = currentTime - startTime;
                long remainingTime = preparationTime * 60 * 1000 - elapsedTime;

                SwingUtilities.invokeLater(() -> {
                    if (remainingTime <= 0) {
                        timerLabel.setText("Time left: 00:00");
                        timer.cancel();
                        contentPanel.remove(timerLabel);
                        for (JLabel label : labels) {
                            contentPanel.remove(label);
                        }
                        contentPanel.revalidate();
                        contentPanel.repaint();

                        // Update the order status in the database
                        try (Connection conn = DatabaseConnection.getConnection()) {
                            if (conn != null) {
                                String updateQuery = "UPDATE Orders SET status = 'Completed' WHERE order_id = ?";
                                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                    updateStmt.setInt(1, Integer.parseInt(labels[0].getText())); // Assuming orderId is
                                                                                                 // the first label
                                    updateStmt.executeUpdate();
                                    OrderNotificationScheduler.checkOrdersAndSendNotifications();
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "Database connection error.");
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Error updating order status: " + ex.getMessage());
                        }
                    } else {
                        long minutes = (remainingTime / 1000) / 60;
                        long seconds = (remainingTime / 1000) % 60;
                        timerLabel.setText(String.format("Time left: %02d:%02d", minutes, seconds));
                    }
                });
            }
        };

        timer.scheduleAtFixedRate(task, 0, 1000);
    }
}