package share.controller.seeder;

import java.io.File;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import share.net.Protocol;
import share.net.SeederServer;

/**
 * JavaFX controller for the Seeder UI.
 * Allows users to add files to share, start/stop the server,
 * and view log messages.
 *
 * @author NS, Daya 
 */
public class SeederController extends BorderPane {

    private final ListView<File> list = new ListView<>();     // List of shared files
    private final TextField portField = new TextField(String.valueOf(Protocol.port)); // Port input
    private final TextArea log = new TextArea();              // Log output

    private SeederServer server;              // Seeder server instance
    private ExecutorService svc;              // Executor for running the server

    /**
     * Constructs the Seeder UI and initializes controls.
     */
    public SeederController() {
        setPadding(new Insets(10));

        // --- Top control bar ---
        HBox top = new HBox(10,
                new Label("Port:"), portField,
                new Button("Add Files"),
                new Button("Start"),
                new Button("Stop")
        );
        setTop(top);
        setCenter(list);

        log.setEditable(false);
        setBottom(new VBox(new Label("Log"), log));

        // Buttons
        Button add = (Button) top.getChildren().get(2);
        Button start = (Button) top.getChildren().get(3);
        Button stop = (Button) top.getChildren().get(4);
        stop.setDisable(true);

        // --- Add files button ---
        add.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            var files = fc.showOpenMultipleDialog(getScene().getWindow());
            if (files != null) list.getItems().addAll(files);
        });

        // --- Start/Stop buttons ---
        start.setOnAction(e -> startServer());
        stop.setOnAction(e -> stopServer());
    }

    /**
     * Stops the Seeder server and executor.
     */
    private void stopServer() {
        if (server != null) server.stop();
        if (svc != null) svc.shutdownNow();
        log("Seeder stopped.");
    }

    /**
     * Starts the Seeder server in a background thread.
     */
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            server = new SeederServer(port, list.getItems(), this::log);
            svc = Executors.newSingleThreadExecutor();
            svc.submit(server);
            log("Seeder started on port " + port);
        } catch (SocketException ex) {
            log("Failed: " + ex.getMessage());
        }
    }

    /**
     * Logs a message safely on the UI thread.
     *
     * @param s the message to log
     */
    private void log(String s) {
        Platform.runLater(() -> log.appendText("[Seeder] " + s + "\n"));
    }
}


