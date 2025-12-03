package share.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import share.model.FileEntry;



/**
 * The LeecherClient class is responsible for connecting to a SeederServer 
 * and requesting file lists and file transfers via UDP.
 *
 * @author NS, Daya 
 */
public class LeecherClient {

    private final InetAddress addr;
    private final int port;

    /**
     * Constructs a LeecherClient with a target host and port.
     *
     * @param host the hostname or IP address of the seeder
     * @param port the port number of the seeder
     * @throws UnknownHostException if the host cannot be resolved
     */
    public LeecherClient(String host, int port) throws UnknownHostException {
        this.addr = InetAddress.getByName(host);
        this.port = port;
    }

    /**
     * Sends a LIST request to the SeederServer and receives a list of files.
     *
     * @return a list of available FileEntry objects
     * @throws IOException if there is a communication error
     */
    public List<FileEntry> requestList() throws IOException {
        try (DatagramSocket sock = new DatagramSocket()) {
            UDPHelp.sendText(sock, Protocol.LIST, addr, port);

            // Receive LIST response
            byte[] buf = new byte[65507];
            DatagramPacket p = new DatagramPacket(buf, buf.length);
            sock.receive(p);

            String payload = UDPHelp.readText(buf, p.getLength());

            List<FileEntry> out = new ArrayList<>();
            String[] lines = payload.split("\n");

            // Skip first line (the LIST header)
            for (int i = 1; i < lines.length; i++) {
                String[] parts = lines[i].split("\\|");
                if (parts.length >= 3) {
                    out.add(new FileEntry(
                            Integer.parseInt(parts[0]),
                            parts[1],
                            Long.parseLong(parts[2])
                    ));
                }
            }
            return out;
        }
    }

    /**
     * Requests a file from the SeederServer by index and saves it to disk.
     *
     * @param index   the index of the file to retrieve
     * @param savedir the directory where the file should be saved
     * @param log     a log handler for progress messages
     * @throws IOException if there is a communication or file error
     */
    public void retrieveFile(int index, Path savedir, LogHandler log) throws IOException {
        try (DatagramSocket sock = new DatagramSocket()) {
            // Request the file
            UDPHelp.sendText(sock, Protocol.FILE + "|" + index, addr, port);

            byte[] buf = new byte[65507];
            DatagramPacket p = new DatagramPacket(buf, buf.length);
            sock.receive(p);

            // Parse META packet
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(p.getData(), 0, p.getLength()));
            String tag = dis.readUTF();

            if (!Protocol.META.equals(tag)) throw new IOException("Unexpected response: " + tag);
            String filename = dis.readUTF();
            long size = dis.readLong();
            int chunk = dis.readInt();

            Path outFile = savedir.resolve(filename);

            try (OutputStream out = Files.newOutputStream(outFile)) {
                long received = 0;
                boolean done = false;
                int expSeq = 0;

                // Receive file chunks until END packet
                while (!done) {
                    p.setData(buf);
                    p.setLength(buf.length);
                    sock.receive(p);

                    DataInputStream d2 = new DataInputStream(new ByteArrayInputStream(p.getData(), 0, p.getLength()));
                    String tag2 = d2.readUTF();

                    if (Protocol.DATA.equals(tag2)) {
                        int seq = d2.readInt();
                        int len = d2.readInt();
                        byte[] data = d2.readNBytes(len);

                        // Write only if sequence matches expected
                        if (seq == expSeq) {
                            out.write(data);
                            received += len;
                            expSeq++;
                        }
                    } else if (Protocol.END.equals(tag2)) {
                        done = true;
                    }
                }
                log.log("Saved " + outFile + " (" + received + " bytes)");
            }
        }
    }

    /**
     * Simple logging interface used by the LeecherClient.
     */
    public interface LogHandler {
        void log(String msg);
    }
}

