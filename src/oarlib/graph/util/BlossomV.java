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

public class BlossomV {

    /**
     * The actual JNI call to our wrapper of Kolmogorov's Blossom V code.  We are following
     * in the spirit of the example.cpp included in the code, and providing directly the edge / weight
     * vectors instead of extracting them from a DIMACS formatted text file.
     *
     * @param n       - num nodes
     * @param edges   - edge i connects vertices edges[2i] and edges[2i+1]
     * @param weights - edge i has cost weights[i]
     * @return - say how this gets formatted.
     * @par m - num edges
     */
    public native static int[] blossomV(int n, int m, int[] edges, int[] weights);

    static {
        System.loadLibrary("BlossomV");
    }

}
