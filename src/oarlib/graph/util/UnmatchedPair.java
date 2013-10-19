package oarlib.graph.util;

/**
 * Generic pair class.  This time, we take two argument types; originally for storing vertex-edge pairs for neighbor 
 * storage, but could be used for other things.
 * @author oliverlum
 *
 * @param <S, T>
 */
public class UnmatchedPair<S, T> {
	
	private S mFirst;
	private T mSecond;
	
	public UnmatchedPair(S first, T second)
	{
		setFirst(first);
		setSecond(second);
	}

	//===============================
	// Getters and Setters
	//===============================
	
	public S getFirst() {
		return mFirst;
	}

	public void setFirst(S mFirst) {
		this.mFirst = mFirst;
	}

	public T getSecond() {
		return mSecond;
	}

	public void setSecond(T mSecond) {
		this.mSecond = mSecond;
	}

}
