package oarlib.exceptions;

/**
 * Exception that should be thrown when demands aren't set and some problem which requires demands 
 * hasn't been set.  
 * 
 * @author oliverlum
 *
 */
public class GraphInfeasibleException extends Exception{
	public GraphInfeasibleException() { super(); }
	public GraphInfeasibleException(String message) { super(message); }
	public GraphInfeasibleException(String message, Throwable cause) {super(message,cause);}
	public GraphInfeasibleException(Throwable cause){super(cause);}
}
