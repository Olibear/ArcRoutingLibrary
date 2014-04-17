package oarlib.exceptions;

/**
 * Exception that should be thrown when the input to a function was messed up, and so the 
 * ret value is either worthless, or indicative of this error.
 * 
 * @author oliverlum
 *
 */
public class SetupException extends Exception{
	/**
	 * Auto-generated serialVersionUID
	 */
	private static final long serialVersionUID = 8204894550590777253L;
	public SetupException() 
	{
		super(); 
	}
	public SetupException(String message) 
	{
		super(message); 
	}
	public SetupException(String message, Throwable cause) 
	{
		super(message,cause);
	}
	public SetupException(Throwable cause)
	{
		super(cause);
	}
}
