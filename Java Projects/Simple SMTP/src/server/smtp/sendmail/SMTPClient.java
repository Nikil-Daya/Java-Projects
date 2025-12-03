package server.smtp.sendmail;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**

	 * This class opens a socket to the given SMTP host and port,
	 * sends HELO, MAIL FROM, RCPT TO, DATA, and QUIT commands in order.

	 * 
	 * @author NS,Daya 223026030
	 */
	public class SMTPClient {

	    private String host;
	    private int port;
	    private String from;
	    private String to;
	    private String message;

	    /**
	     * Constructs an SMTPClient with the necessary email details.
	     * 
	     * @param host    SMTP server hostname 
	     * @param port    SMTP port 
	     * @param from    Sender's email address
	     * @param to      Recipient's email address
	     * @param message Body of the email message
	     */
	    public SMTPClient(String host, int port, String from, String to, String message) {
	        this.host = host;
	        this.port = port;
	        this.from = from;
	        this.to = to;
	        this.message = message;
	    }

	    /**
	     * Connects to the SMTP server and sends the email using basic SMTP commands.
	     * 
	     * @throws IOException 
	     */
	    public void send() throws IOException {
	        // Open a connection to the SMTP server
	        Socket socket = new Socket(host, port);
	        BufferedReader read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

	        // Read server greeting
	        readResponse(read);

	        // Begin SMTP communication
	        sendComm(writer, "HELO " + InetAddress.getLocalHost().getHostName());
	        readResponse(read);

	        sendComm(writer, "MAIL FROM:<" + from + ">");
	        readResponse(read);

	        sendComm(writer, "RCPT TO:<" + to + ">");
	        readResponse(read);

	        sendComm(writer, "DATA");
	        readResponse(read);

	        // Write email headers and body
	        writer.write("From: " + from + "\r\n");
	        writer.write("To: " + to + "\r\n");
	        writer.write("Subject: TestMail\r\n");
	        writer.write("\r\n");
	        writer.write(message + "\r\n.\r\n"); // "." ends the email body
	        writer.flush();
	        readResponse(read);

	        // End the SMTP session
	        sendComm(writer, "QUIT");
	        readResponse(read);

	        // Close
	        writer.close();
	        read.close();
	        socket.close();
	    }

	    /**
	     * 
	     * @param writer BufferedWriter to the socket's output stream
	     * @param command The command string 
	     * @throws IOException If writing fails
	     */
	    private void sendComm(BufferedWriter writer, String command) throws IOException {
	        writer.write(command + "\r\n");
	        writer.flush();
	    }

	    /**
	     * Reads and validates the response from the server.
	     * SMTP success responses begin with 2xx or 3xx codes.
	     * 
	     * @param read BufferedReader to the socket's input stream
	     * @throws IOException If the response is invalid or null
	     */
	    private void readResponse(BufferedReader read) throws IOException {
	        String response = read.readLine();
	        if (response == null || !(response.startsWith("2") || response.startsWith("3"))) {
	            throw new IOException("SMTP Error: " + response);
	        }
	    }
	}


