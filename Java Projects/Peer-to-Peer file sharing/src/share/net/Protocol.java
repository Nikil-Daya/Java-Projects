package share.net;



/**
 * Defines the protocol constants used for communication 
 * between Seeder and Leecher.
 *
 * Messages are exchanged using specific tags:
 * LIST, FILE, META, DATA, END.
 *
 * Also defines the default port and chunk size.
 * 
 * @author NS, Daya 
 */
public class Protocol {

    /** Command to request or send a file list */
    public static final String LIST = "LIST";

    /** Command to request a file */
    public static final String FILE = "FILE";

    /** Metadata packet identifier */
    public static final String META = "FILEMETA";

    /** File data packet identifier */
    public static final String DATA = "FILEDATA";

    /** End-of-transfer packet identifier */
    public static final String END = "FILEEND";

    /** Default port for communication */
    public static final int port = 7777;

    /** Default chunk size for file transfer */
    public static final int size = 4096;
}

