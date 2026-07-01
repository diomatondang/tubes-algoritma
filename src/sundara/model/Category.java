package sundara.model;

public enum Category {
    SIGNATURE      ("✨ Signature"),
    COFFEE         ("☕ Coffee"),
    NON_COFFEE     ("🧋 Non Coffee"),
    RICE_MEALS     ("🍚 Rice Meals"),
    NOODLE_PASTA   ("🍝 Noodle & Pasta"),
    SNACK_SWEET    ("🍩 Snack & Sweet"),
    BUNDLING       ("🎁 Bundling"),
    MUSMID_BAR     ("🥟 Musmid Bar");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
