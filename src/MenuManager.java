import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MenuManager extends JFrame {

    private JTable foodProductsTable;
    private DefaultTableModel tableModel;
    private JButton addBtn, editBtn, deleteBtn;
    private int storeId; // Store ID the staff works at

    public MenuManager(int storeId) {
        this.setTitle("Menu Manager");
        this.storeId = storeId;

        // Initialize components
        tableModel = new DefaultTableModel(
                new String[] { "ID", "Name", "Description", "Price", "Category", "Quantity" }, 0);
        foodProductsTable = new JTable(tableModel);
        addBtn = new JButton("Add");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");

        // Layout setup
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);

        // Add components to frame
        this.add(new JScrollPane(foodProductsTable), BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

        // Populate table with food products
        populateFoodProductsTable();

        // Add action listeners
        addBtn.addActionListener(e -> addFoodProduct());
        editBtn.addActionListener(e -> editFoodProduct());
        deleteBtn.addActionListener(e -> deleteFoodProduct());

        this.setPreferredSize(new Dimension(800, 600));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private boolean isStoreIdValid(int storeId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) FROM Stores WHERE store_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, storeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
        return false;
    }

    private void populateFoodProductsTable() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error. Cannot load food products.");
                return;
            }

            String query = "SELECT item_id, item_name, description, price, item_category, quantity_of_pieces FROM MenuItems WHERE store_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, storeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("item_id");
                String name = rs.getString("item_name");
                String description = rs.getString("description");
                double price = rs.getDouble("price");
                String category = rs.getString("item_category");
                int quantity = rs.getInt("quantity_of_pieces");
                tableModel.addRow(new Object[] { id, name, description, price, category, quantity });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void addFoodProduct() {
        JTextField nameField = new JTextField();
        JTextField descriptionField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField quantityField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Food Product", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String description = descriptionField.getText();
            double price = Double.parseDouble(priceField.getText());
            String category = categoryField.getText();
            int quantity = Integer.parseInt(quantityField.getText());

            if (!isStoreIdValid(storeId)) {
                JOptionPane.showMessageDialog(this, "Invalid store ID. Cannot add food product.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(this, "Database connection error. Cannot add food product.");
                    return;
                }

                String query = "INSERT INTO MenuItems (store_id, item_name, description, price, item_category, quantity_of_pieces) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, storeId);
                pstmt.setString(2, name);
                pstmt.setString(3, description);
                pstmt.setDouble(4, price);
                pstmt.setString(5, category);
                pstmt.setInt(6, quantity);
                pstmt.executeUpdate();

                populateFoodProductsTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void editFoodProduct() {
        int selectedRow = foodProductsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a food product to edit.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentDescription = (String) tableModel.getValueAt(selectedRow, 2);
        double currentPrice = (double) tableModel.getValueAt(selectedRow, 3);
        String currentCategory = (String) tableModel.getValueAt(selectedRow, 4);
        int currentQuantity = (int) tableModel.getValueAt(selectedRow, 5);

        JTextField nameField = new JTextField(currentName);
        JTextField descriptionField = new JTextField(currentDescription);
        JTextField priceField = new JTextField(String.valueOf(currentPrice));
        JTextField categoryField = new JTextField(currentCategory);
        JTextField quantityField = new JTextField(String.valueOf(currentQuantity));

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Edit Food Product", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            String description = descriptionField.getText();
            double price = Double.parseDouble(priceField.getText());
            String category = categoryField.getText();
            int quantity = Integer.parseInt(quantityField.getText());

            if (!isStoreIdValid(storeId)) {
                JOptionPane.showMessageDialog(this, "Invalid store ID. Cannot edit food product.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(this, "Database connection error. Cannot edit food product.");
                    return;
                }

                String query = "UPDATE MenuItems SET item_name = ?, description = ?, price = ?, item_category = ?, quantity_of_pieces = ? WHERE item_id = ? AND store_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, name);
                pstmt.setString(2, description);
                pstmt.setDouble(3, price);
                pstmt.setString(4, category);
                pstmt.setInt(5, quantity);
                pstmt.setInt(6, id);
                pstmt.setInt(7, storeId);
                pstmt.executeUpdate();

                populateFoodProductsTable();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void deleteFoodProduct() {
        int selectedRow = foodProductsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a food product to delete.");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection error. Cannot delete food product.");
                return;
            }

            String query = "DELETE FROM MenuItems WHERE item_id = ? AND store_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            pstmt.setInt(2, storeId);
            pstmt.executeUpdate();

            tableModel.removeRow(selectedRow);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
