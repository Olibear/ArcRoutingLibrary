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
package oarlib.problem.impl.rpp;

import oarlib.core.Graph;
import oarlib.graph.impl.DirectedGraph;
import oarlib.link.impl.Arc;
import oarlib.metrics.SumMetric;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.RuralPostmanProblem;
import oarlib.vertex.impl.DirectedVertex;

/**
 * The Directed Rural Postman Problem.
 *
 * @author oliverlum
 */
public class DirectedRPP extends RuralPostmanProblem<DirectedVertex, Arc, DirectedGraph> {

    public DirectedRPP(DirectedGraph g) {
        this(g, "");
    }

    public DirectedRPP(DirectedGraph g, String name) {
        super(g, name, new SumMetric());
        mGraph = g;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.DIRECTED, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }
}
