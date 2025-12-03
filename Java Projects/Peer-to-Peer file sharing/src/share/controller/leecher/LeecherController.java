package share.controller.leecher;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import share.model.FileEntry;
import share.net.LeecherClient;
import share.net.Protocol;

/**
 * JavaFX controller for the Leecher UI.
 * Allows users to connect to a Seeder, view shared files,
 * and download selected files.
 *
 * @author NS, Daya 
 */
public class LeecherController extends BorderPane {

    private final TextField hostField = new TextField("127.0.0.1"); // Default host
    private final TextField portField = new TextField(String.valueOf(Protocol.port)); // Default port
    private final TableView<FileEntry> table = new TableView<>();  // Table for file list
    private final TextArea log = new TextArea();                   // Log area
    private Path saveDir = Path.of(System.getProperty("user.home"), "Downloads"); // Default save dir

    /**
     * Constructs the Leecher UI and initializes controls.
     */
    public LeecherController() {
        setPadding(new Insets(10));

        // --- Top control bar ---
        Button connectBtn = new Button("Connect");
        Button listBtn = new Button("Get LIST");
        Button getBtn = new Button("Retrieve");
        Button dirBtn = new Button("Save To");

        HBox top = new HBox(10,
                new Label("Host:"), hostField,
                new Label("Port:"), portField,
                connectBtn, listBtn, getBtn, dirBtn
        );
        setTop(top);

        // --- Table setup ---
        TableColumn<FileEntry, Number> cIdx = new TableColumn<>("#");
        cIdx.setCellValueFactory(p -> new SimpleIntegerProperty(p.getValue().getIndex()));

        TableColumn<FileEntry, String> cName = new TableColumn<>("Filename");
        cName.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getName()));

        TableColumn<FileEntry, String> cSize = new TableColumn<>("Size");
        cSize.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getValue().getSize())));

        table.getColumns().addAll(cIdx, cName, cSize);
        setCenter(table);

        // --- Log area ---
        log.setEditable(false);
        VBox bottom = new VBox(6, new Label("Log"), log);
        setBottom(bottom);

        // --- Button actions ---
        connectBtn.setOnAction(e -> log("Seeder at " + hostField.getText() + ":" + portField.getText()));
        listBtn.setOnAction(e -> requestList());
        getBtn.setOnAction(e -> retrieve());
        dirBtn.setOnAction(e -> chooseDir());
    }

    /**
     * Requests the file list from the Seeder asynchronously.
     */
    private void requestList() {
        CompletableFuture.runAsync(() -> {
            try {
                LeecherClient client = new LeecherClient(hostField.getText(), Integer.parseInt(portField.getText()));
                List<FileEntry> list = client.requestList();
                Platform.runLater(() -> table.getItems().setAll(list));
                log("Got list: " + list.size() + " files");
            } catch (Exception e) {
                log("LIST failed: " + e.getMessage());
            }
        });
    }

    /**
     * Downloads the selected file asynchronously.
     */
    private void retrieve() {
        FileEntry sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            log("No file selected");
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                LeecherClient client = new LeecherClient(hostField.getText(), Integer.parseInt(portField.getText()));
                client.retrieveFile(sel.getIndex(), saveDir, this::log);
            } catch (Exception e) {
                log("Retrieve failed: " + e.getMessage());
            }
        });
    }

    /**
     * Opens a directory chooser for the download folder.
     */
    private void chooseDir() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Download Folder");
        File selected = dc.showDialog(getScene().getWindow());
        if (selected != null) {
            saveDir = selected.toPath();
            log("Download folder set to: " + saveDir);
        }
    }

    /**
     * Appends a message to the log box safely from any thread.
     *
     * @param msg the message to log
     */
    private void log(String msg) {
        Platform.runLater(() -> this.log.appendText("[Leecher] " + msg + "\n"));
    }
}



