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
package oarlib.problem.impl.multivehicle;

import oarlib.core.Graph;
import oarlib.graph.impl.MixedGraph;
import oarlib.link.impl.MixedEdge;
import oarlib.metrics.MaxMetric;
import oarlib.problem.impl.MultiVehicleProblem;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.vertex.impl.MixedVertex;

/**
 * Problem class to represent the Capacitated Mixed Chinese Postman Problem.
 * Currently, this only supports a bound on the number of vehicles, and not a capacity constraint.
 * <p/>
 * Created by oliverlum on 8/12/14.
 */
public class MinMaxKMCPP extends MultiVehicleProblem<MixedVertex, MixedEdge, MixedGraph> {

    public MinMaxKMCPP(MixedGraph graph, int numVehicles) {

        super(graph, numVehicles, new MaxMetric());
        mGraph = graph;

    }

    public MinMaxKMCPP(MixedGraph graph, String name, int numVehicles) {

        super(graph, numVehicles, name, new MaxMetric());
        mGraph = graph;

    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.MIXED, ProblemAttributes.Type.CHINESE_POSTMAN, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }
}
