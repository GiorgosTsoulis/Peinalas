import javax.swing.JFrame;

public class DashboardFrame extends JFrame {

    public DashboardFrame(String role) {
        this.setTitle(role + " Dashboard");
        this.setSize(800, 600);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}