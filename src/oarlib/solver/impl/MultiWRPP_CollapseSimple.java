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

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import oarlib.core.*;
import oarlib.display.GraphDisplay;
import oarlib.graph.factory.impl.WindyGraphFactory;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.impl.EdgeInducedRequirementTransform;
import oarlib.graph.transform.partition.impl.PreciseWindyKWayPartitionTransform;
import oarlib.graph.transform.rebalance.impl.ClosestRequiredEdgeRebalancer;
import oarlib.graph.transform.rebalance.impl.IndividualDistanceToDepotRebalancer;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Utils;
import oarlib.link.impl.Arc;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.auxiliary.PartitioningProblem;
import oarlib.problem.impl.io.PartitionFormat;
import oarlib.problem.impl.io.PartitionReader;
import oarlib.problem.impl.io.ProblemFormat;
import oarlib.problem.impl.io.ProblemWriter;
import oarlib.problem.impl.multivehicle.MinMaxKWRPP;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.route.impl.Tour;
import oarlib.route.util.RouteExpander;
import oarlib.route.util.SolutionImporter;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Created by oliverlum on 3/17/16.
 */
public class MultiWRPP_CollapseSimple extends MultiVehicleSolver<WindyVertex, WindyEdge, WindyGraph> {

    private static final Logger LOGGER = Logger.getLogger(MultiWRPP_CollapseSimple.class);

    private int maxDistance;
    private boolean maxDistSet;
    private double alpha;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public MultiWRPP_CollapseSimple(Problem<WindyVertex, WindyEdge, WindyGraph> instance) throws IllegalArgumentException {
        super(instance);
        maxDistSet = false;
        alpha = .5;
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

        if (!maxDistSet)
            setDefaultMaxDist();

        ArrayList<Route<WindyVertex, WindyEdge>> ans = new ArrayList<Route<WindyVertex, WindyEdge>>();

        //take the graph and restrict it to the nodes that are maxDistance away from the depot
        WindyGraph restrictedGraph = getDistanceLimitedGraph();

        //solve it using the exact solver
        HashSet<HashSet<Integer>> partitions = callCorberan(restrictedGraph);

        //then extend that partition out to the rest of the graph
        HashSet<HashSet<Integer>> finalPartitions = extend(partitions);

        //finally, solve the routing problem on the extended partitions
        for (HashSet<Integer> extendedPartition : finalPartitions) {
            ans.add(route(extendedPartition));
        }

        display(ans);

        mInstance.setSol(ans);
        return ans;
    }

    protected Route<WindyVertex, WindyEdge> route(HashSet<Integer> ids) {

        //check out a clean instance
        WindyGraph mGraph = mInstance.getGraph();

        WindyGraphFactory wgf = new WindyGraphFactory();
        EdgeInducedRequirementTransform<WindyGraph> subgraphTransform = new EdgeInducedRequirementTransform<WindyGraph>(mGraph, wgf, ids);

        //check to make sure we have at least 1 required edge
        TIntObjectHashMap<WindyEdge> mEdges = mGraph.getInternalEdgeMap();
        boolean hasReq = false;
        for (Integer i : ids) {
            if (mEdges.get(i).isRequired())
                hasReq = true;
        }
        if (!hasReq)
            return new Tour();

        WindyGraph subgraph = subgraphTransform.transformGraph();

        //now solve the WPP on it
        WindyRPP subInstance = new WindyRPP(subgraph, mInstance.getName());
        WRPPSolver_Benavent_H1 solver = new WRPPSolver_Benavent_H1(subInstance, false);

        long start, end;
        start = System.currentTimeMillis();
        Route ret = solver.solve().iterator().next();
        end = System.currentTimeMillis();
        LOGGER.debug("It took " + (end - start) + " milliseconds to run the sub-solver.");

        return Utils.reclaimTour(ret, mGraph);
    }

    private Tour<WindyVertex, WindyEdge> route2(HashSet<Integer> edges) {

        WindyGraph mGraph = mInstance.getGraph();
        WindyGraphFactory wgf = new WindyGraphFactory();
        EdgeInducedRequirementTransform<WindyGraph> subgraphTransform = new EdgeInducedRequirementTransform<WindyGraph>(mGraph, wgf, edges);

        //check to make sure we have at least 1 required edge
        TIntObjectHashMap<WindyEdge> mEdges = mGraph.getInternalEdgeMap();
        boolean hasReq = false;
        for (Integer i : edges) {
            if (mEdges.get(i).isRequired())
                hasReq = true;
        }
        if (!hasReq)
            return new Tour();

        WindyGraph subgraph = subgraphTransform.transformGraph();

        //now solve the WPP on it

        //to use Rui's exact solver, we have to write the instance, and then call the python
        try {
            String filename = "/Users/oliverlum/Downloads/20node/WPPTZ20nodes_15_15_15.txt";
            WindyRPP subProblem = new WindyRPP(subgraph);
            ProblemWriter pw = new ProblemWriter(ProblemFormat.Name.Zhang_Matrix_Windy);
            pw.writeInstance(subProblem, filename);

            return runIP(filename);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Tour<WindyVertex, WindyEdge> runIP(String inputFileName) {

        WindyGraph mGraph = mInstance.getGraph();

        TIntArrayList ansRoute = new TIntArrayList();
        ArrayList<Boolean> ansDir = new ArrayList<Boolean>();
        ArrayList<Boolean> ansZig = new ArrayList<Boolean>();
        File outputFile = new File("MTZ_NoTimeWindows__DetailRoute.txt");
        if (outputFile.exists())
            outputFile.delete();

        //run the python script which calls CPLEX
        try {

            ProcessBuilder pb = new ProcessBuilder("/opt/local/bin/python", "/Users/oliverlum/Downloads/WRPP_BnC.py");
            Process run = pb.start();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(run.getInputStream()));
            String line = "";
            System.out.println("Running Python starts: " + line);
            int exitCode = run.waitFor();
            System.out.println("Exit Code : " + exitCode);
            line = bfr.readLine();
            System.out.println("First Line: " + line);
            while ((line = bfr.readLine()) != null) {
                System.out.println("Python Output: " + line);
            }
            System.out.println("Complete");
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        //parse the output so we can get the final solution back
        try {
            String line;
            String type = "";
            String[] temp = new String[1];
            String[] temp2 = new String[1];
            String[] temp3 = new String[1];


            BufferedReader br = new BufferedReader(new FileReader(outputFile));
            WindyEdge tempL;

            while ((line = br.readLine()) != null) {
                if (line.contains("Simplified Route")) {
                    line = br.readLine(); //the meat
                    temp = line.split("'");
                    continue;
                }
                if (line.contains("Action Route")) {
                    line = br.readLine();
                    temp2 = line.split("'");
                    break;
                }
            }

            int v1, v2;
            for (int l = 0; l < temp.length; l++) {
                if (!temp[l].contains("_"))
                    continue;
                temp3 = temp[l].split("_");
                v1 = Integer.parseInt(temp3[0]) + 1;
                v2 = Integer.parseInt(temp3[1]) + 1;

                if (v1 == 1)
                    v1 = mGraph.getDepotId();
                else if (v1 == mGraph.getDepotId())
                    v1 = 1;

                if (v2 == 1)
                    v2 = mGraph.getDepotId();
                else if (v2 == mGraph.getDepotId())
                    v2 = 1;

                List<WindyEdge> candidates = mGraph.findEdges(v1, v2);
                if (candidates.size() > 1)
                    LOGGER.warn("There appear to be multiple edges connecting" +
                            " these two vertices.  Behavior is not guaranteed" +
                            " to be correct.");

                tempL = candidates.get(0);

                //id
                ansRoute.add(tempL.getId());

                //dir
                if (v1 == tempL.getFirstEndpointId()) {
                    ansDir.add(true);
                } else {
                    ansDir.add(false);
                }

                //zig
                if (temp2[l].contains("Z")) {
                    ansZig.add(true);
                } else {
                    ansZig.add(false);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        RouteExpander<WindyGraph> we = new RouteExpander<WindyGraph>(mGraph);
        return we.unflattenRoute(ansRoute, ansDir);
    }

    private void display(Collection<Route<WindyVertex, WindyEdge>> record) {

        try {
            int counter = 1;
            HashMap<Integer, Integer> sol = new HashMap<Integer, Integer>();

            for (Route<WindyVertex, WindyEdge> r : record) {

                List<WindyEdge> tempPath = r.getPath();
                ArrayList<Boolean> service = r.getServicingList();
                for (int i = 0; i < tempPath.size(); i++) {
                    if (service.get(i))
                        sol.put(tempPath.get(i).getId(), counter);
                }
                counter++;
            }

            WindyGraph toDisplay = mInstance.getGraph().getDeepCopy();
            int limi = mInstance.getGraph().getEdges().size();
            for (int i = 1; i <= limi; i++) {
                WindyEdge we = toDisplay.getEdge(i);
                if (!sol.containsKey(we.getId()))
                    toDisplay.removeEdge(we.getId());
            }

            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, toDisplay, mInstance.getName());
            gd.setInstanceName("Collapse_" + mInstance.getName());
            gd.exportWithPartition(GraphDisplay.ExportType.PDF, sol);

            //individual routes
            for (Route<WindyVertex, WindyEdge> r : record) {

                gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, toDisplay, mInstance.getName());
                float[] scaling = getScaling();
                gd.setScaling(scaling[0], scaling[1], scaling[2], scaling[3]);

                gd.setInstanceName("Collapse_" + mInstance.getName() + "_" + r.getCost());
                gd.exportRoute(GraphDisplay.ExportType.PDF, r);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private double getAlpha() {
        double idealLowerBound = 0.5;
        double idealUpperBound = 1.5;

        int k = mInstance.getmNumVehicles();
        int Er = 0;
        for (WindyEdge we : mInstance.getGraph().getEdges())
            if (we.isRequired())
                Er++;

        double lowerBound = idealLowerBound * 2 * k / Er;
        double upperBound = idealUpperBound * 2 * k / Er;

        return (lowerBound + upperBound) / 2;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    protected float[] getScaling() {

        //For the display
        WindyVertex tempV;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MAX_VALUE;
        float maxY = Float.MAX_VALUE;
        int n = mInstance.getGraph().getVertices().size();
        for (int i = 1; i <= n; i++) {
            tempV = mInstance.getGraph().getVertex(i);

            if (tempV.getX() > maxX)
                maxX = (float) tempV.getX();
            if (tempV.getX() < minX)
                minX = (float) tempV.getX();


            if (tempV.getY() > maxY)
                maxY = (float) tempV.getY();
            if (tempV.getY() < minY)
                minY = (float) tempV.getY();
        }

        float xRange = (maxX - minX);
        float yRange = (maxY - minY);
        float xScaleFactor = 100f / xRange * 100;
        float yScaleFactor = 100f / yRange * 100;

        float[] ans = new float[4];
        ans[0] = xScaleFactor;
        ans[1] = yScaleFactor;
        ans[2] = minX;
        ans[3] = minY;

        return ans;

    }

    private HashSet<HashSet<Integer>> extend(HashSet<HashSet<Integer>> partitions) {

        try {

            //edge dual
            ClosestRequiredEdgeRebalancer<WindyGraph> beta = new ClosestRequiredEdgeRebalancer<WindyGraph>(mInstance.getGraph(), new WindyGraphFactory(), 1, new IndividualDistanceToDepotRebalancer(mInstance.getGraph(), getAlpha()));
            beta.setDistMatrix(mInstance.getGraph().getAllPairsDistMatrix());
            PreciseWindyKWayPartitionTransform transformer = new PreciseWindyKWayPartitionTransform(mInstance.getGraph(), true, beta);

            WindyGraph edgeDual = transformer.transformGraph();

            /**
             * The extend procedure takes the set of all edges adjacent to the partitioned edges,
             * and connects them to a 'partition vertex' then runs the usual procedure.  That is,
             * suppose we have K10 as the input graph (depot is vertex 1), and 2 vehicles.
             * Further, suppose the partitions produced are 1 = {(1,2)} and 2 = {(1,3), (3,5)}.
             *
             * Then we ask which edges are incident to (1,2), and mark them as affected, and connect
             * them to a partition 1 vertex (this won't change the structure of the graph).
             *
             * Then, since (1,3) was removed in the first step, we look at (3,5), and again mark
             * adjacent edges as effected, and connect them to a partition 2 vertex.
             *
             * We now remove (1,3), (1,2), and (3,5) from this graph (as they are now 'contained'
             * in the partition vertices).
             *
             * Then we run our old full graph heuristic on this graph.
             */
            //collapse the edges in each partition, ORDER MATTERS
            HashSet<Integer> removed;
            HashSet<Integer> affected;
            HashSet<Integer> clustered = new HashSet<Integer>();
            int tempId = -1;
            HashMap<Integer, HashSet<Integer>> partitionsMap = new HashMap<Integer, HashSet<Integer>>();
            int index = 1;

            //for each of the partitions...
            for (HashSet<Integer> partition : partitions) {

                partitionsMap.put(index, partition);

                removed = new HashSet<Integer>();
                affected = new HashSet<Integer>();

                HashSet<WindyEdge> removeThisTurn = new HashSet<WindyEdge>();

                //for each of the edges in the partition
                for (Integer i : partition) {
                    removed.add(i);

                    //add to 'affected' the edges that are adjacent to it
                    for (WindyEdge we : edgeDual.getVertex(i).getIncidentLinks()) {
                        affected.add(we.getFirstEndpointId());
                        affected.add(we.getSecondEndpointId());

                        removeThisTurn.add(we);
                    }

                    //now remove it from the dual
                    for (WindyEdge we : removeThisTurn)
                        edgeDual.removeEdge(we);

                    removeThisTurn.clear();
                    edgeDual.removeVertex(i);
                }

                affected.removeAll(removed);

                WindyVertex toAdd = new WindyVertex("clustered");
                toAdd.setCost(999999);
                toAdd.setMatchId(index);
                edgeDual.addVertex(toAdd);
                clustered.add(toAdd.getId());
                for (Integer i : affected) {
                    edgeDual.addEdge(toAdd.getId(), i, 1);
                }

                index++;
            }

            //OPTIONAL: alter edge weights to guide?

            //run METIS on the graph
            String filename = "C:\\Users\\Oliver\\Desktop\\RandomGraph.graph";

            //write it to a file
            ProblemWriter gw = new ProblemWriter(ProblemFormat.Name.METIS);
            gw.writeInstance(new PartitioningProblem(edgeDual, null, null), filename);

            //num parts to partition into
            int numParts = mInstance.getmNumVehicles();

            //partition the graph
            runMetis(numParts, filename);

            //now read the partition and reconstruct the induced subrgraphs on which we solve the WPP to get our final solution
            PartitionReader pr = new PartitionReader(PartitionFormat.Name.METIS);

            HashMap<Integer, Integer> edgeDualPart = pr.readPartition(filename + ".part." + numParts);

            //figure out who's who
            HashMap<Integer, Integer> partToPart = new HashMap<Integer, Integer>();
            for (Integer i : clustered) {
                partToPart.put(edgeDualPart.get(i), edgeDual.getVertex(i).getMatchId());
            }

            for (Integer i : edgeDualPart.keySet()) {
                if (!partitionsMap.containsKey(partToPart.get(edgeDualPart.get(i))))
                    partitionsMap.put(partToPart.get(edgeDualPart.get(i)), new HashSet<Integer>());
                partitionsMap.get(partToPart.get(edgeDualPart.get(i))).add(i);
            }


            HashSet<HashSet<Integer>> ans = new HashSet<HashSet<Integer>>();
            ans.addAll(partitionsMap.values());

            return ans;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }

    private int getLowerBound(WindyGraph g) {
        //bad lower bound for now, but it's something fast
        int sum = 0;
        for (WindyEdge we : g.getEdges()) {
            sum += we.getReverseCost() + we.getCost();
        }
        return (int) (sum * .5);
    }

    private HashSet<HashSet<Integer>> callCorberan(WindyGraph g) {

        try {
            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, g, "collapsed");
            gd.export(GraphDisplay.ExportType.PDF);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        HashSet<HashSet<Integer>> ans = new HashSet<HashSet<Integer>>();

        //write the graph to a .txt in Corberan format
        try {
            ProblemWriter pw = new ProblemWriter(ProblemFormat.Name.Corberan);
            MinMaxKWRPP prob = new MinMaxKWRPP(g, "collapsed", mInstance.getmNumVehicles());
            pw.writeInstance(prob, "/Users/oliverlum/Downloads/bnc2/collapsed.txt");
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        //call the Corberan code
        try {

            ProcessBuilder pb = new ProcessBuilder("sudo", "/Users/oliverlum/Downloads/bnc2/a.out", "/Users/oliverlum/Downloads/bnc2/collapsed.txt", Integer.toString(mInstance.getmNumVehicles()), Double.toString(alpha), Integer.toString(getLowerBound(g)), "4");
            Process run = pb.start();

            OutputStream stdin = run.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));

            String input = "7hqbdvdbm\n";
            writer.write(input);
            writer.flush();

            input = "X\n";
            writer.write(input);
            writer.flush();

            System.out.println("Running Corberan Solver");
            BufferedReader bfr = new BufferedReader(new InputStreamReader(run.getInputStream()));

            int exitCode = run.waitFor();
            System.out.println("Exit Code : " + exitCode);

            String line;
            while ((line = bfr.readLine()) != null) {
                System.out.println("Corberan Output: " + line);
            }

            System.out.println("Complete");
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        //parse the output
        Collection<Tour<DirectedVertex, Arc>> collapsedRoutes = SolutionImporter.importRoutes("/Users/oliverlum/Downloads/bnc2/collapsed_" + mInstance.getmNumVehicles() + "_3_" + Integer.toString((int) (alpha * 100)) + "_op.txt", g.getDepotId(), SolutionImporter.RouteFormat.CORBERAN);
        HashSet<Tour> finalCollapsedRoutes = SolutionImporter.mapToGraph(g, collapsedRoutes);

        HashSet<Integer> toAdd = new HashSet<Integer>();
        for (Tour t : finalCollapsedRoutes) {
            for (int i : t.getCompactRepresentation().toNativeArray())
                toAdd.add(i);
            ans.add(toAdd);
        }

        return ans;
    }

    private void setDefaultMaxDist() {
        //set max dist
        int maxDist = 0;
        for (WindyEdge we : mInstance.getGraph().getEdges()) {
            maxDist += we.getReverseCost() + we.getCost();
        }
        maxDist = maxDist / mInstance.getGraph().getEdges().size() * 4;

        setMaxDistance(maxDist);
    }

    private WindyGraph getDistanceLimitedGraph() {

        //init
        WindyGraph mGraph = mInstance.getGraph();
        int n = mGraph.getVertices().size();
        WindyGraph ans = new WindyGraph();
        int[] dist = new int[n + 1];
        int[] path = new int[n + 1];
        int depotId = mGraph.getDepotId();

        CommonAlgorithms.dijkstrasAlgorithm(mGraph, depotId, dist, path);

        //add depot
        /*ans.addVertex();
        ans.getVertex(1).setMatchId(depotId);
        mGraph.getVertex(depotId).setMatchId(1);
        ans.getVertex(1).setCoordinates(mGraph.getVertex(depotId).getX(), mGraph.getVertex(depotId).getY());
        */

        //add the rest
        HashSet<Integer> includedVIds = new HashSet<Integer>();
        includedVIds.add(depotId);
        WindyVertex toAdd;
        for (int i = 1; i <= n; i++) {
            if (dist[i] <= maxDistance) {

                toAdd = new WindyVertex("");
                toAdd.setMatchId(i);
                ans.addVertex(toAdd);
                ans.getVertex(toAdd.getId()).setCoordinates(mGraph.getVertex(i).getX(), mGraph.getVertex(i).getY());

                includedVIds.add(i);

                mGraph.getVertex(i).setMatchId(toAdd.getId());

            }
        }

        //add the edges
        try {
            for (WindyEdge e : mGraph.getEdges()) {
                if (includedVIds.contains(e.getFirstEndpointId()) && includedVIds.contains(e.getSecondEndpointId())) {
                    ans.addEdge(e.getEndpoints().getFirst().getMatchId(), e.getEndpoints().getSecond().getMatchId(), e.getCost(), e.getReverseCost(), e.isRequired());
                }
            }

            if (!CommonAlgorithms.isConnected(ans))
                System.out.println("DEBUG");
            return ans;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Simple Collapse Aesthetic Min-Max K Windy Rural Postman Solver";
    }

    @Override
    public Solver<WindyVertex, WindyEdge, WindyGraph> instantiate(Problem<WindyVertex, WindyEdge, WindyGraph> p) {
        return new MultiWRPP_CollapseSimple(p);
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        HashMap<String, Double> ret = new HashMap<String, Double>();
        return ret;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
        maxDistSet = true;
    }
}
