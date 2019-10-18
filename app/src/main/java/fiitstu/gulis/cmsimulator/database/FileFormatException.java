package fiitstu.gulis.cmsimulator.database;

/**
 * Thrown when the input file does not match the CMS/JFF format specification
 *
 * Created by Jakub Sedlář on 07.01.2018.
 */
public class FileFormatException extends Exception {
    public FileFormatException() {
    }

    public FileFormatException(String message) {
        super(message);
    }

    public FileFormatException(Throwable cause) {
        super(cause);
    }

    public FileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
