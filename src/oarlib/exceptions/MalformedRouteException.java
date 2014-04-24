package oarlib.exceptions;

/**
 * Exception that should be thrown when a GraphReader object is told to read using one format
 * and the file specified does not appear to be in that format.
 * 
 * @author oliverlum
 *
 */
public class MalformedRouteException extends Exception{
	/**
	 * Auto-generated serialVersionUID
	 */
	private static final long serialVersionUID = 6028927185057697648L;

	public MalformedRouteException() 
	{ 
		super(); 
	}
	public MalformedRouteException(String message) 
	{ 
		super(message); 
	}
	public MalformedRouteException(String message, Throwable cause) 
	{
		super(message,cause);
	}
	public MalformedRouteException(Throwable cause)
	{
		super(cause);
	}
}
