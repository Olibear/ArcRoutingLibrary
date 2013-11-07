package oarlib.exceptions;

/**
 * Exception that should be thrown when a GraphReader ends up trying to read a graph, but 
 * there's no associated read method for that format 
 * 
 * @author oliverlum
 *
 */
public class UnsupportedFormatException extends Exception{
	public UnsupportedFormatException() { super(); }
	public UnsupportedFormatException(String message) { super(message); }
	public UnsupportedFormatException(String message, Throwable cause) {super(message,cause);}
	public UnsupportedFormatException(Throwable cause){super(cause);}
}
