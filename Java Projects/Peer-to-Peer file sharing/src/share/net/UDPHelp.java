package share.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for sending and receiving UDP messages.
 * Provides helper methods to send plain text or raw bytes
 * and to decode received text.
 *
 * @author NS, Daya 
 */
public class UDPHelp {

    /**
     * Sends a text message to the specified address and port.
     *
     * @param dg   the DatagramSocket to use
     * @param text the message text
     * @param addr the destination IP address
     * @param port the destination port
     * @throws IOException if sending fails
     */
    public static void sendText(DatagramSocket dg, String text, InetAddress addr, int port) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.US_ASCII);
        dg.send(new DatagramPacket(data, data.length, addr, port));
    }

    /**
     * Sends a byte array to the specified address and port.
     *
     * @param dg   the DatagramSocket to use
     * @param data the byte array
     * @param addr the destination IP address
     * @param port the destination port
     * @throws IOException if sending fails
     */
    public static void sendBytes(DatagramSocket dg, byte[] data, InetAddress addr, int port) throws IOException {
        dg.send(new DatagramPacket(data, data.length, addr, port));
    }

    /**
     * Reads a received UDP message into a string.
     *
     * @param data the byte array containing received data
     * @param len  the number of bytes received
     * @return the decoded text
     */
    public static String readText(byte[] data, int len) {
        return new String(data, 0, len, StandardCharsets.US_ASCII).trim();
    }
}


