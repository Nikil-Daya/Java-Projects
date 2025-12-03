package personal_expense_tracker;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX Application for tracking personal expenses.
 * Provides UI for adding, removing, viewing expenses and displays
 * a pie chart summary and total amount spent.
 * 
 * @author NS, Daya
 */
public class ExpenseTrackerUI extends Application {

    private PieChart pieChart;
    private ObservableList<Expense> expList;
    private Label totalSpentLabel;

    /**
     * Entry point for the JavaFX application.
     * Sets up the UI components and event handlers.
     *
     * @param primaryStage The primary window for the JavaFX application.
     * @throws Exception If loading expenses from file fails.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load existing expenses from CSV file into observable list
        expList = FXCollections.observableArrayList(ExpenseData.LoadExp());

        // Create a table to display expenses
        TableView<Expense> tbl = new TableView<>(expList);
        tbl.getColumns().addAll(
                createColumn("Category", "category", 100),
                createColumn("Description", "description", 200),
                createColumn("Amount", "amt", 100),
                createColumn("Date", "date", 100)
        );

        // Input fields for new expense data
        TextField categoryfield = new TextField();
        categoryfield.setPromptText("Category");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        TextField amtField = new TextField();
        amtField.setPromptText("Amount");

        DatePicker datepicker = new DatePicker(LocalDate.now());

        // Button to add new expense
        Button addBtn = new Button("Add Expense");
        addBtn.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(amtField.getText());
                Expense exp = new Expense(
                        categoryfield.getText(),
                        descriptionField.getText(),
                        amt,
                        datepicker.getValue().toString()
                );
                expList.add(exp); // Add new expense to list
                categoryfield.clear();
                descriptionField.clear();
                amtField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Invalid amount", "Please enter a valid number");
            }
        });

        // Button to remove the selected expense from the table and list
        Button removeBtn = new Button("Remove Selected");
        removeBtn.setOnAction(e -> {
            Expense selected = tbl.getSelectionModel().getSelectedItem();
            if (selected != null) {
                expList.remove(selected);
            } else {
                showAlert("No Selection", "Please select an expense to remove.");
            }
        });

        // Button to save the current expenses list to file
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> {
            try {
                ExpenseData.saveExp(expList);
                showAlert("Saved", "Expenses saved Successfully");
            } catch (Exception ex) {
                showAlert("Error", "Could not save Expenses");
            }
        });

        // HBox containing input fields and buttons
        HBox inputBox = new HBox(10, categoryfield, descriptionField, amtField, datepicker, addBtn, removeBtn, saveBtn);
        inputBox.setPadding(new Insets(10));

        // PieChart to display expenses distribution by category
        pieChart = new PieChart();
        pieChart.setTitle("Expenses by Category");
        updatePieChart(); // Initialize chart with current data

        // Label to display total amount spent
        totalSpentLabel = new Label();
        updateTotalSpent();

        // Listen to changes in expense list to update pie chart and total spent dynamically
        expList.addListener((ListChangeListener<Expense>) change -> {
            updatePieChart();
            updateTotalSpent();
        });

        // Layout: Table and PieChart side by side
        HBox mainBox = new HBox(20, tbl, pieChart);

        // Vertical layout combining main content, input box, and total label
        VBox root = new VBox(10, mainBox, inputBox, totalSpentLabel);
        root.setPadding(new Insets(10));

        // Set the scene and display the stage
        primaryStage.setScene(new Scene(root, 900, 500));
        primaryStage.setTitle("Personal Expense Tracker");
        primaryStage.show();
    }

    /**
     * Updates the totalSpentLabel to show the current total expense amount.
     */
    private void updateTotalSpent() {
        double total = expList.stream().mapToDouble(Expense::getAmt).sum();
        totalSpentLabel.setText(String.format("Total Spent: R%.2f", total));
    }

    /**
     * Updates the PieChart to reflect the distribution of expenses by category.
     */
    private void updatePieChart() {
        Map<String, Double> categoryTotals = expList.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmt)
                ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        categoryTotals.forEach((category, total) ->
                pieData.add(new PieChart.Data(category, total))
        );

        pieChart.setData(pieData);
    }

    /**
     * Displays an information alert with the given title and message.
     *
     * @param title   The title of the alert window.
     * @param message The message to display inside the alert.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }

    /**
     * Helper method to create a TableColumn for the Expense TableView.
     *
     * @param title    The column header text.
     * @param property The name of the Expense property to display.
     * @param width    The preferred width of the column.
     * @return A configured TableColumn for the given Expense property.
     */
    private TableColumn<Expense, ?> createColumn(String title, String property, int width) {
        TableColumn<Expense, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }

    /**
     * The main method launches the JavaFX application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        launch(args);
    }
}

