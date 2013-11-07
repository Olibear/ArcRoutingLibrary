package oarlib.graph.util;

/**
 * Generic pair class.  For storing endpoints of an Edge, but could also be used for other things.
 * @author oliverlum
 *
 * @param <T>
 */
public class Pair<T> {
	
	private T mFirst;
	private T mSecond;
	
	public Pair(T first, T second)
	{
		setFirst(first);
		setSecond(second);
	}

	//===============================
	// Getters and Setters
	//===============================
	
	public T getFirst() {
		return mFirst;
	}

	public void setFirst(T mFirst) {
		this.mFirst = mFirst;
	}

	public T getSecond() {
		return mSecond;
	}

	public void setSecond(T mSecond) {
		this.mSecond = mSecond;
	}
	
	//=============================
	// Equals and HashCode overrides
	//=============================
	@Override
	public boolean equals(Object o)
	{
		if(o==null)
			return false;
		else if(o==this)
			return true;
		else if (!( o instanceof Pair<?>))
			return false;
		else
		{
			Pair<T> test = (Pair<T>)o;
			return (test.getFirst().equals(mFirst) && test.getSecond().equals(mSecond));
		}
	}
	@Override
	public int hashCode()
	{
		return(991 * mFirst.hashCode() ^ (mSecond.hashCode()));
	}
}
