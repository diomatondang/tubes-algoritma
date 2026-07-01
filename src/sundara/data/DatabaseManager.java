package sundara.data;

import sundara.model.Category;
import sundara.model.MenuItem;
import sundara.model.OrderItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Handles all database operations for the POS system.
 * Connects to a MySQL/MariaDB database (e.g. running in Laragon).
 * Supports properties file external configuration and fallback handling.
 */
public class DatabaseManager {

    private static final String PROPERTIES_FILE = "db.properties";
    private static Properties dbProperties = new Properties();
    private static boolean isDriverLoaded = false;

    static {
        // Load configurations
        File propFile = new File(PROPERTIES_FILE);
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                dbProperties.load(fis);
            } catch (IOException e) {
                System.err.println("Gagal membaca db.properties: " + e.getMessage());
            }
        } else {
            // Default Laragon configurations
            dbProperties.setProperty("db.url", "jdbc:mysql://localhost:3306/sundara_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8");
            dbProperties.setProperty("db.user", "root");
            dbProperties.setProperty("db.password", "");
        }

        // Run migrations in background thread to prevent GUI startup lag if MySQL is offline
        new Thread(() -> {
            try {
                runMigrations();
            } catch (Exception e) {
                System.err.println("Info: Migrasi database dilewati/gagal (DB Offline): " + e.getMessage());
            }
        }).start();
    }

    /**
     * Loads the MySQL JDBC driver.
     */
    private static synchronized void loadDriver() throws SQLException {
        if (isDriverLoaded) return;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            isDriverLoaded = true;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL JDBC tidak ditemukan di Classpath! Pastikan mysql-connector-j ada di folder lib.", e);
        }
    }

    /**
     * Obtains a connection to the database.
     */
    public static Connection getConnection() throws SQLException {
        loadDriver();
        String url = dbProperties.getProperty("db.url");
        String user = dbProperties.getProperty("db.user");
        String pass = dbProperties.getProperty("db.password");
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Verifies if database connection can be established.
     */
    public static boolean checkConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database tidak terhubung: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifies user credentials against the database.
     */
    public static boolean verifyLogin(String username, String password) throws SQLException {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbPass = rs.getString("password");
                    // plaintext check to match the existing login logic
                    return dbPass.equals(password);
                }
            }
        }
        return false;
    }

    /**
     * Fetches all menu items from the database.
     */
    public static List<MenuItem> getMenuItems() throws SQLException {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT emoji, name, price, category, stock FROM menu_items ORDER BY id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String emoji = rs.getString("emoji");
                String name = rs.getString("name");
                int price = rs.getInt("price");
                String categoryStr = rs.getString("category");
                int stock = rs.getInt("stock");
                
                Category category;
                try {
                    category = Category.valueOf(categoryStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Fail-safe default
                    category = Category.SIGNATURE;
                }
                
                items.add(new MenuItem(emoji, name, price, category, stock));
            }
        }
        return items;
    }

    /**
     * Saves an order and its line items inside a database transaction.
     */
    public static void saveOrder(String transactionId, String customerName, String tableNumber,
                                 String paymentMethod, String referenceNumber, int totalAmount,
                                 Integer cashAmount, Integer changeAmount, List<OrderItem> items,
                                 String promoCode, int discountAmount) throws SQLException {
        String insertOrderSql = "INSERT INTO orders (transaction_id, transaction_date, customer_name, table_number, payment_method, reference_number, total_amount, cash_amount, change_amount, promo_code, discount_amount) VALUES (?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO order_items (order_id, menu_item_name, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psItem = null;
        PreparedStatement psStock = null;
        ResultSet generatedKeys = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Transaction boundary start

            // 1. Check and deduct stock atomically
            String deductStockSql = "UPDATE menu_items SET stock = stock - ? WHERE name = ? AND stock >= ?";
            psStock = conn.prepareStatement(deductStockSql);
            for (OrderItem item : items) {
                psStock.setInt(1, item.getQuantity());
                psStock.setString(2, item.getMenuItem().getName());
                psStock.setInt(3, item.getQuantity());
                int rows = psStock.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Stok tidak mencukupi untuk item: " + item.getMenuItem().getName());
                }
            }

            // 2. Save order details
            psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setString(1, transactionId);
            psOrder.setString(2, customerName);
            psOrder.setString(3, tableNumber);
            psOrder.setString(4, paymentMethod);
            psOrder.setString(5, referenceNumber);
            psOrder.setInt(6, totalAmount);
            
            if (cashAmount != null) {
                psOrder.setInt(7, cashAmount);
            } else {
                psOrder.setNull(7, Types.INTEGER);
            }
            
            if (changeAmount != null) {
                psOrder.setInt(8, changeAmount);
            } else {
                psOrder.setNull(8, Types.INTEGER);
            }
            
            if (promoCode != null && !promoCode.trim().isEmpty()) {
                psOrder.setString(9, promoCode);
            } else {
                psOrder.setNull(9, Types.VARCHAR);
            }
            psOrder.setInt(10, discountAmount);
            
            psOrder.executeUpdate();
            generatedKeys = psOrder.getGeneratedKeys();
            
            int orderId;
            if (generatedKeys.next()) {
                orderId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Gagal mendapatkan generated key ID dari tabel orders.");
            }

            psItem = conn.prepareStatement(insertItemSql);
            for (OrderItem item : items) {
                psItem.setInt(1, orderId);
                psItem.setString(2, item.getMenuItem().getName());
                psItem.setInt(3, item.getQuantity());
                psItem.setInt(4, item.getMenuItem().getPrice());
                psItem.setInt(5, item.getSubtotal());
                psItem.addBatch();
            }
            psItem.executeBatch();

            conn.commit(); // Transaction commit
            System.out.println("Transaksi " + transactionId + " berhasil disimpan ke database.");
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Transaction rollback on error
                    System.err.println("Transaksi di-rollback akibat error database.");
                } catch (SQLException ex) {
                    System.err.println("Gagal melakukan rollback: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (generatedKeys != null) {
                try { generatedKeys.close(); } catch (SQLException ignored) {}
            }
            if (psItem != null) {
                try { psItem.close(); } catch (SQLException ignored) {}
            }
            if (psStock != null) {
                try { psStock.close(); } catch (SQLException ignored) {}
            }
            if (psOrder != null) {
                try { psOrder.close(); } catch (SQLException ignored) {}
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Fetches all sales orders from the database, optional search filter.
     */
    public static List<Map<String, Object>> getSalesList(String search) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id, transaction_id, transaction_date, customer_name, table_number, payment_method, reference_number, total_amount, cash_amount, change_amount FROM orders ";
        if (search != null && !search.trim().isEmpty()) {
            sql += "WHERE transaction_id LIKE ? OR customer_name LIKE ? ";
        }
        sql += "ORDER BY transaction_date DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (search != null && !search.trim().isEmpty()) {
                String searchLike = "%" + search.trim() + "%";
                ps.setString(1, searchLike);
                ps.setString(2, searchLike);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("transaction_id", rs.getString("transaction_id"));
                    row.put("transaction_date", rs.getTimestamp("transaction_date"));
                    row.put("customer_name", rs.getString("customer_name"));
                    row.put("table_number", rs.getString("table_number"));
                    row.put("payment_method", rs.getString("payment_method"));
                    row.put("reference_number", rs.getString("reference_number"));
                    row.put("total_amount", rs.getInt("total_amount"));
                    row.put("cash_amount", rs.getObject("cash_amount"));
                    row.put("change_amount", rs.getObject("change_amount"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /**
     * Fetches details of ordered items for a specific transaction.
     */
    public static List<Map<String, Object>> getOrderItems(int orderId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT menu_item_name, quantity, price, subtotal FROM order_items WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("name", rs.getString("menu_item_name"));
                    row.put("quantity", rs.getInt("quantity"));
                    row.put("price", rs.getInt("price"));
                    row.put("subtotal", rs.getInt("subtotal"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /**
     * Calculates statistics of today's sales (revenue, transaction count, best seller).
     */
    public static Map<String, Object> getSalesStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String sqlRevenue = "SELECT SUM(total_amount) as total FROM orders WHERE DATE(transaction_date) = CURDATE()";
        String sqlCount = "SELECT COUNT(*) as count FROM orders WHERE DATE(transaction_date) = CURDATE()";
        String sqlBest = "SELECT menu_item_name, SUM(quantity) as qty FROM order_items oi JOIN orders o ON oi.order_id = o.id WHERE DATE(o.transaction_date) = CURDATE() GROUP BY menu_item_name ORDER BY qty DESC LIMIT 1";

        try (Connection conn = getConnection()) {
            // Revenue
            try (PreparedStatement ps = conn.prepareStatement(sqlRevenue);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("today_revenue", rs.getInt("total"));
                } else {
                    stats.put("today_revenue", 0);
                }
            }
            // Count
            try (PreparedStatement ps = conn.prepareStatement(sqlCount);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("today_count", rs.getInt("count"));
                } else {
                    stats.put("today_count", 0);
                }
            }
            // Best seller
            try (PreparedStatement ps = conn.prepareStatement(sqlBest);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.put("best_seller", rs.getString("menu_item_name"));
                } else {
                    stats.put("best_seller", "-");
                }
            }
        }
        return stats;
    }

    // ── MENU ITEM CRUD OPERATIONS ────────────────────────────────────

    public static void addMenuItem(String emoji, String name, int price, String category, int stock) throws SQLException {
        String sql = "INSERT INTO menu_items (emoji, name, price, category, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emoji);
            ps.setString(2, name);
            ps.setInt(3, price);
            ps.setString(4, category);
            ps.setInt(5, stock);
            ps.executeUpdate();
        }
    }

    public static void updateMenuItem(String oldName, String emoji, String name, int price, String category, int stock) throws SQLException {
        String sql = "UPDATE menu_items SET emoji = ?, name = ?, price = ?, category = ?, stock = ? WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emoji);
            ps.setString(2, name);
            ps.setInt(3, price);
            ps.setString(4, category);
            ps.setInt(5, stock);
            ps.setString(6, oldName);
            ps.executeUpdate();
        }
    }

    public static void deleteMenuItem(String name) throws SQLException {
        String sql = "DELETE FROM menu_items WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    // ── CASHIER CRUD OPERATIONS ──────────────────────────────────────

    public static List<String> getUsernames() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT username FROM users ORDER BY username ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString("username"));
            }
        }
        return list;
    }

    public static void addUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
        }
    }

    public static void updateUserPassword(String username, String password) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, password);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }

    public static void deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    // ── CATEGORY SALES STATS FOR ANALYTICS ───────────────────────────

    public static Map<String, Integer> getCategorySalesStats() throws SQLException {
        Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT mi.category, SUM(oi.quantity) as qty " +
                     "FROM order_items oi " +
                     "JOIN orders o ON oi.order_id = o.id " +
                     "JOIN menu_items mi ON oi.menu_item_name = mi.name " +
                     "WHERE DATE(o.transaction_date) = CURDATE() " +
                     "GROUP BY mi.category " +
                     "ORDER BY qty DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String cat = rs.getString("category");
                int qty = rs.getInt("qty");
                
                Category categoryEnum = null;
                try {
                    categoryEnum = Category.valueOf(cat.toUpperCase());
                } catch (Exception ignored) {}
                
                String displayName = categoryEnum != null ? categoryEnum.getDisplayName() : cat;
                stats.put(displayName, qty);
            }
        }
        return stats;
    }

    // ── MIGRATIONS ───────────────────────────────────────────────────
    private static void runMigrations() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 1. Add stock to menu_items
            try {
                stmt.executeUpdate("ALTER TABLE menu_items ADD COLUMN stock INT NOT NULL DEFAULT 99");
                System.out.println("Migrasi: Kolom 'stock' berhasil ditambahkan ke 'menu_items'.");
            } catch (SQLException e) {
                if (e.getErrorCode() != 1060) {
                    System.out.println("Info Migrasi (stock): " + e.getMessage());
                }
            }

            // 2. Add promo columns to orders
            try {
                stmt.executeUpdate("ALTER TABLE orders ADD COLUMN promo_code VARCHAR(20) DEFAULT NULL");
                System.out.println("Migrasi: Kolom 'promo_code' berhasil ditambahkan ke 'orders'.");
            } catch (SQLException e) {
                if (e.getErrorCode() != 1060) {
                    System.out.println("Info Migrasi (promo_code): " + e.getMessage());
                }
            }
            try {
                stmt.executeUpdate("ALTER TABLE orders ADD COLUMN discount_amount INT DEFAULT 0");
                System.out.println("Migrasi: Kolom 'discount_amount' berhasil ditambahkan ke 'orders'.");
            } catch (SQLException e) {
                if (e.getErrorCode() != 1060) {
                    System.out.println("Info Migrasi (discount_amount): " + e.getMessage());
                }
            }

            // 3. Create promo_codes table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS promo_codes (" +
                               "code VARCHAR(20) PRIMARY KEY, " +
                               "discount_percent INT NOT NULL, " +
                               "description VARCHAR(100)" +
                               ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Seed default promo codes
            stmt.executeUpdate("INSERT IGNORE INTO promo_codes (code, discount_percent, description) VALUES " +
                               "('SUNDARA10', 10, 'Diskon 10% Spesial Pembukaan'), " +
                               "('DISKON20', 20, 'Diskon 20% Khusus Member'), " +
                               "('SUNDARAMATURITY', 50, 'Diskon 50% Ultah Sundara Coffee')");
        }
    }

    // ── PROMO & WEEKLY STATS ─────────────────────────────────────────
    public static Integer getPromoDiscount(String code) throws SQLException {
        String sql = "SELECT discount_percent FROM promo_codes WHERE UPPER(code) = UPPER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("discount_percent");
                }
            }
        }
        return null;
    }

    public static Map<String, Integer> getWeeklyRevenueStats() throws SQLException {
        Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        String sql = "SELECT DATE(transaction_date) as d, SUM(total_amount) as total " +
                     "FROM orders " +
                     "WHERE transaction_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                     "GROUP BY DATE(transaction_date) " +
                     "ORDER BY DATE(transaction_date) ASC";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            // Pre-populate last 7 days with 0
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM", new java.util.Locale("id", "ID"));
            for (int i = 6; i >= 0; i--) {
                stats.put(today.minusDays(i).format(fmt), 0);
            }
            
            while (rs.next()) {
                java.sql.Date date = rs.getDate("d");
                int total = rs.getInt("total");
                if (date != null) {
                    String formattedDate = date.toLocalDate().format(fmt);
                    stats.put(formattedDate, total);
                }
            }
        }
        return stats;
    }
}
