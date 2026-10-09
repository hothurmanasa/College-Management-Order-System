/**
 * One order = one kind of item. Everything except status is final.
 * The unit price is COPIED from the menu item at order time (captured price).
 */
public class FoodOrder {
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 100;

    private final int id;
    private final int customerId;
    private final int itemId;
    private final int quantity;
    private final int unitPriceRupees;
    private OrderStatus status;

    public FoodOrder(int id, int customerId, int itemId, int quantity,
                     int unitPriceRupees, OrderStatus status) {
        if (id < 1 || customerId < 1 || itemId < 1) {
            throw new IllegalArgumentException("Order, customer and item IDs must be 1 or more.");
        }
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be from 1 to 100.");
        }
        if (unitPriceRupees < MenuItem.MIN_PRICE || unitPriceRupees > MenuItem.MAX_PRICE) {
            throw new IllegalArgumentException("Unit price must be from 1 to 100000 rupees.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required.");
        }
        this.id = id;
        this.customerId = customerId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPriceRupees = unitPriceRupees;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getUnitPriceRupees() {
        return unitPriceRupees;
    }

    public OrderStatus getStatus() {
        return status;
    }

    /** (long) first, so the multiplication is done in long and cannot overflow. */
    public long getTotalRupees() {
        return (long) unitPriceRupees * quantity;
    }

    /** PENDING -> READY only. */
    public void markReady() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order " + id + " is " + status
                    + ". Only a PENDING order can be marked READY.");
        }
        status = OrderStatus.READY;
    }

    /** READY -> COLLECTED only. */
    public void markCollected() {
        if (status == OrderStatus.COLLECTED) {
            throw new IllegalStateException("Order " + id + " is already COLLECTED.");
        }
        if (status != OrderStatus.READY) {
            throw new IllegalStateException("Order " + id + " is " + status
                    + ". It must be READY before it can be collected.");
        }
        status = OrderStatus.COLLECTED;
    }

    /** Package-private: used only by CanteenService to undo a change if saving fails. */
    void restoreStatus(OrderStatus previous) {
        this.status = previous;
    }
}
