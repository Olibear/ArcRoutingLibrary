/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Oliver Lum
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
 *
 */
package oarlib.solver.impl;

import oarlib.core.*;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.vertex.impl.WindyVertex;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by oliverlum on 5/10/16.
 */
public class MultiWRPP_Aesthetic_EXACT extends MultiVehicleSolver<WindyVertex, WindyEdge, WindyGraph> {

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    protected MultiWRPP_Aesthetic_EXACT(Problem<WindyVertex, WindyEdge, WindyGraph> instance) throws IllegalArgumentException {
        super(instance);
    }

    @Override
    protected boolean checkGraphRequirements() {
        //make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            WindyGraph mGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isConnected(mGraph))
                return false;
        }
        return true;
    }

    @Override
    protected Problem<WindyVertex, WindyEdge, WindyGraph> getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<? extends Route> solve() {
        return null;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Accelerated Corberan IP";
    }

    @Override
    public Solver<WindyVertex, WindyEdge, WindyGraph> instantiate(Problem<WindyVertex, WindyEdge, WindyGraph> p) {
        return new MultiWRPP_Aesthetic_EXACT(p);
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        HashMap<String, Double> ret = new HashMap<String, Double>();
        return ret;
    }
}
