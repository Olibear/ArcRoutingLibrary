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
package oarlib.graph.transform.rebalance;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.impl.WindyGraph;
import oarlib.link.impl.WindyEdge;

import java.util.HashMap;

/**
 * Created by oliverlum on 8/9/14.
 */
public abstract class CostRebalancer<S extends Graph<?, ?>> {
    protected S mGraph;
    protected CostRebalancer<S> mNextRebalancer;
    private HashMap<Integer, Integer> defaultMap;

    /**
     * Super constructor for any Rebalance transformers.
     *
     * @param input - the input graph.
     * @throws FormatMismatchException - if the ArrayList is of the wrong size.
     */
    protected CostRebalancer(S input) throws FormatMismatchException {
        this(input, null);
    }

    protected CostRebalancer(S input, CostRebalancer<S> nextRebalancer) {
        mGraph = input;
        mNextRebalancer = nextRebalancer;
        defaultMap = new HashMap<Integer, Integer>();
        if (input.getClass() == WindyGraph.class) {
            for (Link l : mGraph.getEdges())
                defaultMap.put(l.getId(), (int) (l.getCost() + ((WindyEdge) l).getReverseCost() * .5));
        } else {
            for (Link l : mGraph.getEdges()) {
                defaultMap.put(l.getId(), l.getCost());
            }
        }
    }

    public final HashMap<Integer, Integer> rebalance() {
        HashMap<Integer, Integer> baseAns = this.startRebalance(defaultMap);
        if (mNextRebalancer != null) ;
        return mNextRebalancer.startRebalance(baseAns);
    }

    protected abstract HashMap<Integer, Integer> startRebalance(HashMap<Integer, Integer> input);

}
