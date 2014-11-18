package oarlib.solver.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.SingleVehicleSolver;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.Arc;
import oarlib.problem.impl.cpp.DirectedCPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.DirectedVertex;

import java.util.ArrayList;

public class DCPPSolver_Edmonds extends SingleVehicleSolver {

    DirectedCPP mInstance;

    public DCPPSolver_Edmonds(DirectedCPP instance) throws IllegalArgumentException {
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
    protected Route solve() {

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
        currSol = eulerTour;
        return eulerTour;
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.DIRECTED_CHINESE_POSTMAN;
    }

    @Override
    public String printCurrentSol() throws IllegalStateException {
        if (currSol == null)
            throw new IllegalStateException("It does not appear as though this solver has been run yet!");

        String ans = "DCPPSolver_Edmonds: Printing current solution...";
        ans += "\n";
        ans += "=======================================================";
        ans += "\n";
        ans += "Vertices: " + mInstance.getGraph().getVertices().size() + "\n";
        ans += "Edges: " + mInstance.getGraph().getEdges().size() + "\n";
        ans += "Route Cost: " + currSol.getCost() + "\n";
        ans += "\n";
        ans += "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += currSol.toString();
        ans += "\n";
        ans += "\n";
        ans += "=======================================================";

        return ans;
    }

    @Override
    protected DirectedCPP getInstance() {
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
