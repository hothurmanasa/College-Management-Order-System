/**
 * Abstract parent of Staff and Customer (inheritance + abstraction).
 * Fields are private and final (encapsulation): they are set once, in the constructor.
 */
public abstract class User {
    private final int id;
    private final String name;

    protected User(int id, String name) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative.");
        }
        this.id = id;
        this.name = cleanText("Name", name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /** Every child class must supply its own version (polymorphism). */
    public abstract void showProfile();

    /**
     * Shared text validation used for every text field that is saved to a file.
     * Rejects blank text, the | separator and line breaks, then returns trimmed text.
     */
    static String cleanText(String label, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " cannot be blank.");
        }
        String cleaned = value.trim();
        if (cleaned.contains("|")) {
            throw new IllegalArgumentException(label + " cannot contain the | character.");
        }
        if (cleaned.contains("\n") || cleaned.contains("\r")) {
            throw new IllegalArgumentException(label + " cannot contain line breaks.");
        }
        return cleaned;
    }
}

