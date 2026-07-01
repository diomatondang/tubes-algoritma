package sundara.ui;

import sundara.data.DatabaseManager;
import sundara.data.ReceiptSettings;
import sundara.model.Category;
import sundara.model.MenuItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Settings panel for Admin to perform Menu CRUD, Cashier CRUD, and customize Receipts.
 */
public class AdminSettingsDialog extends JDialog {

    private final Runnable onMenuChanged; // callback to refresh MainFrame menu list

    // Tab 1: Menu CRUD Components
    private JTable menuTable;
    private DefaultTableModel menuModel;
    private JTextField menuEmojiField;
    private JTextField menuNameField;
    private JTextField menuPriceField;
    private JTextField menuStockField;
    private JComboBox<Category> menuCategoryCombo;
    private List<MenuItem> currentMenuList = new ArrayList<>();
    private String selectedMenuOldName = null;

    // Tab 2: Cashier CRUD Components
    private JTable userTable;
    private DefaultTableModel userModel;
    private JTextField userField;
    private JPasswordField passField;
    private List<String> currentUserList = new ArrayList<>();

    // Tab 3: Receipt Config Components
    private JTextField cafeNameField;
    private JTextField cafeAddressField;
    private JTextField cafePhoneField;
    private JTextField receiptFooterField;

    public AdminSettingsDialog(JFrame parent, Runnable onMenuChanged) {
        super(parent, "Pengaturan POS (Admin) — Sundara CoffeeSpace", true);
        this.onMenuChanged = onMenuChanged;
        setSize(850, 550);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_SECONDARY);
        setLayout(new BorderLayout(10, 10));

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

        // Header
        JLabel headerLabel = new JLabel("⚙️ Pengaturan Administrasi POS");
        headerLabel.setFont(Theme.FONT_TITLE);
        headerLabel.setForeground(Theme.TEXT_ORANGE);
        headerLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        add(headerLabel, BorderLayout.NORTH);

        // Tabbed Pane Setup
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Theme.BG_CARD);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFont(Theme.FONT_BTN);

        tabbedPane.addTab("🍔 Kelola Menu", buildMenuManagerTab());
        tabbedPane.addTab("👤 Kelola Kasir", buildCashierTab());
        tabbedPane.addTab("🧾 Kustomisasi Struk", buildReceiptConfigTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setOpaque(false);
        JButton closeBtn = new JButton("Tutup");
        closeBtn.setFont(Theme.FONT_BTN);
        closeBtn.setBackground(Theme.BTN_DANGER);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load data initially
        refreshMenuData();
        refreshUserData();
        loadReceiptSettings();
    }

    // ── TAB 1: MENU MANAGER ──────────────────────────────────────────
    private JPanel buildMenuManagerTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Table
        String[] cols = {"Emoji", "Nama Menu", "Harga (Rp)", "Kategori", "Stok"};
        menuModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        menuTable = new JTable(menuModel);
        menuTable.setBackground(Theme.BG_CARD);
        menuTable.setForeground(Color.WHITE);
        menuTable.setGridColor(Theme.BORDER);
        menuTable.setSelectionBackground(Theme.BTN_PRIMARY);
        menuTable.setSelectionForeground(Color.WHITE);
        menuTable.setRowHeight(28);
        menuTable.setFont(Theme.FONT_BODY);
        menuTable.getTableHeader().setBackground(Theme.BG_SECONDARY);
        menuTable.getTableHeader().setForeground(Theme.TEXT_ORANGE);
        menuTable.getTableHeader().setFont(Theme.FONT_BTN);
        menuTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectMenuItemRow();
            }
        });

        JScrollPane scroll = new JScrollPane(menuTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        styleScrollBar(scroll.getVerticalScrollBar());

        // Side Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(280, 0));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        menuEmojiField = makeFormTextField();
        menuNameField = makeFormTextField();
        menuPriceField = makeFormTextField();
        menuStockField = makeFormTextField();
        menuCategoryCombo = new JComboBox<>(Category.values());
        menuCategoryCombo.setBackground(Theme.BG_CARD);
        menuCategoryCombo.setForeground(Color.WHITE);
        menuCategoryCombo.setFont(Theme.FONT_BODY);

        gc.gridx=0; gc.gridy=0; gc.weightx=0.3; form.add(makeFormLabel("Emoji:"), gc);
        gc.gridx=1; gc.weightx=0.7; form.add(menuEmojiField, gc);

        gc.gridx=0; gc.gridy=1; form.add(makeFormLabel("Nama:"), gc);
        gc.gridx=1; form.add(menuNameField, gc);

        gc.gridx=0; gc.gridy=2; form.add(makeFormLabel("Harga (rb):"), gc);
        gc.gridx=1; form.add(menuPriceField, gc);

        gc.gridx=0; gc.gridy=3; form.add(makeFormLabel("Kategori:"), gc);
        gc.gridx=1; form.add(menuCategoryCombo, gc);

        gc.gridx=0; gc.gridy=4; form.add(makeFormLabel("Stok:"), gc);
        gc.gridx=1; form.add(menuStockField, gc);

        // Buttons
        JPanel btnRow = new JPanel(new GridLayout(3, 1, 0, 8));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton addBtn = makeFormButton("➕ Tambah Menu Baru", Theme.BTN_SUCCESS);
        JButton updateBtn = makeFormButton("💾 Simpan Perubahan", Theme.BTN_PRIMARY);
        JButton deleteBtn = makeFormButton("🗑️ Hapus Menu Terpilih", Theme.BTN_DANGER);

        addBtn.addActionListener(e -> addMenuItem());
        updateBtn.addActionListener(e -> updateMenuItem());
        deleteBtn.addActionListener(e -> deleteMenuItem());

        btnRow.add(addBtn);
        btnRow.add(updateBtn);
        btnRow.add(deleteBtn);

        gc.gridx=0; gc.gridy=5; gc.gridwidth=2; form.add(btnRow, gc);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(form, BorderLayout.EAST);
        return panel;
    }

    // ── TAB 2: CASHIER MANAGER ───────────────────────────────────────
    private JPanel buildCashierTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Table
        String[] cols = {"Daftar Akun Kasir"};
        userModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(userModel);
        userTable.setBackground(Theme.BG_CARD);
        userTable.setForeground(Color.WHITE);
        userTable.setGridColor(Theme.BORDER);
        userTable.setSelectionBackground(Theme.BTN_PRIMARY);
        userTable.setSelectionForeground(Color.WHITE);
        userTable.setRowHeight(28);
        userTable.setFont(Theme.FONT_BODY);
        userTable.getTableHeader().setBackground(Theme.BG_SECONDARY);
        userTable.getTableHeader().setForeground(Theme.TEXT_ORANGE);
        userTable.getTableHeader().setFont(Theme.FONT_BTN);
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectUserRow();
            }
        });

        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        styleScrollBar(scroll.getVerticalScrollBar());

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(280, 0));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 6, 8, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        userField = makeFormTextField();
        passField = new JPasswordField();
        passField.setBackground(Theme.BG_CARD);
        passField.setForeground(Color.WHITE);
        passField.setCaretColor(Color.WHITE);
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));

        gc.gridx=0; gc.gridy=0; gc.weightx=0.3; form.add(makeFormLabel("Username:"), gc);
        gc.gridx=1; gc.weightx=0.7; form.add(userField, gc);

        gc.gridx=0; gc.gridy=1; form.add(makeFormLabel("Password:"), gc);
        gc.gridx=1; form.add(passField, gc);

        JPanel btnRow = new JPanel(new GridLayout(3, 1, 0, 8));
        btnRow.setOpaque(false);
        btnRow.setBorder(new EmptyBorder(14, 0, 0, 0));

        JButton addUserBtn = makeFormButton("👤 Tambah Kasir Baru", Theme.BTN_SUCCESS);
        JButton updatePassBtn = makeFormButton("🔑 Ganti Password Staf", Theme.BTN_PRIMARY);
        JButton deleteUserBtn = makeFormButton("🗑️ Hapus Kasir Terpilih", Theme.BTN_DANGER);

        addUserBtn.addActionListener(e -> addUser());
        updatePassBtn.addActionListener(e -> updatePassword());
        deleteUserBtn.addActionListener(e -> deleteUser());

        btnRow.add(addUserBtn);
        btnRow.add(updatePassBtn);
        btnRow.add(deleteUserBtn);

        gc.gridx=0; gc.gridy=2; gc.gridwidth=2; form.add(btnRow, gc);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(form, BorderLayout.EAST);
        return panel;
    }

    // ── TAB 3: RECEIPT CONFIGURATION ─────────────────────────────────
    private JPanel buildReceiptConfigTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;

        cafeNameField = makeFormTextField();
        cafeAddressField = makeFormTextField();
        cafePhoneField = makeFormTextField();
        receiptFooterField = makeFormTextField();

        gc.gridx=0; gc.gridy=0; gc.weightx=0.25; panel.add(makeFormLabel("Nama Toko/Kafe:"), gc);
        gc.gridx=1; gc.weighty=0; gc.weightx=0.75; panel.add(cafeNameField, gc);

        gc.gridx=0; gc.gridy=1; panel.add(makeFormLabel("Alamat Kafe:"), gc);
        gc.gridx=1; panel.add(cafeAddressField, gc);

        gc.gridx=0; gc.gridy=2; panel.add(makeFormLabel("Telepon / Hubungi:"), gc);
        gc.gridx=1; panel.add(cafePhoneField, gc);

        gc.gridx=0; gc.gridy=3; panel.add(makeFormLabel("Catatan Kaki (Footer):"), gc);
        gc.gridx=1; panel.add(receiptFooterField, gc);

        JButton saveBtn = new JButton("💾 Simpan Pengaturan Struk");
        saveBtn.setFont(Theme.FONT_BTN);
        saveBtn.setBackground(Theme.BTN_PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(new EmptyBorder(12, 20, 12, 20));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.addActionListener(e -> saveReceiptSettings());

        gc.gridx=0; gc.gridy=4; gc.gridwidth=2; 
        gc.insets = new Insets(20, 10, 10, 10);
        panel.add(saveBtn, gc);

        return panel;
    }

    // ── CRUD OPERATIONS: MENU ────────────────────────────────────────
    private void refreshMenuData() {
        try {
            currentMenuList = DatabaseManager.getMenuItems();
            menuModel.setRowCount(0);
            for (MenuItem item : currentMenuList) {
                menuModel.addRow(new Object[]{
                        item.getEmoji(),
                        item.getName(),
                        item.getPrice() + ".000",
                        item.getCategory().getDisplayName(),
                        item.getStock()
                });
            }
            clearMenuFields();
        } catch (Exception e) {
            System.err.println("Gagal memuat daftar menu admin: " + e.getMessage());
        }
    }

    private void clearMenuFields() {
        menuEmojiField.setText("");
        menuNameField.setText("");
        menuPriceField.setText("");
        menuStockField.setText("");
        menuCategoryCombo.setSelectedIndex(0);
        selectedMenuOldName = null;
        menuTable.clearSelection();
    }

    private void selectMenuItemRow() {
        int idx = menuTable.getSelectedRow();
        if (idx < 0 || idx >= currentMenuList.size()) return;

        MenuItem item = currentMenuList.get(idx);
        menuEmojiField.setText(item.getEmoji());
        menuNameField.setText(item.getName());
        menuPriceField.setText(String.valueOf(item.getPrice()));
        menuStockField.setText(String.valueOf(item.getStock()));
        menuCategoryCombo.setSelectedItem(item.getCategory());
        selectedMenuOldName = item.getName();
    }

    private void addMenuItem() {
        String emoji = menuEmojiField.getText().trim();
        String name = menuNameField.getText().trim();
        String priceStr = menuPriceField.getText().trim();
        String stockStr = menuStockField.getText().trim();
        Category cat = (Category) menuCategoryCombo.getSelectedItem();

        if (emoji.isEmpty() || name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || cat == null) {
            warn("Isi seluruh kolom terlebih dahulu!");
            return;
        }

        try {
            int price = Integer.parseInt(priceStr);
            int stock = Integer.parseInt(stockStr);
            DatabaseManager.addMenuItem(emoji, name, price, cat.name(), stock);
            refreshMenuData();
            if (onMenuChanged != null) onMenuChanged.run();
            info("Menu baru berhasil ditambahkan!");
        } catch (NumberFormatException e) {
            warn("Harga dan stok harus berupa bilangan bulat!");
        } catch (Exception e) {
            warn("Gagal menambahkan menu: " + e.getMessage());
        }
    }

    private void updateMenuItem() {
        if (selectedMenuOldName == null) {
            warn("Pilih menu yang ingin diedit dari tabel terlebih dahulu!");
            return;
        }

        String emoji = menuEmojiField.getText().trim();
        String name = menuNameField.getText().trim();
        String priceStr = menuPriceField.getText().trim();
        String stockStr = menuStockField.getText().trim();
        Category cat = (Category) menuCategoryCombo.getSelectedItem();

        if (emoji.isEmpty() || name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || cat == null) {
            warn("Isi seluruh kolom terlebih dahulu!");
            return;
        }

        try {
            int price = Integer.parseInt(priceStr);
            int stock = Integer.parseInt(stockStr);
            DatabaseManager.updateMenuItem(selectedMenuOldName, emoji, name, price, cat.name(), stock);
            refreshMenuData();
            if (onMenuChanged != null) onMenuChanged.run();
            info("Menu berhasil diperbarui!");
        } catch (NumberFormatException e) {
            warn("Harga dan stok harus berupa bilangan bulat!");
        } catch (Exception e) {
            warn("Gagal mengupdate menu: " + e.getMessage());
        }
    }

    private void deleteMenuItem() {
        if (selectedMenuOldName == null) {
            warn("Pilih menu yang ingin dihapus dari tabel terlebih dahulu!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin menghapus menu '" + selectedMenuOldName + "'?", 
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            DatabaseManager.deleteMenuItem(selectedMenuOldName);
            refreshMenuData();
            if (onMenuChanged != null) onMenuChanged.run();
            info("Menu berhasil dihapus!");
        } catch (Exception e) {
            warn("Gagal menghapus menu: " + e.getMessage());
        }
    }

    // ── CRUD OPERATIONS: CASHIER ─────────────────────────────────────
    private void refreshUserData() {
        try {
            currentUserList = DatabaseManager.getUsernames();
            userModel.setRowCount(0);
            for (String username : currentUserList) {
                userModel.addRow(new Object[]{username});
            }
            clearUserFields();
        } catch (Exception e) {
            System.err.println("Gagal memuat kasir admin: " + e.getMessage());
        }
    }

    private void clearUserFields() {
        userField.setText("");
        passField.setText("");
        userTable.clearSelection();
    }

    private void selectUserRow() {
        int idx = userTable.getSelectedRow();
        if (idx < 0 || idx >= currentUserList.size()) return;
        userField.setText(currentUserList.get(idx));
        passField.setText("");
    }

    private void addUser() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            warn("Isi username dan password terlebih dahulu!");
            return;
        }

        try {
            DatabaseManager.addUser(username, password);
            refreshUserData();
            info("Kasir baru berhasil ditambahkan!");
        } catch (Exception e) {
            warn("Username sudah terpakai atau terjadi error: " + e.getMessage());
        }
    }

    private void updatePassword() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            warn("Pilih kasir dan isi password baru terlebih dahulu!");
            return;
        }

        try {
            DatabaseManager.updateUserPassword(username, password);
            refreshUserData();
            info("Password kasir '" + username + "' berhasil diperbarui!");
        } catch (Exception e) {
            warn("Gagal mengupdate password: " + e.getMessage());
        }
    }

    private void deleteUser() {
        String username = userField.getText().trim();
        if (username.isEmpty()) {
            warn("Pilih kasir yang ingin dihapus dari tabel terlebih dahulu!");
            return;
        }

        if (username.equalsIgnoreCase("admin")) {
            warn("Akun 'admin' utama tidak boleh dihapus demi keamanan!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin menghapus kasir '" + username + "'?", 
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            DatabaseManager.deleteUser(username);
            refreshUserData();
            info("Kasir '" + username + "' berhasil dihapus!");
        } catch (Exception e) {
            warn("Gagal menghapus kasir: " + e.getMessage());
        }
    }

    // ── SETTINGS LOAD & SAVE: RECEIPT ────────────────────────────────
    private void loadReceiptSettings() {
        cafeNameField.setText(ReceiptSettings.getCafeName());
        cafeAddressField.setText(ReceiptSettings.getAddress());
        cafePhoneField.setText(ReceiptSettings.getPhone());
        receiptFooterField.setText(ReceiptSettings.getFooter());
    }

    private void saveReceiptSettings() {
        String name = cafeNameField.getText().trim();
        String addr = cafeAddressField.getText().trim();
        String tel = cafePhoneField.getText().trim();
        String foot = receiptFooterField.getText().trim();

        if (name.isEmpty() || addr.isEmpty() || tel.isEmpty() || foot.isEmpty()) {
            warn("Semua parameter struk wajib diisi!");
            return;
        }

        ReceiptSettings.setSettings(name, addr, tel, foot);
        info("Pengaturan struk berhasil disimpan!");
    }

    // ── FORM COMPONENT STYLING HELPERS ───────────────────────────────
    private JTextField makeFormTextField() {
        JTextField tf = new JTextField();
        tf.setBackground(Theme.BG_CARD);
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(Theme.FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    private JLabel makeFormLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }

    private JButton makeFormButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Sukses", JOptionPane.INFORMATION_MESSAGE);
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
}
