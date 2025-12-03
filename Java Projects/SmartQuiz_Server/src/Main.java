import java.io.*;
import java.net.*;
import java.util.*;

import smart.quiz.client.ClientHandler;

/**
 * Language Quiz Server Main 
 * 
 * This server listens on port 7777 
 *  a new thread is started using the ClientHandler class For each incoming connection,
 * 
 * @author  NS Daya  
 */
public class Main {

    // Maximum number of attempts allowed 
    private static final int Max_attempt = 4;

    /**
     * 
     * This method initializes port 7777 and waits for clients to connect.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("Ready to make a connection..");

        try (ServerSocket server = new ServerSocket(7777)) {
            // Infinite loop to accept multiple client connections
            while (true) {
                // Accept incoming connection
                Socket clientSocket = server.accept();

                // Create a new thread to handle the client independently
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            // If the server cannot bind  or another IO error occurs
            System.err.println("Could not connect to server");
        }
    }
}

