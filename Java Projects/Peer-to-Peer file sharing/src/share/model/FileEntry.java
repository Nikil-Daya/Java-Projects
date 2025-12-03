package share.model;

/**
 * Represents a file entry shared by the Seeder.
 * Contains index, name, and size information.
 *
 * @author NS, Daya 
 */
public class FileEntry {

    private final int index;   // Position of the file in the Seeder's list
    private final String name; // File name
    private final long size;   // File size in bytes

    /**
     * Constructs a FileEntry object.
     *
     * @param index the index of the file in the list
     * @param name  the file name
     * @param size  the file size in bytes
     */
    public FileEntry(int index, String name, long size) {
        this.index = index;
        this.name = name;
        this.size = size;
    }

    /**
     * Gets the index of the file.
     *
     * @return file index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the name of the file.
     *
     * @return file name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the size of the file.
     *
     * @return file size in bytes
     */
    public long getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "FileEntry [index=" + index + ", name=" + name + ", size=" + size + "]";
    }
}

