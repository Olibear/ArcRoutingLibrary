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

import gnu.trove.TIntArrayList;
import oarlib.core.Graph;
import oarlib.core.Route;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.link.impl.ZigZagLink;
import oarlib.metrics.SumMetric;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.RuralPostmanProblem;
import oarlib.route.impl.ZigZagTour;
import oarlib.vertex.impl.ZigZagVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 6/20/15.
 */
public class WindyRPPZZTW extends RuralPostmanProblem<ZigZagVertex, ZigZagLink, ZigZagGraph> {

    private static Logger LOGGER = Logger.getLogger(WindyRPPZZTW.class);

    public WindyRPPZZTW(ZigZagGraph graph, String name) {
        super(graph, name, new SumMetric());
        mGraph = graph;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, ProblemAttributes.Properties.TIME_WINDOWS);
    }

    @Override
    public boolean isFeasible(Collection<Route<ZigZagVertex, ZigZagLink>> routes) {
        if (super.isFeasible(routes)) {
            //check to make sure that zig-zag status is obeyed
            for (Route<ZigZagVertex, ZigZagLink> r : routes) {
                if (!(r instanceof ZigZagTour)) {
                    LOGGER.warn("Routes must be zig-zag tours. Returning false.");
                    return false;
                }
                ZigZagTour rTemp = (ZigZagTour) r;
                TIntArrayList compactRep = rTemp.getCompactRepresentation();
                ArrayList<Boolean> zzList = rTemp.getCompactZZList();
                TIntArrayList runningCost = rTemp.getIncrementalCost();
                ArrayList<ZigZagLink> tempRoute = rTemp.getPath();

                //check time windows
                if (runningCost.get(0) > tempRoute.get(0).getTimeWindow().getSecond()) {
                    return false;
                }
                for (int i = 1; i < compactRep.size(); i++) {
                    if (runningCost.get(i) > tempRoute.get(i).getTimeWindow().getSecond()) {
                        return false;
                    }
                    if (runningCost.get(i - 1) < tempRoute.get(i).getTimeWindow().getFirst()) {
                        return false;
                    }
                }
            }
        }

        return false;
    }
}
