import javax.swing.*;
import java.awt.*;

public class ActiveDeliveriesFrame extends JFrame {

    private int userId;

    public ActiveDeliveriesFrame(int userId) {
        this.userId = userId;
        this.setTitle("My Active Deliveries");

        initializeLayout();

        this.setPreferredSize(new Dimension(600, 500));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initializeLayout() {

    }

}
