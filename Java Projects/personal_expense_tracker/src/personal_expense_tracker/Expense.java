package personal_expense_tracker;

/**
 * Represents a single expense item with a category, description, amount, and date.
 * 
 * @author NS, Daya
 */
public class Expense {

    private String Category;
    private String description;
    private double amt;
    private String date;

    /**
     * Constructs a new Expense object.
     * 
     * @param category    The category of the expense (e.g., "Food", "Transport").
     * @param description A short description of the expense.
     * @param amt         The amount spent.
     * @param date        The date of the expense in String format (e.g., "2025-08-11").
     */
    public Expense(String category, String description, double amt, String date) {
        super();
        this.Category = category;
        this.description = description;
        this.amt = amt;
        this.date = date;
    }

    /**
     * Returns the category of the expense.
     * 
     * @return The expense category as a String.
     */
    public String getCategory() {
        return Category;
    }

    /**
     * Returns the description of the expense.
     * 
     * @return The expense description as a String.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the amount spent.
     * 
     * @return The amount as a double.
     */
    public double getAmt() {
        return amt;
    }

    /**
     * Returns the date of the expense.
     * 
     * @return The date as a String.
     */
    public String getDate() {
        return date;
    }
}

