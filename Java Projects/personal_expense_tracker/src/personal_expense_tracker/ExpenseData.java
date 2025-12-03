package personal_expense_tracker;

import java.io.*;
import java.util.*;

/**
 * Utility class for saving and loading Expense data to and from a CSV file.
 * 
 * @author NS, Daya
 */
public class ExpenseData {
    
    /**
     * The filename used for storing expenses.
     */
    private static final String Fname = "Expenses.csv";
    
    /**
     * Saves a list of Expense objects to a CSV file.
     * Each expense is saved on a new line in the format:
     * category,description,amount,date
     * 
     * @param expenses The list of expenses to save.
     * @throws IOException If an I/O error occurs during writing.
     */
    public static void saveExp(List<Expense> expenses) throws IOException {
        try (BufferedWriter write = new BufferedWriter(new FileWriter(Fname))) {
            for (Expense e : expenses) {
                write.write(e.getCategory() + "," + e.getDescription() + "," + e.getAmt() + "," + e.getDate());
                write.newLine();
            }
        }
    }
    
    /**
     * Loads the list of Expense objects from the CSV file.
     * If the file does not exist, returns an empty list.
     * 
     * @return A list of Expense objects read from the file.
     * @throws FileNotFoundException If the file cannot be found (not thrown if file missing, returns empty list instead).
     * @throws IOException If an I/O error occurs during reading.
     */
    public static List<Expense> LoadExp() throws FileNotFoundException, IOException {
        List<Expense> expenses = new ArrayList<>();
        File file = new File(Fname);
        if (!file.exists()) return expenses; // Return empty list if no file found
        
        try (BufferedReader reader = new BufferedReader(new FileReader(Fname))) {
            String Ln;
            while ((Ln = reader.readLine()) != null) {
                String[] parts = Ln.split(",");
                if (parts.length == 4) {
                    // Parse each line into an Expense object
                    expenses.add(new Expense(parts[0], parts[1], Double.parseDouble(parts[2]), parts[3]));
                }
            }
        }
        return expenses;
    }
}

