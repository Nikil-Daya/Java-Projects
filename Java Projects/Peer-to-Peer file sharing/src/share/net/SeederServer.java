package share.net;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SeederServer handles requests from leechers via UDP.
 * It can respond with a LIST of available files and 
 * transfer files in chunks when requested.
 *
 * @author NS, Daya 
 */
public class SeederServer implements Runnable {

    private final DatagramSocket socket;       // UDP socket for communication
    private final List<File> sharedFiles;      // Files available for sharing
    private final AtomicBoolean running = new AtomicBoolean(true); // Controls server loop
    private final LogHandler log;              // Logging callback

    /**
     * A simple logging interface for server events.
     */
    public interface LogHandler {
        void log(String msg);
    }

    /**
     * Constructs a SeederServer.
     *
     * @param port        the port to bind the server
     * @param sharedFiles the list of files to share
     * @param logger      the logger implementation
     * @throws SocketException if the socket cannot be opened
     */
    public SeederServer(int port, List<File> sharedFiles, LogHandler logger) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.sharedFiles = sharedFiles;
        this.log = logger;
    }

    /**
     * Stops the server gracefully.
     */
    public void stop() {
        running.set(false);
        socket.close();
    }

    /**
     * Main server loop. Listens for incoming UDP packets and 
     * handles LIST and FILE requests.
     */
    @Override
    public void run() {
        byte[] buffer = new byte[65507]; // Maximum UDP packet size
        while (running.get()) {
            try {
                DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
                socket.receive(pkt);

                // Extract command text
                String cmd = UDPHelp.readText(pkt.getData(), pkt.getLength());
                InetAddress addr = pkt.getAddress();
                int port = pkt.getPort();

                if (cmd.startsWith(Protocol.LIST)) {
                    handleList(addr, port);
                } else if (cmd.startsWith(Protocol.FILE + "|")) {
                    int idx = Integer.parseInt(cmd.split("\\|", 2)[1]);
                    handleFile(idx, addr, port);
                } else {
                    log.log("Unknown Command: " + cmd);
                }

            } catch (SocketException se) {
                // Socket closed during stop(), safe to ignore
            } catch (Exception e) {
                log.log("Error: " + e.getMessage());
            }
        }
    }

    /**
     * Sends the requested file to the client in chunks.
     *
     * @param idx  index of the file in sharedFiles
     * @param addr destination IP address
     * @param port destination port
     * @throws IOException if file reading or sending fails
     */
    private void handleFile(int idx, InetAddress addr, int port) throws IOException {
        if (idx < 0 || idx >= sharedFiles.size()) return;
        File f = sharedFiles.get(idx);

        // --- Send META packet ---
        ByteArrayOutputStream meta = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(meta);
        dos.writeUTF(Protocol.META);
        dos.writeUTF(f.getName());
        dos.writeLong(f.length());
        dos.writeInt(Protocol.size);
        dos.flush();
        UDPHelp.sendBytes(socket, meta.toByteArray(), addr, port);

        // --- Send file data in chunks ---
        try (InputStream in = Files.newInputStream(f.toPath())) {
            byte[] chunk = new byte[Protocol.size];
            int read, seq = 0;

            while ((read = in.read(chunk)) != -1) {
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                DataOutputStream d2 = new DataOutputStream(data);
                d2.writeUTF(Protocol.DATA);
                d2.writeInt(seq);
                d2.writeInt(read);
                d2.write(chunk, 0, read);

                UDPHelp.sendBytes(socket, data.toByteArray(), addr, port);
                seq++;
            }

            // --- Send END packet ---
            ByteArrayOutputStream end = new ByteArrayOutputStream();
            DataOutputStream de = new DataOutputStream(end);
            de.writeUTF(Protocol.END);
            de.writeInt(seq);
            UDPHelp.sendBytes(socket, end.toByteArray(), addr, port);

            log.log("Sent file: " + f.getName());
        }
    }

    /**
     * Sends the list of shared files to the client.
     *
     * @param addr destination IP address
     * @param port destination port
     * @throws IOException if sending fails
     */
    private void handleList(InetAddress addr, int port) throws IOException {
        StringBuilder sb = new StringBuilder(Protocol.LIST + "\n");

        // Append all files with index|name|size
        for (int i = 0; i < sharedFiles.size(); i++) {
            File f = sharedFiles.get(i);
            sb.append(i).append('|').append(f.getName()).append('|').append(f.length()).append('\n');
        }

        UDPHelp.sendText(socket, sb.toString(), addr, port);
        log.log("Sent LIST to " + addr);
    }
}


