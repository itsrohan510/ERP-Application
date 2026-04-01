package edu.univ.erp;

import edu.univ.erp.ui.auth.LoginFrame;
import javax.swing.*;

public class ERPApplication {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
