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
package oarlib.exceptions;

/**
 * Created by oliverlum on 11/5/14.
 */
public class NegativeCycleException extends Exception {

    private int violatingIndex;
    private int[] path;
    private int[] edgePath;

    public NegativeCycleException(int indexInCycle, int[] violatingPath, int[] violatingEdgePath) {
        super();
        violatingIndex = indexInCycle;
        path = violatingPath;
        edgePath = violatingEdgePath;
    }

    public NegativeCycleException(int indexInCycle, int[] violatingPath, int[] violatingEdgePath, String message) {
        super(message);
        violatingIndex = indexInCycle;
        path = violatingPath;
        edgePath = violatingEdgePath;
    }

    public NegativeCycleException(int indexInCycle, int[] violatingPath, int[] violatingEdgePath, String message, Throwable cause) {
        super(message, cause);
        violatingIndex = indexInCycle;
        path = violatingPath;
        edgePath = violatingEdgePath;
    }

    public NegativeCycleException(int indexInCycle, int[] violatingPath, int[] violatingEdgePath, Throwable cause) {
        super(cause);
        violatingIndex = indexInCycle;
        path = violatingPath;
        edgePath = violatingEdgePath;
    }

    public int getViolatingIndex() {
        return violatingIndex;
    }

    public int[] getViolatingPath() {
        return path;
    }

    public int[] getViolatingEdgePath() {
        return edgePath;
    }
}

