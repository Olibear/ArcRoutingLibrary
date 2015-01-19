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
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Created by oliverlum on 12/4/14.
 */
public class Benavent_VND1 extends IntraRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    private static Logger LOGGER = Logger.getLogger(Benavent_VND1.class);

    public Benavent_VND1(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public Benavent_VND1(Problem<WindyVertex, WindyEdge, WindyGraph> problem, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, null, initialSol);
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, null, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public Route<WindyVertex, WindyEdge> improveRoute(Route<WindyVertex, WindyEdge> r) {

        int currBest = r.getCost();
        Route<WindyVertex, WindyEdge> ans = r;

        while (true) {
            OrInterchange oi = new OrInterchange(getProblem(), ImprovementStrategy.Type.SteepestDescent, getInitialSol());
            Route<WindyVertex, WindyEdge> postIP1 = oi.improveRoute(ans);
            LOGGER.debug("VND1-ip1 obj value: " + postIP1.getCost());
            if (postIP1.getCost() < currBest) {
                currBest = postIP1.getCost();
                ans = postIP1;
                continue;
            }

            Reversal reversal = new Reversal(getProblem(), getInitialSol());
            Route<WindyVertex, WindyEdge> postIP2 = reversal.improveRoute(ans);
            LOGGER.debug("VND1-ip2 obj value: " + postIP2.getCost());
            if (postIP2.getCost() < currBest) {
                currBest = postIP2.getCost();
                ans = postIP2;
                continue;
            }

            TwoInterchange ti = new TwoInterchange(getProblem(), ImprovementStrategy.Type.SteepestDescent, getInitialSol());
            Route<WindyVertex, WindyEdge> postIP3 = ti.improveRoute(ans);
            LOGGER.debug("VND1-ip3 obj value: " + postIP3.getCost());
            if (postIP3.getCost() < currBest) {
                currBest = postIP3.getCost();
                ans = postIP3;
                continue;
            }

            break;
        }

        return ans;
    }
}
