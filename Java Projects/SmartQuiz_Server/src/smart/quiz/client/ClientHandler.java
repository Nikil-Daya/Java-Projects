package smart.quiz.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

/**
 * Handles a single client connection for the Language Quiz Server.
 * 
 * Responds to commands such as "SUPER", "ANS [answer]", and "CHEERS" from the client,
 * 
 * @author NS Daya
 */
public class ClientHandler implements Runnable {

	private Socket socket;
	private int attemp_num = 0;         //number of answer attempts
	private int response_num = 0;       //number of responses sent
	private boolean Started = false;    // Tracks if the session has started (after "SUPER")

	private static final Random rand = new Random();

	// Random responses for answers starting with "Happy"
	private static final String[] HAPPY_resp = { "Thanks", "Hooray!", "To You!" };

	// Default fallback responses 
	private static final String[] def_resp = { "Escusez-moi?", "Oh ok!", "Nein" };

	/**
	 * Constructor for ClientHandler
	 * 
	 * @param clientSocket the connected client socket
	 */
	
	public ClientHandler(Socket clientSocket) {
		// TODO Auto-generated constructor stub
		this.socket = clientSocket;
	}


	@Override
	public void run() {
		try (
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
		) {
			// Send greeting
			out.println("HOWZIT");

			String line;

			//line-by-line input
			while ((line = in.readLine()) != null) {
				line = line.trim();

				if (line.equalsIgnoreCase("SUPER")) {
					Started = true;
					out.println("ANSWER the Question:How do you say Happy Birthday in South Africa? or CHEERS");

				} else if (line.equalsIgnoreCase("CHEERS")) {
					out.println("0# OK BYE - " + attemp_num + "answers");
					break;

				} else if (line.toUpperCase().startsWith("ANS") && Started) {
					attemp_num++;

					String ans = line.substring(4).trim(); // Extract answer after "ANS "
					String resp = processAns(ans);        // Generate response
					response_num++;

					out.println(response_num + "#" + resp); // Send response

					if (attemp_num >= 4) {
						out.println("HAMBA KAHLE - you had 4 chances");
						break;
					} else {
						out.println("Command unknown! please use SUPER, ANS[answer] or CHEERS");
					}
				}
			}

		} catch (IOException e) {
			System.err.println("Client has disconnected");
		} finally {
			try {
				socket.close();
			} catch (IOException ex) {
				System.err.println("Could not close socket");
			}
		}
	}

	/**
	 * Processes the answer string and returns a response based on the rules.
	 * 
	 * @param ans the client's answer
	 * @return server response string
	 */
	private String processAns(String ans) {
		ans = ans.toLowerCase();

		if (ans.contains("gelukkige") || ans.contains("verjaarsdag")) {
			return "Baie dankie!";
		} else if (ans.startsWith("happy")) {
			return HAPPY_resp[rand.nextInt(HAPPY_resp.length)];
		} else if (ans.contains("usuku olumnandi") || ans.contains("lokuzalwa")) {
			return "Yebo - kuhle!";
		} else {
			return def_resp[rand.nextInt(def_resp.length)];
		}
	}
}

