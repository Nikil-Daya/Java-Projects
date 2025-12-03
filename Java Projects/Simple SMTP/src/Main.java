import server.smtp.sendmail.SMTPClient;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * JavaFX application to send plain text emails via an SMTP server.
 * Provides GUI fields for SMTP host, port, and message, and displays status.
 * 
 * Author: NS Daya
 */
public class Main extends Application {

    private TextField hostField, portField;
    private TextArea messageArea;
    private Label statusLabel;

    private final String from = "sender@gmail.com";
    private final String to = "recipient@gmail.com";

    /**
     * Entry point for launching the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Creates the GUI and sets up user interaction.
     * 
     * @param primaryStage The main application window
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX SMTP Client");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        // SMTP Host field
        grid.add(new Label("SMTP Host"), 0, 0);
        hostField = new TextField("localhost");
        grid.add(hostField, 1, 0);

        // Port field
        grid.add(new Label("Port"), 0, 1);
        portField = new TextField("25");
        grid.add(portField, 1, 1);

        // From field (non-editable)
        grid.add(new Label("From"), 0, 2);
        TextField fromField = new TextField(from);
        fromField.setEditable(false);
        grid.add(fromField, 1, 2);

        // To field (non-editable)
        grid.add(new Label("To"), 0, 3);
        TextField toField = new TextField(to);
        toField.setEditable(false);
        grid.add(toField, 1, 3);

        // Message body area
        grid.add(new Label("Message"), 0, 4);
        messageArea = new TextArea();
        messageArea.setPrefRowCount(6);
        grid.add(messageArea, 1, 4);

        // Send button
        Button btn = new Button("Send");
        btn.setOnAction(e -> sendEmail());
        grid.add(btn, 1, 5);

        // Status label for feedback
        statusLabel = new Label();
        grid.add(statusLabel, 1, 6);

        // Display the scene
        Scene scene = new Scene(grid, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Triggered when the Send button is clicked.
     * Reads input, creates SMTPClient, and sends the email.
     */
    public void sendEmail() {
        try {
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());
            String message = messageArea.getText();

            // Create and send the email
            SMTPClient smtp = new SMTPClient(host, port, from, to, message);
            smtp.send();

            statusLabel.setText("Email has been sent successfully.");
        } catch (Exception e) {
            statusLabel.setText("Error sending email: " + e.getMessage());
        }
    }
}

