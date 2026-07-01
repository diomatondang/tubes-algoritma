package sundara.data;

import sundara.model.Category;
import sundara.model.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MenuData {

    public static List<MenuItem> getAllItems() {
        try {
            List<MenuItem> dbItems = DatabaseManager.getMenuItems();
            if (dbItems != null && !dbItems.isEmpty()) {
                System.out.println("Berhasil memuat daftar menu dari database MySQL.");
                return dbItems;
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat daftar menu dari database (menggunakan fallback statis): " + e.getMessage());
        }

        System.out.println("Memuat daftar menu cadangan (statis)...");
        List<MenuItem> items = new ArrayList<>();

        // ── SIGNATURE ───────────────────────────────────────────────
        items.add(new MenuItem("☕", "Es Kopi Sundara",            20, Category.SIGNATURE));
        items.add(new MenuItem("🍵", "Bitter Sweet",               25, Category.SIGNATURE));
        items.add(new MenuItem("⚡", "Black Acid",                 25, Category.SIGNATURE));
        items.add(new MenuItem("🍫", "Choco Berry",                25, Category.SIGNATURE));
        items.add(new MenuItem("🌅", "Mango Sunrise",              25, Category.SIGNATURE));
        items.add(new MenuItem("🦋", "Night Butterfly",            25, Category.SIGNATURE));

        // ── COFFEE ───────────────────────────────────────────────────
        items.add(new MenuItem("🍨", "Affogato",                   25, Category.COFFEE));
        items.add(new MenuItem("🍮", "Creme Brulle",               25, Category.COFFEE));
        items.add(new MenuItem("✨", "Magic Coffee",               25, Category.COFFEE));
        items.add(new MenuItem("☕", "Filter Coffee",              25, Category.COFFEE));
        items.add(new MenuItem("🧈", "Butterscotch Latte",         25, Category.COFFEE));
        items.add(new MenuItem("🍦", "Vanilla Latte",              25, Category.COFFEE));
        items.add(new MenuItem("☕", "Cappucino",                  23, Category.COFFEE));
        items.add(new MenuItem("🥛", "Latte",                      23, Category.COFFEE));
        items.add(new MenuItem("🖤", "Americano",                  20, Category.COFFEE));
        items.add(new MenuItem("🇻🇳", "Vietnam Drip",              18, Category.COFFEE));
        items.add(new MenuItem("🫙", "Kopi Sanger",                18, Category.COFFEE));

        // ── NON COFFEE ───────────────────────────────────────────────
        items.add(new MenuItem("🖤", "Black Charcoal",             25, Category.NON_COFFEE));
        items.add(new MenuItem("🍵", "Matcha",                     25, Category.NON_COFFEE));
        items.add(new MenuItem("🍫", "Chocolate",                  23, Category.NON_COFFEE));
        items.add(new MenuItem("🧋", "Taro",                       23, Category.NON_COFFEE));
        items.add(new MenuItem("🍓", "Berry Yakult",               20, Category.NON_COFFEE));
        items.add(new MenuItem("🫧", "Bubblegum Yakult",           20, Category.NON_COFFEE));
        items.add(new MenuItem("🍈", "Lychee Yakult",              20, Category.NON_COFFEE));
        items.add(new MenuItem("🥭", "Mango Tea",                  18, Category.NON_COFFEE));
        items.add(new MenuItem("🍈", "Lychee Tea",                 18, Category.NON_COFFEE));
        items.add(new MenuItem("🍌", "Banana Lava",                18, Category.NON_COFFEE));
        items.add(new MenuItem("🫖", "Black Tea",                  12, Category.NON_COFFEE));
        items.add(new MenuItem("💧", "Mineral Water",               7, Category.NON_COFFEE));

        // ── RICE MEALS ───────────────────────────────────────────────
        items.add(new MenuItem("🍗", "Sundara Chicken Rice Bowl",  26, Category.RICE_MEALS));
        items.add(new MenuItem("🥩", "Sundara Beef Rice Bowl",     30, Category.RICE_MEALS));
        items.add(new MenuItem("🍗", "Yakiniku Chicken Rice Bowl", 26, Category.RICE_MEALS));
        items.add(new MenuItem("🥩", "Yakiniku Beef Rice Bowl",    30, Category.RICE_MEALS));
        items.add(new MenuItem("🍛", "Curry Chicken Rice Bowl",    26, Category.RICE_MEALS));
        items.add(new MenuItem("🍛", "Curry Beef Rice Bowl",       30, Category.RICE_MEALS));
        items.add(new MenuItem("🍳", "Nasi Goreng Sundara",        23, Category.RICE_MEALS));
        items.add(new MenuItem("🍳", "Nasi Telor Nugget",          18, Category.RICE_MEALS));
        items.add(new MenuItem("🍳", "Nasi Telor Sosis",           18, Category.RICE_MEALS));
        items.add(new MenuItem("🍚", "Extra Nasi",                  5, Category.RICE_MEALS));
        items.add(new MenuItem("🥚", "Extra Telor",                 5, Category.RICE_MEALS));

        // ── NOODLE & PASTA ───────────────────────────────────────────
        items.add(new MenuItem("🧄", "Aglio Olio",                 25, Category.NOODLE_PASTA));
        items.add(new MenuItem("🍝", "Creamy Chicken Pasta",       25, Category.NOODLE_PASTA));
        items.add(new MenuItem("🧀", "Cheese Bolognese Pasta",     25, Category.NOODLE_PASTA));
        items.add(new MenuItem("🍜", "Mie Goreng Sundara",         18, Category.NOODLE_PASTA));
        items.add(new MenuItem("🍜", "Mie Nyemek Sundara",         18, Category.NOODLE_PASTA));
        items.add(new MenuItem("🍜", "Mie Nyemek Sundara Lite",    15, Category.NOODLE_PASTA));

        // ── SNACK & SWEET TOOTH ──────────────────────────────────────
        items.add(new MenuItem("🍽️", "Mix Platter",               35, Category.SNACK_SWEET));
        items.add(new MenuItem("🍗", "Chicken Wings",              25, Category.SNACK_SWEET));
        items.add(new MenuItem("🌭", "Kentang Sosis",              22, Category.SNACK_SWEET));
        items.add(new MenuItem("🍟", "French Fries",               20, Category.SNACK_SWEET));
        items.add(new MenuItem("🥡", "Cireng Bumbu Rujak",         18, Category.SNACK_SWEET));
        items.add(new MenuItem("🥟", "Wonton Saus Wijen",          18, Category.SNACK_SWEET));
        items.add(new MenuItem("🥟", "Wonton Goreng Chili Oil",    18, Category.SNACK_SWEET));
        items.add(new MenuItem("🍲", "Wonton Kuah Tomyum",         18, Category.SNACK_SWEET));
        items.add(new MenuItem("🥐", "Risoles",                    18, Category.SNACK_SWEET));
        items.add(new MenuItem("🥞", "Pancake Ice Cream",          23, Category.SNACK_SWEET));
        items.add(new MenuItem("🍓", "Pancake Strawberry Ice Cream",25, Category.SNACK_SWEET));
        items.add(new MenuItem("🍩", "Donat Kentang",              16, Category.SNACK_SWEET));
        items.add(new MenuItem("🍌", "Pisang Coklat Aroma",        18, Category.SNACK_SWEET));
        items.add(new MenuItem("🧀", "Keju Aroma",                 18, Category.SNACK_SWEET));
        items.add(new MenuItem("🍦", "Ice Cream Scoop",            15, Category.SNACK_SWEET));

        // ── BUNDLING ─────────────────────────────────────────────────
        items.add(new MenuItem("🎁", "Rice Bowl Chicken + Black Tea", 30, Category.BUNDLING));
        items.add(new MenuItem("🎁", "Rice Bowl Beef + Black Tea",    35, Category.BUNDLING));
        items.add(new MenuItem("🎁", "All Pasta + Black Tea",         30, Category.BUNDLING));
        items.add(new MenuItem("🎁", "Nasi Goreng + Black Tea",       28, Category.BUNDLING));
        items.add(new MenuItem("🎁", "Mie Goreng/Nyemek + Black Tea", 23, Category.BUNDLING));
        items.add(new MenuItem("🎁", "Nasi Telor Nugget/Sosis + Tea", 23, Category.BUNDLING));
        items.add(new MenuItem("🎁", "Mie Nyemek Lite + Black Tea",   20, Category.BUNDLING));
        items.add(new MenuItem("🎁", "Creme Brulle + Donut",          28, Category.BUNDLING));
        items.add(new MenuItem("🎁", "Magic + Donut",                 28, Category.BUNDLING));
        items.add(new MenuItem("🎁", "Cappucino/Latte + Donut",       26, Category.BUNDLING));
        items.add(new MenuItem("🎁", "Es Kopi Sundara + Donut",       25, Category.BUNDLING));
        items.add(new MenuItem("🎁", "Americano + Donut",             23, Category.BUNDLING));

        // ── MUSMID BAR ───────────────────────────────────────────────
        items.add(new MenuItem("🥟", "musmiD Udang (isi 4)",          18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "musmiD Ayam (isi 4)",           18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "musmiD Nori (isi 4)",           18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "musmiD Mix (isi 4)",            18, Category.MUSMID_BAR));
        items.add(new MenuItem("🦐", "Hakau musmiD (isi 4)",          18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "Wonton Ayam (isi 6)",           18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥚", "Lumpia Kulit Tahu Udang",       18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥚", "Lumpia Kulit Tahu Ayam",        18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥚", "Lumpia Kulit Tahu Mix",         18, Category.MUSMID_BAR));
        items.add(new MenuItem("🍞", "Bakpao Coklat",                 18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "musmiD Bakar / Mentai",         22, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "musmiD Goreng Ujang Kedu",      18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "musmiD Goreng Ekado",           18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "musmiD Goreng Wonton Ayam",     18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "musmiD Goreng Lumpia Udang",    18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "musmiD Goreng Lumpia Ayam",     18, Category.MUSMID_BAR));
        items.add(new MenuItem("🥟", "musmiD Goreng Lumpia Mix",      18, Category.MUSMID_BAR));
        items.add(new MenuItem("🧀", "musmiD Goreng Keju",            20, Category.MUSMID_BAR));
        items.add(new MenuItem("🎉", "Party Mix 10",                  45, Category.MUSMID_BAR));
        items.add(new MenuItem("🎉", "Party Mix 15",                  65, Category.MUSMID_BAR));
        items.add(new MenuItem("🎉", "Party Mix 20",                  85, Category.MUSMID_BAR));

        return items;
    }
}
