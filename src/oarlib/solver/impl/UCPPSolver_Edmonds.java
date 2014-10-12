package oarlib.solver.impl;

import oarlib.core.Edge;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.SingleVehicleSolver;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class UCPPSolver_Edmonds extends SingleVehicleSolver {

    UndirectedCPP mInstance;

    public UCPPSolver_Edmonds(UndirectedCPP instance) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
    }

    /**
     * Carries out the bulk of the solve logic; it produces a least cost eulerian augmentation of the graph.
     *
     * @param input - the original undirected graph
     * @return the least cost eulerian augmentation
     */
    private static void eulerAugment(UndirectedGraph input) {
        try {

            /*
             * The procedure that handles the graph augmentation phase, where edges are added to the graph
             * to make it eulerian, (upon which a tour construction procedure is called).
             *
             * n - num vertices in the input graph
             *
             * dist - the shortest paths distance matrix
             * path - the shortest paths next hop matrix
             * edgePath - the shortest paths next edge matrix
             *
             * matchingGraph - the complete graph where each vertex represents an odd vertex in the original,
             * and edges have weight equal to the shortest path costs in the original.
             *
             * oddVertices - the vertex set of matchingGraph
             *
             * matchingSolution - the solution to the min cost perfect matching; it is a collection of pairs of vertices
             */

            //solve shortest paths
            int n = input.getVertices().size();
            int[][] dist = new int[n + 1][n + 1];
            int[][] path = new int[n + 1][n + 1];
            int[][] edgePath = new int[n + 1][n + 1];
            CommonAlgorithms.fwLeastCostPaths(input, dist, path, edgePath);

            //setup the complete graph composed entirely of the unbalanced vertices
            UndirectedGraph matchingGraph = new UndirectedGraph();

            //setup our graph of unbalanced vertices
            for (UndirectedVertex v : input.getVertices()) {
                if (v.getDegree() % 2 == 1) {
                    matchingGraph.addVertex(new UndirectedVertex("oddVertex"), v.getId());
                }
            }

            //connect with least cost edges
            Collection<UndirectedVertex> oddVertices = matchingGraph.getVertices();
            for (UndirectedVertex v : oddVertices) {
                for (UndirectedVertex v2 : oddVertices) {
                    //only add one edge per pair of vertices
                    if (v.getId() <= v2.getId())
                        continue;
                    matchingGraph.addEdge(new Edge("matchingEdge", new Pair<UndirectedVertex>(v, v2), dist[v.getMatchId()][v2.getMatchId()]));
                }
            }

            Set<Pair<UndirectedVertex>> matchingSolution = CommonAlgorithms.minCostMatching(matchingGraph);

            //add the paths to the graph
            for (Pair<UndirectedVertex> p : matchingSolution) {
                CommonAlgorithms.addShortestPath(input, dist, path, edgePath, new Pair<Integer>(p.getFirst().getMatchId(), p.getSecond().getMatchId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Route solve() {
        try {
            /*
             * The algorithm is simply:
             *
             * -Determine odd vertices
             * -Solve a min-cost perfect matching on a complete graph of these vertices, where costs are shortest paths in the original graph.
             * -Connect up with shortest paths
             * -Route
             *
             * copy - so we don't screw with the original graph passed in
             *
             * indexedEdges - the edge map for copy
             *
             * ans - the list of edges returned by the routing procedure, in the order they are traversed in the tour.
             *
             * eulerTour - the route container which will make the string rep. look more like something we want to see
             * (e.g. a vertex route).
             */

            UndirectedGraph copy = mInstance.getGraph().getDeepCopy();
            eulerAugment(copy);

            HashMap<Integer, Edge> indexedEdges = copy.getInternalEdgeMap();
            //return the answer
            ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(copy);
            Tour eulerTour = new Tour();
            for (int i = 0; i < ans.size(); i++) {
                eulerTour.appendEdge(indexedEdges.get(ans.get(i)));
            }
            currSol = eulerTour;
            return eulerTour;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.UNDIRECTED_CHINESE_POSTMAN;
    }

    @Override
    public String printCurrentSol() throws IllegalStateException {
        if (currSol == null)
            throw new IllegalStateException("It does not appear as though this solver has been run yet!");

        String ans = "UCPPSolver_Edmonds: Printing current solution...";
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
    protected UndirectedCPP getInstance() {
        return mInstance;
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            UndirectedGraph mGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isConnected(mGraph))
                return false;
        }
        return true;
    }
}
