package oarlib.exceptions;

/**
 * Exception that should be thrown when demands aren't set and some problem which requires demands 
 * hasn't been set.  
 * 
 * @author oliverlum
 *
 */
public class SetupException extends Exception{
	public SetupException() { super(); }
	public SetupException(String message) { super(message); }
	public SetupException(String message, Throwable cause) {super(message,cause);}
	public SetupException(Throwable cause){super(cause);}
}
