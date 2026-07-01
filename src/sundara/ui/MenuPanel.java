package sundara.ui;

import sundara.data.MenuData;
import sundara.model.Category;
import sundara.model.MenuItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

public class MenuPanel extends JPanel {

    private Category activeCategory = Category.SIGNATURE;
    private String   searchQuery    = "";
    private final java.util.function.Consumer<MenuItem> onAddItem;

    private final JPanel gridPanel;
    private final JPanel tabBar;

    public MenuPanel(java.util.function.Consumer<MenuItem> onAddItem) {
        this.onAddItem = onAddItem;
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        // ── Search bar ───────────────────────────────────────────────
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_SECONDARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(Theme.FONT_EMOJI_SM);
        searchPanel.add(searchIcon, BorderLayout.WEST);

        JTextField searchField = new JTextField();
        searchField.setBackground(Theme.BG_ACCENT);
        searchField.setForeground(Theme.TEXT_PRIMARY);
        searchField.setCaretColor(Color.WHITE);
        searchField.setFont(Theme.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Cari menu...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                searchQuery = searchField.getText().trim().toLowerCase();
                refreshGrid();
            }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("🔄") {
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
        refreshBtn.setFont(Theme.FONT_BTN);
        refreshBtn.setBackground(Theme.BG_ACCENT);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { refreshBtn.setBackground(Theme.BTN_PRIMARY); }
            @Override public void mouseExited(MouseEvent e) { refreshBtn.setBackground(Theme.BG_ACCENT); }
        });
        refreshBtn.addActionListener(e -> {
            refreshGrid();
            JOptionPane.showMessageDialog(MenuPanel.this, "Menu berhasil diperbarui dari database!", "Refresh Sukses", JOptionPane.INFORMATION_MESSAGE);
        });
        searchPanel.add(refreshBtn, BorderLayout.EAST);

        // ── Category tab bar ─────────────────────────────────────────
        tabBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_SECONDARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        tabBar.setOpaque(false);
        tabBar.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 6));
        tabBar.setBorder(new EmptyBorder(0, 8, 0, 8));

        for (Category cat : Category.values()) {
            JButton tab = buildTabButton(cat);
            tabBar.add(tab);
        }

        JScrollPane tabScroll = new JScrollPane(tabBar,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tabScroll.setBorder(null);
        tabScroll.setOpaque(false);
        tabScroll.getViewport().setOpaque(false);
        tabScroll.getHorizontalScrollBar().setUnitIncrement(16);
        styleScrollBar(tabScroll.getHorizontalScrollBar());

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setOpaque(false);
        topArea.add(searchPanel, BorderLayout.NORTH);
        topArea.add(tabScroll, BorderLayout.CENTER);

        // ── Menu grid ────────────────────────────────────────────────
        gridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 8, 8));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Watermark layer — transparent to let cover background show
        JPanel watermarkLayer = new JPanel(new BorderLayout());
        watermarkLayer.setOpaque(false);
        watermarkLayer.add(gridPanel, BorderLayout.CENTER);

        JScrollPane gridScroll = new JScrollPane(watermarkLayer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gridScroll.setBorder(null);
        gridScroll.setOpaque(false);
        gridScroll.getViewport().setOpaque(false);
        gridScroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(gridScroll.getVerticalScrollBar());

        add(topArea, BorderLayout.NORTH);
        add(gridScroll, BorderLayout.CENTER);

        refreshGrid();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.BG_PRIMARY);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private JButton buildTabButton(Category cat) {
        boolean active = cat == activeCategory;
        JButton btn = new JButton(cat.getDisplayName());
        btn.setFont(Theme.FONT_SMALL);
        btn.setBackground(active ? Theme.TAB_ACTIVE : Theme.TAB_INACTIVE);
        btn.setForeground(Theme.TEXT_PRIMARY);
        btn.setBorder(new EmptyBorder(5, 10, 5, 10));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (cat != activeCategory)
                    btn.setBackground(Theme.BG_ACCENT);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(cat == activeCategory ? Theme.TAB_ACTIVE : Theme.TAB_INACTIVE);
            }
        });
        btn.addActionListener(e -> {
            activeCategory = cat;
            refreshTabColors();
            refreshGrid();
        });
        return btn;
    }

    private void refreshTabColors() {
        for (Component c : tabBar.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                String label = b.getText();
                boolean active = label.equals(activeCategory.getDisplayName());
                b.setBackground(active ? Theme.TAB_ACTIVE : Theme.TAB_INACTIVE);
            }
        }
    }

    public void refreshGrid() {
        gridPanel.removeAll();

        List<MenuItem> filtered = MenuData.getAllItems().stream()
                .filter(item -> searchQuery.isEmpty() ? (item.getCategory() == activeCategory) : true)
                .filter(item -> searchQuery.isEmpty()
                        || item.getName().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            JLabel empty = new JLabel("😔 Menu tidak ditemukan");
            empty.setForeground(Theme.TEXT_SECONDARY);
            empty.setFont(Theme.FONT_BODY);
            gridPanel.add(empty);
        } else {
            for (MenuItem item : filtered) {
                MenuItemCard card = new MenuItemCard(item, () -> onAddItem.accept(item));
                gridPanel.add(card);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setBackground(Theme.BG_PRIMARY);
        bar.setForeground(Theme.BG_ACCENT);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor       = Theme.BG_ACCENT;
                trackColor       = Theme.BG_PRIMARY;
                thumbHighlightColor = Theme.BTN_PRIMARY;
            }
            @Override protected JButton createDecreaseButton(int o) { return invisibleBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return invisibleBtn(); }
            private JButton invisibleBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    // ── Simple WrapLayout ────────────────────────────────────────────
    static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();

                // If target is inside a scroll pane viewport, get the viewport width
                Container parent = target.getParent();
                while (parent != null && !(parent instanceof JViewport)) {
                    parent = parent.getParent();
                }
                if (parent instanceof JViewport) {
                    targetWidth = parent.getWidth();
                }

                if (targetWidth == 0) {
                    targetWidth = 800; // reasonable default width
                }

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        if (rowWidth > 0) {
                            rowWidth += hgap;
                        }
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                addRow(dim, rowWidth, rowHeight);

                dim.width = targetWidth; // use full available viewport width
                dim.height += insets.top + insets.bottom + (vgap * 2);
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) {
                dim.height += getVgap();
            }
            dim.height += rowHeight;
        }

        @Override
        public void layoutContainer(Container target) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) {
                    super.layoutContainer(target);
                    return;
                }

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                int nmembers = target.getComponentCount();
                int x = insets.left + hgap;
                int y = insets.top + vgap;
                int rowHeight = 0;

                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = m.getPreferredSize();
                        m.setSize(d.width, d.height);

                        if (x + d.width > maxWidth) {
                            x = insets.left + hgap;
                            y += rowHeight + vgap;
                            rowHeight = 0;
                        }

                        m.setLocation(x, y);

                        x += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
            }
        }
    }
}
