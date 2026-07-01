package sundara.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import sundara.data.DatabaseManager;

/**
 * Login screen shown before the main POS window.
 * Default credentials: admin / sundara2025
 */
public class LoginDialog extends JDialog {

    private boolean authenticated = false;

    // Hardcoded credentials (in a real system use hashed DB lookup)
    private static final String USER     = "admin";
    private static final String PASSWORD = "12345";

    private JTextField     userField;
    private JPasswordField passField;
    private JLabel         errorLabel;

    public LoginDialog() {
        super((Frame) null, "Sundara CoffeeSpace — Login", true);
        setUndecorated(true);            // remove native titlebar
        setSize(420, 560);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0)); // transparent frame for rounded window

        JPanel root = buildRoot();
        setContentPane(root);

        // Allow dragging the undecorated window
        DragListener drag = new DragListener(this);
        root.addMouseListener(drag);
        root.addMouseMotionListener(drag);

        // Enter key submits
        getRootPane().setDefaultButton(null);
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, "login");
        root.getActionMap().put("login", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { attemptLogin(); }
        });
    }

    // ── Root panel with rounded corners ─────────────────────────────
    private JPanel buildRoot() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Outer shadow illusion
                g2.setColor(new Color(0, 0, 0, 80));
                g2.fill(new RoundRectangle2D.Float(4, 4, getWidth() - 4, getHeight() - 4, 24, 24));
                // Main background
                g2.setColor(Theme.BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 4, getHeight() - 4, 24, 24));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 4, 4)); // shadow offset

        panel.add(buildLogoSection(), BorderLayout.NORTH);
        panel.add(buildFormSection(), BorderLayout.CENTER);
        panel.add(buildFooter(),      BorderLayout.SOUTH);
        return panel;
    }

    // ── Top section: background gradient + logo ──────────────────────
    private JPanel buildLogoSection() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background for logo area
                GradientPaint gp = new GradientPaint(
                        0, 0,           new Color(0x0A, 0x1C, 0x38),
                        0, getHeight(), new Color(0x0D, 0x2B, 0x52));
                g2.setPaint(gp);
                // Rounded only top corners
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() + 24, 24, 24));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(420, 240));

        SundaraLogo logo = LoginDialog.makeLogo();
        p.add(logo);
        return p;
    }

    /** Draws the Sundara logo inline — full version, medium size. */
    private static SundaraLogo makeLogo() {
        return new SundaraLogo(SundaraLogo.Style.FULL, 140, 1.0f) {
            { setPreferredSize(new Dimension(200, 220)); }
        };
    }

    // ── Form section ────────────────────────────────────────────────
    private JPanel buildFormSection() {
        JPanel p = new JPanel(null); // absolute layout for tight control
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(420, 240));

        int lx = 40, fw = 340;

        // Username
        JLabel uLbl = styled("👤  Username");
        uLbl.setBounds(lx, 10, fw, 20);

        userField = styledField(false);
        userField.setBounds(lx, 34, fw, 42);

        // Password
        JLabel pLbl = styled("🔒  Password");
        pLbl.setBounds(lx, 88, fw, 20);

        passField = new JPasswordField();
        styleField(passField);
        passField.setBounds(lx, 112, fw, 42);

        // Error label
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setForeground(Theme.BTN_DANGER);
        errorLabel.setBounds(lx, 162, fw, 20);

        // Login button
        JButton loginBtn = new JButton("  Masuk  →");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setBackground(Theme.BTN_PRIMARY);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        loginBtn.setFocusPainted(false);
        loginBtn.setOpaque(true);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setBounds(lx, 186, fw, 44);
        loginBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { loginBtn.setBackground(Theme.BTN_PRIMARY_H); }
            @Override public void mouseExited(MouseEvent e)  { loginBtn.setBackground(Theme.BTN_PRIMARY); }
        });
        loginBtn.addActionListener(e -> attemptLogin());

        // Make button rounded by overriding paint
        JButton roundBtn = new JButton("  Masuk  →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        roundBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roundBtn.setBackground(Theme.BTN_PRIMARY);
        roundBtn.setForeground(Color.WHITE);
        roundBtn.setBorderPainted(false);
        roundBtn.setFocusPainted(false);
        roundBtn.setContentAreaFilled(false);
        roundBtn.setOpaque(false);
        roundBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        roundBtn.setBounds(lx, 186, fw, 44);
        roundBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { roundBtn.setBackground(Theme.BTN_PRIMARY_H); }
            @Override public void mouseExited(MouseEvent e)  { roundBtn.setBackground(Theme.BTN_PRIMARY); }
        });
        roundBtn.addActionListener(e -> attemptLogin());

        p.add(uLbl);
        p.add(userField);
        p.add(pLbl);
        p.add(passField);
        p.add(errorLabel);
        p.add(roundBtn);
        return p;
    }

    // ── Footer ───────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(420, 42));
        p.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel hint = new JLabel("default: admin / 12345", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(new Color(0x44, 0x60, 0x80));
        p.add(hint, BorderLayout.CENTER);
        return p;
    }

    // ── Login logic ──────────────────────────────────────────────────
    private void attemptLogin() {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword()).trim();

        boolean success = false;
        try {
            success = DatabaseManager.verifyLogin(u, p);
        } catch (Exception e) {
            System.err.println("Gagal verifikasi login via Database (menggunakan fallback statis): " + e.getMessage());
            success = u.equals(USER) && p.equals(PASSWORD);
        }

        if (success) {
            authenticated = true;
            // Brief flash success
            errorLabel.setForeground(Theme.BTN_SUCCESS);
            errorLabel.setText("✓ Login berhasil...");
            Timer t = new Timer(400, e -> dispose());
            t.setRepeats(false);
            t.start();
        } else {
            errorLabel.setForeground(Theme.BTN_DANGER);
            errorLabel.setText("✗ Username atau password salah!");
            passField.setText("");
            // Shake animation
            shakeWindow();
        }
    }

    private void shakeWindow() {
        Point origin = getLocation();
        int[] offsets = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        Timer t = new Timer(30, null);
        int[] idx = {0};
        t.addActionListener(e -> {
            if (idx[0] < offsets.length) {
                setLocation(origin.x + offsets[idx[0]], origin.y);
                idx[0]++;
            } else {
                setLocation(origin);
                t.stop();
            }
        });
        t.start();
    }

    public boolean isAuthenticated() { return authenticated; }

    // ── Field styling helpers ────────────────────────────────────────
    private JTextField styledField(boolean isPass) {
        JTextField tf = isPass ? new JPasswordField() : new JTextField();
        styleField(tf);
        return tf;
    }

    private void styleField(JTextField tf) {
        tf.setBackground(new Color(0x0A, 0x1C, 0x38));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_FOCUS, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        // Focus highlight
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BTN_PRIMARY, 2, true),
                        new EmptyBorder(9, 13, 9, 13)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BORDER_FOCUS, 1, true),
                        new EmptyBorder(10, 14, 10, 14)
                ));
            }
        });
    }

    private JLabel styled(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    // ── Drag support for undecorated window ──────────────────────────
    static class DragListener extends MouseAdapter {
        private final JDialog dialog;
        private Point startPoint;

        DragListener(JDialog d) { this.dialog = d; }

        @Override public void mousePressed(MouseEvent e)  { startPoint = e.getPoint(); }
        @Override public void mouseDragged(MouseEvent e) {
            if (startPoint == null) return;
            Point loc = dialog.getLocation();
            dialog.setLocation(loc.x + e.getX() - startPoint.x,
                               loc.y + e.getY() - startPoint.y);
        }
    }
}
