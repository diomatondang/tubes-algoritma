package sundara.ui;

import sundara.model.MenuItem;
import sundara.model.OrderItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {

    private final List<OrderItem> orderItems = new ArrayList<>();
    private final JPanel    itemsPanel;
    private final JLabel    totalLabel;
    private final JTextField customerField;
    private final JTextField tableField;

    public OrderPanel() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setPreferredSize(new Dimension(320, 0));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BORDER));

        // ── Header ───────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_ACCENT);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel headerIcon = new JLabel("🛒");
        headerIcon.setFont(Theme.FONT_EMOJI_SM);
        JLabel headerTitle = new JLabel("Pesanan");
        headerTitle.setFont(Theme.FONT_SUBTITLE);
        headerTitle.setForeground(Theme.TEXT_PRIMARY);
        header.add(headerIcon, BorderLayout.WEST);
        header.add(headerTitle, BorderLayout.CENTER);

        // ── Customer & table info ────────────────────────────────────
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 8, 6));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(10, 12, 6, 12));

        JLabel custLbl  = makeLabel("👤 Nama:");
        JLabel tableLbl = makeLabel("🪑 Meja:");
        customerField   = makeTextField("(opsional)");
        tableField      = makeTextField("No. Meja");

        infoPanel.add(custLbl);
        infoPanel.add(customerField);
        infoPanel.add(tableLbl);
        infoPanel.add(tableField);

        // ── Order items list ─────────────────────────────────────────
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);
        itemsPanel.setBorder(new EmptyBorder(4, 8, 4, 8));

        JScrollPane scroll = new JScrollPane(itemsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        styleScrollBar(scroll.getVerticalScrollBar());

        // ── Total panel ──────────────────────────────────────────────
        JPanel totalPanel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_ACCENT);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        totalPanel.setOpaque(false);
        totalPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel totalLbl = new JLabel("TOTAL");
        totalLbl.setFont(Theme.FONT_SUBTITLE);
        totalLbl.setForeground(Theme.TEXT_SECONDARY);

        totalLabel = new JLabel("Rp 0");
        totalLabel.setFont(Theme.FONT_TOTAL);
        totalLabel.setForeground(Theme.TEXT_ORANGE);

        totalPanel.add(totalLbl,  BorderLayout.WEST);
        totalPanel.add(totalLabel, BorderLayout.EAST);

        // ── Action buttons ───────────────────────────────────────────
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(new EmptyBorder(10, 12, 14, 12));

        JButton clearBtn = makeButton("🗑️ Reset", Theme.BTN_DANGER, Theme.BTN_DANGER_H);
        JButton payBtn   = makeButton("💳 Bayar",  Theme.BTN_SUCCESS, Theme.BTN_SUCCESS_H);

        clearBtn.addActionListener(e -> clearOrder());
        payBtn.addActionListener(e -> processPayment());

        btnPanel.add(clearBtn);
        btnPanel.add(payBtn);

        // ── Empty state label ────────────────────────────────────────
        JLabel emptyLbl = new JLabel("<html><center>Belum ada pesanan.<br/>Klik menu untuk menambah.</center></html>",
                SwingConstants.CENTER);
        emptyLbl.setFont(Theme.FONT_BODY);
        emptyLbl.setForeground(Theme.TEXT_SECONDARY);
        emptyLbl.setBorder(new EmptyBorder(30, 10, 30, 10));
        emptyLbl.setName("emptyLabel");
        itemsPanel.add(emptyLbl);

        // ── Assembly ─────────────────────────────────────────────────
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(infoPanel, BorderLayout.CENTER);

        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.setOpaque(false);
        bottomSection.add(totalPanel, BorderLayout.NORTH);
        bottomSection.add(btnPanel,   BorderLayout.CENTER);

        add(topSection,    BorderLayout.NORTH);
        add(scroll,        BorderLayout.CENTER);
        add(bottomSection, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.BG_SECONDARY);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    // ── Public method called from MenuPanel ──────────────────────────
    public void addItem(MenuItem menuItem) {
        for (OrderItem oi : orderItems) {
            if (oi.getMenuItem().getName().equals(menuItem.getName())) {
                oi.incrementQty();
                refreshItemsPanel();
                return;
            }
        }
        orderItems.add(new OrderItem(menuItem));
        refreshItemsPanel();
    }

    // ── Build one row for an order item ──────────────────────────────
    private void refreshItemsPanel() {
        itemsPanel.removeAll();

        if (orderItems.isEmpty()) {
            JLabel emptyLbl = new JLabel(
                    "<html><center>Belum ada pesanan.<br/>Klik menu untuk menambah.</center></html>",
                    SwingConstants.CENTER);
            emptyLbl.setFont(Theme.FONT_BODY);
            emptyLbl.setForeground(Theme.TEXT_SECONDARY);
            emptyLbl.setBorder(new EmptyBorder(30, 10, 30, 10));
            itemsPanel.add(emptyLbl);
        } else {
            for (int i = 0; i < orderItems.size(); i++) {
                OrderItem oi = orderItems.get(i);
                itemsPanel.add(buildOrderRow(oi, i));
                itemsPanel.add(Box.createVerticalStrut(4));
            }
        }

        updateTotal();
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel buildOrderRow(OrderItem oi, int index) {
        JPanel row = new JPanel(new BorderLayout(6, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 12, 12));
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 10, 8, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // Left: emoji + name
        JPanel leftP = new JPanel(new BorderLayout(4, 0));
        leftP.setOpaque(false);

        JLabel emojiLbl = new JLabel(oi.getMenuItem().getEmoji());
        emojiLbl.setFont(Theme.FONT_EMOJI_SM);

        JLabel nameLbl = new JLabel("<html><b>" + oi.getMenuItem().getName() + "</b></html>");
        nameLbl.setFont(Theme.FONT_SMALL);
        nameLbl.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtotalLbl = new JLabel(oi.getFormattedSubtotal());
        subtotalLbl.setFont(Theme.FONT_SMALL);
        subtotalLbl.setForeground(Theme.TEXT_PRICE);

        JPanel nameBlock = new JPanel(new GridLayout(2, 1, 0, 0));
        nameBlock.setOpaque(false);
        nameBlock.add(nameLbl);
        nameBlock.add(subtotalLbl);

        leftP.add(emojiLbl, BorderLayout.WEST);
        leftP.add(nameBlock, BorderLayout.CENTER);

        // Right: qty controls + delete
        JPanel rightP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        rightP.setOpaque(false);

        JButton minusBtn = makeSmallBtn("−");
        JButton plusBtn  = makeSmallBtn("+");
        JButton delBtn   = makeSmallBtn("✕");
        delBtn.setBackground(Theme.BTN_DANGER);

        JLabel qtyLbl = new JLabel(String.valueOf(oi.getQuantity()), SwingConstants.CENTER);
        qtyLbl.setFont(Theme.FONT_SUBTITLE);
        qtyLbl.setForeground(Theme.TEXT_PRIMARY);
        qtyLbl.setPreferredSize(new Dimension(26, 26));

        minusBtn.addActionListener(e -> {
            oi.decrementQty();
            refreshItemsPanel();
        });
        plusBtn.addActionListener(e -> {
            oi.incrementQty();
            refreshItemsPanel();
        });
        delBtn.addActionListener(e -> {
            orderItems.remove(index);
            refreshItemsPanel();
        });

        rightP.add(minusBtn);
        rightP.add(qtyLbl);
        rightP.add(plusBtn);
        rightP.add(Box.createHorizontalStrut(4));
        rightP.add(delBtn);

        row.add(leftP,  BorderLayout.CENTER);
        row.add(rightP, BorderLayout.EAST);
        return row;
    }

    private void updateTotal() {
        int total = orderItems.stream().mapToInt(OrderItem::getSubtotal).sum();
        totalLabel.setText(String.format("Rp %,d", total * 1000).replace(',', '.'));
    }

    private int getTotal() {
        return orderItems.stream().mapToInt(OrderItem::getSubtotal).sum();
    }

    private void clearOrder() {
        if (orderItems.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin hapus semua pesanan?", "Konfirmasi Reset",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            orderItems.clear();
            refreshItemsPanel();
        }
    }

    private void processPayment() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Belum ada pesanan!", "Perhatian",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String customer = customerField.getText().trim();
        String table    = tableField.getText().trim();

        PaymentDialog dialog = new PaymentDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                orderItems, getTotal(), customer, table);
        dialog.setVisible(true);

        if (dialog.isPaid()) {
            orderItems.clear();
            customerField.setText("");
            tableField.setText("");
            refreshItemsPanel();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setBackground(Theme.BG_CARD);
        tf.setForeground(Theme.TEXT_PRIMARY);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(Theme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        return tf;
    }

    private JButton makeButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private JButton makeSmallBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BTN);
        btn.setBackground(Theme.BG_ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(26, 26));
        btn.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = Theme.BG_ACCENT;
                trackColor = Theme.BG_SECONDARY;
            }
            @Override protected JButton createDecreaseButton(int o) { return invis(); }
            @Override protected JButton createIncreaseButton(int o) { return invis(); }
            private JButton invis() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
        });
    }
}
