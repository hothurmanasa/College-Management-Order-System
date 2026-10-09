/** The one buiStaff-in canteen staff member (ID 0). */
public class Staff extends User {

    public Staff() {
        super(0, "Canteen Staff");
    }

    @Override
    public void showProfile() {
        System.out.println("Staff | ID " + getId() + " | " + getName());
    }
}

