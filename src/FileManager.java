import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Handles all file reading and writing.
 *
 * File format:
 *
 * customers.txt:
 * ID|Name
 *
 * menu.txt:
 * ID|Name|Category|Price
 *
 * orders.txt:
 * ID|CustomerID|ItemID|Quantity|UnitPrice|Status
 */
public class FileManager {

    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String MENU_FILE = "menu.txt";
    private static final String ORDERS_FILE = "orders.txt";
    private static final String REPORT_FILE = "report.txt";

    private final Path dataDir;

    public FileManager(String folderName) {

        if (folderName == null || folderName.isBlank()) {
            throw new IllegalArgumentException(
                    "Data folder name cannot be blank."
            );
        }

        this.dataDir = Paths.get(folderName);
    }

    // ==============================================================
    // PREPARE FOLDER
    // ==============================================================

    public void prepareFolder() throws IOException {

        Files.createDirectories(dataDir);
    }

    // ==============================================================
    // LOAD CUSTOMERS
    // ==============================================================

    public ArrayList<Customer> loadCustomers()
            throws IOException {

        ArrayList<Customer> customers =
                new ArrayList<>();

        HashSet<Integer> seenIds =
                new HashSet<>();

        List<String> lines =
                readAllLines(CUSTOMERS_FILE);

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);

            if (line.isBlank()) {
                continue;
            }

            try {

                String[] fields =
                        line.split("\\|", -1);

                checkFieldCount(
                        fields,
                        2
                );

                int id =
                        parseNumber(
                                fields[0],
                                "customer ID"
                        );

                if (id < 1) {
                    throw new IllegalArgumentException(
                            "customer ID must be 1 or more"
                    );
                }

                if (!seenIds.add(id)) {

                    throw new IllegalArgumentException(
                            "duplicate customer ID " + id
                    );
                }

                customers.add(
                        new Customer(
                                id,
                                fields[1]
                        )
                );

            } catch (IllegalArgumentException e) {

                throw lineError(
                        CUSTOMERS_FILE,
                        i + 1,
                        e
                );
            }
        }

        return customers;
    }

    // ==============================================================
    // LOAD MENU
    // ==============================================================

    public ArrayList<MenuItem> loadMenu()
            throws IOException {

        ArrayList<MenuItem> items =
                new ArrayList<>();

        HashSet<Integer> seenIds =
                new HashSet<>();

        List<String> lines =
                readAllLines(MENU_FILE);

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);

            if (line.isBlank()) {
                continue;
            }

            try {

                String[] fields =
                        line.split("\\|", -1);

                checkFieldCount(
                        fields,
                        4
                );

                int id =
                        parseNumber(
                                fields[0],
                                "item ID"
                        );

                if (id < 1) {
                    throw new IllegalArgumentException(
                            "item ID must be 1 or more"
                    );
                }

                if (!seenIds.add(id)) {

                    throw new IllegalArgumentException(
                            "duplicate item ID " + id
                    );
                }

                int price =
                        parseNumber(
                                fields[3],
                                "price"
                        );

                items.add(
                        new MenuItem(
                                id,
                                fields[1],
                                fields[2],
                                price
                        )
                );

            } catch (IllegalArgumentException e) {

                throw lineError(
                        MENU_FILE,
                        i + 1,
                        e
                );
            }
        }

        return items;
    }

    // ==============================================================
    // LOAD ORDERS
    // ==============================================================

    public ArrayList<FoodOrder> loadOrders(
            List<Customer> customers,
            List<MenuItem> items)
            throws IOException {

        ArrayList<FoodOrder> orders =
                new ArrayList<>();

        HashSet<Integer> seenIds =
                new HashSet<>();

        List<String> lines =
                readAllLines(ORDERS_FILE);

        for (int i = 0; i < lines.size(); i++) {

            String line = lines.get(i);

            if (line.isBlank()) {
                continue;
            }

            try {

                String[] fields =
                        line.split("\\|", -1);

                checkFieldCount(
                        fields,
                        6
                );

                int id =
                        parseNumber(
                                fields[0],
                                "order ID"
                        );

                if (id < 1) {
                    throw new IllegalArgumentException(
                            "order ID must be 1 or more"
                    );
                }

                if (!seenIds.add(id)) {

                    throw new IllegalArgumentException(
                            "duplicate order ID " + id
                    );
                }

                int customerId =
                        parseNumber(
                                fields[1],
                                "customer ID"
                        );

                int itemId =
                        parseNumber(
                                fields[2],
                                "item ID"
                        );

                int quantity =
                        parseNumber(
                                fields[3],
                                "quantity"
                        );

                int price =
                        parseNumber(
                                fields[4],
                                "unit price"
                        );

                OrderStatus status =
                        parseStatus(fields[5]);

                // Check customer reference.
                if (!customerExists(
                        customers,
                        customerId)) {

                    throw new IllegalArgumentException(
                            "customer ID " +
                                    customerId +
                                    " does not exist"
                    );
                }

                // Check item reference.
                MenuItem item =
                        findItem(
                                items,
                                itemId
                        );

                if (item == null) {

                    throw new IllegalArgumentException(
                            "item ID " +
                                    itemId +
                                    " does not exist"
                    );
                }

                // Order keeps the price that existed
                // when the order was created.
                if (price !=
                        item.getUnitPriceRupees()) {

                    throw new IllegalArgumentException(
                            "unit price " +
                                    price +
                                    " does not match menu item price " +
                                    item.getUnitPriceRupees()
                    );
                }

                orders.add(
                        new FoodOrder(
                                id,
                                customerId,
                                itemId,
                                quantity,
                                price,
                                status
                        )
                );

            } catch (IllegalArgumentException e) {

                throw lineError(
                        ORDERS_FILE,
                        i + 1,
                        e
                );
            }
        }

        return orders;
    }

    // ==============================================================
    // SAVE CUSTOMERS
    // ==============================================================

    public void saveCustomers(
            List<Customer> customers)
            throws IOException {

        List<String> lines =
                new ArrayList<>();

        for (Customer customer : customers) {

            lines.add(
                    customer.getId()
                            + "|"
                            + customer.getName()
            );
        }

        writeSafely(
                CUSTOMERS_FILE,
                lines
        );
    }

    // ==============================================================
    // SAVE MENU
    // ==============================================================

    public void saveMenu(
            List<MenuItem> items)
            throws IOException {

        List<String> lines =
                new ArrayList<>();

        for (MenuItem item : items) {

            lines.add(
                    item.getId()
                            + "|"
                            + item.getName()
                            + "|"
                            + item.getCategory()
                            + "|"
                            + item.getUnitPriceRupees()
            );
        }

        writeSafely(
                MENU_FILE,
                lines
        );
    }

    // ==============================================================
    // SAVE ORDERS
    // ==============================================================

    public void saveOrders(
            List<FoodOrder> orders)
            throws IOException {

        List<String> lines =
                new ArrayList<>();

        for (FoodOrder order : orders) {

            lines.add(
                    order.getId()
                            + "|"
                            + order.getCustomerId()
                            + "|"
                            + order.getItemId()
                            + "|"
                            + order.getQuantity()
                            + "|"
                            + order.getUnitPriceRupees()
                            + "|"
                            + order.getStatus()
            );
        }

        writeSafely(
                ORDERS_FILE,
                lines
        );
    }

    // ==============================================================
    // SAVE REPORT
    // ==============================================================

    public void saveReport(
            List<String> reportLines)
            throws IOException {

        writeSafely(
                REPORT_FILE,
                reportLines
        );
    }

    // ==============================================================
    // READ FILE
    // ==============================================================

    private List<String> readAllLines(
            String fileName)
            throws IOException {

        List<String> lines =
                new ArrayList<>();

        Path file =
                dataDir.resolve(fileName);

        // First run: file does not exist.
        if (!Files.exists(file)) {
            return lines;
        }

        try (BufferedReader reader =
                     Files.newBufferedReader(
                             file,
                             StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (IOException e) {

            throw new IOException(
                    "Cannot read " +
                            fileName +
                            ": " +
                            e.getMessage(),
                    e
            );
        }

        return lines;
    }

    // ==============================================================
    // SAFE WRITE
    // ==============================================================

    private void writeSafely(
            String fileName,
            List<String> lines)
            throws IOException {

        Files.createDirectories(dataDir);

        Path target =
                dataDir.resolve(fileName);

        Path temp =
                dataDir.resolve(
                        fileName + ".tmp"
                );

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             temp,
                             StandardCharsets.UTF_8)) {

            for (String line : lines) {

                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {

            Files.deleteIfExists(temp);

            throw new IOException(
                    "Cannot write " +
                            fileName +
                            ": " +
                            e.getMessage(),
                    e
            );
        }

        try {

            Files.move(
                    temp,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            Files.deleteIfExists(temp);

            throw new IOException(
                    "Cannot replace " +
                            fileName +
                            ": " +
                            e.getMessage(),
                    e
            );
        }
    }

    // ==============================================================
    // VALIDATION HELPERS
    // ==============================================================

    private static void checkFieldCount(
            String[] fields,
            int expected) {

        if (fields.length != expected) {

            throw new IllegalArgumentException(
                    "expected " +
                            expected +
                            " fields but found " +
                            fields.length
            );
        }
    }

    private static int parseNumber(
            String text,
            String label) {

        try {

            return Integer.parseInt(
                    text.trim()
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    label +
                            " '" +
                            text +
                            "' is not a whole number"
            );
        }
    }

    private static OrderStatus parseStatus(
            String text) {

        try {

            return OrderStatus.valueOf(
                    text.trim()
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "status '" +
                            text +
                            "' must be PENDING, READY or COLLECTED"
            );
        }
    }

    private static boolean customerExists(
            List<Customer> customers,
            int id) {

        for (Customer customer : customers) {

            if (customer.getId() == id) {
                return true;
            }
        }

        return false;
    }

    private static MenuItem findItem(
            List<MenuItem> items,
            int id) {

        for (MenuItem item : items) {

            if (item.getId() == id) {
                return item;
            }
        }

        return null;
    }

    private static IOException lineError(
            String fileName,
            int lineNumber,
            IllegalArgumentException e) {

        return new IOException(
                fileName +
                        " line " +
                        lineNumber +
                        ": " +
                        e.getMessage()
        );
    }
}
