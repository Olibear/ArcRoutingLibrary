package oarlib.exceptions;

/**
 * Exception that should be thrown when an edge is added with vertices that haven't been added to the graph yet.
 * 
 * @author oliverlum
 *
 */
public class InvalidEndpointsException extends Exception{
	/**
	 * Auto-generated serialVersionUID
	 */
	private static final long serialVersionUID = 4683916456011537071L;
	public InvalidEndpointsException() 
	{
		super(); 
	}
	public InvalidEndpointsException(String message) 
	{
		super(message); 
	}
	public InvalidEndpointsException(String message, Throwable cause) 
	{
		super(message,cause);
	}
	public InvalidEndpointsException(Throwable cause)
	{
		super(cause);
	}
}
