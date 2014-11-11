package oarlib.solver.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.*;
import oarlib.display.GraphDisplay;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.IndexedRecord;
import oarlib.link.impl.Arc;
import oarlib.problem.impl.MultiVehicleWRPP;
import oarlib.problem.impl.WindyRPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.WindyVertex;

import java.util.*;

/**
 * Created by oliverlum on 10/17/14.
 */
public class MultiWRPPSolver_Benavent extends MultiVehicleSolver {

    MultiVehicleWRPP mInstance;
    WindyGraph mGraph;
    String mInstanceName;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public MultiWRPPSolver_Benavent(MultiVehicleWRPP instance, String instanceName) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
        mGraph = mInstance.getGraph();
        mInstanceName = instanceName;
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
    protected MultiVehicleProblem getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<Route> solve() {
        //Solve the single-vehicle WRPP instance, and then split it into K routes by the process discussed in Lacomme, Prins, and Ramdane-Cherif
        WindyGraph copy = mGraph.getDeepCopy();

        /*
         * Solve the WRPP instance
         */
        WindyRPP singleProblem = new WindyRPP(copy);
        WRPPSolver_Benavent_H1 singleSolver = new WRPPSolver_Benavent_H1(singleProblem);
        Route singleAns = singleSolver.solve();

        /*
         * Display it
         */
        try {
            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, copy, mInstanceName);
            gd.export(GraphDisplay.ExportType.PDF);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error displaying graph.");
        }

        /*
         * now split it
         */
        Collection<Route> multiAns = splitRoute(singleAns);

        return multiAns;
    }

    private Collection<Route> splitRoute(Route singleAns) {

        try {
            //Compile the ordered list of required edges.
            ArrayList<Arc> orderedReqEdges = new ArrayList<Arc>();
            for (Link<? extends Vertex> arc : singleAns.getRoute()) {
                if (arc.isRequired())
                    orderedReqEdges.add((Arc) arc);
            }

            //Compute shortest path distances in the full graph
            int n = mGraph.getVertices().size();
            int[][] dist = new int[n + 1][n + 1];
            int[][] path = new int[n + 1][n + 1];

            CommonAlgorithms.fwLeastCostPaths(mGraph, dist, path);

        /*
         * Compute the acyclic digraph H in which an edge (i-1,j) represents the cost of having a
         * tour that traverses edges i through j, and starts and ends at the depot
         */
            int m = orderedReqEdges.size();
            int tempCost;
            int depotId = mGraph.getDepotId();
            int maxtempCost = Integer.MIN_VALUE;
            DirectedGraph H = new DirectedGraph(m + 1);
            int prevEnd, nextStart;
            for (int i = 1; i <= m; i++) {
                for (int j = i; j <= m; j++) {
                    tempCost = 0;
                    for (int k = i; k <= j; k++) {

                        //add the cost of getting here
                        if (k > 1) {
                            prevEnd = orderedReqEdges.get(k - 2).getHead().getId();
                            nextStart = orderedReqEdges.get(k - 1).getTail().getId();
                            if (prevEnd != nextStart)
                                tempCost += dist[prevEnd][nextStart];
                        }
                        //add the cost of this req. edge
                        tempCost += orderedReqEdges.get(k - 1).getCost();

                    }
                    tempCost += dist[depotId][orderedReqEdges.get(i - 1).getTail().getId()];
                    tempCost += dist[orderedReqEdges.get(j - 1).getHead().getId()][depotId];
                    H.addEdge(i, j + 1, tempCost);
                    if (tempCost > maxtempCost)
                        maxtempCost = tempCost;
                }
            }

            //transform the graph so that when we solve the widest path problem, we get the min-max shortest path
            for (Arc a : H.getEdges())
                a.setCost(maxtempCost - a.getCost());

            //calculate a min-max shortest path problem from 1 to m+1 in H.
            IndexedRecord<Integer>[] width = new IndexedRecord[m + 2];
            IndexedRecord<Integer>[] widestPath = new IndexedRecord[m + 2];
            IndexedRecord<Integer>[] widestEdgePath = new IndexedRecord[m + 2];
            CommonAlgorithms.dijkstrasWidestPathAlgorithmWithMaxPathCardinality(H, 1, width, widestPath, widestEdgePath, mInstance.getmNumVehicles());

            //now construct the routes
            Stack<Integer> stoppingPoints = new Stack<Integer>();

            int prev = m + 1;
            Tour temp;
            int numSteps = width[prev].getRecordKey();
            do {
                stoppingPoints.push(prev);
                prev = widestPath[prev].getEntry(numSteps--);
            } while (prev != 1);

            ArrayList<Route> ans = new ArrayList<Route>();
            int counter = 0;
            int singleRouteCounter = 0;
            int curr, next, end, cost;
            List<? extends Link<? extends Vertex>> singleRoute = singleAns.getRoute();
            Link<? extends Vertex> linkToAdd;
            TIntObjectHashMap<WindyVertex> mVertices = mGraph.getInternalVertexMap();
            do {
                //DirectedGraph toAddGraph = new DirectedGraph(mGraph.getVertices().size(), mGraph.getDepotId());
                DirectedGraph toAddGraph = new DirectedGraph();
                toAddGraph.setDepotId(mGraph.getDepotId());
                for (int i = 1; i <= n; i++) {
                    DirectedVertex toAdd = new DirectedVertex("");
                    toAdd.setCoordinates(mVertices.get(i).getX(), mVertices.get(i).getY());
                    toAddGraph.addVertex(toAdd);
                }

                //add path from depot to start
                curr = mGraph.getDepotId();
                end = orderedReqEdges.get(counter).getTail().getId();
                do {
                    next = path[curr][end];
                    cost = dist[curr][next];
                    toAddGraph.addEdge(curr, next, cost, false);
                } while ((curr = next) != end);

                //increment singleRouteCounter to catch up
                while (singleRoute.get(singleRouteCounter).getId() != orderedReqEdges.get(counter).getId()) {
                    singleRouteCounter++;
                }

                //add guys from single route
                end = stoppingPoints.pop();
                while (singleRoute.get(singleRouteCounter).getId() != orderedReqEdges.get(end - 2).getId()) {
                    //add singleRoute.get(singleRouteCounter) to the path
                    linkToAdd = singleRoute.get(singleRouteCounter);
                    toAddGraph.addEdge(linkToAdd.getEndpoints().getFirst().getId(), linkToAdd.getEndpoints().getSecond().getId(), linkToAdd.getCost(), linkToAdd.isRequired());
                    singleRouteCounter++;
                }
                //add singleRoute.get(singleRouteCounter) to the path
                linkToAdd = singleRoute.get(singleRouteCounter);
                toAddGraph.addEdge(linkToAdd.getEndpoints().getFirst().getId(), linkToAdd.getEndpoints().getSecond().getId(), linkToAdd.getCost(), linkToAdd.isRequired());
                counter = end - 2;

                //add path from end to depot
                curr = orderedReqEdges.get(counter).getHead().getId();
                end = mGraph.getDepotId();
                do {
                    next = path[curr][end];
                    cost = dist[curr][next];
                    toAddGraph.addEdge(curr, next, cost, false);
                } while ((curr = next) != end);

                //add the route
                ArrayList<Integer> tour = CommonAlgorithms.tryHierholzer(toAddGraph);
                TIntObjectHashMap<Arc> indexedArcs = toAddGraph.getInternalEdgeMap();
                Tour toAdd = new Tour();
                for (int i = 0; i < tour.size(); i++) {
                    toAdd.appendEdge(indexedArcs.get(tour.get(i)));
                }
                ans.add(toAdd);

                counter++;

            } while (!stoppingPoints.empty());

            int max = 0;
            for (Route r : ans) {
                if (r.getCost() > max)
                    max = r.getCost();
            }

            currSol = ans;

            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public String printCurrentSol() throws IllegalStateException {
        if (currSol == null)
            throw new IllegalStateException("It does not appear as though this solver has been run yet!");

        int tempCost;
        int numZeroRoutes = 0;
        int totalCost = 0;
        int minLength = Integer.MAX_VALUE;
        int maxLength = Integer.MIN_VALUE;
        double percentVariance, averageCost, averageCostNoEmpty;
        double deviationFromAverage, deviationFromAverageNoEmpty;
        int addedCost = 0;
        int origTotalCost;

        for (Link l : mInstance.getGraph().getEdges())
            addedCost -= l.getCost();

        origTotalCost = -1 * addedCost;


        String ans = "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += "CapacitatedWRPPSolver: Printing current solution for instance " + mInstanceName + "...";
        ans += "\n";
        ans += "\n";
        ans += "=======================================================";
        for (Route r : currSol) {
            //gather metrics
            tempCost = r.getCost();

            if (tempCost == 0)
                numZeroRoutes++;

            if (tempCost < minLength)
                minLength = tempCost;

            if (tempCost > maxLength)
                maxLength = tempCost;

            totalCost += tempCost;

            ans += "\n";
            ans += "Route: " + r.toString() + "\n";
            ans += "Route Cost: " + tempCost + "\n";
            ans += "Route Required Cost: " + r.getReqCost() + "\n";
            ans += "Route Unrequired Cost: " + (tempCost - r.getReqCost()) + "\n";
            ans += "\n";

            //exportSol
            r.exportRouteToPDF(mInstanceName + tempCost);
        }

        percentVariance = ((double) maxLength - minLength) / maxLength;
        averageCost = (double) totalCost / currSol.size();
        averageCostNoEmpty = (double) totalCost / (currSol.size() - numZeroRoutes);
        deviationFromAverage = ((double) maxLength - averageCost) / maxLength;
        deviationFromAverageNoEmpty = ((double) maxLength - averageCostNoEmpty) / maxLength;
        addedCost += totalCost;

        WindyRPP tempInstance = new WindyRPP(mInstance.getGraph());
        WRPPSolver_Benavent_H1 tempSolver = new WRPPSolver_Benavent_H1(tempInstance);
        int oneVObjective = tempSolver.solve().getCost();
        int totalCostShare = origTotalCost / mInstance.getmNumVehicles();
        int solutionCostShare = oneVObjective / mInstance.getmNumVehicles();

        ans += "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += "Vertices: " + mInstance.getGraph().getVertices().size() + "\n";
        ans += "Edges: " + mInstance.getGraph().getEdges().size() + "\n";
        ans += "Total Edge Cost: " + origTotalCost + "\n";
        ans += "Total Edge Cost / num vehicles (Total Cost Share): " + totalCostShare + "\n";
        ans += "Cost of 1-Vehicle Solution: " + oneVObjective + "\n";
        ans += "Cost of 1-Vehicle Solution / num vehicles (Solution Cost Share): " + solutionCostShare + "\n";
        ans += "Max Route Length: " + maxLength + "\n";
        ans += "Max Route Length / Total Cost Share: " + (double) maxLength / totalCostShare + "\n";
        ans += "Max Route Length / Solution Cost Share: " + (double) maxLength / solutionCostShare + "\n";
        ans += "Min Route Length: " + minLength + "\n";
        ans += "Average Route Length: " + averageCost + "\n";
        ans += "Average RouteLength (excluding empty): " + averageCostNoEmpty + "\n";
        ans += "% variance: " + 100.0 * percentVariance + "\n";
        ans += "% deviation from average length: " + 100.0 * deviationFromAverage + "\n";
        ans += "% deviation from average length (excluding empty): " + 100.0 * deviationFromAverageNoEmpty + "\n";
        ans += "Added cost: " + addedCost + "\n";
        ans += "\n";
        ans += "\n";
        ans += "=======================================================";

        return ans;
    }
}
