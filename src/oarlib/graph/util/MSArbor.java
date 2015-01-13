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

public class MSArbor {

    /**
     * The actual JNI call to the shortest spanning arborescence C++ code MSArbor.
     * We are following in the spirit of the msa15 example included with the code,
     * and providing directly the edge weights instead of extracting them from a
     * formatted text file.
     *
     * @param n       - the number of nodes
     * @param m       - the number of edges
     * @param weights - the associated edge costs; the graph is assumed to be complete, and
     *                the weights are ordered according to the source vertex,
     *                <p/>
     *                (e.g. if there are 5 nodes, then they are numbered 0,1,2,3,4 and:
     *                <p/>
     *                weights = ["0",0-1,0-2,0-3,1-0,"0",1-2,1-3...4-2,4-3]
     *                <p/>
     *                Notice that we do not include weights going into the final vertex because this
     *                is assumed to be the root of the spanning arborescence.
     *                ).
     * @return
     */
    public native static int[] msArbor(int n, int m, int[] weights);

    static {
        System.loadLibrary("MSArbor");
    }
}
