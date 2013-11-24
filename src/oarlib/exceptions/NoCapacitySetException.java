package oarlib.exceptions;

/**
 * Exception that should be thrown when demands aren't set and some problem which requires demands 
 * to be set is being solved.  
 * 
 * @author oliverlum
 *
 */
public class NoCapacitySetException extends Exception{
	public NoCapacitySetException() { super(); }
	public NoCapacitySetException(String message) { super(message); }
	public NoCapacitySetException(String message, Throwable cause) {super(message,cause);}
	public NoCapacitySetException(Throwable cause){super(cause);}
}
