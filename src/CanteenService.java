
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * All project actions (business rules). Composition: this class HAS the three record lists.
 * Rule used for every change: change memory -> save file -> if saving fails, undo the change.
 */
public class CanteenService {
    private final FileManager fileManager;
    private final Staff staff = new Staff();
    private final ArrayList<Customer> customers;
    private final ArrayList<MenuItem> menuItems;
    private final ArrayList<FoodOrder> orders;

    private int nextCustomerId;
    private int nextItemId;
    private int nextOrderId;

    /** Loads everything into temporary lists first; only valid data is accepted. */
    public CanteenService(FileManager fileManager) throws IOException {
        this.fileManager = fileManager;

        ArrayList<Customer> loadedCustomers = fileManager.loadCustomers();
        ArrayList<MenuItem> loadedItems = fileManager.loadMenu();
        ArrayList<FoodOrder> loadedOrders = fileManager.loadOrders(loadedCustomers, loadedItems);

        this.customers = loadedCustomers;
        this.menuItems = loadedItems;
        this.orders = loadedOrders;

        // next ID = largest saved ID + 1 (an empty list gives 0 + 1 = 1)
        int maxCustomer = 0;
        for (Customer c : customers) {
            maxCustomer = Math.max(maxCustomer, c.getId());
        }
        int maxItem = 0;
        for (MenuItem m : menuItems) {
            maxItem = Math.max(maxItem, m.getId());
        }
        int maxOrder = 0;
        for (FoodOrder o : orders) {
            maxOrder = Math.max(maxOrder, o.getId());
        }
        nextCustomerId = maxCustomer + 1;
        nextItemId = maxItem + 1;
        nextOrderId = maxOrder + 1;
    }

    public Staff getStaff() {
        return staff;
    }

    // ------------------------------------------------------------ add actions

    public Customer registerCustomer(String name) throws IOException {
        Customer customer = new Customer(nextCustomerId, name);   // validates name
        customers.add(customer);
        try {
            fileManager.saveCustomers(customers);
        } catch (IOException e) {
            customers.remove(customer);                           // cancel change in memory
            throw e;
        }
        nextCustomerId++;                                         // only after a successful save
        return customer;
    }

    public MenuItem addItem(String name, String category, int unitPriceRupees) throws IOException {
        MenuItem item = new MenuItem(nextItemId, name, category, unitPriceRupees);
        menuItems.add(item);
        try {
            fileManager.saveMenu(menuItems);
        } catch (IOException e) {
            menuItems.remove(item);
            throw e;
        }
        nextItemId++;
        return item;
    }

    public FoodOrder placeOrder(int customerId, int itemId, int quantity) throws IOException {
        if (findCustomer(customerId) == null) {
            throw new IllegalArgumentException("Customer ID " + customerId + " not found.");
        }
        MenuItem item = findItem(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Item ID " + itemId + " not found.");
        }
        // The item's CURRENT price is copied into the order (captured price).
        FoodOrder order = new FoodOrder(nextOrderId, customerId, itemId, quantity,
                item.getUnitPriceRupees(), OrderStatus.PENDING);
        orders.add(order);
        try {
            fileManager.saveOrders(orders);
        } catch (IOException e) {
            orders.remove(order);
            throw e;
        }
        nextOrderId++;
        return order;
    }

    // ---------------------------------------------------------- status change

    /** The service controls status changes; FoodOrder enforces the exact progression. */
    public void updateStatus(int orderId, OrderStatus newStatus) throws IOException {
        FoodOrder order = findOrder(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order ID " + orderId + " not found.");
        }
        OrderStatus previous = order.getStatus();
        if (newStatus == OrderStatus.READY) {
            order.markReady();
        } else if (newStatus == OrderStatus.COLLECTED) {
            order.markCollected();
        } else {
            throw new IllegalArgumentException("Staff can only set READY or COLLECTED.");
        }
        try {
            fileManager.saveOrders(orders);
        } catch (IOException e) {
            order.restoreStatus(previous);                        // cancel change in memory
            throw e;
        }
    }

    // ----------------------------------------------------------------- finders

    public Customer findCustomer(int id) {
        for (Customer c : customers) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public MenuItem findItem(int id) {
        for (MenuItem m : menuItems) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;
    }

    public FoodOrder findOrder(int id) {
        for (FoodOrder o : orders) {
            if (o.getId() == id) {
                return o;
            }
        }
        return null;
    }

    // ----------------------------------------------------------------- queries

    /** Pass null for all items, or a category name to filter. */
    public List<MenuItem> getMenuItems(String category) {
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem m : menuItems) {
            if (category == null || m.getCategory().equals(category)) {
                result.add(m);
            }
        }
        return result;
    }

    public List<FoodOrder> getAllOrders() {
        return new ArrayList<>(orders);
    }

    /** Customers only ever see orders with their own customer ID. */
    public List<FoodOrder> getOrdersForCustomer(int customerId) {
        List<FoodOrder> result = new ArrayList<>();
        for (FoodOrder o : orders) {
            if (o.getCustomerId() == customerId) {
                result.add(o);
            }
        }
        return result;
    }

    public String getItemName(int itemId) {
        MenuItem item = findItem(itemId);
        return item == null ? "Unknown item" : item.getName();
    }

    // ----------------------------------------------------------------- reports

    /** Report lines are rebuilt from the records every time; nothing is stored. */
    public List<String> buildReport() {
        int pending = 0;
        int ready = 0;
        int collected = 0;
        long collectedValue = 0;                                  // long for sums
        HashSet<Integer> orderingCustomers = new HashSet<>();     // each customer counted once

        for (FoodOrder order : orders) {
            orderingCustomers.add(order.getCustomerId());
            switch (order.getStatus()) {
                case PENDING -> pending++;
                case READY -> ready++;
                case COLLECTED -> {
                    collected++;
                    collectedValue += order.getTotalRupees();
                }
            }
        }

        List<String> lines = new ArrayList<>();
        lines.add("CANTEEN REPORT");
        lines.add("Total orders: " + orders.size());
        lines.add("PENDING orders: " + pending);
        lines.add("READY orders: " + ready);
        lines.add("COLLECTED orders: " + collected);
        lines.add("Collected order value: " + collectedValue + " rupees");
        lines.add("Distinct customers who ordered: " + orderingCustomers.size());
        return lines;
    }

    public void showReport() {
        System.out.println();
        for (String line : buildReport()) {
            System.out.println(line);
        }
    }

    public void saveReport() throws IOException {
        fileManager.saveReport(buildReport());
    }
}
