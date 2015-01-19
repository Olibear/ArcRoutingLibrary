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
package oarlib.solver.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.SingleVehicleSolver;
import oarlib.core.Solver;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.Arc;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.DirectedVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class DCPPSolver_Edmonds extends SingleVehicleSolver<DirectedVertex, Arc, DirectedGraph> {

    public DCPPSolver_Edmonds(Problem<DirectedVertex, Arc, DirectedGraph> instance) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
    }

    private static void eulerAugment(DirectedGraph input) {

        /*
         * The procedure that handles the graph augmentation phase, where edges are added to the graph
         * to make it eulerian, (upon which a tour construction procedure is called).
         */
        //prepare our unbalanced vertex sets
        for (DirectedVertex v : input.getVertices()) {
            if (v.getDelta() != 0) {
                v.setDemand(v.getDelta());
            }
        }
        try {
            if (!CommonAlgorithms.isEulerian(input)) {
                int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(input);

                TIntObjectHashMap<Arc> indexedArcs = input.getInternalEdgeMap();
                Arc temp;
                //add the solution to the graph (augment)
                for (int i = 1; i < flowanswer.length; i++) {
                    temp = indexedArcs.get(i);
                    for (int j = 0; j < flowanswer[i]; j++) {
                        input.addEdge(new Arc("added from flow", temp.getEndpoints(), temp.getCost()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Collection<Tour<DirectedVertex, Arc>> solve() {

        /*
         * The algorithm is simply:
         *
         * -Determine the imbalanced vertices (excess in or out degree)
         * -Solve the min-cost flow problem induced by the graph
         * -For each edge, add copies equal to the amount of flow specified
         * by the solution to the flow problem
         * -Route
         *
         * copy - so we don't screw with the original graph passed in
         *
         * indexedArcs - the edge map for copy
         *
         * ans - the list of edges returned by the routing procedure, in the order they are traversed on the tour
         *
         * eulerTour - the route container which will make the string rep. look more like something we want to see
         * (e.g. a vertex route).
         */
        DirectedGraph copy = mInstance.getGraph().getDeepCopy();
        TIntObjectHashMap<Arc> indexedArcs = copy.getInternalEdgeMap();

        eulerAugment(copy);

        // return the answer
        ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(copy);
        Tour<DirectedVertex, Arc> eulerTour = new Tour<DirectedVertex, Arc>();
        for (int i = 0; i < ans.size(); i++) {
            eulerTour.appendEdge(indexedArcs.get(ans.get(i)));
        }
        mInstance.setSol(eulerTour);

        HashSet<Tour<DirectedVertex, Arc>> ret = new HashSet<Tour<DirectedVertex, Arc>>();
        ret.add(eulerTour);
        return ret;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.DIRECTED, ProblemAttributes.Type.CHINESE_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Edmonds' Directed Chinese Postman Solver (Exact)";
    }

    @Override
    public Solver instantiate(Problem p) {
        return new DCPPSolver_Edmonds(p);
    }

    @Override
    protected Problem<DirectedVertex, Arc, DirectedGraph> getInstance() {
        return mInstance;
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            DirectedGraph mGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isStronglyConnected(mGraph))
                return false;
        }
        return true;
    }

}
