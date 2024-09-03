import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EmailVerificationFrame extends JFrame {

    private final String verificationCode;
    private final JTextField codeField;
    private final EmailVerificationCallback callback;

    public EmailVerificationFrame(String email, String verificationCode, String name,
            EmailVerificationCallback callback) {
        this.verificationCode = verificationCode;
        this.callback = callback;

        this.setTitle("Email Verification");
        this.setSize(400, 200);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Verification Label
        JLabel label = new JLabel("Enter the verification code sent to " + email);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        this.add(label, gbc);

        // Code TextField
        codeField = new JTextField();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        this.add(codeField, gbc);

        // Verify Button
        JButton verifyButton = new JButton("Verify");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        this.add(verifyButton, gbc);

        // ActionListener for the Verify Button
        verifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verifyCode();
            }
        });

        this.setVisible(true);
    }

    private void verifyCode() {
        String enteredCode = codeField.getText();
        if (enteredCode.equals(verificationCode)) {
            JOptionPane.showMessageDialog(null, "Email verified successfully.");
            callback.onVerificationSuccess();
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(null, "Invalid verification code.");
            callback.onVerificationFailure();
        }
    }
}
