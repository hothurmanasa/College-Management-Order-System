import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Console input and menus only.
 * Business rules are handled by CanteenService.
 */
public class Main {

    // One Scanner for the whole program.
    private static final Scanner scanner = new Scanner(System.in);

    private static CanteenService service;

    public static void main(String[] args) {

        FileManager fileManager = new FileManager("data");

        try {
            fileManager.prepareFolder();
            service = new CanteenService(fileManager);

        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Cannot start: " + e.getMessage());
            System.out.println("Please correct the data files and run the program again.");
            return;
        }

        try {
            runMainMenu();
        } catch (NoSuchElementException e) {
            System.out.println("\nInput closed. Exiting.");
        } finally {
            scanner.close();
        }
    }

    // ==============================================================
    // MAIN MENU
    // ==============================================================

    private static void runMainMenu() {

        while (true) {

            System.out.println("\n========== MAIN MENU ==========");
            System.out.println("1. Register customer");
            System.out.println("2. Staff menu");
            System.out.println("3. Customer menu");
            System.out.println("0. Exit");

            int choice = readInt("Choose: ", 0, 3);

            switch (choice) {

                case 1:
                    registerCustomer();
                    break;

                case 2:
                    showStaffMenu();
                    break;

                case 3:
                    showCustomerMenu();
                    break;

                case 0:
                    System.out.println("Goodbye!");
                    return;

                default:
                    // readInt already prevents this.
                    break;
            }
        }
    }

    // ==============================================================
    // STAFF MENU
    // ==============================================================

    private static void showStaffMenu() {

        User user = service.getStaff();

        // Polymorphism:
        // User reference -> Staff object
        user.showProfile();

        while (true) {

            System.out.println("\n========== STAFF MENU ==========");
            System.out.println("1. Add menu item");
            System.out.println("2. View menu");
            System.out.println("3. View orders");
            System.out.println("4. Mark order ready");
            System.out.println("5. Mark order collected");
            System.out.println("6. View reports");
            System.out.println("0. Back");

            int choice = readInt("Choose: ", 0, 6);

            switch (choice) {

                case 1:
                    addMenuItem();
                    break;

                case 2:
                    viewMenu();
                    break;

                case 3:
                    viewAllOrders();
                    break;

                case 4:
                    changeStatus(OrderStatus.READY);
                    break;

                case 5:
                    changeStatus(OrderStatus.COLLECTED);
                    break;

                case 6:
                    viewReport();
                    break;

                case 0:
                    return;

                default:
                    break;
            }
        }
    }

    // ==============================================================
    // CUSTOMER MENU
    // ==============================================================

    private static void showCustomerMenu() {

        System.out.println("\n========== CUSTOMER MENU ==========");

        Customer customer = askCustomer();

        if (customer == null) {
            return;
        }

        // Polymorphism:
        // User reference -> Customer object
        User user = customer;
        user.showProfile();

        while (true) {

            System.out.println("\n1. View menu");
            System.out.println("2. Place order");
            System.out.println("3. View my orders");
            System.out.println("0. Back");

            int choice = readInt("Choose: ", 0, 3);

            switch (choice) {

                case 1:
                    viewMenu();
                    break;

                case 2:
                    placeOrder(customer);
                    break;

                case 3:
                    viewMyOrders(customer);
                    break;

                case 0:
                    return;

                default:
                    break;
            }
        }
    }

    // ==============================================================
    // CUSTOMER REGISTRATION
    // ==============================================================

    private static void registerCustomer() {

        String name = readText("Customer name: ", "Name");

        if (name == null) {
            return;
        }

        try {

            Customer customer = service.registerCustomer(name);

            System.out.println(
                    "Customer registered successfully."
            );

            System.out.println(
                    "Your customer ID is: " + customer.getId()
            );

        } catch (IOException | IllegalArgumentException e) {

            System.out.println(
                    "Could not register customer: " + e.getMessage()
            );
        }
    }

    // ==============================================================
    // ADD MENU ITEM
    // ==============================================================

    private static void addMenuItem() {

        String name = readText("Item name: ", "Item name");

        if (name == null) {
            return;
        }

        String[] categories = MenuItem.getCategories();

        System.out.println("\nCategories:");

        for (int i = 0; i < categories.length; i++) {
            System.out.println((i + 1) + ". " + categories[i]);
        }

        int choice = readInt(
                "Category (1-" + categories.length + "): ",
                1,
                categories.length
        );

        String category = categories[choice - 1];

        int price = readInt(
                "Unit price in rupees (" +
                        MenuItem.MIN_PRICE + "-" +
                        MenuItem.MAX_PRICE + "): ",
                MenuItem.MIN_PRICE,
                MenuItem.MAX_PRICE
        );

        try {

            MenuItem item =
                    service.addItem(name, category, price);

            System.out.println(
                    "Menu item saved successfully."
            );

            System.out.println(
                    "Item ID: " + item.getId()
            );

        } catch (IOException | IllegalArgumentException e) {

            System.out.println(
                    "Could not save item: " + e.getMessage()
            );
        }
    }

    // ==============================================================
    // VIEW MENU
    // ==============================================================

    private static void viewMenu() {

        String[] categories = MenuItem.getCategories();

        System.out.println("\nFilter by category:");
        System.out.println("0. All");

        for (int i = 0; i < categories.length; i++) {
            System.out.println(
                    (i + 1) + ". " + categories[i]
            );
        }

        int choice = readInt(
                "Choose: ",
                0,
                categories.length
        );

        String wanted;

        if (choice == 0) {
            wanted = null;
        } else {
            wanted = categories[choice - 1];
        }

        printItems(service.getMenuItems(wanted));
    }

    private static void printItems(List<MenuItem> items) {

        if (items.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.println(
                "\nID | Name | Category | Price"
        );

        System.out.println(
                "-----------------------------------------"
        );

        for (MenuItem item : items) {
            item.showDetails();
        }
    }

    // ==============================================================
    // PLACE ORDER
    // ==============================================================

    private static void placeOrder(Customer customer) {

        List<MenuItem> items =
                service.getMenuItems(null);

        printItems(items);

        if (items.isEmpty()) {
            return;
        }

        int itemId = askItemId();

        if (itemId == 0) {
            return;
        }

        MenuItem item = service.findItem(itemId);

        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        System.out.println(
                "Selected item: " + item.getName()
        );

        System.out.println(
                "Unit price: Rs." +
                        item.getUnitPriceRupees()
        );

        int quantity = readInt(
                "Quantity (" +
                        FoodOrder.MIN_QUANTITY + "-" +
                        FoodOrder.MAX_QUANTITY + "): ",
                FoodOrder.MIN_QUANTITY,
                FoodOrder.MAX_QUANTITY
        );

        try {

            FoodOrder order =
                    service.placeOrder(
                            customer.getId(),
                            itemId,
                            quantity
                    );

            System.out.println(
                    "Order saved successfully."
            );

            System.out.println(
                    "Order ID: " + order.getId()
            );

            System.out.println(
                    "Total: Rs." +
                            order.getTotalRupees()
            );

            System.out.println(
                    "Status: " +
                            order.getStatus()
            );

        } catch (IOException | IllegalArgumentException e) {

            System.out.println(
                    "Could not place order: " +
                            e.getMessage()
            );
        }
    }

    // ==============================================================
    // CUSTOMER ORDERS
    // ==============================================================

    private static void viewMyOrders(Customer customer) {

        List<FoodOrder> mine =
                service.getOrdersForCustomer(
                        customer.getId()
                );

        if (mine.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.println(
                "\n========== MY ORDERS =========="
        );

        for (FoodOrder order : mine) {

            System.out.println(
                    "Order ID: " + order.getId()
                            + " | Item: "
                            + service.getItemName(
                                    order.getItemId())
                            + " | Quantity: "
                            + order.getQuantity()
                            + " | Status: "
                            + order.getStatus()
                            + " | Total: Rs."
                            + order.getTotalRupees()
            );
        }
    }

    // ==============================================================
    // ALL ORDERS
    // ==============================================================

    private static void viewAllOrders() {

        List<FoodOrder> all =
                service.getAllOrders();

        if (all.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.println(
                "\nOrder | Customer | Item | Qty | Total | Status"
        );

        System.out.println(
                "--------------------------------------------------------"
        );

        for (FoodOrder order : all) {

            System.out.println(
                    order.getId()
                            + " | "
                            + order.getCustomerId()
                            + " | "
                            + service.getItemName(
                                    order.getItemId())
                            + " | "
                            + order.getQuantity()
                            + " | Rs."
                            + order.getTotalRupees()
                            + " | "
                            + order.getStatus()
            );
        }
    }

    // ==============================================================
    // CHANGE ORDER STATUS
    // ==============================================================

    private static void changeStatus(OrderStatus newStatus) {

        if (service.getAllOrders().isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        int orderId = askOrderId();

        if (orderId == 0) {
            return;
        }

        try {

            service.updateStatus(
                    orderId,
                    newStatus
            );

            System.out.println(
                    "Order status saved: " +
                            newStatus
            );

        } catch (IllegalStateException |
                 IllegalArgumentException e) {

            System.out.println(
                    "Refused: " + e.getMessage()
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not save status change: "
                            + e.getMessage()
            );
        }
    }

    // ==============================================================
    // REPORT
    // ==============================================================

    private static void viewReport() {

        service.showReport();

        String answer =
                readLine(
                        "Save report? (y/n): "
                ).trim().toLowerCase();

        if (answer.equals("y")) {

            try {

                service.saveReport();

                System.out.println(
                        "Report saved to data/report.txt"
                );

            } catch (IOException e) {

                System.out.println(
                        "Could not save report: "
                                + e.getMessage()
                );
            }
        }
    }

    // ==============================================================
    // CUSTOMER ID
    // ==============================================================

    private static Customer askCustomer() {

        while (true) {

            int id = readInt(
                    "Enter customer ID (0 to go back): ",
                    0,
                    Integer.MAX_VALUE
            );

            if (id == 0) {
                return null;
            }

            Customer customer =
                    service.findCustomer(id);

            if (customer != null) {
                return customer;
            }

            System.out.println(
                    "Customer ID not found."
            );
        }
    }

    // ==============================================================
    // ITEM ID
    // ==============================================================

    private static int askItemId() {

        while (true) {

            int id = readInt(
                    "Item ID (0 to cancel): ",
                    0,
                    Integer.MAX_VALUE
            );

            if (id == 0) {
                return 0;
            }

            if (service.findItem(id) != null) {
                return id;
            }

            System.out.println(
                    "Item ID not found."
            );
        }
    }

    // ==============================================================
    // ORDER ID
    // ==============================================================

    private static int askOrderId() {

        while (true) {

            int id = readInt(
                    "Order ID (0 to cancel): ",
                    0,
                    Integer.MAX_VALUE
            );

            if (id == 0) {
                return 0;
            }

            if (service.findOrder(id) != null) {
                return id;
            }

            System.out.println(
                    "Order ID not found."
            );
        }
    }

    // ==============================================================
    // INPUT HELPERS
    // ==============================================================

    private static String readLine(String prompt) {

        System.out.print(prompt);

        return scanner.nextLine();
    }

    /**
     * Reads a whole number between min and max.
     */
    private static int readInt(
            String prompt,
            int min,
            int max) {

        while (true) {

            String text =
                    readLine(prompt).trim();

            try {

                int value =
                        Integer.parseInt(text);

                if (value >= min && value <= max) {
                    return value;
                }

                if (max == Integer.MAX_VALUE) {

                    System.out.println(
                            "Enter a number from " +
                                    min +
                                    " to the highest ID."
                    );

                } else {

                    System.out.println(
                            "Enter a number from " +
                                    min +
                                    " to " +
                                    max +
                                    "."
                    );
                }

            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid input. Please enter a whole number."
                );
            }
        }
    }

    /**
     * Reads and validates text.
     * Returns null when validation fails.
     */
    private static String readText(
            String prompt,
            String label) {

        String text = readLine(prompt);

        try {

            return User.cleanText(
                    label,
                    text
            );

        } catch (IllegalArgumentException e) {

            System.out.println(
                    e.getMessage()
            );

            return null;
        }
    }
}
