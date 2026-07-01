package sundara.model;

public class MenuItem {
    private final String emoji;
    private final String name;
    private final int price; // in thousands
    private final Category category;
    private int stock;

    public MenuItem(String emoji, String name, int price, Category category) {
        this(emoji, name, price, category, 99);
    }

    public MenuItem(String emoji, String name, int price, Category category, int stock) {
        this.emoji    = emoji;
        this.name     = name;
        this.price    = price;
        this.category = category;
        this.stock    = stock;
    }

    public String  getEmoji()    { return emoji; }
    public String  getName()     { return name; }
    public int     getPrice()    { return price; }
    public Category getCategory(){ return category; }
    public int     getStock()    { return stock; }
    public void    setStock(int stock) { this.stock = stock; }

    public String getFormattedPrice() {
        return String.format("Rp %,d", price * 1000).replace(',', '.');
    }

    @Override
    public String toString() {
        return emoji + " " + name;
    }
}
