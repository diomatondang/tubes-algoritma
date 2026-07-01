package sundara.ui;

import sundara.model.OrderItem;
import sundara.data.DatabaseManager;
import sundara.data.ReceiptSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Payment dialog with Cash / QRIS / Transfer / Debit-Credit methods.
 * Shows a receipt window after successful payment.
 */
public class PaymentDialog extends JDialog {

    // ── Payment methods ──────────────────────────────────────────────
    private enum PayMethod { CASH, QRIS, TRANSFER, CARD }

    private boolean    paid       = false;
    private PayMethod  payMethod  = PayMethod.CASH;
    private final int  totalAmount;          // in thousands (Rp)
    private final List<OrderItem> items;
    private final String customer, table;

    // promo / discount state
    private String     appliedPromoCode = "";
    private int        discountAmount   = 0; // in thousands (Rp)
    private int        finalAmount;          // in thousands (Rp) after discount
    private int        discountPercent  = 0;

    private final JLabel discountValLabel = new JLabel("Rp 0");
    private final JLabel totalValLabel    = new JLabel();

    // dynamic UI refs
    private JLabel     changeLabel;
    private JTextField cashField;
    private JPanel     cashInputPanel;
    private JPanel     nonCashPanel;
    private String     txnRef = "";          // for QRIS/Transfer ref number

    // ── Constructor ──────────────────────────────────────────────────
    public PaymentDialog(JFrame parent, List<OrderItem> items,
                         int totalAmount, String customer, String table) {
        super(parent, "Pembayaran — Sundara CoffeeSpace", true);
        this.items       = items;
        this.totalAmount = totalAmount;
        this.finalAmount = totalAmount;
        this.customer    = customer;
        this.table       = table;

        setSize(520, 680);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(Theme.BG_SECONDARY);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── HEADER ───────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Theme.BG_ACCENT);
        p.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("☕  Sundara CoffeeSpace  —  Pembayaran");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_ORANGE);

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        meta.setOpaque(false);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm"));
        meta.add(small("🕐 " + ts));
        if (!customer.isEmpty()) meta.add(small("👤 " + customer));
        if (!table.isEmpty())    meta.add(small("🪑 Meja " + table));

        p.add(title, BorderLayout.NORTH);
        p.add(meta,  BorderLayout.CENTER);
        return p;
    }

    // ── BODY ─────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setBackground(Theme.BG_SECONDARY);
        outer.setBorder(new EmptyBorder(10, 14, 6, 14));

        outer.add(buildOrderTable(),   BorderLayout.CENTER);
        outer.add(buildPaySection(),   BorderLayout.SOUTH);
        return outer;
    }

    // ── ORDER TABLE ───────────────────────────────────────────────────
    private JScrollPane buildOrderTable() {
        JPanel table = new JPanel();
        table.setLayout(new BoxLayout(table, BoxLayout.Y_AXIS));
        table.setBackground(Theme.BG_CARD);
        table.setBorder(new EmptyBorder(6, 8, 6, 8));

        table.add(tableRow("Item", "Qty", "Harga", "Subtotal", true));
        table.add(divider());

        for (OrderItem oi : items) {
            String name = oi.getMenuItem().getName();
            if (name.length() > 22) name = name.substring(0, 19) + "...";
            table.add(tableRow(
                oi.getMenuItem().getEmoji() + " " + name,
                String.valueOf(oi.getQuantity()),
                oi.getMenuItem().getFormattedPrice(),
                oi.getFormattedSubtotal(),
                false
            ));
        }

        table.add(divider());

        // Subtotal / tax / total rows
        totalValLabel.setText(fmt(totalAmount));
        table.add(summaryRow("Subtotal", fmt(totalAmount), false));
        table.add(createSummaryRow("Diskon Promo", discountValLabel, false));
        table.add(summaryRow("Pajak / Tax", "Rp 0", false));
        table.add(createSummaryRow("TOTAL", totalValLabel, true));

        JScrollPane sp = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.setBackground(Theme.BG_CARD);
        sp.getViewport().setBackground(Theme.BG_CARD);
        sp.getVerticalScrollBar().setUnitIncrement(14);
        sp.setPreferredSize(new Dimension(490, 240));
        return sp;
    }

    // ── PAYMENT METHOD SECTION ────────────────────────────────────────
    private JPanel buildPaySection() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.BG_SECONDARY);

        // Method selector tabs
        JPanel tabs = new JPanel(new GridLayout(1, 4, 6, 0));
        tabs.setBackground(Theme.BG_SECONDARY);

        String[][] methods = {
            {"💵", "Cash",     "CASH"},
            {"📱", "QRIS",     "QRIS"},
            {"🏦", "Transfer", "TRANSFER"},
            {"💳", "Debit/CC", "CARD"}
        };

        ButtonGroup bg = new ButtonGroup();
        for (String[] m : methods) {
            JToggleButton tb = buildMethodTab(m[0], m[1], PayMethod.valueOf(m[2]));
            bg.add(tb);
            tabs.add(tb);
            if (m[2].equals("CASH")) tb.setSelected(true);
        }

        // Promo bar
        JPanel promoBar = new JPanel(new BorderLayout(8, 0));
        promoBar.setOpaque(false);
        promoBar.setBorder(new EmptyBorder(4, 0, 4, 0));

        JTextField promoField = new JTextField();
        promoField.setBackground(Theme.BG_CARD);
        promoField.setForeground(Color.WHITE);
        promoField.setCaretColor(Color.WHITE);
        promoField.setFont(Theme.FONT_BODY);
        promoField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        promoField.putClientProperty("JTextField.placeholderText", "Masukkan Kode Promo (e.g. SUNDARA10)");

        JButton applyPromoBtn = new JButton("Terapkan 🎟️") {
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
        applyPromoBtn.setFont(Theme.FONT_BTN);
        applyPromoBtn.setBackground(Theme.BG_ACCENT);
        applyPromoBtn.setForeground(Color.WHITE);
        applyPromoBtn.setBorder(new EmptyBorder(5, 12, 5, 12));
        applyPromoBtn.setFocusPainted(false);
        applyPromoBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyPromoBtn.addActionListener(ev -> {
            String code = promoField.getText().trim();
            if (code.isEmpty()) {
                appliedPromoCode = "";
                discountPercent = 0;
                discountAmount = 0;
                finalAmount = totalAmount;
                updateOrderSummaryTable();
                computeChange();
                return;
            }

            try {
                Integer pct = DatabaseManager.getPromoDiscount(code);
                if (pct != null) {
                    appliedPromoCode = code.toUpperCase();
                    discountPercent = pct;
                    discountAmount = (int) Math.round((double) totalAmount * pct / 100.0);
                    finalAmount = totalAmount - discountAmount;
                    if (finalAmount < 0) finalAmount = 0;

                    JOptionPane.showMessageDialog(PaymentDialog.this, 
                        "Promo '" + appliedPromoCode + "' berhasil diterapkan! Diskon " + pct + "% (Rp " + (discountAmount * 1000) + ")", 
                        "Promo Sukses", JOptionPane.INFORMATION_MESSAGE);

                    updateOrderSummaryTable();
                    computeChange();
                } else {
                    JOptionPane.showMessageDialog(PaymentDialog.this, 
                        "Kode promo tidak valid!", 
                        "Promo Gagal", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                System.err.println("Gagal memvalidasi promo: " + ex.getMessage());
            }
        });

        promoBar.add(promoField, BorderLayout.CENTER);
        promoBar.add(applyPromoBtn, BorderLayout.EAST);

        // Switchable input panels
        cashInputPanel = buildCashPanel();
        nonCashPanel   = buildNonCashPanel();
        nonCashPanel.setVisible(false);

        JPanel inputArea = new JPanel(new CardLayout());
        inputArea.setBackground(Theme.BG_SECONDARY);
        inputArea.add(cashInputPanel, "CASH");
        inputArea.add(nonCashPanel,   "NONCASH");

        // Store reference so tabs can swap panels
        for (Component c : tabs.getComponents()) {
            if (c instanceof JToggleButton) {
                JToggleButton tb = (JToggleButton) c;
                tb.addActionListener(e -> {
                    CardLayout cl = (CardLayout) inputArea.getLayout();
                    if (payMethod == PayMethod.CASH) {
                        cl.show(inputArea, "CASH");
                    } else {
                        nonCashPanel.removeAll();
                        nonCashPanel.add(buildNonCashContent(), BorderLayout.CENTER);
                        nonCashPanel.revalidate();
                        cl.show(inputArea, "NONCASH");
                    }
                });
            }
        }

        JPanel lowerPanel = new JPanel(new BorderLayout(0, 4));
        lowerPanel.setOpaque(false);
        lowerPanel.add(promoBar, BorderLayout.NORTH);
        lowerPanel.add(inputArea, BorderLayout.CENTER);

        p.add(tabs,       BorderLayout.NORTH);
        p.add(lowerPanel, BorderLayout.CENTER);
        return p;
    }

    private JToggleButton buildMethodTab(String emoji, String label, PayMethod method) {
        JToggleButton tb = new JToggleButton("<html><center>" + emoji + "<br><small>" + label + "</small></center></html>") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? Theme.BTN_PRIMARY : Theme.BG_ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                if (isSelected()) {
                    g2.setColor(new Color(0xFF, 0xFF, 0xFF, 40));
                    g2.fillRect(0, 0, getWidth(), getHeight() / 2);
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        tb.setFont(Theme.FONT_SMALL);
        tb.setForeground(Color.WHITE);
        tb.setBackground(Theme.BG_ACCENT);
        tb.setBorderPainted(false);
        tb.setFocusPainted(false);
        tb.setContentAreaFilled(false);
        tb.setOpaque(false);
        tb.setPreferredSize(new Dimension(110, 52));
        tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tb.addItemListener(e -> {
            if (tb.isSelected()) payMethod = method;
        });
        return tb;
    }

    // ── CASH INPUT ────────────────────────────────────────────────────
    private JPanel buildCashPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG_SECONDARY);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel cashLbl = small("💵  Jumlah Bayar (Rp) :");
        cashField = new JTextField();
        cashField.setBackground(Theme.BG_CARD);
        cashField.setForeground(Color.WHITE);
        cashField.setCaretColor(Color.WHITE);
        cashField.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cashField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_FOCUS, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        cashField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { computeChange(); }
        });

        changeLabel = new JLabel("Masukkan nominal uang tunai");
        changeLabel.setFont(Theme.FONT_SUBTITLE);
        changeLabel.setForeground(Theme.TEXT_SECONDARY);

        // Quick cash buttons
        JPanel quick = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quick.setOpaque(false);
        int[] vals = {5000, 10000, 20000, 50000, 100000, 150000, 200000};
        for (int v : vals) {
            JButton qb = quickBtn(fmtShort(v), v);
            quick.add(qb);
        }
        // "Uang Pas" button
        JButton exactBtn = new JButton("Pas ✓");
        exactBtn.setFont(Theme.FONT_SMALL);
        exactBtn.setBackground(Theme.BTN_ORANGE);
        exactBtn.setForeground(Color.WHITE);
        exactBtn.setBorder(new EmptyBorder(5, 10, 5, 10));
        exactBtn.setFocusPainted(false);
        exactBtn.setOpaque(true);
        exactBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exactBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { exactBtn.setBackground(Theme.BTN_PRIMARY); }
            @Override public void mouseExited (MouseEvent e) { exactBtn.setBackground(Theme.BTN_ORANGE); }
        });
        exactBtn.addActionListener(e -> {
            cashField.setText(String.valueOf((long) finalAmount * 1000));
            computeChange();
        });
        quick.add(exactBtn);

        gc.gridx=0; gc.gridy=0; gc.gridwidth=1; gc.weightx=0.3; p.add(cashLbl,     gc);
        gc.gridx=1; gc.gridy=0; gc.gridwidth=2; gc.weightx=0.7; p.add(cashField,   gc);
        gc.gridx=0; gc.gridy=1; gc.gridwidth=1; gc.weightx=0;   p.add(small("💰  Kembalian :"), gc);
        gc.gridx=1; gc.gridy=1; gc.gridwidth=2; gc.weightx=1;   p.add(changeLabel, gc);
        gc.gridx=0; gc.gridy=2; gc.gridwidth=3;                  p.add(quick,       gc);
        return p;
    }

    private JButton quickBtn(String label, int rupiah) {
        JButton b = new JButton(label);
        b.setFont(Theme.FONT_SMALL);
        b.setBackground(Theme.BG_ACCENT);
        b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(5, 10, 5, 10));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(Theme.BTN_PRIMARY); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(Theme.BG_ACCENT); }
        });
        b.addActionListener(e -> {
            cashField.setText(String.valueOf(rupiah));
            computeChange();
        });
        return b;
    }

    private void computeChange() {
        String raw = cashField.getText().replaceAll("[^0-9]", "");
        if (raw.isEmpty()) {
            changeLabel.setText("Masukkan nominal uang tunai");
            changeLabel.setForeground(Theme.TEXT_SECONDARY);
            return;
        }
        long cash  = Long.parseLong(raw);
        long total = (long) finalAmount * 1000;
        if (cash < total) {
            changeLabel.setText("Kurang  " + fmtL(total - cash));
            changeLabel.setForeground(Theme.BTN_DANGER);
        } else {
            changeLabel.setText("Kembalian  " + fmtL(cash - total));
            changeLabel.setForeground(Theme.BTN_SUCCESS);
        }
    }

    // ── NON-CASH PANEL (QRIS / Transfer / Card) ───────────────────────
    private JPanel buildNonCashPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.BG_SECONDARY);
        p.setBorder(new EmptyBorder(10, 0, 0, 0));
        p.add(buildNonCashContent(), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildNonCashContent() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG_SECONDARY);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        String icon, info1, info2;
        switch (payMethod) {
            case QRIS:
                icon  = "📱";
                info1 = "Scan QR Code di kasir";
                info2 = "Bank / e-Wallet apapun diterima";
                break;
            case TRANSFER:
                icon  = "🏦";
                info1 = "BCA  123-456-7890  a/n Sundara CS";
                info2 = "Konfirmasi ke kasir setelah transfer";
                break;
            default: // CARD
                icon  = "💳";
                info1 = "Kartu Debit / Kredit — EDC Tersedia";
                info2 = "Visa, Mastercard, GPN diterima";
        }

        // Icon + info box
        JPanel infoBox = new JPanel(new BorderLayout(12, 0));
        infoBox.setBackground(Theme.BG_CARD);
        infoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_FOCUS, 1, true),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 38));
        iconLbl.setPreferredSize(new Dimension(56, 56));

        JPanel textBox = new JPanel(new GridLayout(3, 1, 0, 2));
        textBox.setOpaque(false);
        JLabel l1 = new JLabel("Metode: " + payMethod.name()); l1.setFont(Theme.FONT_BTN); l1.setForeground(Theme.TEXT_ORANGE);
        JLabel l2 = new JLabel(info1); l2.setFont(Theme.FONT_BODY); l2.setForeground(Theme.TEXT_PRIMARY);
        JLabel l3 = new JLabel(info2); l3.setFont(Theme.FONT_SMALL); l3.setForeground(Theme.TEXT_SECONDARY);
        textBox.add(l1); textBox.add(l2); textBox.add(l3);

        infoBox.add(iconLbl, BorderLayout.WEST);
        infoBox.add(textBox, BorderLayout.CENTER);

        // Total to pay
        JLabel totalLbl = new JLabel("Total yang harus dibayar:");
        totalLbl.setFont(Theme.FONT_SMALL);
        totalLbl.setForeground(Theme.TEXT_SECONDARY);
        JLabel totalVal = new JLabel(); // will be updated dynamically
        totalVal.setText(fmt(finalAmount));
        totalVal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalVal.setForeground(Theme.TEXT_ORANGE);

        // Ref number input (for confirmation)
        JLabel refLbl = small("No. Referensi / Bukti Transfer (opsional):");
        JTextField refField = new JTextField();
        refField.setBackground(Theme.BG_CARD);
        refField.setForeground(Color.WHITE);
        refField.setCaretColor(Color.WHITE);
        refField.setFont(Theme.FONT_BODY);
        refField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_FOCUS, 1, true),
                new EmptyBorder(7, 10, 7, 10)));
        refField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { txnRef = refField.getText().trim(); }
        });

        gc.gridx=0; gc.gridy=0; gc.gridwidth=2; gc.weightx=1; p.add(infoBox,   gc);
        gc.gridy=1; gc.gridwidth=1; gc.weightx=0.5; p.add(totalLbl,  gc);
        gc.gridx=1; p.add(totalVal, gc);
        gc.gridx=0; gc.gridy=2; gc.gridwidth=2; p.add(refLbl,   gc);
        gc.gridy=3; p.add(refField, gc);
        return p;
    }

    // ── FOOTER ───────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
        p.setBackground(Theme.BG_SECONDARY);
        p.setBorder(new EmptyBorder(8, 14, 14, 14));
        p.add(makeBtn("❌  Batal",      Theme.BTN_DANGER,  Theme.BTN_DANGER_H,  e -> dispose()));
        p.add(makeBtn("✅  Konfirmasi", Theme.BTN_SUCCESS, Theme.BTN_SUCCESS_H, e -> confirmPayment()));
        return p;
    }

    // ── CONFIRM PAYMENT ───────────────────────────────────────────────
    private void confirmPayment() {
        long cash = 0, change = 0;

        if (payMethod == PayMethod.CASH) {
            String raw = cashField.getText().replaceAll("[^0-9]", "");
            if (raw.isEmpty()) {
                warn("Masukkan nominal uang tunai!"); return;
            }
            cash = Long.parseLong(raw);
            if (cash < (long) finalAmount * 1000) {
                warn("Uang tunai kurang dari total!"); return;
            }
            change = cash - (long) finalAmount * 1000;
        }
        // For non-cash: no amount validation needed

        paid = true;

        // Generate unified transaction ID
        String trxId = "TRX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        // Save transaction to database
        try {
            Integer cashVal = (payMethod == PayMethod.CASH) ? (int)(cash / 1000) : null;
            Integer changeVal = (payMethod == PayMethod.CASH) ? (int)(change / 1000) : null;
            DatabaseManager.saveOrder(
                trxId,
                customer,
                table,
                payMethod.name(),
                txnRef,
                finalAmount,
                cashVal,
                changeVal,
                items,
                appliedPromoCode,
                discountAmount
            );
        } catch (Exception e) {
            System.err.println("Gagal menyimpan transaksi ke database MySQL: " + e.getMessage());
            warn("Gagal memproses transaksi: " + e.getMessage());
            paid = false;
            return;
        }

        showReceiptWindow(trxId, cash, change);
        dispose();
    }

    // ── RECEIPT WINDOW ────────────────────────────────────────────────
    private void showReceiptWindow(String trxId, long cash, long change) {
        JDialog receipt = new JDialog((JFrame) getOwner(), "🧾 Struk Pembayaran", false);
        receipt.setSize(400, 620);
        receipt.setLocationRelativeTo(getOwner());
        receipt.getContentPane().setBackground(Color.WHITE);
        receipt.setLayout(new BorderLayout());

        JPanel rp = buildReceiptPanel(trxId, cash, change);
        JScrollPane sp = new JScrollPane(rp);
        sp.setBorder(null);

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setBackground(new Color(0xF8, 0xF8, 0xF8));
        btnRow.setBorder(new EmptyBorder(8, 12, 12, 12));

        JButton printBtn = new JButton("🖨️  Print / Simpan");
        printBtn.setFont(Theme.FONT_BTN);
        printBtn.setBackground(Theme.BTN_PRIMARY);
        printBtn.setForeground(Color.WHITE);
        printBtn.setFocusPainted(false);
        printBtn.setOpaque(true);
        printBtn.addActionListener(e -> {
            printToConsole(trxId, cash, change);
            JOptionPane.showMessageDialog(receipt,
                "Struk berhasil dicetak ke konsol.\n(Integrasikan printer thermal untuk cetak fisik)",
                "Print", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton closeBtn = new JButton("✓  Tutup");
        closeBtn.setFont(Theme.FONT_BTN);
        closeBtn.setBackground(Theme.BTN_SUCCESS);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setOpaque(true);
        closeBtn.addActionListener(e -> receipt.dispose());

        btnRow.add(printBtn);
        btnRow.add(closeBtn);

        receipt.add(sp,     BorderLayout.CENTER);
        receipt.add(btnRow, BorderLayout.SOUTH);
        receipt.setVisible(true);

        // Also print to console automatically
        printToConsole(trxId, cash, change);
    }

    // ── BUILD RECEIPT PANEL (visual receipt) ──────────────────────────
    private JPanel buildReceiptPanel(String trxId, long cash, long change) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 24, 20, 24));

        Color navy  = new Color(0x0A, 0x1C, 0x38);
        Color gray  = new Color(0x66, 0x66, 0x66);
        Color lgray = new Color(0xBB, 0xBB, 0xBB);
        Color black = Color.BLACK;

        // Header block
        p.add(rLabel(ReceiptSettings.getCafeName(), new Font("Segoe UI", Font.BOLD, 17), navy, CENTER));
        p.add(rLabel(ReceiptSettings.getAddress(), new Font("Segoe UI", Font.PLAIN, 11), gray, CENTER));
        p.add(rLabel("Telp: " + ReceiptSettings.getPhone(), new Font("Segoe UI", Font.PLAIN, 11), gray, CENTER));
        p.add(Box.createVerticalStrut(6));
        p.add(rDivider(lgray, false));

        // Trx info
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss"));
        p.add(Box.createVerticalStrut(4));
        p.add(rRowTwo("No. Transaksi", trxId,  new Font("Segoe UI", Font.PLAIN, 11), black, gray));
        p.add(rRowTwo("Tanggal",       ts,      new Font("Segoe UI", Font.PLAIN, 11), black, gray));
        if (!customer.isEmpty()) p.add(rRowTwo("Pelanggan", customer, new Font("Segoe UI", Font.PLAIN, 11), black, gray));
        if (!table.isEmpty())    p.add(rRowTwo("Meja",      table,    new Font("Segoe UI", Font.PLAIN, 11), black, gray));
        p.add(rRowTwo("Metode",        payMethod.name(), new Font("Segoe UI", Font.BOLD, 11), black, navy));
        if (!txnRef.isEmpty())   p.add(rRowTwo("Ref",       txnRef,   new Font("Segoe UI", Font.PLAIN, 11), black, gray));
        p.add(Box.createVerticalStrut(4));
        p.add(rDivider(lgray, false));

        // Items header
        p.add(Box.createVerticalStrut(4));
        p.add(rItemHeader(gray));
        p.add(rDivider(lgray, false));

        for (OrderItem oi : items) {
            p.add(rItemRow(oi, black, gray));
        }

        p.add(rDivider(lgray, false));
        p.add(Box.createVerticalStrut(2));
        p.add(rSummaryRow("Subtotal",  fmt(totalAmount), black, gray, false));
        if (discountAmount > 0) {
            p.add(rSummaryRow("Diskon (" + discountPercent + "%)", "-Rp " + (discountAmount * 1000), Color.RED, Color.RED, false));
        }
        p.add(rSummaryRow("Pajak",     "Rp 0",          black, gray, false));
        p.add(Box.createVerticalStrut(2));
        p.add(rDivider(navy, true));
        p.add(rSummaryRow("TOTAL",     fmt(finalAmount), navy,  navy, true));
        p.add(rDivider(navy, true));

        if (payMethod == PayMethod.CASH) {
            p.add(Box.createVerticalStrut(4));
            p.add(rSummaryRow("Tunai",       fmtL(cash),   black, gray, false));
            p.add(rSummaryRow("Kembalian",   fmtL(change), black, gray, false));
        }

        p.add(Box.createVerticalStrut(10));
        p.add(rDivider(lgray, false));
        p.add(Box.createVerticalStrut(8));
        p.add(rLabel(ReceiptSettings.getFooter(), new Font("Segoe UI", Font.ITALIC, 12), gray, CENTER));
        p.add(rLabel("♥  " + ReceiptSettings.getCafeName() + "  ♥", new Font("Segoe UI", Font.BOLD, 12), navy, CENTER));
        p.add(Box.createVerticalStrut(12));
        return p;
    }

    private static final int LEFT=SwingConstants.LEFT, CENTER=SwingConstants.CENTER, RIGHT=SwingConstants.RIGHT;

    // ── RECEIPT COMPONENT HELPERS ─────────────────────────────────────
    private JLabel rLabel(String text, Font font, Color color, int align) {
        JLabel l = new JLabel(text, align);
        l.setFont(font); l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, l.getPreferredSize().height + 2));
        return l;
    }

    private JSeparator rDivider(Color c, boolean thick) {
        JSeparator s = new JSeparator();
        s.setForeground(c); s.setBackground(c);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, thick ? 2 : 1));
        return s;
    }

    private JPanel rRowTwo(String left, String right, Font font, Color lc, Color rc) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        JLabel l = new JLabel(left);  l.setFont(font); l.setForeground(lc);
        JLabel r = new JLabel(right); r.setFont(font); r.setForeground(rc);
        row.add(l, BorderLayout.WEST); row.add(r, BorderLayout.EAST);
        return row;
    }

    private JPanel rItemHeader(Color c) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        Font f = new Font("Segoe UI", Font.BOLD, 10);
        JLabel n = new JLabel("Item");        n.setFont(f); n.setForeground(c);
        JLabel q = new JLabel("Qty", RIGHT);  q.setFont(f); q.setForeground(c); q.setPreferredSize(new Dimension(30,16));
        JLabel s = new JLabel("Subtotal", RIGHT); s.setFont(f); s.setForeground(c); s.setPreferredSize(new Dimension(80,16));
        JPanel right = new JPanel(new BorderLayout()); right.setOpaque(false);
        right.add(q, BorderLayout.WEST); right.add(s, BorderLayout.EAST);
        row.add(n, BorderLayout.CENTER); row.add(right, BorderLayout.EAST);
        return row;
    }

    private JPanel rItemRow(OrderItem oi, Color tc, Color sc) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        String name = oi.getMenuItem().getEmoji() + " " + oi.getMenuItem().getName();
        Font fn = new Font("Segoe UI", Font.PLAIN, 12);
        Font fs = new Font("Segoe UI", Font.PLAIN, 10);

        // Name row
        JPanel top = new JPanel(new BorderLayout(4, 0));
        top.setOpaque(false);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        JLabel nl = new JLabel(name); nl.setFont(fn); nl.setForeground(tc);
        JLabel sl = new JLabel(oi.getFormattedSubtotal(), RIGHT); sl.setFont(fn); sl.setForeground(tc); sl.setPreferredSize(new Dimension(90,16));
        top.add(nl, BorderLayout.CENTER); top.add(sl, BorderLayout.EAST);

        // Price × qty sub-line
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        JLabel pl = new JLabel("  " + oi.getMenuItem().getFormattedPrice() + " × " + oi.getQuantity());
        pl.setFont(fs); pl.setForeground(sc);
        bot.add(pl, BorderLayout.WEST);

        col.add(top); col.add(bot);
        col.add(Box.createVerticalStrut(2));
        return col;
    }

    private JPanel rSummaryRow(String label, String value, Color lc, Color vc, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, bold ? 26 : 20));
        Font f = bold ? new Font("Segoe UI", Font.BOLD, 14) : new Font("Segoe UI", Font.PLAIN, 12);
        JLabel l = new JLabel(label); l.setFont(f); l.setForeground(lc);
        JLabel v = new JLabel(value); v.setFont(f); v.setForeground(vc);
        row.add(l, BorderLayout.WEST); row.add(v, BorderLayout.EAST);
        return row;
    }

    // ── CONSOLE RECEIPT ───────────────────────────────────────────────
    private void printToConsole(String id, long cash, long change) {
        String w  = "----------------------------------------";
        String w2 = "========================================";
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(w2).append("\n");
        sb.append("        *** ").append(ReceiptSettings.getCafeName().toUpperCase()).append(" ***\n");
        sb.append("     ").append(ReceiptSettings.getAddress()).append(" | ").append(ReceiptSettings.getPhone()).append("\n");
        sb.append(w).append("\n");
        sb.append(String.format("%-16s: %s%n", "No Transaksi", id));
        sb.append(String.format("%-16s: %s%n", "Tanggal",      ts));
        if (!customer.isEmpty()) sb.append(String.format("%-16s: %s%n", "Pelanggan", customer));
        if (!table.isEmpty())    sb.append(String.format("%-16s: %s%n", "Meja",      table));
        sb.append(String.format("%-16s: %s%n", "Metode",       payMethod.name()));
        if (!txnRef.isEmpty())   sb.append(String.format("%-16s: %s%n", "Ref",       txnRef));
        sb.append(w).append("\n");
        sb.append(String.format("%-24s %4s %10s%n", "Item", "Qty", "Subtotal"));
        sb.append(w).append("\n");
        for (OrderItem oi : items) {
            String n = oi.getMenuItem().getName();
            if (n.length() > 24) n = n.substring(0, 21) + "...";
            sb.append(String.format("%-24s %4d %10s%n", n, oi.getQuantity(), oi.getFormattedSubtotal()));
            sb.append(String.format("  @ %-22s%n", oi.getMenuItem().getFormattedPrice()));
        }
        sb.append(w).append("\n");
        sb.append(String.format("%-24s %4s %10s%n", "SUBTOTAL", "",  fmt(totalAmount)));
        if (discountAmount > 0) {
            sb.append(String.format("%-24s %4s %10s%n", "DISKON (" + discountPercent + "%)", "", "-Rp " + (discountAmount * 1000)));
        }
        sb.append(String.format("%-24s %4s %10s%n", "PAJAK",    "",  "Rp 0"));
        sb.append(String.format("%-24s %4s %10s%n", "TOTAL",    "",  fmt(finalAmount)));
        if (payMethod == PayMethod.CASH) {
            sb.append(String.format("%-24s %4s %10s%n", "TUNAI",     "", fmtL(cash)));
            sb.append(String.format("%-24s %4s %10s%n", "KEMBALIAN", "", fmtL(change)));
        }
        sb.append(w2).append("\n");
        sb.append("     ").append(ReceiptSettings.getFooter()).append("\n");
        sb.append("          ♥  ").append(ReceiptSettings.getCafeName()).append("  ♥\n");
        sb.append(w2).append("\n");
        System.out.println(sb);
    }

    // ── MISC HELPERS ──────────────────────────────────────────────────
    public boolean isPaid() { return paid; }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Perhatian", JOptionPane.WARNING_MESSAGE);
    }

    private JLabel small(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    private JButton makeBtn(String text, Color bg, Color hover, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(Theme.FONT_BTN);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(10, 14, 10, 14));
        b.setFocusPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(bg); }
        });
        b.addActionListener(al);
        return b;
    }

    private JPanel tableRow(String c1, String c2, String c3, String c4, boolean header) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(3, 6, 3, 6));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        Font  f  = header ? Theme.FONT_BTN : Theme.FONT_SMALL;
        Color tc = header ? Theme.TEXT_SECONDARY : Theme.TEXT_PRIMARY;
        Color pc = header ? Theme.TEXT_SECONDARY : Theme.TEXT_PRICE;
        JLabel l1 = new JLabel(c1);           l1.setFont(f); l1.setForeground(tc);
        JLabel l2 = new JLabel(c2, RIGHT);    l2.setFont(f); l2.setForeground(tc); l2.setPreferredSize(new Dimension(28,16));
        JLabel l3 = new JLabel(c3, RIGHT);    l3.setFont(f); l3.setForeground(tc); l3.setPreferredSize(new Dimension(80,16));
        JLabel l4 = new JLabel(c4, RIGHT);    l4.setFont(f); l4.setForeground(pc); l4.setPreferredSize(new Dimension(80,16));
        JPanel right = new JPanel(new GridLayout(1, 3, 2, 0)); right.setOpaque(false);
        right.add(l2); right.add(l3); right.add(l4);
        row.add(l1, BorderLayout.CENTER); row.add(right, BorderLayout.EAST);
        return row;
    }

    private void updateOrderSummaryTable() {
        discountValLabel.setText("-Rp " + (discountAmount * 1000));
        if (discountAmount > 0) {
            discountValLabel.setForeground(Theme.BTN_DANGER);
        } else {
            discountValLabel.setForeground(Theme.TEXT_PRIMARY);
        }
        totalValLabel.setText(fmt(finalAmount));
    }

    private JPanel createSummaryRow(String labelText, JLabel valLabel, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(2, 6, 2, 6));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, bold ? 28 : 22));
        Font  f  = bold ? Theme.FONT_SUBTITLE : Theme.FONT_SMALL;
        Color c  = bold ? Theme.TEXT_ORANGE   : Theme.TEXT_PRIMARY;
        JLabel l = new JLabel(labelText); l.setFont(f); l.setForeground(c);
        valLabel.setFont(f); valLabel.setForeground(c);
        row.add(l, BorderLayout.WEST); row.add(valLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel summaryRow(String label, String val, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(2, 6, 2, 6));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, bold ? 28 : 22));
        Font  f  = bold ? Theme.FONT_SUBTITLE : Theme.FONT_SMALL;
        Color c  = bold ? Theme.TEXT_ORANGE   : Theme.TEXT_PRIMARY;
        JLabel l = new JLabel(label); l.setFont(f); l.setForeground(c);
        JLabel v = new JLabel(val);   v.setFont(f); v.setForeground(c);
        row.add(l, BorderLayout.WEST); row.add(v, BorderLayout.EAST);
        return row;
    }

    private JSeparator divider() {
        JSeparator s = new JSeparator();
        s.setForeground(Theme.BORDER);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return s;
    }

    private String fmt(int k)   { return String.format("Rp %,d", (long)k * 1000).replace(',', '.'); }
    private String fmtL(long r) { return String.format("Rp %,d", r).replace(',', '.'); }
    private String fmtShort(int r) {
        if (r >= 1_000_000) return r / 1_000_000 + "jt";
        return r / 1000 + "rb";
    }
}
