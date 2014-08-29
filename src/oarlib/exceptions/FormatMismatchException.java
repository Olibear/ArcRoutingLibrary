package oarlib.exceptions;

/**
 * Exception that should be thrown when a GraphReader object is told to read using one format
 * and the file specified does not appear to be in that format.
 *
 * @author oliverlum
 */
public class FormatMismatchException extends Exception {
    /**
     * Auto-generated serialVersionUID
     */
    private static final long serialVersionUID = 320275301493290688L;

    public FormatMismatchException() {
        super();
    }

    public FormatMismatchException(String message) {
        super(message);
    }

    public FormatMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public FormatMismatchException(Throwable cause) {
        super(cause);
    }
}
