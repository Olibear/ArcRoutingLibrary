package oarlib.exceptions;

/**
 * Exception that should be thrown when it is not feasible to solve the problem on the specified graph.
 *
 * @author oliverlum
 */
public class GraphInfeasibleException extends Exception {
    /**
     * Auto-generated serialVersionUID
     */
    private static final long serialVersionUID = -358601937505440472L;

    public GraphInfeasibleException() {
        super();
    }

    public GraphInfeasibleException(String message) {
        super(message);
    }

    public GraphInfeasibleException(String message, Throwable cause) {
        super(message, cause);
    }

    public GraphInfeasibleException(Throwable cause) {
        super(cause);
    }
}
