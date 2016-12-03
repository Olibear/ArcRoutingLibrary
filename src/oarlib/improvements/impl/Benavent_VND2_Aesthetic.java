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
package oarlib.improvements.impl;

import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.ImprovementStrategy;
import oarlib.improvements.InterRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.vertex.impl.WindyVertex;

import java.util.Collection;

/**
 * Created by oliverlum on 12/4/14.
 */
public class Benavent_VND2_Aesthetic extends InterRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    public Benavent_VND2_Aesthetic(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public Benavent_VND2_Aesthetic(Problem<WindyVertex, WindyEdge, WindyGraph> problem, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, null, initialSol);
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, null, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public Collection<Route<WindyVertex, WindyEdge>> improveSolution() {

        long start, end;

        Collection<Route<WindyVertex, WindyEdge>> initialSol = getInitialSol();
        start = System.currentTimeMillis();
        Change1to0Aesthetic ip1 = new Change1to0Aesthetic(getProblem(), ImprovementStrategy.Type.FirstImprovement, initialSol);
        Collection<Route<WindyVertex, WindyEdge>> postIP1 = ip1.improveSolution();
        end = System.currentTimeMillis();
        System.out.println("1 to 0 took " + (end - start) / 1000 + " seconds to run.");
        start = System.currentTimeMillis();
        Change2to0Aesthetic ip2 = new Change2to0Aesthetic(getProblem(), ImprovementStrategy.Type.FirstImprovement, postIP1);
        Collection<Route<WindyVertex, WindyEdge>> postIP2 = ip2.improveSolution();
        end = System.currentTimeMillis();
        System.out.println("1 to 0 took " + (end - start) / 1000 + " seconds to run.");
        start = System.currentTimeMillis();
        Change1to1Aesthetic ip3 = new Change1to1Aesthetic(getProblem(), ImprovementStrategy.Type.FirstImprovement, postIP2);
        Collection<Route<WindyVertex, WindyEdge>> postIP3 = ip3.improveSolution();
        end = System.currentTimeMillis();
        System.out.println("1 to 0 took " + (end - start) / 1000 + " seconds to run.");

        return postIP3;
    }
}
