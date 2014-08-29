package oarlib.graph.util;

/**
 * Generic pair class.  This time, we take two argument types; originally for storing vertex-edge pairs for neighbor
 * storage, but could be used for other things.
 *
 * @param <S, T>
 * @author oliverlum
 */
public class UnmatchedPair<S, T> {

    private S mFirst;
    private T mSecond;

    public UnmatchedPair(S first, T second) {
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

    //=============================
    // Equals and HashCode overrides
    //=============================
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        else if (o == this)
            return true;
        else if (!(o instanceof UnmatchedPair<?, ?>))
            return false;
        else {
            @SuppressWarnings("unchecked")
            UnmatchedPair<S, T> test = (UnmatchedPair<S, T>) o;
            return (test.getFirst().equals(mFirst) && test.getSecond().equals(mSecond));
        }
    }

    @Override
    public int hashCode() {
        return (991 * mFirst.hashCode() ^ (mSecond.hashCode()));
    }

}
