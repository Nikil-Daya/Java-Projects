import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * GUI class for the Car Loan Calculator.
 * Displays inputs for car price, deposit, interest rate, and loan term,
 * calculates payments using the backend, and shows a pie chart breakdown
 * of Loan Amount vs Total Interest.
 */
public class GUI extends Application {

    @Override
    public void start(Stage stage) {

        /* -------------------- TITLE -------------------- */
        Label title = new Label("Car Loan Calculator");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        /* -------------------- INPUT FIELDS -------------------- */
        TextField carPriceField = new TextField();
        TextField depositField = new TextField();
        TextField interestField = new TextField();
        TextField yearsField = new TextField();

        carPriceField.setPromptText("Car Price (R)");
        depositField.setPromptText("Deposit (R)");
        interestField.setPromptText("Interest Rate (%)");
        yearsField.setPromptText("Loan Term (Years)");

        /* -------------------- BUTTON -------------------- */
        Button calculateBtn = new Button("Calculate");

        /* -------------------- OUTPUT LABELS -------------------- */
        Label loanAmountLabel = new Label("Loan Amount: ");
        Label monthlyPaymentLabel = new Label("Monthly Payment: ");
        Label totalPaymentLabel = new Label("Total Payment: ");
        Label totalInterestLabel = new Label("Total Interest: ");

        /* -------------------- PIE CHART SETUP -------------------- */
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Cost Breakdown");

        PieChart.Data principalSlice = new PieChart.Data("Loan Amount", 1);
        PieChart.Data interestSlice = new PieChart.Data("Total Interest", 1);

        pieChart.getData().addAll(principalSlice, interestSlice);

        /* -------------------- BUTTON ACTION -------------------- */
        calculateBtn.setOnAction(e -> {
            try {
                // Read user input
                double carPrice = Double.parseDouble(carPriceField.getText());
                double deposit = Double.parseDouble(depositField.getText());
                double interestRate = Double.parseDouble(interestField.getText());
                int years = Integer.parseInt(yearsField.getText());

                // Validate inputs using backend
                if (!CarLoanCalculator.isValid(
                        carPrice, deposit, interestRate, years)) {
                    showError("Invalid values entered.");
                    return;
                }

                // Backend calculations
                double loanAmount =
                        CarLoanCalculator.CalculateLoan(carPrice, deposit);

                double monthlyPayment =
                        CarLoanCalculator.MonthlyPayment(loanAmount, interestRate, years);

                double totalPayment = monthlyPayment * years * 12;

                double totalInterest =
                        CarLoanCalculator.calculateTotalInterest(totalPayment, loanAmount);

                // Update labels
                loanAmountLabel.setText(String.format("Loan Amount: R%.2f", loanAmount));
                monthlyPaymentLabel.setText(String.format("Monthly Payment: R%.2f", monthlyPayment));
                totalPaymentLabel.setText(String.format("Total Payment: R%.2f", totalPayment));
                totalInterestLabel.setText(String.format("Total Interest: R%.2f", totalInterest));

                // Update pie chart
                principalSlice.setPieValue(loanAmount);
                interestSlice.setPieValue(totalInterest);

                principalSlice.setName(String.format("Loan Amount (%.1f%%)", (loanAmount / totalPayment) * 100));
                interestSlice.setName(String.format("Total Interest (%.1f%%)", (totalInterest / totalPayment) * 100));

            } catch (NumberFormatException ex) {
                showError("Please enter only numeric values.");
            }
        });

        /* -------------------- LAYOUT GRID -------------------- */
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(12);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);

        grid.add(new Label("Car Price:"), 0, 0);
        grid.add(carPriceField, 1, 0);
        grid.add(new Label("Deposit:"), 0, 1);
        grid.add(depositField, 1, 1);
        grid.add(new Label("Interest Rate (%):"), 0, 2);
        grid.add(interestField, 1, 2);
        grid.add(new Label("Loan Term (Years):"), 0, 3);
        grid.add(yearsField, 1, 3);
        grid.add(calculateBtn, 1, 4);

        /* -------------------- ROOT CONTAINER -------------------- */
        VBox root = new VBox(20,
                title,
                grid,
                loanAmountLabel,
                monthlyPaymentLabel,
                totalPaymentLabel,
                totalInterestLabel,
                pieChart); // Pie chart displayed here

        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        /* -------------------- SCENE -------------------- */
        Scene scene = new Scene(root, 520, 650);
        stage.setTitle("Car Loan Calculator");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Shows an error dialog with the given message.
     *
     * @param message Error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Input Error");
        alert.setHeaderText("Invalid Input");
        alert.setContentText(message);
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

