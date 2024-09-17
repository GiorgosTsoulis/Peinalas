import java.awt.*;
import javax.swing.*;

public class DeliveryDashboardFrame extends DashboardFrame {

    private int userId;

    public DeliveryDashboardFrame(int userId) {
        super("Delivery");
        this.userId = userId;

        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

}
