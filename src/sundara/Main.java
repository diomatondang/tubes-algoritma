package sundara;

import sundara.ui.LoginDialog;
import sundara.ui.MainFrame;
import sundara.ui.Theme;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Global UI defaults for dark theme
        UIManager.put("OptionPane.background",        Theme.BG_SECONDARY);
        UIManager.put("Panel.background",             Theme.BG_SECONDARY);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT_PRIMARY);
        UIManager.put("Button.background",            Theme.BTN_PRIMARY);
        UIManager.put("Button.foreground",            java.awt.Color.WHITE);
        UIManager.put("Button.focus",                 Theme.BTN_PRIMARY);
        UIManager.put("TextField.background",         Theme.BG_CARD);
        UIManager.put("TextField.foreground",         Theme.TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",    java.awt.Color.WHITE);
        UIManager.put("Label.foreground",             Theme.TEXT_PRIMARY);
        UIManager.put("ComboBox.background",          Theme.BG_CARD);
        UIManager.put("ComboBox.foreground",          Theme.TEXT_PRIMARY);
        UIManager.put("ScrollPane.background",        Theme.BG_PRIMARY);
        UIManager.put("Viewport.background",          Theme.BG_PRIMARY);
        UIManager.put("SplitPane.background",         Theme.BG_PRIMARY);
        UIManager.put("SplitPaneDivider.background",  Theme.BORDER);

        SwingUtilities.invokeLater(() -> {
            // Show login first
            LoginDialog login = new LoginDialog();
            login.setVisible(true);

            // Only open main app if authenticated
            if (login.isAuthenticated()) {
                new MainFrame();
            } else {
                System.exit(0);
            }
        });
    }
}
