package network.server.client;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

/**
 * ImgClient
 * 
 * A JavaFX client for connecting to ImgServer.
 * Users can:
 *   - Connect to server
 *   - View a list of uploaded images
 *   - Upload new images
 *   - Download images by ID
 * 
 * Downloads are saved to data/client/ folder.
 * 
 * @author NS Daya 
 */
public class ImgClient extends Application {

    private PrintWriter out;
    private BufferedReader in;
    private DataInputStream din;
    private DataOutputStream dout;

    private TextArea fileListArea;
    private TextField idField;
    private ImageView imageView;
    private Label statusLabel;

    private static final String CLIENT_DIR = "data/client/";

    public static void main(String[] args) {
        new File(CLIENT_DIR).mkdirs(); // ensure client folder exists
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Women's Month Image Client");

        // GUI Components
        Button connectBtn = new Button("Connect");
        Button listBtn = new Button("Refresh List");
        Button uploadBtn = new Button("Upload Image");
        Button downloadBtn = new Button("Download Image");

        idField = new TextField();
        idField.setPromptText("Enter Image ID");

        fileListArea = new TextArea();
        fileListArea.setEditable(false);
        fileListArea.setPrefHeight(150);

        imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);

        statusLabel = new Label("Not connected.");

        // Layout
        VBox root = new VBox(10,
                new HBox(10, connectBtn, listBtn),
                new HBox(10, uploadBtn, new Label("ID:"), idField, downloadBtn),
                new Label("Available Images:"), fileListArea,
                new Label("Downloaded Image:"), imageView,
                statusLabel
        );
        root.setPadding(new Insets(10));

        // Events
        connectBtn.setOnAction(e -> connectToServer());
        listBtn.setOnAction(e -> requestList());
        uploadBtn.setOnAction(e -> uploadImage(stage));
        downloadBtn.setOnAction(e -> downloadImage());

        stage.setScene(new Scene(root, 500, 600));
        stage.show();
    }

    /**
     * Connects to the image server and initializes streams.
     */
    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 5432);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            statusLabel.setText("Connected to server.");
        } catch (IOException e) {
            statusLabel.setText("Connection failed.");
        }
    }

    /**
     * Requests the list of available images from the server and displays it.
     */
    private void requestList() {
        try {
            out.println("LIST");
            fileListArea.clear();
            String line;
            while (!(line = in.readLine()).equals("END")) {
                fileListArea.appendText(line + "\n");
            }
            statusLabel.setText("List updated.");
        } catch (IOException e) {
            statusLabel.setText("Error fetching list.");
        }
    }

    /**
     * Uploads a user-selected image to the server.
     * @param stage Stage for FileChooser dialog
     */
    private void uploadImage(Stage stage) {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(stage);
        if (file == null) return;

        try {
            out.println("UP 0 " + file.getName());
            dout.writeLong(file.length());

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytes;
                while ((bytes = fis.read(buffer)) != -1) {
                    dout.write(buffer, 0, bytes);
                }
            }

            String resp = in.readLine();
            statusLabel.setText("Upload: " + resp);

        } catch (IOException e) {
            statusLabel.setText("Upload failed.");
        }
    }

    /**
     * Downloads the image with the specified ID and displays it.
     */
    private void downloadImage() {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            statusLabel.setText("Enter an ID first.");
            return;
        }

        try {
            out.println("DOWN " + id);
            String resp = in.readLine();
            if (!resp.equals("SUCCESS")) {
                statusLabel.setText("Download failed.");
                return;
            }

            long size = din.readLong();
            File file = new File(CLIENT_DIR + "downloaded_" + id + ".png");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                long remaining = size;
                int bytes;
                while (remaining > 0 &&
                        (bytes = din.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                    fos.write(buffer, 0, bytes);
                    remaining -= bytes;
                }
            }

            imageView.setImage(new Image(file.toURI().toString()));
            statusLabel.setText("Downloaded image saved to client folder.");

        } catch (IOException e) {
            statusLabel.setText("Error downloading image.");
        }
    }
}

