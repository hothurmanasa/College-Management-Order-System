/**
 * One food item. All fields are private and final, so the price and ID
 * can never change after creation ("each item keeps its original price").
 */
public class MenuItem {
    // Array for the three fixed food categories.
    private static final String[] CATEGORIES = {"Snacks", "Meals", "Drinks"};
    public static final int MIN_PRICE = 1;
    public static final int MAX_PRICE = 100000;

    private final int id;
    private final String name;
    private final String category;
    private final int unitPriceRupees;

    public MenuItem(int id, String name, String category, int unitPriceRupees) {
        if (id < 1) {
            throw new IllegalArgumentException("Item ID must be 1 or more.");
        }
        this.id = id;
        this.name = User.cleanText("Item name", name);
        if (!isValidCategory(category)) {
            throw new IllegalArgumentException("Category must be Snacks, Meals or Drinks.");
        }
        this.category = category;
        if (unitPriceRupees < MIN_PRICE || unitPriceRupees > MAX_PRICE) {
            throw new IllegalArgumentException("Price must be from " + MIN_PRICE + " to " + MAX_PRICE + " rupees.");
        }
        this.unitPriceRupees = unitPriceRupees;
    }

    /** Returns a copy so outside code cannot change the original array. */
    public static String[] getCategories() {
        return CATEGORIES.clone();
    }

    public static boolean isValidCategory(String category) {
        for (String c : CATEGORIES) {
            if (c.equals(category)) {
                return true;
            }
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getUnitPriceRupees() {
        return unitPriceRupees;
    }

    public void showDetails() {
        System.out.println(id + " | " + name + " | " + category + " | Rs." + unitPriceRupees);
    }
}
