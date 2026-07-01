package sundara.model;

public class OrderItem {
    private final MenuItem menuItem;
    private int quantity;

    public OrderItem(MenuItem menuItem) {
        this.menuItem = menuItem;
        this.quantity = 1;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public int      getQuantity() { return quantity; }

    public void incrementQty() { if (quantity < 99) quantity++; }
    public void decrementQty() { if (quantity > 1)  quantity--; }

    public int getSubtotal() {
        return menuItem.getPrice() * quantity;
    }

    public String getFormattedSubtotal() {
        return String.format("Rp %,d", getSubtotal() * 1000).replace(',', '.');
    }
}
