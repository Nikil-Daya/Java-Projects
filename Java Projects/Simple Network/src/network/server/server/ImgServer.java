package network.server.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ImgServer
 * 
 * A TCP server for managing image uploads and downloads for Women's Month project.
 * It handles multiple clients using threads and supports:
 *   - LIST: returns a list of image IDs and file names
 *   - DOWN <ID>: sends an image file to the client
 *   - UP <ID> <filename>: uploads an image to the server
 * 
 * Images metadata is stored in data/images.txt.
 * 
 * @author NS Daya 
 */
public class ImgServer {

    public static final int PORT = 5432;
    private static final String DATA_FILE = "data/images.txt";
    private static final String SERVER_DIR = "data/server/";

    public static void main(String[] args) {
        // Ensure server directory exists
        new File(SERVER_DIR).mkdirs();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Image Server started on port " + PORT);

            // Accept clients indefinitely
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(new ClientHandler(client)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles one client connection.
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("Client connected: " + socket);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 DataInputStream din = new DataInputStream(socket.getInputStream());
                 DataOutputStream dout = new DataOutputStream(socket.getOutputStream())) {

                String request;
                while ((request = in.readLine()) != null) {
                    if (request.equals("LIST")) {
                        handleList(out);
                    } else if (request.startsWith("DOWN")) {
                        handleDown(request, dout, out);
                    } else if (request.startsWith("UP")) {
                        handleUp(request, din, out);
                    } else {
                        out.println("FAILURE");
                    }
                }

            } catch (IOException e) {
                System.out.println("Client disconnected.");
            }
        }

        /**
         * Sends the list of image IDs and file names to the client.
         */
        private void handleList(PrintWriter out) {
            try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    out.println(line);
                }
                out.println("END"); // indicate end of list
            } catch (IOException e) {
                out.println("FAILURE");
            }
        }

        /**
         * Sends the requested image to the client.
         * @param request DOWN <ID>
         * @param dout DataOutputStream for sending file bytes
         * @param out PrintWriter for sending status messages
         */
        private void handleDown(String request, DataOutputStream dout, PrintWriter out) {
            try {
                String[] parts = request.split(" ");
                int id = Integer.parseInt(parts[1]);

                // Find filename by ID
                String fileName = null;
                try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] data = line.split(",");
                        if (Integer.parseInt(data[0].trim()) == id) {
                            fileName = data[1].trim();
                            break;
                        }
                    }
                }

                if (fileName == null) {
                    out.println("FAILURE");
                    return;
                }

                File file = new File(SERVER_DIR + fileName);
                if (!file.exists()) {
                    out.println("FAILURE");
                    return;
                }

                // Send file
                out.println("SUCCESS");
                dout.writeLong(file.length());
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytes;
                    while ((bytes = fis.read(buffer)) != -1) {
                        dout.write(buffer, 0, bytes);
                    }
                }

            } catch (Exception e) {
                out.println("FAILURE");
            }
        }

        /**
         * Receives an image from the client and saves it to the server folder.
         * @param request UP <ID> <filename>
         * @param din DataInputStream for reading file bytes
         * @param out PrintWriter for sending status messages
         */
        private void handleUp(String request, DataInputStream din, PrintWriter out) {
            try {
                String[] parts = request.split(" ", 3);
                if (parts.length < 2) {
                    out.println("FAILURE");
                    return;
                }
                String fileName = parts[parts.length - 1].trim();
                long fileSize = din.readLong();
                File file = new File(SERVER_DIR + fileName);

                synchronized (ImgServer.class) {
                    int newId = getNextId(); // generate unique ID

                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        long remaining = fileSize;
                        int bytes;
                        while (remaining > 0 &&
                                (bytes = din.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                            fos.write(buffer, 0, bytes);
                            remaining -= bytes;
                        }
                    }

                    // Append entry to text file
                    try (FileWriter fw = new FileWriter(DATA_FILE, true)) {
                        fw.write(newId + ", " + fileName + "\n");
                    }
                }

                out.println("SUCCESS");

            } catch (Exception e) {
                out.println("FAILURE");
            }
        }
    }

    /**
     * Returns the next available image ID by scanning images.txt
     * @return next unique integer ID
     */
    private static int getNextId() {
        int lastId = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        if (id > lastId) lastId = id;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException ignored) {}
        return lastId + 1;
    }
}


