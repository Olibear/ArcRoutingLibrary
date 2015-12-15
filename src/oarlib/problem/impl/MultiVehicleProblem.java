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
package oarlib.problem.impl;

import oarlib.core.*;
import oarlib.graph.util.Utils;
import oarlib.metrics.Metric;
import oarlib.route.impl.Tour;

import java.util.Collection;
import java.util.HashSet;

/**
 * Problem abstraction for capacitated problems.  This includes support for number of vehicles, or for vehicle capacity.
 * Currently, this does not include support for heterogeneous fleets.
 * <p/>
 * Created by Oliver Lum on 7/25/2014.
 */
public abstract class MultiVehicleProblem<V extends Vertex, E extends Link<V>, G extends Graph<V, E>> extends Problem<V, E, G> {

    /**
     * Default constructor for a Capacitated Problem, this assumes there is no max capacity, but rather a constraint
     * on the number of vehicles used.
     *
     * @param numVehicles - the number of routes allowed to exist in the final solution.
     */
    protected MultiVehicleProblem(G graph, int numVehicles, Metric objFunc) {
        super(graph, "", objFunc);
        setNumVehicles(numVehicles);
    }

    protected MultiVehicleProblem(G graph, int numVehicles, String name, Metric objFunc) {
        super(graph, name, objFunc);
        setNumVehicles(numVehicles);
    }

    @Override
    public boolean isFeasible(Collection<Route<V, E>> routes) {

        if (routes.size() > getmNumVehicles())
            return false;

        //all ids
        HashSet<Integer> ids = new HashSet<Integer>();
        for (Link l : mGraph.getEdges())
            if (l.isRequired())
                ids.add(l.getId());

        for (Route<V, E> r : routes) {
            if (!Tour.isTour(r))
                return false;

            //route ids
            Route<V, E> reclaimed = Utils.reclaimTour(r, mGraph);
            for (E e : reclaimed.getPath())
                ids.remove(e.getId());
        }

        return ids.isEmpty();
    }
}
