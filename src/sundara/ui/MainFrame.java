package sundara.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import sundara.data.DatabaseManager;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("☕ Sundara CoffeeSpace — Kasir");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 640));
        setSize(1200, 760);
        setLocationRelativeTo(null);

        // Apply dark title bar feeling via background panel
        BackgroundPanel bgPanel = new BackgroundPanel(SundaraLogo.getLogoImage());
        setContentPane(bgPanel);

        // ── Main content ─────────────────────────────────────────────
        OrderPanel orderPanel = new OrderPanel();
        MenuPanel  menuPanel  = new MenuPanel(orderPanel::addItem);

        // ── Header ───────────────────────────────────────────────────
        JPanel header = buildHeader(menuPanel);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPanel, orderPanel);
        split.setDividerLocation(860);
        split.setResizeWeight(1.0);
        split.setDividerSize(4);
        split.setBorder(null);
        split.setOpaque(false);
        split.setBackground(Theme.BORDER);

        add(header, BorderLayout.NORTH);
        add(split,  BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel buildHeader(MenuPanel menuPanel) {
        JPanel p = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_SECONDARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        // Logo area (West)
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoArea.setOpaque(false);

        JLabel logoEmoji = new JLabel("☕");
        logoEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));

        JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 0));
        textBlock.setOpaque(false);

        JLabel brandName = new JLabel("Sundara CoffeeSpace");
        brandName.setFont(Theme.FONT_TITLE);
        brandName.setForeground(Theme.TEXT_ORANGE);

        JLabel tagline = new JLabel("Point of Sale System  •  2025");
        tagline.setFont(Theme.FONT_SMALL);
        tagline.setForeground(Theme.TEXT_SECONDARY);

        textBlock.add(brandName);
        textBlock.add(tagline);

        logoArea.add(logoEmoji);
        logoArea.add(textBlock);

        // Center Area: Real-time clock widget
        JPanel centerArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        centerArea.setOpaque(false);

        JLabel clockLabel = new JLabel();
        clockLabel.setFont(Theme.FONT_SUBTITLE);
        clockLabel.setForeground(Color.WHITE);
        
        Timer clockTimer = new Timer(1000, ev -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  •  HH:mm:ss", new java.util.Locale("id", "ID"));
            clockLabel.setText(now.format(dtf));
        });
        clockTimer.start();
        clockLabel.setText(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  •  HH:mm:ss", new java.util.Locale("id", "ID"))));
        centerArea.add(clockLabel);

        // Right side status & action buttons (East)
        JPanel rightArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 4));
        rightArea.setOpaque(false);

        JLabel statusDot = new JLabel("● OPEN");
        statusDot.setFont(Theme.FONT_SMALL);
        statusDot.setForeground(Theme.BTN_SUCCESS);

        JLabel dbStatusLabel = new JLabel("● DB ONLINE");
        dbStatusLabel.setFont(Theme.FONT_SMALL);
        dbStatusLabel.setForeground(Theme.BTN_SUCCESS);
        
        Timer dbCheckTimer = new Timer(5000, ev -> {
            boolean connected = DatabaseManager.checkConnection();
            if (connected) {
                dbStatusLabel.setText("● DB ONLINE");
                dbStatusLabel.setForeground(Theme.BTN_SUCCESS);
            } else {
                dbStatusLabel.setText("● DB OFFLINE");
                dbStatusLabel.setForeground(Theme.BTN_DANGER);
            }
        });
        dbCheckTimer.start();
        boolean initDb = DatabaseManager.checkConnection();
        dbStatusLabel.setText(initDb ? "● DB ONLINE" : "● DB OFFLINE");
        dbStatusLabel.setForeground(initDb ? Theme.BTN_SUCCESS : Theme.BTN_DANGER);

        JButton archiveBtn = new JButton("Arsip Penjualan 🧾") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        archiveBtn.setFont(Theme.FONT_SMALL);
        archiveBtn.setBackground(Theme.BG_ACCENT);
        archiveBtn.setForeground(Color.WHITE);
        archiveBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        archiveBtn.setFocusPainted(false);
        archiveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        archiveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { archiveBtn.setBackground(Theme.BTN_PRIMARY); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { archiveBtn.setBackground(Theme.BG_ACCENT); }
        });
        archiveBtn.addActionListener(e -> {
            ArchiveDialog dialog = new ArchiveDialog(MainFrame.this);
            dialog.setVisible(true);
        });

        JButton adminBtn = new JButton("Pengaturan Admin ⚙️") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        adminBtn.setFont(Theme.FONT_SMALL);
        adminBtn.setBackground(Theme.BG_ACCENT);
        adminBtn.setForeground(Color.WHITE);
        adminBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        adminBtn.setFocusPainted(false);
        adminBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        adminBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { adminBtn.setBackground(Theme.BTN_PRIMARY); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { adminBtn.setBackground(Theme.BG_ACCENT); }
        });
        adminBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(MainFrame.this, 
                "Masukkan Password Admin untuk mengakses pengaturan:", 
                "Otorisasi Admin Required", JOptionPane.QUESTION_MESSAGE);
            if (input == null) return;
            if (input.equals("12345")) {
                AdminSettingsDialog dialog = new AdminSettingsDialog(MainFrame.this, menuPanel::refreshGrid);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(MainFrame.this, 
                    "Password salah! Akses ditolak.", 
                    "Error Otorisasi", JOptionPane.ERROR_MESSAGE);
            }
        });

        rightArea.add(statusDot);
        rightArea.add(dbStatusLabel);
        rightArea.add(archiveBtn);
        rightArea.add(adminBtn);

        p.add(logoArea,  BorderLayout.WEST);
        p.add(centerArea, BorderLayout.CENTER);
        p.add(rightArea, BorderLayout.EAST);
        return p;
    }

    /**
     * Custom panel that renders a blue gradient background and draws logo.jpg 
     * centered and stretched to cover the entire screen with low opacity.
     */
    static class BackgroundPanel extends JPanel {
        private final java.awt.image.BufferedImage image;

        public BackgroundPanel(java.awt.image.BufferedImage img) {
            this.image = img;
            setLayout(new BorderLayout());
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            // Draw a deep blue gradient background
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(0x06, 0x11, 0x24),
                0, getHeight(), new Color(0x0C, 0x1E, 0x3D)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Cover style scaling of the background logo.jpg
            if (image != null) {
                int imgW = image.getWidth();
                int imgH = image.getHeight();
                
                double panelRatio = (double) getWidth() / getHeight();
                double imgRatio = (double) imgW / imgH;
                
                int drawW, drawH;
                if (panelRatio > imgRatio) {
                    drawW = getWidth();
                    drawH = (int) (drawW / imgRatio);
                } else {
                    drawH = getHeight();
                    drawW = (int) (drawH * imgRatio);
                }
                
                int x = (getWidth() - drawW) / 2;
                int y = (getHeight() - drawH) / 2;
                
                // Draw logo with 0.08 alpha for a very subtle watermark
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                g2.drawImage(image, x, y, drawW, drawH, null);
            }
            
            g2.dispose();
        }
    }
}
