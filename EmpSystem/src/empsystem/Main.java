package empsystem;

import empsystem.ui.LoginFrame;
import java.awt.EventQueue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/** Application entry point. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        useNimbusLookAndFeel();
        EventQueue.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    private static void useNimbusLookAndFeel() {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
                    // Keep the default look and feel.
                }
                return;
            }
        }
    }
}
