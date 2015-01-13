/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Oliver Lum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
