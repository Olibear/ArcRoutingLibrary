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
import oarlib.core.*;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.graph.util.Utils;
import oarlib.link.impl.ZigZagLink;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.io.ProblemFormat;
import oarlib.problem.impl.io.ProblemWriter;
import oarlib.problem.impl.rpp.WindyRPPZZTW;
import oarlib.route.impl.ZigZagTour;
import oarlib.route.util.RouteExporter;
import oarlib.route.util.ZigZagExpander;
import oarlib.vertex.impl.ZigZagVertex;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Created by oliverlum on 7/5/15.
 */
public class WRPPZZTW_PFIH extends SingleVehicleSolver<ZigZagVertex, ZigZagLink, ZigZagGraph> {

    private static final Logger LOGGER = Logger.getLogger(WRPPZZTW_PFIH.class);

    //4 weights on the cost for tuning later
    private double mAlpha;
    private double mBeta;
    private double mGamma;
    private double mLambda;
    private double latePenalty; //if you are late 1 unit of time, this costs you latePenalty extra cost units
    private Random rng;
    private int bestPartialSize = 0;
    private int bestPartialLength = 0;
    private int bestPartialNumZigzags = 0;
    private int bestSumLengthZigzags = 0;
    private int avgPartialSize = 0;
    private int avgPartialLength = 0;
    private int avgPartialNumZigzags = 0;
    private int avgSumLengthZigzags = 0;
    private int greatestSumLengthZigzags = 0;
    private double bestPercentZZ = 0;
    private double avgPercentZZ = 0;
    private double bestPercentService = 0;
    private double avgPercentService = 0;
    private double bestZZSavings = 0;
    private double avgZZSavings = 0;
    private double bestZZDeadhead;
    private double avgZZDeadhead;
    private double bestServiceLeft = 0;
    private double avgServiceLeft = 0;


    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public WRPPZZTW_PFIH(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> instance) throws IllegalArgumentException {
        this(instance, 1, 1, 1, 1, 1);
    }

    public WRPPZZTW_PFIH(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> instance, double alpha, double beta, double gamma, double lambda, double penalty) throws IllegalArgumentException {
        super(instance);
        setmAlpha(alpha);
        setmBeta(beta);
        setmGamma(gamma);
        setmLambda(lambda);
        setLatePenalty(penalty);
        rng = new Random(1000);
    }

    public static double runIPNoRoute(ZigZagGraph g, int i, int j, int k, int iter, double latePenalty) {
        //run the python script which calls CPLEX
        String objValue = "";
        try {

            ProcessBuilder pb = new ProcessBuilder("/opt/local/bin/python", "/Users/oliverlum/Downloads/ZigzagCPP_PartialOrderNEW.py", Integer.toString(i), Integer.toString(j), Integer.toString(k), Integer.toString(iter));
            //ProcessBuilder pb = new ProcessBuilder("/opt/local/bin/python", "/Users/oliverlum/Downloads/ZigzagCPPT_BnC_PrefixZigzagOrder.py", Integer.toString(i), Integer.toString(j), Integer.toString(k),Integer.toString(iter));
            Process run = pb.start();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(run.getInputStream()));
            String line = "";
            System.out.println("Running Python starts: " + line);
            int exitCode = run.waitFor();
            System.out.println("Exit Code : " + exitCode);
            line = bfr.readLine();
            System.out.println("First Line : " + line);
            while ((line = bfr.readLine()) != null) {
                objValue = line;
                System.out.println("Python Output: " + line);
            }
            double realObjValue = Double.parseDouble(objValue);
            System.out.println("Complete");
            return realObjValue;
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public static ZigZagTour runIP(ZigZagGraph g, int i, int j, int k, int iter, double latePenalty) {

        TIntArrayList ansRoute = new TIntArrayList();
        ArrayList<Boolean> ansDir = new ArrayList<Boolean>();
        ArrayList<Boolean> ansZig = new ArrayList<Boolean>();

        //run the python script which calls CPLEX
        try {

            ProcessBuilder pb = new ProcessBuilder("/opt/local/bin/python", "/Users/oliverlum/Downloads/ZigzagCPP_PartialOrder.py", Integer.toString(i), Integer.toString(j), Integer.toString(k), Integer.toString(iter));
            //ProcessBuilder pb = new ProcessBuilder("/opt/local/bin/python", "/Users/oliverlum/Downloads/ZigzagCPPT_BnC_PrefixZigzagOrder.py", Integer.toString(i), Integer.toString(j), Integer.toString(k),Integer.toString(iter));
            Process run = pb.start();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(run.getInputStream()));
            String line = "";
            System.out.println("Running Python starts: " + line);
            int exitCode = run.waitFor();
            System.out.println("Exit Code : " + exitCode);
            line = bfr.readLine();
            System.out.println("First Line : " + line);
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

            File graphFile = new File("BnC_PartialOrder_DetailRoute.txt");
            BufferedReader br = new BufferedReader(new FileReader(graphFile));
            ZigZagLink tempL;

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

                List<ZigZagLink> candidates = g.findEdges(v1, v2);
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

        ZigZagExpander zze = new ZigZagExpander(g, latePenalty);
        return zze.unflattenRoute(ansRoute, ansDir, ansZig);
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            UndirectedGraph connectedCheck = new UndirectedGraph(mInstance.getGraph().getVertices().size());
            try {
                for (ZigZagLink l : mInstance.getGraph().getEdges())
                    connectedCheck.addEdge(l.getFirstEndpointId(), l.getSecondEndpointId(), 1);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            if (!CommonAlgorithms.isConnected(connectedCheck))
                return false;
        }
        return true;
    }

    @Override
    protected Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> getInstance() {
        return mInstance;
    }

    public double getLowerBound(ZigZagGraph g){

        //setup the lb graph
        ZigZagGraph lb = new ZigZagGraph(g.getVertices().size());

        try {
            for (ZigZagLink zzl : g.getEdges()) {
                ZigZagLink toAdd = lb.constructEdge(zzl.getFirstEndpointId(), zzl.getSecondEndpointId(), "", zzl.getCost(), zzl.getReverseCost(), zzl.getZigzagCost(), zzl.getServiceCost(), zzl.getReverseServiceCost(), zzl.getStatus());
                toAdd.setRequired(zzl.isRequired());
                toAdd.setReverseRequired(zzl.isReverseRequired());
                lb.addEdge(toAdd);
            }

            WindyRPPZZTW lbProb = new WindyRPPZZTW(lb, "12_" + 12 + "_" + 12);
            ProblemWriter probw = new ProblemWriter(ProblemFormat.Name.Zhang_Matrix_Zigzag);
            probw.writeInstance(lbProb, "/Users/oliverlum/Downloads/20node/WPPTZ20nodes_12_12_12.txt");
            String fileName = "/Users/oliverlum/Downloads/Sols/" + lbProb.getName() + "_ans_101.txt";
            ZigZagTour partRoute = new ZigZagTour(lb, getLatePenalty());
            RouteExporter.exportRoute(partRoute, RouteExporter.RouteFormat.ZHANG, fileName);
            double ans = runIPNoRoute(lb,12,12,12,101,1e6);
            return ans;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected Collection<? extends Route> solve() {

        long start = System.currentTimeMillis();
        int numSeeds = 0;
        long tempStart, tempEnd; //for timing just the solves
        double avgIPSolve = 0;
        //init
        double bestCost;
        ZigZagGraph g = mInstance.getGraph();
        int n = g.getVertices().size();
        double alpha = .7; //1 - alpha is left over for insertion of other required edges
        ZigZagTour ans;
        int timeWindow = Integer.MAX_VALUE; //will hold the most restrictive time window
        ZigZagExpander zze = new ZigZagExpander(g, latePenalty);
        int depotId = g.getDepotId();
        String fileName = "/Users/oliverlum/Downloads/Sols/" + mInstance.getName() + "_ans_101.txt";

        //new stuff
        HashSet<Integer> solStore = new HashSet<Integer>();
        HashMap<Double, ZigZagTour> partRoutes = new HashMap<Double, ZigZagTour>(); //key = zigzags / deadhead
        //ArrayList<ZigZagTour> partRoutes = new ArrayList<ZigZagTour>();
        double maxZZDeadhead = Double.MAX_VALUE;
        int keepTop = 5;

        ZigZagGraph gWithServce = g.getDeepCopy();
        for (ZigZagLink zzl : gWithServce.getEdges()) {
            zzl.setCost(zzl.getCost() + zzl.getServiceCost());
            zzl.setmReverseCost(zzl.getReverseCost() + zzl.getReverseServiceCost());
        }

        //shortest paths

        int[][] dist = new int[n+1][n+1];
        int[][] path = new int[n+1][n+1];
        CommonAlgorithms.fwLeastCostPaths(gWithServce, dist, path);

        //zero out dist
        for(int i = 1; i <= n; i++)
            dist[i][i] = 0;

        //try the empty one as well
        int i;
        int j;
        int k;

        if (mInstance.getName().substring(2, 3).equals("_")) {
            i = Integer.parseInt(mInstance.getName().substring(0, 2));
            j = Integer.parseInt(mInstance.getName().substring(3, 4));
            k = Integer.parseInt(mInstance.getName().substring(5, 6));
        } else {
            i = Integer.parseInt(mInstance.getName().substring(0, 1));
            j = Integer.parseInt(mInstance.getName().substring(2, 3));
            k = Integer.parseInt(mInstance.getName().substring(4, 5));
        }

        ZigZagTour partialRoute;
        //RouteExporter.exportRoute(partialRoute, RouteExporter.RouteFormat.ZHANG, fileName);
        //ans = runIP(g, i, j, k, 101, latePenalty);
        //bestCost = ans.getCost();
        bestCost = Integer.MAX_VALUE; //runIPNoRoute(g, i, j, k, 101, latePenalty);

        //order the zz optional edges by distance to depot
        HashSet<Integer> optionalEdges = new HashSet<Integer>();
        for(ZigZagLink zzl : g.getEdges()) {
            if(zzl.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL && zzl.getTimeWindow().getSecond() < RouteExporter.ZZ_TIME_WINDOW_THRESHOLD) {
                optionalEdges.add(zzl.getId());
                if(timeWindow > zzl.getTimeWindow().getSecond())
                    timeWindow = zzl.getTimeWindow().getSecond();
            }
        }

        PriorityQueue<Pair<Integer>> optionalEdgeQueue = new PriorityQueue<Pair<Integer>>(optionalEdges.size(), new Utils.DijkstrasComparator());
        for (Integer ii : optionalEdges) {
            optionalEdgeQueue.add(new Pair<Integer>(ii, Utils.distanceToEdge(dist[depotId], g.getEdge(ii))));
        }

        //insert them until they reach timeWindow * alpha
        ZigZagLink toInsert;
        int toInsertId;

        TIntArrayList compactAns;
        ArrayList<Boolean> compactDir;
        ArrayList<Boolean> compactZZ;
        int numPartialRoutes = 0;

        while(!optionalEdgeQueue.isEmpty()) {

            //PHASE I: Insert TW edges
            toInsertId = optionalEdgeQueue.poll().getFirst();
            toInsert = g.getEdge(toInsertId);

            if (Utils.distanceToEdge(dist[depotId], toInsert) + toInsert.getCost() + toInsert.getZigzagCost() > alpha * timeWindow)
                continue;

            compactAns = new TIntArrayList();
            compactDir = new ArrayList<Boolean>();
            compactZZ = new ArrayList<Boolean>();

            compactAns.add(toInsertId);
            if (Utils.distanceToEdge(dist[depotId], toInsert) == dist[depotId][toInsert.getFirstEndpointId()])
                compactDir.add(true);
            else
                compactDir.add(false);
            compactZZ.add(true);

            //insert it, and check the cost
            double threshold = alpha * timeWindow;

            //gonna be slow, but prototype
            int maxIndex = 0;
            for (Integer ii : optionalEdges) {
                //don't double insert
                if (compactAns.contains(ii))
                    continue;

                PriorityQueue<Pair<Integer>> nextMoves = cheapestInsertion2(zze.unflattenRoute(compactAns, compactDir, compactZZ), g.getEdge(ii), true, dist);
                if (nextMoves.isEmpty()) {
                    LOGGER.warn("No feasible moves exist.");
                    break;
                }
                //change this check to be cost of partial route, not unflattened route
                else if (nextMoves.peek().getFirst() <= maxIndex && nextMoves.peek().getSecond() + zze.unflattenRoute(compactAns, compactDir, compactZZ).getCost() < threshold) {
                    makeMove(nextMoves, compactAns, compactDir, g.getEdge(ii), dist);
                    compactZZ.add(true);
                }

            }

            partialRoute = zze.unflattenRoute(compactAns, compactDir, compactZZ);

            //go through and mark guys that can be serviced along the way
            ArrayList<Boolean> dir = partialRoute.getTraversalDirection();
            ZigZagLink zzl;
            int compactIndex = 0;
            for (int l = 0; l < partialRoute.getPath().size(); l++) {
                zzl = partialRoute.getPath().get(l);
                if (zzl.getId() == compactAns.get(compactIndex)) {
                    compactIndex++;
                    if (compactIndex == compactAns.size())
                        break;
                    continue;
                }
                if (zzl.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL || zzl.getStatus() == ZigZagLink.ZigZagStatus.MANDATORY)
                    continue;
                if ((zzl.isRequired() && dir.get(l)) || (zzl.isReverseRequired() && !dir.get(l))) {
                    partialRoute.changeService(l);
                }
            }

            partialRoute = zze.unflattenRoute(partialRoute.getCompactRepresentation(), partialRoute.getCompactTraversalDirection(), partialRoute.getCompactZZList());

            if (solStore.contains(partialRoute.toString().hashCode()))
                continue;

            //RouteExporter.exportRoute(partialRoute, RouteExporter.RouteFormat.ZHANG, fileName);

            numPartialRoutes++;
            avgPartialSize += partialRoute.getPath().size();
            avgPartialLength += partialRoute.getCost();
            int numZigs = 0;
            double sumZigs = 0;
            double zzSavings = 0;
            ZigZagLink temp;
            double serviceLeft = 0;
            int lastServiceIndex = 0;
            for (int l = 0; l < partialRoute.getCompactRepresentation().size(); l++) {
                if (partialRoute.getCompactZZList().get(l)) {
                    temp = g.getEdge(partialRoute.getCompactRepresentation().get(l));
                    numZigs++;
                    sumZigs += temp.getZigzagCost();
                    zzSavings += temp.getZigzagCost() - temp.getServiceCost() - temp.getReverseServiceCost();
                }
            }
            for (int l = 0; l < partialRoute.getServicingList().size(); l++) {
                if (partialRoute.getServicingList().get(l))
                    lastServiceIndex = l;
            }
            for (int l = 0; l <= lastServiceIndex; l++) {
                if (!partialRoute.getServicingList().get(l)) {
                    temp = partialRoute.getPath().get(l);
                    if (temp.isRequired() || temp.isReverseRequired())
                        serviceLeft += temp.getServiceCost() + temp.getReverseServiceCost();
                }
            }
            avgServiceLeft += serviceLeft;
            avgPartialNumZigzags += numZigs;
            avgSumLengthZigzags += (int) sumZigs;
            avgPercentService += partialRoute.getServiceComponent() / partialRoute.getCost();
            avgZZSavings += zzSavings;
            if (sumZigs > greatestSumLengthZigzags) {
                greatestSumLengthZigzags = (int) sumZigs;
            }


            //only add it if it's in the highest topNum in terms of zz/deadhead
            double zzDeadhead = (double) sumZigs / (partialRoute.getCost() - sumZigs);
            partRoutes.put(zzDeadhead, partialRoute);
            /*
            if(partRoutes.size() < keepTop) {
                if(zzDeadhead < maxZZDeadhead)
                    maxZZDeadhead = zzDeadhead;
                solStore.add(partialRoute.toString().hashCode());
                partRoutes.put(zzDeadhead, partialRoute);
            } else if(zzDeadhead < maxZZDeadhead) {
                partRoutes.remove(maxZZDeadhead);
                maxZZDeadhead = Collections.max(partRoutes.keySet());
                partRoutes.put(zzDeadhead,partialRoute);
            }*/

        }

        int numRuns = partRoutes.size();
        if (numRuns == 0) {
            avgPartialLength = 0;
            avgPartialNumZigzags = 0;
            avgPartialSize = 0;
            avgSumLengthZigzags = 0;
            avgPercentZZ = 0;
            avgZZSavings = 0;
            avgServiceLeft = 0;
            avgZZDeadhead = 0;
        } else {
            avgPartialLength = avgPartialLength / numPartialRoutes;
            avgPartialNumZigzags = avgPartialNumZigzags / numPartialRoutes;
            avgPartialSize = avgPartialSize / numPartialRoutes;
            avgSumLengthZigzags = avgSumLengthZigzags / numPartialRoutes;
            avgPercentZZ = (double) avgSumLengthZigzags / (double) avgPartialLength / numPartialRoutes;
            avgPercentService = avgPercentService / numPartialRoutes;
            avgZZSavings = avgZZSavings / numPartialRoutes;
            avgServiceLeft = avgServiceLeft / numPartialRoutes;
            avgZZDeadhead = (double) avgSumLengthZigzags / (avgPartialLength - avgSumLengthZigzags) / numPartialRoutes;
        }

        for (ZigZagTour partRoute : partRoutes.values()) {

            //filter on length
            //if(partRoute.getCost() > 1.05 * avgPartialLength)
            //continue;

            RouteExporter.exportRoute(partRoute, RouteExporter.RouteFormat.ZHANG, fileName);

            //complete the route and compare
            //ZigZagTour candidate = runIP(g, i, j, k, 101, latePenalty);
            tempStart = System.currentTimeMillis();
            double candidateCost = runIPNoRoute(g, i, j, k, 101, latePenalty);
            tempEnd = System.currentTimeMillis();
            avgIPSolve += (tempEnd - tempStart) / 1000.0;
            numSeeds++;
            if (candidateCost < bestCost) {
                bestCost = candidateCost;
                //ans = candidate;

                //some record keeping to try and figure out a strategy to keep them
                bestPartialSize = partRoute.getPath().size();
                bestPartialLength = partRoute.getCost();
                int numZigs = 0;
                double sumZigs = 0;
                double zzSavings = 0;
                int lastServiceIndex = 0;
                double serviceLeft = 0;
                ZigZagLink temp;
                for (int l = 0; l < partRoute.getCompactRepresentation().size(); l++) {
                    if (partRoute.getCompactZZList().get(l)) {
                        temp = g.getEdge(partRoute.getCompactRepresentation().get(l));
                        numZigs++;
                        sumZigs += temp.getZigzagCost();
                        zzSavings += temp.getZigzagCost() - temp.getServiceCost() - temp.getReverseServiceCost();
                    }
                }
                for (int l = 0; l < partRoute.getServicingList().size(); l++) {
                    if (partRoute.getServicingList().get(l))
                        lastServiceIndex = l;
                }
                for (int l = 0; l <= lastServiceIndex; l++) {
                    if (!partRoute.getServicingList().get(l)) {
                        temp = partRoute.getPath().get(l);
                        if (temp.isRequired() || temp.isReverseRequired())
                            serviceLeft += temp.getServiceCost() + temp.getReverseServiceCost();
                    }
                }
                bestZZSavings = zzSavings;
                bestPartialNumZigzags = numZigs;
                bestSumLengthZigzags = (int) sumZigs;
                bestPercentZZ = (double) bestSumLengthZigzags / (double) bestPartialLength;
                bestPercentService = partRoute.getServiceComponent() / partRoute.getCost();
                bestServiceLeft = serviceLeft;
                bestZZDeadhead = (double) bestSumLengthZigzags / (bestPartialLength - bestSumLengthZigzags);
            }
        }

        //RouteExporter.exportRoute(ans, RouteExporter.RouteFormat.ZHANG, fileName);
        long end = System.currentTimeMillis();
        try {
            FileWriter fw = new FileWriter("RevisedZZHeuristicScalingResults_SquareNoRestr.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            double avgIPSolveTime = (avgIPSolve / (double) numSeeds);
            out.println(bestCost + "," + (end - start) + "," + numSeeds + "," + avgIPSolveTime + "," +
                    avgPartialSize + "," + bestPartialSize + "," + avgPartialLength + "," + bestPartialLength + ","
                    + avgZZSavings + "," + bestZZSavings + "," + avgPartialNumZigzags + "," + bestPartialNumZigzags +
                    "," + avgSumLengthZigzags + "," + bestSumLengthZigzags + "," + avgPercentZZ + "," + bestPercentZZ +
                    "," + avgPercentService + "," + bestPercentService + "," + avgServiceLeft + "," + bestServiceLeft +
                    "," + avgZZDeadhead + "," + bestZZDeadhead + "," + getLowerBound(g) + ";");
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        ArrayList<ZigZagTour> ret = new ArrayList<ZigZagTour>();
        //ret.add(ans);

        return ret;
    }

    public ZigZagTour determineZZ(TIntArrayList compactRoute, ArrayList<Boolean> compactDir, int[][] dist) {
        /*
         * init
         */
        ArrayList<Boolean> zigzag = new ArrayList<Boolean>();
        ZigZagGraph g = mInstance.getGraph();
        ZigZagLink temp;
        int cost = 0; //running tally
        int index = 1; //keep track of where we may need to make mods to compactDir

        int depotId = g.getDepotId();
        int prevEnd = -1;
        double penalty = 0; //running tally of lateness penalty (gets added to raw cost at the end)
        double candidatePenalty = 0; //when running through the edges, this is the penalty associated with meandering this
        double candidateBackAndForth = 0; //when running through the edges, this is the time to traverse

        /*
         * copy the route in case we need to throw new guys in it
         */
        ArrayList<Boolean> dirCopy = new ArrayList<Boolean>();
        TIntArrayList compactCopy = new TIntArrayList();
        for (int i = 0; i < compactRoute.size(); i++) {
            compactCopy.add(compactRoute.get(i));
            dirCopy.add(compactDir.get(i));
        }

        /*
         * the first edge
         */

        //get it
        temp = g.getEdge(compactRoute.get(0));

        //if it's traversed forward, then...
        if (compactDir.get(0)) {

            //add the distance to get there
            cost += dist[depotId][temp.getFirstEndpointId()];

            //if we're traversing in the opposite direction as required, then zig zag
            if (temp.isReverseRequired() && (temp.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL || temp.getStatus() == ZigZagLink.ZigZagStatus.MANDATORY)) {
                cost += temp.getCost() + temp.getZigzagCost();

                //when considering the non-zig-zag option, take into account penalties from zig-zagging past the window
                if (temp.hasTimeWindow()) {
                    candidatePenalty = Math.max(0, (cost - temp.getTimeWindow().getSecond()) * latePenalty);
                    candidateBackAndForth = temp.getCost() + temp.getReverseCost() + temp.getReverseServiceCost() - temp.getZigzagCost();
                    if (temp.isRequired())
                        candidateBackAndForth += temp.getServiceCost();
                    if (candidatePenalty < candidateBackAndForth) {
                        penalty += Math.max(0, candidatePenalty);
                        zigzag.add(true);
                    } else {
                        cost += candidateBackAndForth;
                        zigzag.add(false);

                        //add the appropriate guys
                        compactCopy.insert(1, temp.getId());
                        dirCopy.add(1, !compactDir.get(0));
                        zigzag.add(false);
                        index++;
                    }
                }
            } else if (temp.isReverseRequired() && temp.getStatus() == ZigZagLink.ZigZagStatus.NOT_AVAILABLE) {
                LOGGER.warn("You're trying to traverse a link in the wrong direction as required, with no possibility of zig-zag.");
            } else {
                //we're just traversing like normal
                cost += temp.getCost() + temp.getServiceCost();
                zigzag.add(false);
            }

            //bookkeeping for the next edge
            prevEnd = temp.getSecondEndpointId();
        }

        //same, but for the case where we're traversing 2nd endpoint - 1st
        else {
            cost += dist[depotId][temp.getSecondEndpointId()];
            if (temp.isRequired() && (temp.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL || temp.getStatus() == ZigZagLink.ZigZagStatus.MANDATORY)) {
                cost += temp.getReverseCost() + temp.getZigzagCost();
                if (temp.hasTimeWindow()) {
                    candidatePenalty = Math.max(0, (cost - temp.getTimeWindow().getSecond()) * latePenalty);
                    candidateBackAndForth = temp.getCost() + temp.getReverseCost() + temp.getServiceCost() - temp.getZigzagCost();
                    if (temp.isReverseRequired())
                        candidateBackAndForth += temp.getReverseServiceCost();
                    if (candidatePenalty < candidateBackAndForth) {
                        penalty += Math.max(0, candidatePenalty);
                        zigzag.add(true);
                    } else {
                        cost += candidateBackAndForth;
                        zigzag.add(false);

                        //add the appropriate guys
                        compactCopy.insert(1, temp.getId());
                        dirCopy.add(1, !compactDir.get(0));
                        zigzag.add(false);
                        index++;
                    }
                }
            } else if (temp.isRequired() && temp.getStatus() == ZigZagLink.ZigZagStatus.NOT_AVAILABLE) {
                LOGGER.warn("You're trying to traverse a link in the wrong direction as required, with no possibility of zig-zag.");
            } else {
                cost += temp.getReverseCost() + temp.getReverseServiceCost();
                zigzag.add(false);
            }
            prevEnd = temp.getFirstEndpointId();
        }

        //do the same for the rest of the edges
        for (int i = 1; i < compactRoute.size(); i++) {
            index++;
            temp = g.getEdge(compactRoute.get(i));

            if (compactDir.get(i)) {
                cost += dist[prevEnd][temp.getFirstEndpointId()];
                if (temp.isReverseRequired() && (temp.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL || temp.getStatus() == ZigZagLink.ZigZagStatus.MANDATORY)) {
                    cost += temp.getCost() + temp.getZigzagCost();
                    if (temp.hasTimeWindow()) {
                        candidatePenalty = Math.max(0, (cost - temp.getTimeWindow().getSecond()) * latePenalty);
                        candidateBackAndForth = temp.getCost() + temp.getReverseCost() + temp.getReverseServiceCost() - temp.getZigzagCost();
                        if (temp.isRequired())
                            candidateBackAndForth += temp.getServiceCost();
                        if (candidatePenalty < candidateBackAndForth) {
                            penalty += Math.max(0, candidatePenalty);
                            zigzag.add(true);
                        } else {
                            cost += candidateBackAndForth;
                            zigzag.add(false);

                            //add the appropriate guys
                            compactCopy.insert(index, temp.getId());
                            dirCopy.add(index, !compactDir.get(i));
                            zigzag.add(false);
                            index++;
                        }
                    }
                } else if (temp.isReverseRequired() && temp.getStatus() == ZigZagLink.ZigZagStatus.NOT_AVAILABLE) {
                    LOGGER.warn("You're trying to traverse a link in the wrong direction as required, with no possibility of zig-zag.");
                } else {
                    cost += temp.getCost() + temp.getServiceCost();
                    zigzag.add(false);
                }
                prevEnd = temp.getSecondEndpointId();
            } else {
                cost += dist[prevEnd][temp.getSecondEndpointId()];
                if (temp.isRequired() && (temp.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL || temp.getStatus() == ZigZagLink.ZigZagStatus.MANDATORY)) {
                    cost += temp.getReverseCost() + temp.getZigzagCost();
                    if (temp.hasTimeWindow()) {
                        candidatePenalty = Math.max(0, (cost - temp.getTimeWindow().getSecond()) * latePenalty);
                        candidateBackAndForth = temp.getCost() + temp.getReverseCost() + temp.getServiceCost() - temp.getZigzagCost();
                        if (temp.isReverseRequired())
                            candidateBackAndForth += temp.getReverseServiceCost();
                        if (candidatePenalty < candidateBackAndForth) {
                            penalty += Math.max(0, candidatePenalty);
                            zigzag.add(true);
                        } else {
                            cost += candidateBackAndForth;
                            zigzag.add(false);

                            //add the appropriate guys
                            compactCopy.insert(index, temp.getId());
                            dirCopy.add(index, !compactDir.get(i));
                            zigzag.add(false);
                            index++;
                        }
                    }
                } else if (temp.isRequired() && temp.getStatus() == ZigZagLink.ZigZagStatus.NOT_AVAILABLE) {
                    LOGGER.warn("You're trying to traverse a link in the wrong direction as required, with no possibility of zig-zag.");
                } else {
                    cost += temp.getReverseCost() + temp.getReverseServiceCost();
                    zigzag.add(false);
                }
                prevEnd = temp.getFirstEndpointId();
            }
        }
        cost += dist[prevEnd][depotId];

        cost += penalty;
        System.out.println("PENALTY: " + penalty);

        //put it all together
        ZigZagExpander ex = new ZigZagExpander(g, latePenalty);
        ZigZagTour ans = ex.unflattenRoute(compactCopy, dirCopy, zigzag);

        return ans;
    }

    /**
     * Perform the insertion specified.
     *
     * @param moveList     - a list of candidate moves, from which the cheapest feasible one (or
     *                     if none exist, then the least violating infeasible one) will be chosen.
     * @param compactRoute - the compact route representation of the route to be modified (that is,
     *                     the ordered list of required ids which it services, with shortest paths
     *                     between them assumed).
     * @param compactDir   - the companion to the compact route representation which specifies whether
     *                     or not the required edge in the same position of compactRoute is traversed
     *                     forward (true) or backwards (false)
     * @param toRoute      - the link to be added
     * @param dist         - the distance matrix for the graph (all pairs; memory saver)
     */
    private void makeMove(PriorityQueue<Pair<Integer>> moveList, TIntArrayList compactRoute, ArrayList<Boolean> compactDir, ZigZagLink toRoute, int[][] dist) {

        ZigZagGraph g = mInstance.getGraph();
        Pair<Integer> move = moveList.poll();
        int index = move.getFirst().intValue();

        compactRoute.insert(index, toRoute.getId());

        //if we're constrained (meaning must service in one direction with no zig-zag available), then add in the appropriate direction
        if (toRoute.getStatus() == ZigZagLink.ZigZagStatus.NOT_AVAILABLE) {
        if (!toRoute.isRequired()) {
            compactDir.add(move.getFirst().intValue(), false);
            System.out.println("Adding reverse: " + toRoute.getId());
            return;
        } else if (!toRoute.isReverseRequired()) {
            compactDir.add(move.getFirst().intValue(), true);
            System.out.println("Adding forward: " + toRoute.getId());
            return;
        }
        }


        //figure out which direction is cheaper
        int prevIndex = index - 1;
        int nextIndex = index + 1;

        int index1 = -1;
        int index2 = -1;

        //figure out the first index
        if (prevIndex == -1)
            index1 = mInstance.getGraph().getDepotId();
        else if (compactDir.get(prevIndex))
            index1 = g.getEdge(compactRoute.get(prevIndex)).getSecondEndpointId();
        else
            index1 = g.getEdge(compactRoute.get(prevIndex)).getFirstEndpointId();

        //figure out the second index
        if (index >= compactDir.size())
            index2 = mInstance.getGraph().getDepotId();
        else if (compactDir.get(index))
            index2 = g.getEdge(compactRoute.get(nextIndex)).getFirstEndpointId();
        else
            index2 = g.getEdge(compactRoute.get(nextIndex)).getSecondEndpointId();

        //figure out the cheaper dir
        int dist1 = dist[index1][toRoute.getFirstEndpointId()] + dist[toRoute.getSecondEndpointId()][index2] + toRoute.getCost();
        int dist2 = dist[index1][toRoute.getSecondEndpointId()] + dist[toRoute.getFirstEndpointId()][index2] + toRoute.getReverseCost();
        if (dist1 < dist2)
            compactDir.add(move.getFirst().intValue(), true);
        else
            compactDir.add(move.getFirst().intValue(), false);

    }

    /**
     * Computes the insertion cost for the operation involving 3 elements:
     * These can be any combination of edges and vertices, and this returns the
     * cheapest possible insertion move subject to the stipulated constraints
     *
     * @param vertexOrLink - array of size 3 specifying whether the id numbers passed in
     *                     in ids are for vertices or links in the graph. A value of 0
     *                     means the id in that position of ids is for a vertex.  A value
     *                     of 1 means the id in that position of ids is for a link.  For
     *                     example, if vertexOrLink = [0,1,0] and ids = [1,2,3], then that
     *                     would mean that we consider the insertion of edge 2 in between
     *                     vertices 1 and 3.
     * @param ids          - the ids of the elements to be considered in the insertion.  For example,
     *                     if ids = [1,2,3], then this function considers the insertion of element 2
     *                     in between elements 1 and 3.
     * @param dist         - the all pairs shortest path matrix for the graph (memory saver)
     * @param zigzag       - true if you are inserting an edge, and you are attempting to zigzag it.
     *                     false if you don't know, or are not attempting to zigzag it.
     *                     This is ignored if you are inserting a vertex.
     * @return - A pair of integers: the first is the cost of insertion;
     * the second is either
     * <p/>
     * -1 if the element to be inserted (the middle one) if a vertex,
     * 0  if the element to be inserted is a link, and should be traversed forward to get this cost
     * 1  if the element to be inserted is a link, and should be traversed backward to get this cost
     * @throws IllegalArgumentException - If ids.length or vertexOrLink.length != 3, or if vertexOrLink contains
     *                                  entries that are not 0 or 1.
     */
    private Pair<Integer> insertCost(int[] vertexOrLink, int[] ids, int[][] dist, boolean zigzag) throws IllegalArgumentException {

        //arg check
        if (vertexOrLink.length != 3 || ids.length != 3) {
            LOGGER.error("You are attempting to fetch an insertion cost for an operation with more than 3 elements.");
            throw new IllegalArgumentException("Please use arrays of size 3 for this operation.");
        }

        Pair<Integer> ans = new Pair<Integer>(-1, -1);

        ZigZagGraph g = mInstance.getGraph();
        ZigZagLink temp;
        int e11, e12, e21, e22, e31, e32;
        if (vertexOrLink[0] == 0) {
            e11 = ids[0];
            e12 = -1;
        } else if (vertexOrLink[0] == 1) {
            temp = g.getEdge(ids[0]);
            e11 = temp.getFirstEndpointId();
            e12 = temp.getSecondEndpointId();
        } else {
            throw new IllegalArgumentException("VertexOrLink array may only contain 0 or 1.");
        }

        if (vertexOrLink[1] == 0) {
            e21 = ids[1];
            e22 = -1;
        } else if (vertexOrLink[1] == 1) {
            temp = g.getEdge(ids[1]);
            e21 = temp.getFirstEndpointId();
            e22 = temp.getSecondEndpointId();
        } else {
            throw new IllegalArgumentException("VertexOrLink array may only contain 0 or 1.");
        }

        if (vertexOrLink[2] == 0) {
            e31 = ids[2];
            e32 = -1;
        } else if (vertexOrLink[2] == 1) {
            temp = g.getEdge(ids[2]);
            e31 = temp.getFirstEndpointId();
            e32 = temp.getSecondEndpointId();
        } else {
            throw new IllegalArgumentException("VertexOrLink array may only contain 0 or 1.");
        }

        int min = Integer.MAX_VALUE;
        int minDir = -1;
        int orientation = -1;
        //try out the various possibilities

        int candidate;
        double traversalCost;
        //if we're inserting a vertex
        if (vertexOrLink[1] == 0) {

            //case 1
            candidate = dist[e11][e21] + dist[e21][e31] - dist[e11][e31];
            if (candidate < min) {
                min = candidate;
            }
            //case 2
            if (e12 > 0) {
                candidate = dist[e12][e21] + dist[e21][e31] - dist[e21][e31];
                if (candidate < min) {
                    min = candidate;
                }
            }
            //case 3
            if (e32 > 0) {
                candidate = dist[e11][e21] + dist[e21][e32] - dist[e11][e32];
                if (candidate < min) {
                    min = candidate;
                }
            }
            //case 4
            if (e32 > 0 && e12 > 0) {
                candidate = dist[e12][e21] + dist[e21][e32] - dist[e12][e32];
                if (candidate < min) {
                    min = candidate;
                }
            }
        } else {

            temp = g.getEdge(ids[1]);

            int debug;
            //case 1
            traversalCost = temp.getCost() + temp.getServiceCost();
            if(zigzag)
                traversalCost = temp.getCost() + temp.getZigzagCost();
            candidate = dist[e11][e21] + dist[e22][e31] - dist[e11][e31] + (int)traversalCost;
            if (candidate < min) {
                min = candidate;
                minDir = 0;
            }

            traversalCost = temp.getReverseCost() + temp.getReverseServiceCost();
            if(zigzag)
                traversalCost = temp.getReverseCost() + temp.getZigzagCost();
            candidate = dist[e11][e22] + dist[e21][e31] - dist[e11][e31] + (int)traversalCost;
            if (candidate < min) {
                min = candidate;
                minDir = 1;
            }

            //case 2
            if (e12 > 0) {
                traversalCost = temp.getCost() + temp.getServiceCost();
                if(zigzag)
                    traversalCost = temp.getCost() + temp.getZigzagCost();
                candidate = dist[e12][e21] + dist[e22][e31] - dist[e12][e31] + (int)traversalCost;
                if (candidate < min) {
                    min = candidate;
                    minDir = 0;
                }
                traversalCost = temp.getReverseCost() + temp.getReverseServiceCost();
                if(zigzag)
                    traversalCost = temp.getReverseCost() + temp.getZigzagCost();
                candidate = dist[e12][e22] + dist[e21][e31] - dist[e12][e31] + (int)traversalCost;
                if (candidate < min) {
                    min = candidate;
                    minDir = 1;
                }
            }
            //case 3
            if (e32 > 0) {
                traversalCost = temp.getCost() + temp.getServiceCost();
                if(zigzag)
                    traversalCost = temp.getCost() + temp.getZigzagCost();
                candidate = dist[e11][e21] + dist[e22][e32] - dist[e11][e32] + (int)traversalCost;
                if (candidate < min) {
                    min = candidate;
                    minDir = 0;
                }
                traversalCost = temp.getReverseCost() + temp.getReverseServiceCost();
                if(zigzag)
                    traversalCost = temp.getReverseCost() + temp.getZigzagCost();
                candidate = dist[e11][e22] + dist[e21][e32] - dist[e11][e32] + (int)traversalCost;
                if (candidate < min) {
                    min = candidate;
                    minDir = 1;
                }
            }
            //case 4
            if (e32 > 0 && e12 > 0) {
                traversalCost = temp.getCost() + temp.getServiceCost();
                if(zigzag)
                    traversalCost = temp.getCost() + temp.getZigzagCost();
                candidate = dist[e12][e21] + dist[e22][e32] - dist[e12][e32] + (int)traversalCost;
                if (candidate < min) {
                    min = candidate;
                    minDir = 0;
                }
                traversalCost = temp.getReverseCost() + temp.getReverseServiceCost();
                if(zigzag)
                    traversalCost = temp.getReverseCost() + temp.getZigzagCost();
                candidate = dist[e12][e22] + dist[e21][e32] - dist[e12][e32] + (int)traversalCost;
                if (candidate < min) {
                    min = candidate;
                    minDir = 1;
                }
            }
        }

        ans.setFirst(min);
        ans.setSecond(minDir);
        return ans;

    }

    private PriorityQueue<Pair<Integer>> cheapestInsertion2(ZigZagTour currTour, ZigZagLink toRoute, boolean zigzag, int[][] dist) {

        //some initial checks
        if(toRoute.getStatus() == ZigZagLink.ZigZagStatus.NOT_AVAILABLE && zigzag)
            throw new IllegalArgumentException("You are attempting to zigzag a link which does not allow it");

        //init
        int tempIndex = 0;
        int penalty = 0;
        double serviceCost = 0;
        double insertionCost;
        ZigZagLink temp;
        Pair<Integer> insertionMove;
        TIntArrayList compactRoute = currTour.getCompactRepresentation();
        ArrayList<Boolean> compactDir = currTour.getCompactTraversalDirection();
        ArrayList<Boolean> serviceList = currTour.getServicingList();
        TIntArrayList incCost = currTour.getIncrementalCost();
        ArrayList<Integer> compactIndices = new ArrayList<Integer>();
        PriorityQueue<Pair<Integer>> ans = new PriorityQueue<Pair<Integer>>(compactDir.size(), new Utils.DijkstrasComparator());
        for(int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i)) {
                compactIndices.add(i);
            }
        }

        ZigZagGraph g = mInstance.getGraph();

        ZigZagLink prev = g.getEdge(compactRoute.get(0));
        Boolean prevDir = compactDir.get(0);

        int[] vertexOrLink = new int[3];
        vertexOrLink[0] = 0;
        vertexOrLink[1] = 1;
        vertexOrLink[2] = 0;
        int[] ids = new int[3];
        ids[0] = g.getDepotId();
        ids[1] = toRoute.getId();
        if(prevDir)
            ids[2] = prev.getFirstEndpointId();
        else
            ids[2] = prev.getSecondEndpointId();

        // go through and calculate the insertion cost for each position
        for(int i = 0; i < compactRoute.size(); i++) {

            penalty = 0;

            if(i > 0) {
                if(compactDir.get(i-1))
                    ids[0] = g.getEdge(compactRoute.get(i-1)).getSecondEndpointId();
                else
                    ids[0] = g.getEdge(compactRoute.get(i-1)).getFirstEndpointId();

                if(i == compactRoute.size() - 1)
                    ids[2] = g.getDepotId();
                else if(compactDir.get(i))
                    ids[2] = g.getEdge(compactRoute.get(i)).getFirstEndpointId();
                else
                    ids[2] = g.getEdge(compactRoute.get(i)).getSecondEndpointId();
            }
            //compute insertion cost
            insertionMove = insertCost(vertexOrLink,ids,dist, true);
            insertionCost = insertionMove.getFirst();

            //the push forward
            for(int j = i; j < compactRoute.size(); j++) {
                temp = g.getEdge(compactRoute.get(j));
                if(incCost.get(compactIndices.get(j)) + insertionCost > temp.getTimeWindow().getSecond()) {
                    penalty += latePenalty * ((incCost.get(compactIndices.get(j)) + insertionCost) - temp.getTimeWindow().getSecond());
                }
            }

            //add the move
            ans.add(new Pair<Integer> (i, (int)(insertionCost + penalty)));
        }

        return ans;
    }

    /*private PriorityQueue<Pair<Integer>> cheapestInsertion(TIntArrayList compactRoute, ArrayList<Boolean> compactDir, ZigZagLink toRoute, int[][] dist) {

        //the graph
        ZigZagGraph g = mInstance.getGraph();
        int n = g.getVertices().size();

        //go through and find the cheapest insertion location

        //first order by standard insertion formula
        int maxi = compactRoute.size() - 1;
        PriorityQueue<Pair<Integer>> moves = new PriorityQueue<Pair<Integer>>(1, new Utils.DijkstrasComparator());
        ZigZagLink temp1, temp2;
        boolean dir1, dir2;
        int insertionCost;
        int startTime = 0; //the back-half cost
        int addedTime = 0; //when we think we're going to reach toRoute
        double penalty = 0; //measures how much the proposed move violates time windows

        //calculate costs for inserting as the first element
        temp1 = g.getEdge(compactRoute.get(0));
        dir1 = compactDir.get(0);
        int depotId = g.getDepotId();
        int[] vertexOrLink = new int[3];
        vertexOrLink[0] = 0;
        vertexOrLink[1] = 1;
        vertexOrLink[2] = 0;
        int[] ids = new int[3];
        ids[0] = depotId;
        ids[1] = toRoute.getId();
        ids[2] = temp1.getFirstEndpointId();


        if (dir1) {
            insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
            addedTime = dist[depotId][toRoute.getFirstEndpointId()];
            if (startTime + addedTime > toRoute.getTimeWindow().getSecond()) {
                penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
            }
            startTime += dist[depotId][temp1.getFirstEndpointId()];
        } else {
            ids[2] = temp1.getSecondEndpointId();
            insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
            addedTime = dist[depotId][toRoute.getFirstEndpointId()];
            if (startTime + addedTime > toRoute.getTimeWindow().getSecond()) {
                penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
            }
            startTime += dist[depotId][temp1.getSecondEndpointId()];
        }

        if (startTime + insertionCost > g.getEdge(compactRoute.get(0)).getTimeWindow().getSecond())
            penalty += latePenalty * (startTime + insertionCost - g.getEdge(compactRoute.get(0)).getTimeWindow().getSecond());

        //check feasibility of the rest of the moves
        boolean violated = false;
        int addedTime2 = 0;
        for (int j = 1; j < maxi + 1; j++) {

            dir1 = compactDir.get(j - 1);
            dir2 = compactDir.get(j);
            temp1 = g.getEdge(compactRoute.get(j - 1));
            temp2 = g.getEdge(compactRoute.get(j));

            if (dir1 && dir2) {
                addedTime2 += dist[temp1.getSecondEndpointId()][temp2.getFirstEndpointId()];
            } else if (dir1 && !dir2) {
                addedTime2 += dist[temp1.getSecondEndpointId()][temp2.getSecondEndpointId()];
            } else if (dir2) {
                addedTime2 += dist[temp1.getFirstEndpointId()][temp2.getFirstEndpointId()];
            } else {
                addedTime2 += dist[temp1.getFirstEndpointId()][temp2.getSecondEndpointId()];
            }

            if (startTime + insertionCost + addedTime2 > g.getEdge(compactRoute.get(j)).getTimeWindow().getSecond()) {
                penalty += latePenalty * (startTime + insertionCost + addedTime2 - g.getEdge(compactRoute.get(j)).getTimeWindow().getSecond());
            }
        }
        moves.add(new Pair<Integer>(0, insertionCost + (int) penalty));

        //iterate through the list
        for (int i = 0; i < maxi; i++) {

            penalty = 0;
            temp1 = g.getEdge(compactRoute.get(i));
            temp2 = g.getEdge(compactRoute.get(i + 1));
            dir1 = compactDir.get(i);
            dir2 = compactDir.get(i + 1);

            if (dir1 && dir2) {

                ids[0] = temp1.getSecondEndpointId();
                ids[2] = temp2.getFirstEndpointId();

                insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
                addedTime = Utils.shortestEdgeDistance(temp1, toRoute, dist, 1).getFirst();
                if (startTime + addedTime > toRoute.getTimeWindow().getSecond())
                    penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
                startTime += dist[temp1.getSecondEndpointId()][temp2.getFirstEndpointId()];
            } else if (dir1 && !dir2) {

                ids[0] = temp1.getSecondEndpointId();
                ids[2] = temp2.getSecondEndpointId();

                insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
                addedTime = Utils.shortestEdgeDistance(temp1, toRoute, dist, 1).getFirst();
                if (startTime + addedTime > toRoute.getTimeWindow().getSecond())
                    penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
                startTime += dist[temp1.getSecondEndpointId()][temp2.getSecondEndpointId()];
            } else if (dir2) {

                ids[0] = temp1.getFirstEndpointId();
                ids[2] = temp2.getFirstEndpointId();

                insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
                addedTime = Utils.shortestEdgeDistance(temp1, toRoute, dist, 3).getFirst();
                if (startTime + addedTime > toRoute.getTimeWindow().getSecond())
                    penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
                startTime += dist[temp1.getFirstEndpointId()][temp2.getFirstEndpointId()];

            } else {

                ids[0] = temp1.getFirstEndpointId();
                ids[2] = temp2.getSecondEndpointId();

                insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
                addedTime = Utils.shortestEdgeDistance(temp1, toRoute, dist, 3).getFirst();
                if (startTime + addedTime > toRoute.getTimeWindow().getSecond())
                    penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
                startTime += dist[temp1.getFirstEndpointId()][temp2.getSecondEndpointId()];
            }

            //check feasibility of the rest of the moves
            violated = false;
            addedTime2 = 0;
            for (int j = i + 1; j < maxi + 1; j++) {

                dir1 = compactDir.get(j - 1);
                dir2 = compactDir.get(j);
                temp1 = g.getEdge(compactRoute.get(j - 1));
                temp2 = g.getEdge(compactRoute.get(j));

                if (dir1 && dir2) {
                    addedTime2 += dist[temp1.getSecondEndpointId()][temp2.getFirstEndpointId()];
                } else if (dir1 && !dir2) {
                    addedTime2 += dist[temp1.getSecondEndpointId()][temp2.getSecondEndpointId()];
                } else if (dir2) {
                    addedTime2 += dist[temp1.getFirstEndpointId()][temp2.getFirstEndpointId()];
                } else {
                    addedTime2 += dist[temp1.getFirstEndpointId()][temp2.getSecondEndpointId()];
                }

                if (startTime + insertionCost + addedTime2 > g.getEdge(compactRoute.get(j)).getTimeWindow().getSecond())
                    penalty += latePenalty * (startTime + insertionCost + addedTime2 - g.getEdge(compactRoute.get(j)).getTimeWindow().getSecond());
            }

            moves.add(new Pair<Integer>(i + 1, insertionCost + (int) penalty));

        }

        //last insertion spot
        ids[2] = depotId;
        penalty = 0;
        temp2 = g.getEdge(compactRoute.get(maxi));
        dir2 = compactDir.get(maxi);
        if (dir2) {
            ids[0] = temp2.getSecondEndpointId();
            insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
            addedTime = dist[temp2.getSecondEndpointId()][toRoute.getFirstEndpointId()];
            if (startTime + addedTime > toRoute.getTimeWindow().getSecond()) {
                penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
            }
        } else {
            ids[0] = temp2.getFirstEndpointId();
            insertionCost = insertCost(vertexOrLink, ids, dist).getFirst();
            addedTime = dist[temp2.getFirstEndpointId()][toRoute.getFirstEndpointId()];
            if (startTime + addedTime > toRoute.getTimeWindow().getSecond()) {
                penalty += latePenalty * (startTime + addedTime - toRoute.getTimeWindow().getSecond());
            }
        }
        moves.add(new Pair<Integer>(maxi + 1, insertionCost + (int) penalty));

        return moves;
    }*/

    /**
     * Seeds the route for the WRPPZZTW solution.
     *
     * @param req      - contains ids of the required edges in the graph
     * @param dist     - the distance matrix (memory saver)
     * @param lenience - n, where the routine will produce the id of one of
     *                 the n required edges furthest from the depot
     * @return - iterates through the required edges and returns one randomly from the
     * n edges furthest from the depot, (n = lenience).
     */
    private int seedRoute(HashSet<Integer> req, int[][] dist, int lenience) throws IllegalArgumentException {

        if (lenience <= 0)
            throw new IllegalArgumentException("Lenience must be at least 1.");
        if (lenience > req.size()) {
            LOGGER.warn("Lenience is greater than the number of required edges, setting lenience to be the size of req.");
            lenience = req.size();
        }

        PriorityQueue<Pair<Integer>> pq = new PriorityQueue<Pair<Integer>>(req.size(), new Utils.InverseDijkstrasComparator());
        ZigZagGraph g = mInstance.getGraph();
        int depotId = g.getDepotId();
        int max = Integer.MIN_VALUE;
        int maxi = -1;
        int j, k;

        ZigZagLink temp;

        for (Integer i : req) {

            temp = g.getEdge(i);
            j = temp.getFirstEndpointId();
            k = temp.getSecondEndpointId();
            max = -2;

            if (dist[depotId][k] > max) {
                max = dist[depotId][k];
            }
            if (dist[depotId][j] > max) {
                max = dist[depotId][j];
            }

            pq.add(new Pair<Integer>(i, max));

        }

        int rank = rng.nextInt(lenience) + 1;

        int ans = -1;
        for (int i = 1; i <= rank; i++) {
            ans = pq.poll().getFirst();
        }

        return ans;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, ProblemAttributes.Properties.TIME_WINDOWS);
    }

    @Override
    public String getSolverName() {
        return "Push First Insertion Heuristic for the Windy Rural Postman Problem with Time Windows";
    }

    @Override
    public Solver<ZigZagVertex, ZigZagLink, ZigZagGraph> instantiate(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> p) {
        return new WRPPZZTW_PFIH(p);
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        return new HashMap<String, Double>();
    }

    /**
     * From Solutions in Under 10 Seconds for Vehicle Routing Problems
     * with Time Windows using Commodity Computers (Cardoso et al.),
     * adapted to operate on edges.  Lower costs indicate edges to be routed first.
     *
     * @param zzl  - the link to be assessed
     * @param dist - distance matrix for this instance's graph (memory saver).
     * @return
     */
    private double assessPFIHCost(ZigZagLink zzl, int[][] dist) {

        ZigZagGraph g = mInstance.getGraph();

        int dist1 = dist[g.getDepotId()][zzl.getFirstEndpointId()];
        int dist2 = dist[g.getDepotId()][zzl.getSecondEndpointId()];
        int endId;
        int dio = -1; //distance to the edge

        if (!g.getVertex(g.getDepotId()).hasCoordinates())
            LOGGER.debug("The depot does not have coordinates.  Behavior is not guaranteed.");

        double dx = g.getVertex(g.getDepotId()).getX();
        double dy = g.getVertex(g.getDepotId()).getY();

        if (dist1 < dist2) {
            dio = dist1;
            endId = zzl.getSecondEndpointId();
        } else {
            dio = dist2;
            endId = zzl.getFirstEndpointId();
        }


        if (!g.getVertex(endId).hasCoordinates())
            LOGGER.debug("Vertex " + endId + " does not have coordinates.  Behavior is not guaranteed.");

        double theta;
        if (g.getVertex(endId).getX() == dx && g.getVertex(endId).getY() == dy) {
            LOGGER.debug("It appears as though this vertex is coincident with the depot.  Ignoring theta component.");
            theta = 0;
        } else
            theta = Math.atan2(Math.abs(g.getVertex(endId).getY() - dy), (Math.abs(g.getVertex(endId).getX()) - dx));

        int tStart, tEnd;
        tStart = zzl.getTimeWindow().getFirst();
        tEnd = zzl.getTimeWindow().getSecond();

        return -mAlpha * dio + mBeta * tEnd + mGamma * dio * theta / (2 * Math.PI) + mLambda * (1 / (tEnd - tStart));
    }

    public double getmAlpha() {
        return mAlpha;
    }

    public void setmAlpha(double mAlpha) {
        this.mAlpha = mAlpha;
    }

    public double getmBeta() {
        return mBeta;
    }

    public void setmBeta(double mBeta) {
        this.mBeta = mBeta;
    }

    public double getmGamma() {
        return mGamma;
    }

    public void setmGamma(double mGamma) {
        this.mGamma = mGamma;
    }

    public double getmLambda() {
        return mLambda;
    }

    public void setmLambda(double mLambda) {
        this.mLambda = mLambda;
    }

    public double getLatePenalty() {
        return latePenalty;
    }

    public void setLatePenalty(double latePenalty) {
        this.latePenalty = latePenalty;
    }

    public int getBestPartialSize() {
        return bestPartialSize;
    }

    public int getBestPartialLength() {
        return bestPartialLength;
    }

    public int getBestPartialNumZigzags() {
        return bestPartialNumZigzags;
    }

    public int getBestSumLengthZigzags() {
        return bestSumLengthZigzags;
    }

    public int getAvgPartialSize() {
        return avgPartialSize;
    }

    public int getAvgPartialLength() {
        return avgPartialLength;
    }

    public int getAvgPartialNumZigzags() {
        return avgPartialNumZigzags;
    }

    public int getAvgSumLengthZigzags() {
        return avgSumLengthZigzags;
    }

    public int getGreatestSumLengthZigzags() {
        return greatestSumLengthZigzags;
    }
}
