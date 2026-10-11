package empsystem.util;

import java.awt.Component;
import javax.swing.JOptionPane;

/** Message boxes used across the screens. */
public final class Dialogs {

    private Dialogs() {
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message);
    }

    public static void showError(Component parent, Exception e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Asks a yes/no question and returns true for yes. */
    public static boolean confirm(Component parent, String question) {
        int answer = JOptionPane.showConfirmDialog(parent, question, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }
}
