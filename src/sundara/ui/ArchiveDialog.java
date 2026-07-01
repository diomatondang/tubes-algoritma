package sundara.ui;

import sundara.data.DatabaseManager;
import sundara.data.ReceiptSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sales Archive dialog that displays past transactions from database
 * and a summary dashboard of today's earnings.
 */
public class ArchiveDialog extends JDialog {

    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JTextPane receiptDetailArea;
    private JTextField searchField;
    
    // Stats labels
    private JLabel revenueLabel;
    private JLabel countLabel;
    private JLabel bestSellerLabel;

    private List<Map<String, Object>> currentSalesList = new ArrayList<>();
    private CategorySalesChart chartPanel;
    private WeeklyRevenueChart weeklyChartPanel;

    public ArchiveDialog(JFrame parent) {
        super(parent, "Arsip Penjualan — Sundara CoffeeSpace", true);
        setSize(950, 650);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_SECONDARY);
        setLayout(new BorderLayout(10, 10));

        // Glassmorphic panel layout
        JPanel mainWrapper = new JPanel(new BorderLayout(12, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_SECONDARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainWrapper.setOpaque(false);
        mainWrapper.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(mainWrapper);

        // 1. TOP SECTION: Dashboard Cards
        add(buildStatsDashboard(), BorderLayout.NORTH);

        // 2. CENTER SECTION: Tabbed Pane (Daftar Transaksi + Grafik Analisis)
        JTabbedPane centerTabbedPane = new JTabbedPane();
        centerTabbedPane.setBackground(Theme.BG_CARD);
        centerTabbedPane.setForeground(Color.WHITE);
        centerTabbedPane.setFont(Theme.FONT_BTN);

        // Tab 1: Daftar Riwayat Transaksi
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeftPanel(), buildRightPanel());
        splitPane.setDividerLocation(560);
        splitPane.setDividerSize(4);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setBackground(Theme.BORDER);
        centerTabbedPane.addTab("🧾 Daftar Riwayat Transaksi", splitPane);

        // Tab 2: Grafik Analisis Penjualan
        chartPanel = new CategorySalesChart();
        centerTabbedPane.addTab("📊 Grafik Analisis Penjualan", chartPanel);

        // Tab 3: Tren Penjualan Mingguan
        weeklyChartPanel = new WeeklyRevenueChart();
        centerTabbedPane.addTab("📈 Tren Penjualan Mingguan", weeklyChartPanel);

        add(centerTabbedPane, BorderLayout.CENTER);

        // 3. BOTTOM SECTION: Buttons
        add(buildBottomBar(), BorderLayout.SOUTH);

        // Load data initially
        refreshData();
    }

    // ── 1. STATS DASHBOARD ───────────────────────────────────────────
    private JPanel buildStatsDashboard() {
        JPanel p = new JPanel(new GridLayout(1, 3, 14, 0));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 80));

        revenueLabel = new JLabel("Rp 0", SwingConstants.CENTER);
        countLabel = new JLabel("0 Transaksi", SwingConstants.CENTER);
        bestSellerLabel = new JLabel("-", SwingConstants.CENTER);

        p.add(createStatCard("💵 Pendapatan Hari Ini", revenueLabel, Theme.TEXT_ORANGE));
        p.add(createStatCard("📊 Jumlah Transaksi Hari Ini", countLabel, Color.WHITE));
        p.add(createStatCard("🏆 Menu Terlaris Hari Ini", bestSellerLabel, new Color(0x38, 0xBD, 0xF8)));

        return p;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(4, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_CARD);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 16, 16));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_SMALL);
        titleLabel.setForeground(Theme.TEXT_SECONDARY);

        valueLabel.setFont(Theme.FONT_TITLE);
        valueLabel.setForeground(valueColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ── 2. LEFT PANEL: Transactions Table ─────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);

        // Search panel
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(Theme.FONT_EMOJI_SM);
        searchBar.add(searchIcon, BorderLayout.WEST);

        searchField = new JTextField();
        searchField.setBackground(Theme.BG_CARD);
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setFont(Theme.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Cari ID transaksi / nama pelanggan...");
        searchField.addActionListener(e -> refreshData()); // Enter key triggers search
        
        JButton btnCari = new JButton("Cari");
        btnCari.setFont(Theme.FONT_BTN);
        btnCari.setBackground(Theme.BTN_PRIMARY);
        btnCari.setForeground(Color.WHITE);
        btnCari.setBorder(new EmptyBorder(0, 14, 0, 14));
        btnCari.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCari.addActionListener(e -> refreshData());

        searchBar.add(searchField, BorderLayout.CENTER);
        searchBar.add(btnCari, BorderLayout.EAST);

        // JTable Setup
        String[] cols = {"ID Transaksi", "Tanggal", "Nama", "Meja", "Tipe", "Total"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        salesTable = new JTable(tableModel);
        salesTable.setBackground(Theme.BG_CARD);
        salesTable.setForeground(Color.WHITE);
        salesTable.setGridColor(Theme.BORDER);
        salesTable.setSelectionBackground(Theme.BTN_PRIMARY);
        salesTable.setSelectionForeground(Color.WHITE);
        salesTable.setRowHeight(28);
        salesTable.setFont(Theme.FONT_BODY);
        salesTable.getTableHeader().setBackground(Theme.BG_SECONDARY);
        salesTable.getTableHeader().setForeground(Theme.TEXT_ORANGE);
        salesTable.getTableHeader().setFont(Theme.FONT_BTN);
        salesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        salesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedReceipt();
            }
        });

        JScrollPane tableScroll = new JScrollPane(salesTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        styleScrollBar(tableScroll.getVerticalScrollBar());

        p.add(searchBar, BorderLayout.NORTH);
        p.add(tableScroll, BorderLayout.CENTER);
        return p;
    }

    // ── 3. RIGHT PANEL: Receipt Detail Preview ─────────────────────────
    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 8, 0, 0));

        JLabel title = new JLabel("🧾 Detail Struk Penjualan");
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_SECONDARY);
        p.add(title, BorderLayout.NORTH);

        receiptDetailArea = new JTextPane();
        receiptDetailArea.setContentType("text/html");
        receiptDetailArea.setEditable(false);
        receiptDetailArea.setBackground(Color.WHITE);
        receiptDetailArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JScrollPane scroll = new JScrollPane(receiptDetailArea);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        styleScrollBar(scroll.getVerticalScrollBar());
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // ── 4. BOTTOM BAR ─────────────────────────────────────────────────
    private JPanel buildBottomBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);

        JButton exportBtn = new JButton("Ekspor Laporan 💾") {
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
        exportBtn.setFont(Theme.FONT_BTN);
        exportBtn.setBackground(Theme.BTN_SUCCESS);
        exportBtn.setForeground(Color.WHITE);
        exportBtn.setBorder(new EmptyBorder(10, 16, 10, 16));
        exportBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportBtn.addActionListener(e -> exportReport());

        JButton reprintBtn = new JButton("🖨️ Cetak Struk ke Konsol") {
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
        reprintBtn.setFont(Theme.FONT_BTN);
        reprintBtn.setBackground(Theme.BTN_ORANGE);
        reprintBtn.setForeground(Color.WHITE);
        reprintBtn.setBorder(new EmptyBorder(10, 16, 10, 16));
        reprintBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reprintBtn.addActionListener(e -> reprintSelectedOrder());

        JButton refreshBtn = new JButton("🔄 Refresh Data") {
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
        refreshBtn.setBorder(new EmptyBorder(10, 16, 10, 16));
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshData());

        JButton closeBtn = new JButton("Tutup") {
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
        closeBtn.setFont(Theme.FONT_BTN);
        closeBtn.setBackground(Theme.BTN_DANGER);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorder(new EmptyBorder(10, 20, 10, 20));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        p.add(exportBtn);
        p.add(reprintBtn);
        p.add(refreshBtn);
        p.add(closeBtn);
        return p;
    }

    // ── DATA UTILITIES ────────────────────────────────────────────────
    private void refreshData() {
        String search = searchField.getText().trim();
        try {
            // Load dashboard stats
            Map<String, Object> stats = DatabaseManager.getSalesStats();
            int revenue = (int) stats.getOrDefault("today_revenue", 0);
            int count = (int) stats.getOrDefault("today_count", 0);
            String best = (String) stats.getOrDefault("best_seller", "-");

            revenueLabel.setText(String.format("Rp %,d", (long)revenue * 1000).replace(',', '.'));
            countLabel.setText(count + " Transaksi");
            bestSellerLabel.setText(best != null ? best : "-");

            // Load orders list
            currentSalesList = DatabaseManager.getSalesList(search);
            tableModel.setRowCount(0);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            
            for (Map<String, Object> sale : currentSalesList) {
                Timestamp ts = (Timestamp) sale.get("transaction_date");
                int total = (int) sale.get("total_amount");
                tableModel.addRow(new Object[]{
                        sale.get("transaction_id"),
                        ts != null ? df.format(ts) : "-",
                        sale.get("customer_name"),
                        sale.get("table_number"),
                        sale.get("payment_method"),
                        String.format("Rp %,d", (long)total * 1000).replace(',', '.')
                });
            }
            receiptDetailArea.setText("<html><body style='font-family:monospace; color:#666666;'><center><br><br>Pilih transaksi untuk melihat detail struk.</center></body></html>");
            
            // Load category sales stats for chart
            try {
                Map<String, Integer> catStats = DatabaseManager.getCategorySalesStats();
                if (chartPanel != null) {
                    chartPanel.setData(catStats);
                }
            } catch (Exception e) {
                System.err.println("Gagal memuat statistik kategori: " + e.getMessage());
            }

            // Load weekly revenue stats for chart
            try {
                Map<String, Integer> weekStats = DatabaseManager.getWeeklyRevenueStats();
                if (weeklyChartPanel != null) {
                    weeklyChartPanel.setData(weekStats);
                }
            } catch (Exception e) {
                System.err.println("Gagal memuat statistik mingguan: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat arsip penjualan: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Gagal memuat arsip penjualan dari database MySQL Laragon!\nPastikan MySQL aktif dan database terkonfigurasi.", 
                "Error Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedReceipt() {
        int idx = salesTable.getSelectedRow();
        if (idx < 0 || idx >= currentSalesList.size()) {
            receiptDetailArea.setText("");
            return;
        }

        Map<String, Object> order = currentSalesList.get(idx);
        int orderId = (int) order.get("id");

        try {
            List<Map<String, Object>> items = DatabaseManager.getOrderItems(orderId);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Timestamp ts = (Timestamp) order.get("transaction_date");
            
            String cust = (String) order.get("customer_name");
            String tab = (String) order.get("table_number");
            String ref = (String) order.get("reference_number");
            String method = (String) order.get("payment_method");
            int total = (int) order.get("total_amount");
            
            Object cashVal = order.get("cash_amount");
            Object changeVal = order.get("change_amount");

            StringBuilder sb = new StringBuilder();
            sb.append("<html><body style='font-family:monospace; font-size:11px; color:black;'>");
            sb.append("<center><b>").append(ReceiptSettings.getCafeName()).append("</b><br>");
            sb.append(ReceiptSettings.getAddress()).append("<br>");
            sb.append("Telp: ").append(ReceiptSettings.getPhone()).append("</center>");
            sb.append("<hr style='border-top:1px dashed #bbb;'>");
            
            sb.append("No. Trx  : ").append(order.get("transaction_id")).append("<br>");
            sb.append("Tanggal  : ").append(ts != null ? df.format(ts) : "-").append("<br>");
            if (cust != null && !cust.isEmpty()) sb.append("Pelanggan: ").append(cust).append("<br>");
            if (tab != null && !tab.isEmpty())   sb.append("Meja     : Meja ").append(tab).append("<br>");
            sb.append("Metode   : ").append(method).append("<br>");
            if (ref != null && !ref.isEmpty())   sb.append("Ref      : ").append(ref).append("<br>");
            
            sb.append("<hr style='border-top:1px dashed #bbb;'>");
            sb.append(String.format("%-22s %2s %8s<br>", "Item", "Qty", "Total"));
            sb.append("<hr style='border-top:1px dashed #bbb;'>");

            for (Map<String, Object> item : items) {
                String name = (String) item.get("name");
                int qty = (int) item.get("quantity");
                int sub = (int) item.get("subtotal");
                int price = (int) item.get("price");
                
                if (name.length() > 20) name = name.substring(0, 18) + "..";
                sb.append(String.format("<b>%-22s</b> %2d %,10d<br>", name, qty, sub * 1000));
                sb.append(String.format("  @ %,d<br>", price * 1000));
            }
            
            sb.append("<hr style='border-top:1px dashed #bbb;'>");
            sb.append(String.format("<b>SUBTOTAL %22s</b><br>", fmt(total)));
            sb.append(String.format("<b>PAJAK    %22s</b><br>", "Rp 0"));
            sb.append(String.format("<font color='blue'><b>TOTAL    %22s</b></font><br>", fmt(total)));
            
            if (cashVal != null) {
                long c = ((Number) cashVal).longValue();
                long ch = changeVal != null ? ((Number) changeVal).longValue() : 0;
                sb.append(String.format("TUNAI    %22s<br>", fmt((int) c)));
                sb.append(String.format("KEMBALIAN%22s<br>", fmt((int) ch)));
            }
            
            sb.append("<hr style='border-top:1px dashed #bbb;'>");
            sb.append("<center>♥ ").append(ReceiptSettings.getFooter()).append(" ♥</center>");
            sb.append("</body></html>");

            receiptDetailArea.setText(sb.toString());
            receiptDetailArea.setCaretPosition(0);
        } catch (Exception e) {
            System.err.println("Gagal memuat detail item order: " + e.getMessage());
            receiptDetailArea.setText("Error loading details");
        }
    }

    private void reprintSelectedOrder() {
        int idx = salesTable.getSelectedRow();
        if (idx < 0 || idx >= currentSalesList.size()) {
            JOptionPane.showMessageDialog(this, "Pilih baris transaksi terlebih dahulu!", "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, Object> order = currentSalesList.get(idx);
        int orderId = (int) order.get("id");
        String trxId = (String) order.get("transaction_id");

        try {
            List<Map<String, Object>> items = DatabaseManager.getOrderItems(orderId);
            Timestamp ts = (Timestamp) order.get("transaction_date");
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            String cust = (String) order.get("customer_name");
            String tab = (String) order.get("table_number");
            String ref = (String) order.get("reference_number");
            String method = (String) order.get("payment_method");
            int total = (int) order.get("total_amount");

            Object cashVal = order.get("cash_amount");
            Object changeVal = order.get("change_amount");

            long cash = cashVal != null ? ((Number) cashVal).longValue() * 1000 : 0;
            long change = changeVal != null ? ((Number) changeVal).longValue() * 1000 : 0;

            // Reproduce console print receipt
            String w  = "----------------------------------------";
            String w2 = "========================================";
            StringBuilder sb = new StringBuilder();
            sb.append("\n").append(w2).append("\n");
            sb.append("     *** ").append(ReceiptSettings.getCafeName().toUpperCase()).append(" (REPRINT) ***\n");
            sb.append("     ").append(ReceiptSettings.getAddress()).append(" | ").append(ReceiptSettings.getPhone()).append("\n");
            sb.append(w).append("\n");
            sb.append(String.format("%-16s: %s%n", "No Transaksi", trxId));
            sb.append(String.format("%-16s: %s%n", "Tanggal",      ts != null ? df.format(ts) : "-"));
            if (cust != null && !cust.isEmpty()) sb.append(String.format("%-16s: %s%n", "Pelanggan", cust));
            if (tab != null && !tab.isEmpty())   sb.append(String.format("%-16s: Meja %s%n", "Meja",      tab));
            sb.append(String.format("%-16s: %s%n", "Metode",       method));
            if (ref != null && !ref.isEmpty())   sb.append(String.format("%-16s: %s%n", "Ref",       ref));
            sb.append(w).append("\n");
            sb.append(String.format("%-24s %4s %10s%n", "Item", "Qty", "Subtotal"));
            sb.append(w).append("\n");
            
            for (Map<String, Object> it : items) {
                String n = (String) it.get("name");
                int q = (int) it.get("quantity");
                int sub = (int) it.get("subtotal");
                int price = (int) it.get("price");
                
                if (n.length() > 24) n = n.substring(0, 21) + "...";
                sb.append(String.format("%-24s %4d %10s%n", n, q, String.format("Rp %,d", sub * 1000).replace(',', '.')));
                sb.append(String.format("  @ %-22s%n", String.format("Rp %,d", price * 1000).replace(',', '.')));
            }
            sb.append(w).append("\n");
            sb.append(String.format("%-24s %4s %10s%n", "SUBTOTAL", "",  fmt(total)));
            sb.append(String.format("%-24s %4s %10s%n", "PAJAK",    "",  "Rp 0"));
            sb.append(String.format("%-24s %4s %10s%n", "TOTAL",    "",  fmt(total)));
            
            if (cashVal != null) {
                sb.append(String.format("%-24s %4s %10s%n", "TUNAI",     "", String.format("Rp %,d", cash).replace(',', '.')));
                sb.append(String.format("%-24s %4s %10s%n", "KEMBALIAN", "", String.format("Rp %,d", change).replace(',', '.')));
            }
            sb.append(w2).append("\n");
            sb.append("     ").append(ReceiptSettings.getFooter()).append("\n");
            sb.append("          ♥  ").append(ReceiptSettings.getCafeName()).append("  ♥\n");
            sb.append(w2).append("\n");

            System.out.println(sb);
            JOptionPane.showMessageDialog(this, 
                "Struk berhasil dicetak ulang ke Konsol secara real-time!\nLihat panel output terminal Anda.", 
                "Reprint Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            System.err.println("Gagal mencetak ulang struk: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Gagal mencetak ulang struk dari database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String fmt(int k) {
        return String.format("Rp %,d", (long)k * 1000).replace(',', '.');
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setBackground(Theme.BG_PRIMARY);
        bar.setForeground(Theme.BG_ACCENT);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = Theme.BG_ACCENT;
                trackColor = Theme.BG_PRIMARY;
                thumbHighlightColor = Theme.BTN_PRIMARY;
            }
            @Override protected JButton createDecreaseButton(int o) { return invis(); }
            @Override protected JButton createIncreaseButton(int o) { return invis(); }
            private JButton invis() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
            }
        });
    }

    private void exportReport() {
        if (currentSalesList == null || currentSalesList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data penjualan untuk diekspor!", "Ekspor Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Ekspor Laporan Penjualan — Sundara");
        chooser.setSelectedFile(new java.io.File("Laporan_Penjualan_Sundara.html"));
        
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Halaman HTML (*.html)", "html"));
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Format CSV (*.csv)", "csv"));

        int choice = chooser.showSaveDialog(this);
        if (choice != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        String path = file.getAbsolutePath();
        
        String ext = "html";
        if (chooser.getFileFilter().getDescription().contains("CSV") || path.toLowerCase().endsWith(".csv")) {
            ext = "csv";
            if (!path.toLowerCase().endsWith(".csv")) {
                file = new java.io.File(path + ".csv");
            }
        } else {
            if (!path.toLowerCase().endsWith(".html") && !path.toLowerCase().endsWith(".htm")) {
                file = new java.io.File(path + ".html");
            }
        }

        try {
            if (ext.equals("csv")) {
                exportToCSV(file);
            } else {
                exportToHTML(file);
            }
            JOptionPane.showMessageDialog(this, "Laporan penjualan berhasil diekspor ke:\n" + file.getAbsolutePath(), "Ekspor Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            System.err.println("Gagal mengekspor laporan: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Gagal mengekspor laporan: " + e.getMessage(), "Error Ekspor", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToCSV(java.io.File file) throws java.io.IOException {
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
            writer.write("ID Transaksi,Tanggal,Nama Pelanggan,Meja,Metode Pembayaran,No Referensi,Omzet (Rupiah)\n");
            
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long totalAll = 0;

            for (Map<String, Object> sale : currentSalesList) {
                String trxId = (String) sale.get("transaction_id");
                Timestamp ts = (Timestamp) sale.get("transaction_date");
                String dateStr = ts != null ? df.format(ts) : "";
                String name = (String) sale.get("customer_name");
                String table = (String) sale.get("table_number");
                String method = (String) sale.get("payment_method");
                String ref = (String) sale.get("reference_number");
                int total = (int) sale.get("total_amount");

                name = name == null ? "" : name.replace("\"", "\"\"");
                table = table == null ? "" : table;
                ref = ref == null ? "" : ref;
                
                long totalRupiah = (long) total * 1000;
                totalAll += totalRupiah;

                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d\n",
                    trxId, dateStr, name, table, method, ref, totalRupiah));
            }
            writer.write(String.format(",,,,,,Total Pendapatan,%d\n", totalAll));
        }
    }

    private void exportToHTML(java.io.File file) throws java.io.IOException {
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
            writer.write("<!DOCTYPE html>\n<html>\n<head>\n");
            writer.write("<meta charset='utf-8'>\n<title>Laporan Penjualan — Sundara CoffeeSpace</title>\n");
            writer.write("<style>\n");
            writer.write("  body { font-family: 'Segoe UI', Arial, sans-serif; background: #f8fafc; color: #1e293b; padding: 30px; }\n");
            writer.write("  .header { display: flex; justify-content: space-between; border-bottom: 2px solid #e2e8f0; padding-bottom: 15px; margin-bottom: 20px; }\n");
            writer.write("  h1 { margin: 0; color: #0f172a; font-size: 24px; }\n");
            writer.write("  .tagline { color: #64748b; font-size: 14px; margin-top: 5px; }\n");
            writer.write("  table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }\n");
            writer.write("  th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #f1f5f9; }\n");
            writer.write("  th { background: #0f172a; color: white; font-weight: 600; font-size: 13px; text-transform: uppercase; }\n");
            writer.write("  tr:hover { background: #f8fafc; }\n");
            writer.write("  .total-row { font-weight: bold; background: #f1f5f9; }\n");
            writer.write("  .footer { text-align: center; margin-top: 30px; color: #94a3b8; font-size: 12px; }\n");
            writer.write("</style>\n</head>\n<body>\n");

            writer.write("<div class='header'>\n");
            writer.write("  <div>\n");
            writer.write("    <h1>" + ReceiptSettings.getCafeName() + "</h1>\n");
            writer.write("    <div class='tagline'>Laporan Penjualan Rekapitulasi POS</div>\n");
            writer.write("  </div>\n");
            writer.write("  <div style='text-align: right;'>\n");
            writer.write("    <div>Tanggal Cetak: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()) + "</div>\n");
            writer.write("    <div style='color: #64748b; margin-top: 5px;'>Database Status: Online</div>\n");
            writer.write("  </div>\n");
            writer.write("</div>\n");

            writer.write("<table>\n");
            writer.write("  <thead>\n");
            writer.write("    <tr><th>ID Transaksi</th><th>Waktu</th><th>Pelanggan</th><th>Meja</th><th>Pembayaran</th><th>No Referensi</th><th style='text-align: right;'>Omzet (Rp)</th></tr>\n");
            writer.write("  </thead>\n");
            writer.write("  <tbody>\n");

            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            long totalAll = 0;

            for (Map<String, Object> sale : currentSalesList) {
                String trxId = (String) sale.get("transaction_id");
                Timestamp ts = (Timestamp) sale.get("transaction_date");
                String dateStr = ts != null ? df.format(ts) : "-";
                String name = (String) sale.get("customer_name");
                String table = (String) sale.get("table_number");
                String method = (String) sale.get("payment_method");
                String ref = (String) sale.get("reference_number");
                int total = (int) sale.get("total_amount");

                name = name == null || name.isEmpty() ? "-" : name;
                table = table == null || table.isEmpty() ? "-" : "Meja " + table;
                ref = ref == null || ref.isEmpty() ? "-" : ref;
                long totalRupiah = (long) total * 1000;
                totalAll += totalRupiah;

                writer.write(String.format("    <tr><td><b>%s</b></td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td style='text-align: right;'>Rp %,d</td></tr>\n",
                    trxId, dateStr, name, table, method, ref, totalRupiah).replace(',', '.'));
            }

            writer.write("    <tr class='total-row'>\n");
            writer.write("      <td colspan='6' style='text-align: right;'>TOTAL PENDAPATAN REKAPITULASI</td>\n");
            writer.write(String.format("      <td style='text-align: right; color: #0284c7; font-size: 16px;'>Rp %,d</td>\n", totalAll).replace(',', '.'));
            writer.write("    </tr>\n");
            writer.write("  </tbody>\n");
            writer.write("</table>\n");

            writer.write("<div class='footer'>\n");
            writer.write("  <p>" + ReceiptSettings.getAddress() + " | Telp: " + ReceiptSettings.getPhone() + "</p>\n");
            writer.write("  <p>&copy; 2026 " + ReceiptSettings.getCafeName() + ". Hak Cipta Dilindungi.</p>\n");
            writer.write("</div>\n");

            writer.write("</body>\n</html>\n");
        }
    }

    public static class CategorySalesChart extends JPanel {
        private Map<String, Integer> data = new java.util.LinkedHashMap<>();

        public CategorySalesChart() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 360));
        }

        public void setData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Draw card background
            g2.setColor(Theme.BG_CARD);
            g2.fillRoundRect(10, 10, w - 20, h - 20, 20, 20);
            g2.setColor(Theme.BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(10, 10, w - 20, h - 20, 20, 20);

            if (data == null || data.isEmpty()) {
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.setFont(Theme.FONT_SUBTITLE);
                FontMetrics fm = g2.getFontMetrics();
                String msg = "Belum ada data penjualan hari ini.";
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                g2.dispose();
                return;
            }

            // Draw Title
            g2.setColor(Theme.TEXT_ORANGE);
            g2.setFont(Theme.FONT_SUBTITLE);
            g2.drawString("Analisis Penjualan per Kategori (Hari Ini)", 30, 45);

            // Find max value to scale the bars
            int maxQty = 0;
            int totalQty = 0;
            for (int qty : data.values()) {
                if (qty > maxQty) maxQty = qty;
                totalQty += qty;
            }
            if (maxQty == 0) maxQty = 1;

            int chartX = 60;
            int chartY = 80;
            int chartW = w - 120;
            int chartH = h - 160;

            // Draw grid lines and Y axis markers
            g2.setColor(new Color(255, 255, 255, 20));
            g2.setStroke(new BasicStroke(1.0f));
            int numGridLines = 4;
            g2.setFont(Theme.FONT_SMALL);
            for (int i = 0; i <= numGridLines; i++) {
                float pct = (float) i / numGridLines;
                int y = chartY + chartH - (int) (pct * chartH);
                g2.drawLine(chartX, y, chartX + chartW, y);
                
                int val = (int) (pct * maxQty);
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.drawString(String.valueOf(val), chartX - 35, y + 5);
                g2.setColor(new Color(255, 255, 255, 20));
            }

            // Draw X and Y axis lines
            g2.setColor(Theme.BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(chartX, chartY, chartX, chartY + chartH); // Y axis
            g2.drawLine(chartX, chartY + chartH, chartX + chartW, chartY + chartH); // X axis

            // Draw bars
            int numBars = data.size();
            int gap = 30;
            int barW = (chartW - (gap * (numBars + 1))) / numBars;
            if (barW < 20) barW = 20;

            int index = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                String category = entry.getKey();
                int qty = entry.getValue();
                float ratio = (float) qty / maxQty;
                int barH = (int) (ratio * chartH);

                int x = chartX + gap + index * (barW + gap);
                int y = chartY + chartH - barH;

                // Draw bar with gradient (cyan to blue)
                GradientPaint gp = new GradientPaint(
                    x, y, new Color(0x00, 0xD2, 0xFF),
                    x, y + barH, new Color(0x00, 0x66, 0xEE)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, barH, 12, 12);
                
                g2.setColor(Theme.BORDER_FOCUS);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, barW, barH, 12, 12);

                // Draw label: Qty (Percentage)
                String qtyStr = qty + " Pcs";
                String pctStr = "";
                if (totalQty > 0) {
                    int percentage = Math.round(((float) qty / totalQty) * 100);
                    pctStr = " (" + percentage + "%)";
                }
                String fullStr = qtyStr + pctStr;
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.FONT_PRICE);
                int fullStrW = g2.getFontMetrics().stringWidth(fullStr);
                g2.drawString(fullStr, x + (barW - fullStrW) / 2, y - 8);

                // Draw Category label
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.FONT_SMALL);
                String label = category;
                int labelW = g2.getFontMetrics().stringWidth(label);
                if (labelW > barW + gap - 10) {
                    label = label.substring(0, Math.min(label.length(), 8)) + "..";
                    labelW = g2.getFontMetrics().stringWidth(label);
                }
                g2.drawString(label, x + (barW - labelW) / 2, chartY + chartH + 22);

                index++;
            }

            g2.dispose();
        }
    }

    public static class WeeklyRevenueChart extends JPanel {
        private Map<String, Integer> data = new java.util.LinkedHashMap<>();

        public WeeklyRevenueChart() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 360));
        }

        public void setData(Map<String, Integer> data) {
            this.data = data;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Draw card background
            g2.setColor(Theme.BG_CARD);
            g2.fillRoundRect(10, 10, w - 20, h - 20, 20, 20);
            g2.setColor(Theme.BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(10, 10, w - 20, h - 20, 20, 20);

            if (data == null || data.isEmpty()) {
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.setFont(Theme.FONT_SUBTITLE);
                FontMetrics fm = g2.getFontMetrics();
                String msg = "Belum ada data pendapatan mingguan.";
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                g2.dispose();
                return;
            }

            // Draw Title
            g2.setColor(Theme.TEXT_ORANGE);
            g2.setFont(Theme.FONT_SUBTITLE);
            g2.drawString("Tren Pendapatan Harian (7 Hari Terakhir)", 30, 45);

            // Find max value
            int maxRev = 0;
            for (int r : data.values()) {
                if (r > maxRev) maxRev = r;
            }
            if (maxRev == 0) maxRev = 100;

            int chartX = 80;
            int chartY = 80;
            int chartW = w - 140;
            int chartH = h - 160;

            // Draw grid lines
            g2.setColor(new Color(255, 255, 255, 20));
            g2.setStroke(new BasicStroke(1.0f));
            int numGridLines = 4;
            g2.setFont(Theme.FONT_SMALL);
            for (int i = 0; i <= numGridLines; i++) {
                float pct = (float) i / numGridLines;
                int y = chartY + chartH - (int) (pct * chartH);
                g2.drawLine(chartX, y, chartX + chartW, y);
                
                int val = (int) (pct * maxRev);
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.drawString("Rp " + val + "rb", chartX - 70, y + 5);
                g2.setColor(new Color(255, 255, 255, 20));
            }

            // Draw Axis
            g2.setColor(Theme.BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(chartX, chartY, chartX, chartY + chartH);
            g2.drawLine(chartX, chartY + chartH, chartX + chartW, chartY + chartH);

            // Plot line path
            int numPoints = data.size();
            int stepX = chartW / (numPoints - 1);
            int[] pointsX = new int[numPoints];
            int[] pointsY = new int[numPoints];

            int index = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int rev = entry.getValue();
                float ratio = (float) rev / maxRev;
                pointsX[index] = chartX + index * stepX;
                pointsY[index] = chartY + chartH - (int) (ratio * chartH);
                index++;
            }

            // Fill area under line with translucent gradient
            java.awt.geom.Path2D area = new java.awt.geom.Path2D.Float();
            area.moveTo(pointsX[0], chartY + chartH);
            for (int i = 0; i < numPoints; i++) {
                area.lineTo(pointsX[i], pointsY[i]);
            }
            area.lineTo(pointsX[numPoints - 1], chartY + chartH);
            area.closePath();

            GradientPaint gp = new GradientPaint(
                0, chartY, new Color(0x00, 0xD2, 0xFF, 60),
                0, chartY + chartH, new Color(0, 0, 0, 0)
            );
            g2.setPaint(gp);
            g2.fill(area);

            // Draw line
            g2.setColor(Theme.BORDER_FOCUS);
            g2.setStroke(new BasicStroke(3.0f));
            for (int i = 0; i < numPoints - 1; i++) {
                g2.drawLine(pointsX[i], pointsY[i], pointsX[i+1], pointsY[i+1]);
            }

            // Draw nodes and label values
            index = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                String label = entry.getKey();
                int rev = entry.getValue();
                int x = pointsX[index];
                int y = pointsY[index];

                g2.setColor(Theme.BORDER_FOCUS);
                g2.fillOval(x - 6, y - 6, 12, 12);
                g2.setColor(Color.WHITE);
                g2.fillOval(x - 3, y - 3, 6, 6);

                g2.setFont(Theme.FONT_SMALL);
                g2.setColor(Theme.TEXT_PRICE);
                String valStr = "Rp " + rev + "rb";
                int valStrW = g2.getFontMetrics().stringWidth(valStr);
                g2.drawString(valStr, x - valStrW / 2, y - 10);

                g2.setColor(Color.WHITE);
                int labelW = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x - labelW / 2, chartY + chartH + 20);

                index++;
            }

            g2.dispose();
        }
    }
}
