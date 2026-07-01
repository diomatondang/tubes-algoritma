-- Database Initialization Script for Sundara CoffeeSpace POS
-- Import this file into your Laragon MySQL server

CREATE DATABASE IF NOT EXISTS sundara_db;
USE sundara_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default user (admin / sundara2025)
INSERT INTO users (username, password) 
VALUES ('admin', '12345') 
ON DUPLICATE KEY UPDATE password=VALUES(password);

-- 2. Menu Items Table
CREATE TABLE IF NOT EXISTS menu_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    emoji VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    name VARCHAR(100) UNIQUE NOT NULL,
    price INT NOT NULL,
    category VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed default menu items
INSERT INTO menu_items (emoji, name, price, category) VALUES
-- SIGNATURE
('☕', 'Es Kopi Sundara', 20, 'SIGNATURE'),
('🍵', 'Bitter Sweet', 25, 'SIGNATURE'),
('⚡', 'Black Acid', 25, 'SIGNATURE'),
('🍫', 'Choco Berry', 25, 'SIGNATURE'),
('🌅', 'Mango Sunrise', 25, 'SIGNATURE'),
('🦋', 'Night Butterfly', 25, 'SIGNATURE'),

-- COFFEE
('🍨', 'Affogato', 25, 'COFFEE'),
('🍮', 'Creme Brulle', 25, 'COFFEE'),
('✨', 'Magic Coffee', 25, 'COFFEE'),
('☕', 'Filter Coffee', 25, 'COFFEE'),
('🧈', 'Butterscotch Latte', 25, 'COFFEE'),
('🍦', 'Vanilla Latte', 25, 'COFFEE'),
('☕', 'Cappucino', 23, 'COFFEE'),
('🥛', 'Latte', 23, 'COFFEE'),
('🖤', 'Americano', 20, 'COFFEE'),
('🇻🇳', 'Vietnam Drip', 18, 'COFFEE'),
('🫙', 'Kopi Sanger', 18, 'COFFEE'),

-- NON COFFEE
('🖤', 'Black Charcoal', 25, 'NON_COFFEE'),
('🍵', 'Matcha', 25, 'NON_COFFEE'),
('🍫', 'Chocolate', 23, 'NON_COFFEE'),
('🧋', 'Taro', 23, 'NON_COFFEE'),
('🍓', 'Berry Yakult', 20, 'NON_COFFEE'),
('🫧', 'Bubblegum Yakult', 20, 'NON_COFFEE'),
('🍈', 'Lychee Yakult', 20, 'NON_COFFEE'),
('🥭', 'Mango Tea', 18, 'NON_COFFEE'),
('🍈', 'Lychee Tea', 18, 'NON_COFFEE'),
('🍌', 'Banana Lava', 18, 'NON_COFFEE'),
('🫖', 'Black Tea', 12, 'NON_COFFEE'),
('💧', 'Mineral Water', 7, 'NON_COFFEE'),

-- RICE MEALS
('🍗', 'Sundara Chicken Rice Bowl', 26, 'RICE_MEALS'),
('🥩', 'Sundara Beef Rice Bowl', 30, 'RICE_MEALS'),
('🍗', 'Yakiniku Chicken Rice Bowl', 26, 'RICE_MEALS'),
('🥩', 'Yakiniku Beef Rice Bowl', 30, 'RICE_MEALS'),
('🍛', 'Curry Chicken Rice Bowl', 26, 'RICE_MEALS'),
('🍛', 'Curry Beef Rice Bowl', 30, 'RICE_MEALS'),
('🍳', 'Nasi Goreng Sundara', 23, 'RICE_MEALS'),
('🍳', 'Nasi Telor Nugget', 18, 'RICE_MEALS'),
('🍳', 'Nasi Telor Sosis', 18, 'RICE_MEALS'),
('🍚', 'Extra Nasi', 5, 'RICE_MEALS'),
('🥚', 'Extra Telor', 5, 'RICE_MEALS'),

-- NOODLE & PASTA
('🧄', 'Aglio Olio', 25, 'NOODLE_PASTA'),
('🍝', 'Creamy Chicken Pasta', 25, 'NOODLE_PASTA'),
('🧀', 'Cheese Bolognese Pasta', 25, 'NOODLE_PASTA'),
('🍜', 'Mie Goreng Sundara', 18, 'NOODLE_PASTA'),
('🍜', 'Mie Nyemek Sundara', 18, 'NOODLE_PASTA'),
('🍜', 'Mie Nyemek Sundara Lite', 15, 'NOODLE_PASTA'),

-- SNACK & SWEET TOOTH
('🍽️', 'Mix Platter', 35, 'SNACK_SWEET'),
('🍗', 'Chicken Wings', 25, 'SNACK_SWEET'),
('🌭', 'Kentang Sosis', 22, 'SNACK_SWEET'),
('🍟', 'French Fries', 20, 'SNACK_SWEET'),
('🥡', 'Cireng Bumbu Rujak', 18, 'SNACK_SWEET'),
('🥟', 'Wonton Saus Wijen', 18, 'SNACK_SWEET'),
('🥟', 'Wonton Goreng Chili Oil', 18, 'SNACK_SWEET'),
('🍲', 'Wonton Kuah Tomyum', 18, 'SNACK_SWEET'),
('🥐', 'Risoles', 18, 'SNACK_SWEET'),
('🥞', 'Pancake Ice Cream', 23, 'SNACK_SWEET'),
('🍓', 'Pancake Strawberry Ice Cream', 25, 'SNACK_SWEET'),
('🍩', 'Donat Kentang', 16, 'SNACK_SWEET'),
('🍌', 'Pisang Coklat Aroma', 18, 'SNACK_SWEET'),
('🧀', 'Keju Aroma', 18, 'SNACK_SWEET'),
('🍦', 'Ice Cream Scoop', 15, 'SNACK_SWEET'),

-- BUNDLING
('🎁', 'Rice Bowl Chicken + Black Tea', 30, 'BUNDLING'),
('🎁', 'Rice Bowl Beef + Black Tea', 35, 'BUNDLING'),
('🎁', 'All Pasta + Black Tea', 30, 'BUNDLING'),
('🎁', 'Nasi Goreng + Black Tea', 28, 'BUNDLING'),
('🎁', 'Mie Goreng/Nyemek + Black Tea', 23, 'BUNDLING'),
('🎁', 'Nasi Telor Nugget/Sosis + Tea', 23, 'BUNDLING'),
('🎁', 'Mie Nyemek Lite + Black Tea', 20, 'BUNDLING'),
('🎁', 'Creme Brulle + Donut', 28, 'BUNDLING'),
('🎁', 'Magic + Donut', 28, 'BUNDLING'),
('🎁', 'Cappucino/Latte + Donut', 26, 'BUNDLING'),
('🎁', 'Es Kopi Sundara + Donut', 25, 'BUNDLING'),
('🎁', 'Americano + Donut', 23, 'BUNDLING'),

-- MUSMID BAR
('🥟', 'musmiD Udang (isi 4)', 18, 'MUSMID_BAR'),
('🥟', 'musmiD Ayam (isi 4)', 18, 'MUSMID_BAR'),
('🥟', 'musmiD Nori (isi 4)', 18, 'MUSMID_BAR'),
('🥟', 'musmiD Mix (isi 4)', 18, 'MUSMID_BAR'),
('🦐', 'Hakau musmiD (isi 4)', 18, 'MUSMID_BAR'),
('🥟', 'Wonton Ayam (isi 6)', 18, 'MUSMID_BAR'),
('🥚', 'Lumpia Kulit Tahu Udang', 18, 'MUSMID_BAR'),
('🥚', 'Lumpia Kulit Tahu Ayam', 18, 'MUSMID_BAR'),
('🥚', 'Lumpia Kulit Tahu Mix', 18, 'MUSMID_BAR'),
('🍞', 'Bakpao Coklat', 18, 'MUSMID_BAR'),
('🥟', 'musmiD Bakar / Mentai', 22, 'MUSMID_BAR'),
('🥟', 'musmiD Goreng Ujang Kedu', 18, 'MUSMID_BAR'),
('🥟', 'musmiD Goreng Ekado', 18, 'MUSMID_BAR'),
('🥟', 'musmiD Goreng Wonton Ayam', 18, 'MUSMID_BAR'),
('🥟', 'musmiD Goreng Lumpia Udang', 18, 'MUSMID_BAR'),
('🥟', 'musmiD Goreng Lumpia Ayam', 18, 'MUSMID_BAR'),
('🥟', 'musmiD Goreng Lumpia Mix', 18, 'MUSMID_BAR'),
('🧀', 'musmiD Goreng Keju', 20, 'MUSMID_BAR'),
('🎉', 'Party Mix 10', 45, 'MUSMID_BAR'),
('🎉', 'Party Mix 15', 65, 'MUSMID_BAR'),
('🎉', 'Party Mix 20', 85, 'MUSMID_BAR')
ON DUPLICATE KEY UPDATE price=VALUES(price), emoji=VALUES(emoji), category=VALUES(category);

-- 3. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    transaction_date DATETIME NOT NULL,
    customer_name VARCHAR(100),
    table_number VARCHAR(20),
    payment_method VARCHAR(20) NOT NULL,
    reference_number VARCHAR(100),
    total_amount INT NOT NULL,
    cash_amount INT,
    change_amount INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    menu_item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    price INT NOT NULL,
    subtotal INT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
