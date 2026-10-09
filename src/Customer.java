/** A registered customer. IDs start from 1. */
public class Customer extends User {

    public Customer(int id, String name) {
        super(id, name);
        if (id < 1) {
            throw new IllegalArgumentException("Customer ID must be 1 or more.");
        }
    }

    @Override
    public void showProfile() {
        System.out.println("Customer | ID " + getId() + " | " + getName());
    }
}
