package sundara.data;

import java.io.*;
import java.util.Properties;

/**
 * Handles loading and saving cashier receipt custom properties
 * (cafe name, address, phone number, and receipt footer message).
 */
public class ReceiptSettings {
    private static final String FILE_NAME = "receipt.properties";
    private static Properties props = new Properties();

    static {
        load();
    }

    public static void load() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            // Read with UTF-8 encoding to preserve emojis on the receipt!
            try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                props.load(isr);
            } catch (IOException e) {
                System.err.println("Gagal membaca receipt.properties: " + e.getMessage());
            }
        } else {
            // Default properties if file missing
            props.setProperty("cafeName", "☕ SUNDARA COFFEESPACE");
            props.setProperty("address", "Jl. Contoh No.1, Kota Anda");
            props.setProperty("phone", "0812-3456-7890");
            props.setProperty("footer", "Terima kasih atas kunjungan Anda!");
            save();
        }
    }

    public static void save() {
        // Write with UTF-8 encoding to support emojis in file!
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(FILE_NAME), "UTF-8")) {
            props.store(osw, "Kustomisasi Struk Kasir Sundara CoffeeSpace");
        } catch (IOException e) {
            System.err.println("Gagal menyimpan receipt.properties: " + e.getMessage());
        }
    }

    public static String getCafeName() { return props.getProperty("cafeName", "☕ SUNDARA COFFEESPACE"); }
    public static String getAddress()  { return props.getProperty("address", "Jl. Contoh No.1, Kota Anda"); }
    public static String getPhone()    { return props.getProperty("phone", "0812-3456-7890"); }
    public static String getFooter()   { return props.getProperty("footer", "Terima kasih atas kunjungan Anda!"); }

    public static void setSettings(String name, String addr, String tel, String foot) {
        props.setProperty("cafeName", name);
        props.setProperty("address", addr);
        props.setProperty("phone", tel);
        props.setProperty("footer", foot);
        save();
    }
}
