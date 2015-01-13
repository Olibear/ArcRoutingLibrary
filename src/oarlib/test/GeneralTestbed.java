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
package oarlib.test;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Route;
import oarlib.display.GraphDisplay;
import oarlib.graph.graphgen.OSM_Fetcher;
import oarlib.graph.graphgen.Util.BoundingBox;
import oarlib.graph.graphgen.Util.OSM_BoundingBoxes;
import oarlib.graph.graphgen.erdosrenyi.DirectedErdosRenyiGraphGenerator;
import oarlib.graph.graphgen.erdosrenyi.UndirectedErdosRenyiGraphGenerator;
import oarlib.graph.graphgen.rectangular.WindyRectangularGraphGenerator;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.io.GraphFormat;
import oarlib.graph.io.GraphReader;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.IndexedRecord;
import oarlib.graph.util.MSArbor;
import oarlib.graph.util.Pair;
import oarlib.link.impl.Arc;
import oarlib.link.impl.Edge;
import oarlib.link.impl.MixedEdge;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.cpp.MixedCPP;
import oarlib.problem.impl.cpp.UndirectedCPP;
import oarlib.problem.impl.cpp.WindyCPP;
import oarlib.problem.impl.multivehicle.MultiVehicleMCPP;
import oarlib.problem.impl.multivehicle.MultiVehicleWRPP;
import oarlib.problem.impl.rpp.DirectedRPP;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.solver.impl.*;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;
import oarlib.vertex.impl.UndirectedVertex;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class
        GeneralTestbed {

    private static final Logger LOGGER = Logger.getLogger(GeneralTestbed.class);

    /**
     * The main method.  This class contains a bunch of test / validation methods, and is meant to give examples of
     * how to use the architecture.
     *
     * @param args
     */
    public static void main(String[] args) {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
        PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
        rootLogger.addAppender(new ConsoleAppender(layout));

        //testSimpleGraphReader("/Users/File/Location/Of/A/Graph.txt");
        //validateEulerTour();
        //snippetUCPPSolver();
        //testFredericksons("/Users/Username/FolderName");
        //validateMCPPSolver("/Users/Username/FolderName");
        //validateImprovedMCPPSolver("/Users/Username/FolderName");
        //validateWRPPSolver("/Users/Username/FolderName", "/Users/Output/File.txt");
        //validateImprovedWRPPSolver("/Users/Username/FolderName", "/Users/Output/File.txt");
        //testCamposGraphReader();
        //validateSimplifyGraph();
        //testMSArbor();
        //testDRPPSolver("/Users/Username/FolderName", "/Users/Output/File.txt");
        //POMSexample();
        testCapacitatedSolvers("/Users/oliverlum/Downloads/WPP", "C:\\Users\\Oliver\\Desktop\\kwrpp_real_rest.txt");
        //testGraphDisplay();
        //testOSMQuery();
        //testMMkWRPPSolver();
        //testWidestPath();

    }

    @SuppressWarnings("unused")
    private static void testWidestPath() {
        try {
            DirectedGraph dg = new DirectedGraph(4);
            dg.addEdge(1, 2, 3);
            dg.addEdge(1, 3, 4);
            dg.addEdge(1, 4, 2);
            dg.addEdge(2, 3, 4);
            dg.addEdge(2, 4, 2);
            dg.addEdge(3, 4, 5);

            int[] width = new int[5];
            int[] widestPath = new int[5];
            int[] widestEdgePath = new int[5];
            CommonAlgorithms.dijkstrasWidestPathAlgorithm(dg, 1, width, widestPath, widestEdgePath);

            IndexedRecord<Integer>[] width2 = new IndexedRecord[5];
            IndexedRecord<Integer>[] widestPath2 = new IndexedRecord[5];
            IndexedRecord<Integer>[] widestEdgePath2 = new IndexedRecord[5];
            CommonAlgorithms.dijkstrasWidestPathAlgorithmWithMaxPathCardinality(dg, 1, width2, widestPath2, widestEdgePath2, 2);

            System.out.println("Done");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unused")
    private static void testMMkWRPPSolver() {
        try {
            MultiVehicleWRPP wrpp;
            MultiWRPPSolver_Benavent wrppSolver;
            Collection<Route<WindyVertex, WindyEdge>> ans;

            GraphReader gr = new GraphReader(GraphFormat.Name.Corberan);

            WindyGraph test = (WindyGraph) gr.readGraph("/Users/oliverlum/Downloads/WPP/WA0531");

            wrpp = new MultiVehicleWRPP(test, 5);
            wrppSolver = new MultiWRPPSolver_Benavent(wrpp, "WA0531");

            ans = wrppSolver.trySolve();
            System.out.println(wrppSolver.printCurrentSol());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static void testOSMQuery() {

        try {
            System.out.println("====================================");
            for (BoundingBox bb : OSM_BoundingBoxes.CITY_INSTANCES) {
                OSM_Fetcher fetcher = new OSM_Fetcher(bb);
                fetcher.queryForGraph();
                WindyGraph g = fetcher.queryForGraph();
                System.out.println("N: " + g.getVertices().size());
                System.out.println("M: " + g.getEdges().size());
                GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, g, bb.getTitle());
                gd.export(GraphDisplay.ExportType.PDF);
            }

            System.out.println("====================================");
            for (BoundingBox bb : OSM_BoundingBoxes.SUBURBAN_INSTANCES) {
                OSM_Fetcher fetcher = new OSM_Fetcher(bb);
                fetcher.queryForGraph();
                WindyGraph g = fetcher.queryForGraph();
                System.out.println("N: " + g.getVertices().size());
                System.out.println("M: " + g.getEdges().size());
                GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, g, bb.getTitle());
                gd.export(GraphDisplay.ExportType.PDF);
            }

            System.out.println("====================================");
            for (BoundingBox bb : OSM_BoundingBoxes.RURAL_INSTANCES) {
                OSM_Fetcher fetcher = new OSM_Fetcher(bb);
                fetcher.queryForGraph();
                WindyGraph g = fetcher.queryForGraph();
                System.out.println("N: " + g.getVertices().size());
                System.out.println("M: " + g.getEdges().size());
                GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, g, bb.getTitle());
                gd.export(GraphDisplay.ExportType.PDF);
            }


            System.out.println("====================================");
            for (BoundingBox bb : OSM_BoundingBoxes.BIG_INSTANCES) {
                OSM_Fetcher fetcher = new OSM_Fetcher(bb);
                fetcher.queryForGraph();
                WindyGraph g = fetcher.queryForGraph();
                System.out.println("N: " + g.getVertices().size());
                System.out.println("M: " + g.getEdges().size());
                GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, g, bb.getTitle());
                gd.export(GraphDisplay.ExportType.PDF);
            }

            System.out.println("====================================");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unused")
    private static void testGraphDisplay() {
        try {
            DirectedErdosRenyiGraphGenerator dgg = new DirectedErdosRenyiGraphGenerator();
            DirectedGraph dGraph = dgg.generateGraph(100, 5, true, .005);

            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, dGraph, "test");
            gd.export(GraphDisplay.ExportType.PDF);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static void testCapacitatedSolvers(String instanceFolder, String outputFile) {
        try {

            /*
            //UNDIRECTED
            System.out.println("========================================================");
            System.out.println("Beginning Test of the Undirected Partitioning Code");
            System.out.println("========================================================");
            UndirectedGraphGenerator ugg = new UndirectedGraphGenerator();
            UndirectedGraph utest = (UndirectedGraph)ugg.generateGraph(100, 10, true, .5);
            CapacitatedUCPP validUInstance = new CapacitatedUCPP(utest, 5);
            CapacitatedUCPPSolver validUSolver = new CapacitatedUCPPSolver(validUInstance);
            Collection<Route> validUAns = validUSolver.trySolve();

            int routeCounter = 1;

            for(Route r: validUAns) {
                System.out.println("Now displaying route " + routeCounter++);
                System.out.println(r.toString());
                System.out.println("This route costs " + r.getCost());
                System.out.println();
            }

            //DIRECTED

            System.out.println("========================================================");
            System.out.println("Beginning Test of the Directed Partitioning Code");
            System.out.println("========================================================");
            DirectedGraphGenerator dgg = new DirectedGraphGenerator();
            DirectedGraph dtest = (DirectedGraph)dgg.generateGraph(200, 10, true, .5);
            CapacitatedDCPP validDInstance = new CapacitatedDCPP(dtest, 5);
            CapacitatedDCPPSolver validDSolver = new CapacitatedDCPPSolver(validDInstance);
            Collection<Route> validDAns = validDSolver.trySolve();

            routeCounter = 1;

            for(Route r: validUAns) {
                System.out.println("Now displaying route " + routeCounter++);
                System.out.println(r.toString());
                System.out.println("This route costs " + r.getCost());
                System.out.println();
            }
            */

            int routeCounter;

            //MIXED

            System.out.println("========================================================");
            System.out.println("Beginning Test of the Mixed Partitioning Code");
            System.out.println("========================================================");
            GraphReader gr = new GraphReader(GraphFormat.Name.Corberan);
            try {
                MultiVehicleMCPP validMInstance;
                MultiMCPPSolver validMSolver;
                Collection<Route<MixedVertex, MixedEdge>> validMAns;

                File testInstanceFolder = new File(instanceFolder);
                long start;
                long end;

                //run on all instances in the folder
                /*for (final File testInstance : testInstanceFolder.listFiles()) {
                    String temp = testInstance.getName();
                    System.out.println(temp);
                    if (temp.equals(".DS_Store"))
                        continue;
                    Graph<?, ?> g = gr.readGraph(instanceFolder + "/" + temp);
                    if (g.getClass() == MixedGraph.class) {
                        MixedGraph g2 = (MixedGraph) g;
                        validMInstance = new MultiVehicleMCPP(g2, 5);
                        validMSolver = new MultiMCPPSolver(validMInstance);
                        start = System.nanoTime();
                        validMAns = validMSolver.trySolve();
                        end = System.nanoTime();

                        routeCounter = 1;

                        for (Route r : validMAns) {
                            System.out.println("Now displaying route " + routeCounter++);
                            System.out.println(r.toString());
                            System.out.println("This route costs " + r.getCost());
                            System.out.println();
                        }
                        System.out.println("It took " + (end - start) / (1e6) + " milliseconds to run our Yaoyuenyong's implementation on a graph with " + g2.getEdges().size() + " edges.");
                    }
                }*/

                //WINDY

                System.out.println("========================================================");
                System.out.println("Beginning Test of the Windy Partitioning Code");
                System.out.println("========================================================");

                MultiVehicleWRPP validWInstance;
                MultiWRPPSolver_Benavent validWSolver;
                Collection<Route<WindyVertex, WindyEdge>> validWAns;
                PrintWriter pw = new PrintWriter(outputFile, "UTF-8");


                //run on all instances in the folder
                int limForDebug = 2; //only run on the first 10 instances for now
                int debugCounter = 0;
                String output;

                /*
                for (BoundingBox bb : OSM_BoundingBoxes.CITY_INSTANCES) {

                    OSM_Fetcher fetcher = new OSM_Fetcher(bb);
                    WindyGraph g = fetcher.queryForGraph();

                    validWInstance = new MultiVehicleWRPP(g, 5);
                    validWSolver = new MultiWRPPSolver_Benavent(validWInstance, bb.getTitle());
                    start = System.nanoTime();
                    validWAns = validWSolver.trySolve();
                    end = System.nanoTime();
                    System.out.println("It took " + (end - start) / 1000000000 + " seconds to run the solver on this instance.");
                    pw.println("It took " + (end - start) / 1000000000 + " seconds to run the solver on this instance.");

                    routeCounter = 1;
                    int maxCost = 0;
                    int minCost = Integer.MAX_VALUE;
                    int tempCost;

                    for (Route r : validWAns) {
                        tempCost = r.getCost();
                        if (tempCost > maxCost)
                            maxCost = tempCost;
                        if (tempCost < minCost)
                            minCost = tempCost;

                    }

                    output = validWSolver.printCurrentSol();
                    System.out.println(output);
                    pw.println(output);
                }*/


                //now do the rectangular instances
                for (int i = 1; i <= 10; i++) {
                    WindyRectangularGraphGenerator wrg = new WindyRectangularGraphGenerator();
                    WindyGraph g = wrg.generateGraph(25 - i, 10, .5, true);

                    validWInstance = new MultiVehicleWRPP(g, 5);
                    validWSolver = new MultiWRPPSolver_Benavent(validWInstance, "Random Instance " + i);
                    start = System.nanoTime();
                    validWAns = validWSolver.trySolve();
                    end = System.nanoTime();
                    System.out.println("It took " + (end - start) / 1000000000 + " seconds to run the solver on this instance.");
                    pw.println("It took " + (end - start) / 1000000000 + " seconds to run the solver on this instance.");

                    routeCounter = 1;
                    int maxCost = 0;
                    int minCost = Integer.MAX_VALUE;
                    int tempCost;

                    for (Route<WindyVertex, WindyEdge> r : validWAns) {
                        tempCost = r.getCost();
                        if (tempCost > maxCost)
                            maxCost = tempCost;
                        if (tempCost < minCost)
                            minCost = tempCost;
                    }

                    output = validWSolver.printCurrentSol();
                    System.out.println(output);
                    pw.println(output);
                }


                pw.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static void POMSexample() {
        try {
            WindyGraph neighborhood = new WindyGraph(17);
            neighborhood.addEdge(1, 2, 5, 5, false);
            neighborhood.addEdge(2, 3, 4, 4, false);
            neighborhood.addEdge(4, 17, 7, 7, true);
            neighborhood.addEdge(1, 6, 6, 6, true);
            neighborhood.addEdge(2, 8, 5, 5, true);
            neighborhood.addEdge(3, 10, 3, 3, false);
            neighborhood.addEdge(4, 5, 2, 2, true);
            neighborhood.addEdge(5, 6, 3, 3, true);
            neighborhood.addEdge(6, 7, 1, 1, false);
            neighborhood.addEdge(7, 8, 3, 3, true);
            neighborhood.addEdge(8, 9, 2, 2, true);
            neighborhood.addEdge(9, 10, 2, 2, false);
            neighborhood.addEdge(4, 11, 3, 3, false);
            neighborhood.addEdge(7, 11, 5, 5, true);
            neighborhood.addEdge(11, 12, 5, 5, false);
            neighborhood.addEdge(7, 13, 7, 7, false);
            neighborhood.addEdge(8, 14, 8, 8, false);
            neighborhood.addEdge(9, 15, 7, 7, true);
            neighborhood.addEdge(10, 16, 8, 8, true);
            neighborhood.addEdge(12, 13, 4, 4, true);
            neighborhood.addEdge(13, 14, 3, 3, false);
            neighborhood.addEdge(14, 15, 3, 3, true);
            neighborhood.addEdge(15, 16, 2, 2, false);

            WindyRPP testProblem = new WindyRPP(neighborhood);
            WRPPSolver_Win testSolver = new WRPPSolver_Win(testProblem);
            Route ans = testSolver.trySolve();
            System.out.println(ans.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to validate our graph simplification in the DRPP Solver.  We construct the example given in the original
     * paper by Christofides, and ensure that the output matches with that displayed in the figures of the paper.
     */
    @SuppressWarnings("unused")
    private static void validateSimplifyGraph() {
        try {
            // Create the test instance
            DirectedGraph test = new DirectedGraph(13);
            test.addEdge(1, 2, "a1", 2, true);
            test.addEdge(4, 5, "a2", 3, true);

            test.addEdge(3, 1, "a3", 5, false);
            test.addEdge(2, 3, "a4", 4, true);
            test.addEdge(2, 13, "a5", 7, false);
            test.addEdge(13, 4, "a6", 4, false);
            test.addEdge(4, 7, "a7", 6, false);
            test.addEdge(6, 4, "a8", 3, true);
            test.addEdge(5, 6, "a9", 5, true);
            test.addEdge(6, 5, "a10", 3, false);

            test.addEdge(3, 8, "a11", 3, false);
            test.addEdge(12, 3, "a12", 7, false);
            test.addEdge(12, 13, "a13", 9, false);
            test.addEdge(6, 7, "a14", 4, true);

            test.addEdge(9, 12, "a15", 5, false);
            test.addEdge(11, 12, "a16", 2, false);
            test.addEdge(7, 11, "a17", 8, false);
            test.addEdge(11, 7, "a18", 3, false);

            test.addEdge(8, 9, "a19", 4, true);
            test.addEdge(9, 10, "a20", 1, true);
            test.addEdge(10, 11, "a21", 6, true);

            test.addEdge(11, 10, "a22", 3, false);
            test.addEdge(10, 9, "a23", 5, true);
            test.addEdge(9, 8, "a24", 3, true);

            // run the solver on it
            DirectedRPP validInstance = new DirectedRPP(test);
            DRPPSolver_Christofides validSolver = new DRPPSolver_Christofides(validInstance);
            validSolver.trySolve();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to test / validate our implementation of Christofides' heuristic for DRPP Solver.
     */
    @SuppressWarnings("unused")
    private static void testDRPPSolver(String instanceFolder, String outputFile) {
        GraphReader gr = new GraphReader(GraphFormat.Name.Campos);
        try {
            DirectedRPP validInstance;
            DRPPSolver_Christofides validSolver;
            Route validAns;

            File testInstanceFolder = new File(instanceFolder);
            PrintWriter pw = new PrintWriter(outputFile, "UTF-8");
            long start;
            long end;

            //run on all the instances in the folder
            for (final File testInstance : testInstanceFolder.listFiles()) {
                int bestCost = Integer.MAX_VALUE; // the running best cost over running the solver repeatedly on the same instance
                for (int i = 0; i < 1; i++) {
                    String temp = testInstance.getName();
                    System.out.println(temp); // print the file name

                    //ensure that the problem is a valid instance
                    if (!temp.endsWith(".0") && !temp.endsWith(".1") && !temp.endsWith(".1_3") && !temp.endsWith(".2_3") && !temp.endsWith(".3_3"))
                        continue;

                    // read the graph
                    Graph<?, ?> g = gr.readGraph(instanceFolder + "/" + temp);

                    if (g.getClass() == DirectedGraph.class) {
                        // run it and time it
                        DirectedGraph g2 = (DirectedGraph) g;
                        validInstance = new DirectedRPP(g2);
                        validSolver = new DRPPSolver_Christofides(validInstance);
                        start = System.nanoTime();
                        validAns = validSolver.trySolve();
                        end = System.nanoTime();
                        if (validAns.getCost() < bestCost)
                            bestCost = validAns.getCost();
                        System.out.println("It took " + (end - start) / (1e6) + " milliseconds to run our DRPP implementation on a graph with " + g2.getEdges().size() + " edges.");
                        System.out.println(validAns.toString());
                    }
                }
                if (bestCost == Integer.MAX_VALUE)
                    continue;
                System.out.println("bestCost: " + bestCost);
                //pw.println(bestCost + ";");
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to test our JNI Wrapper for MSArbor's minimum spanning arborescence code.
     * We test on a toy instance, and make sure it's robust to edge removal.
     */
    @SuppressWarnings("unused")
    private static void testMSArbor() {
        try {
            // set up the toy instance
            int n = 3;
            int m = 6;
            int[] weights = new int[n * (n - 1)];
            weights[0] = 1000; //0-0
            weights[1] = 6; //0-1
            weights[2] = 5; //1-0
            weights[3] = 1000; //1-1
            weights[4] = 2; //2-0
            weights[5] = 10; //2-1

            // run it directly
            int[] ans = MSArbor.msArbor(n, m, weights);

            // run our wrapper that takes our graph structure
            DirectedGraph test = new DirectedGraph();
            test.addVertex(new DirectedVertex("orig"));
            test.addVertex(new DirectedVertex("orig"));
            test.addVertex(new DirectedVertex("orig"));

            test.addEdge(1, 2, "orig", 6);
            test.addEdge(2, 1, "orig", 5);
            test.addEdge(3, 1, "orig", 2);
            test.addEdge(3, 2, "orig", 3);
            test.addEdge(1, 3, "orig", 1);
            test.addEdge(2, 3, "orig", 4);

            TIntObjectHashMap<Arc> testArcs = test.getInternalEdgeMap();
            test.removeEdge(testArcs.get(3));
            test.removeEdge(testArcs.get(4));
            test.removeEdge(testArcs.get(1));
            test.removeEdge(testArcs.get(6));

            HashSet<Integer> msa = CommonAlgorithms.minSpanningArborescence(test, 2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * An example method which shows how to use our graph generators.
     */
    @SuppressWarnings("unused")
    private static void testUndirectedGraphGenerator() {
        UndirectedErdosRenyiGraphGenerator ugg = new UndirectedErdosRenyiGraphGenerator();
        /**
         * Request a graph with 1000 nodes, edge cost from {0,1,...,10} that is
         * connected, and density roughly .5.
         */
        UndirectedGraph g = ugg.generateGraph(1000, 10, true, .5);
    }

    /**
     * An example method which shows how to use our graph readers.
     */
    @SuppressWarnings("unused")
    private static void testSimpleGraphReader(String instancePath) {
        GraphReader gr = new GraphReader(GraphFormat.Name.Simple);
        try {
            Graph<?, ?> g = gr.readGraph(instancePath);
            if (g.getClass() == DirectedGraph.class) {
                DirectedGraph g2 = (DirectedGraph) g;
            }
            System.out.println("check things");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to test our implementation of Frederickson's algorithm on the instances
     * provided on Angel Corberan's website
     */
    @SuppressWarnings("unused")
    private static void testFredericksons(String instanceFolder) {
        GraphReader gr = new GraphReader(GraphFormat.Name.Corberan);
        try {
            MixedCPP validInstance;
            MCPPSolver_Frederickson validSolver;
            Route validAns;

            File testInstanceFolder = new File(instanceFolder);
            long start;
            long end;

            // run on all instances in the folder
            for (final File testInstance : testInstanceFolder.listFiles()) {
                String temp = testInstance.getName();
                System.out.println(temp);
                if (temp.equals(".DS_Store")) // ignore mac stuff
                    continue;

                Graph<?, ?> g = gr.readGraph(instanceFolder + "/" + temp);
                if (g.getClass() == MixedGraph.class) {
                    MixedGraph g2 = (MixedGraph) g;
                    validInstance = new MixedCPP(g2);
                    validSolver = new MCPPSolver_Frederickson(validInstance);
                    start = System.nanoTime();
                    validAns = validSolver.trySolve(); //my ans
                    end = System.nanoTime();
                    System.out.println("It took " + (end - start) / (1e6) + " milliseconds to run our Frederickson's implementation on a graph with " + g2.getEdges().size() + " edges.");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to test / validate our implementation of Yaoyuenyong's algorithm on the instances
     * graciously provided by Yaoyuenyong; the results are compared against those given in
     * his paper.
     */
    @SuppressWarnings("unused")
    private static void validateImprovedMCPPSolver(String instanceFolder) {
        GraphReader gr = new GraphReader(GraphFormat.Name.Yaoyuenyong);
        try {
            MixedCPP validInstance;
            MCPPSolver_Yaoyuenyong validSolver;
            Route validAns;

            File testInstanceFolder = new File(instanceFolder);
            long start;
            long end;

            //run on all instances in the folder
            for (final File testInstance : testInstanceFolder.listFiles()) {
                String temp = testInstance.getName();
                System.out.println(temp);
                if (temp.equals(".DS_Store"))
                    continue;
                Graph<?, ?> g = gr.readGraph(instanceFolder + "/" + temp);
                if (g.getClass() == MixedGraph.class) {
                    MixedGraph g2 = (MixedGraph) g;
                    validInstance = new MixedCPP(g2);
                    validSolver = new MCPPSolver_Yaoyuenyong(validInstance);
                    start = System.nanoTime();
                    validAns = validSolver.trySolve();
                    end = System.nanoTime();
                    System.out.println(validAns.getCost());
                    System.out.println("It took " + (end - start) / (1e6) + " milliseconds to run our Yaoyuenyong's implementation on a graph with " + g2.getEdges().size() + " edges.");
                    System.out.println(validAns.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to validate our implementation of Frederickson's algorithm on the instances
     * graciously provided by Yaoyuenyong; the results are compared against those given in
     * his paper.
     */
    @SuppressWarnings("unused")
    private static void validateMCPPSolver(String instanceFolder) {
        GraphReader gr = new GraphReader(GraphFormat.Name.Corberan);
        //GraphReader gr = new GraphReader(Format.Name.Yaoyuenyong);
        try {
            MixedCPP validInstance;
            MCPPSolver_Frederickson validSolver;
            Route validAns;

            File testInstanceFolder = new File(instanceFolder);
            long start;
            long end;

            for (final File testInstance : testInstanceFolder.listFiles()) {
                String temp = testInstance.getName();
                System.out.println(temp);
                if (temp.equals(".DS_Store"))
                    continue;
                Graph<?, ?> g = gr.readGraph(instanceFolder + "/" + temp);
                int arcCount = 0;
                int edgeCount = 0;
                int oddDegreeCount = 0;
                if (g.getClass() == MixedGraph.class) {
                    MixedGraph g2 = (MixedGraph) g;
                    validInstance = new MixedCPP(g2);
                    validSolver = new MCPPSolver_Frederickson(validInstance);
                    start = System.nanoTime();
                    validAns = validSolver.trySolve();
                    end = System.nanoTime();
                    System.out.println(validAns.getCost());
                    System.out.println("It took " + (end - start) / (1e6) + " milliseconds to run our Frederickson's implementation on a graph with " + g2.getEdges().size() + " edges.");
                    System.out.println(validAns.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to validate the WRPP Solver based on instances provided on
     * Angel Corberan's website.
     */
    @SuppressWarnings("unused")
    private static void validateWRPPSolver(String instanceFolder, String outputFile) {
        GraphReader gr = new GraphReader(GraphFormat.Name.Corberan);
        try {
            WindyRPP validInstance;
            WRPPSolver_Win validSolver;
            Route validAns;

            File testInstanceFolder = new File(instanceFolder);
            PrintWriter pw = new PrintWriter(outputFile, "UTF-8");
            long start;
            long end;

            // run the solver on the instances in the provided folder
            for (final File testInstance : testInstanceFolder.listFiles()) {
                int bestCost = Integer.MAX_VALUE;
                for (int i = 0; i < 1; i++) {
                    String temp = testInstance.getName();
                    System.out.println(temp);
                    if (!temp.startsWith("A") && !temp.startsWith("M") && !temp.startsWith("m"))
                        continue;
                    Graph<?, ?> g = gr.readGraph(instanceFolder + "/" + temp);

                    if (g.getClass() == WindyGraph.class) {
                        WindyGraph g2 = (WindyGraph) g;
                        validInstance = new WindyRPP(g2);
                        validSolver = new WRPPSolver_Win(validInstance);
                        start = System.nanoTime();
                        validAns = validSolver.trySolve();
                        if (validAns.getCost() < bestCost)
                            bestCost = validAns.getCost();
                        end = System.nanoTime();
                        System.out.println("It took " + (end - start) / (1e6) + " milliseconds to run our WRPP1 implementation on a graph with " + g2.getEdges().size() + " edges.");
                        System.out.println(validAns.toString());
                        pw.println((end - start) / (1e6) + "," + g2.getEdges().size() + ";");
                    }
                }
                if (bestCost == Integer.MAX_VALUE)
                    continue;
                //pw.println(bestCost + ";");
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to validate the improved WRPP Solver based on instances provided on
     * Angel Corberan's website.
     */
    @SuppressWarnings("unused")
    private static void validateImprovedWRPPSolver(String instanceFolder, String outputFile) {
        GraphReader gr = new GraphReader(GraphFormat.Name.Corberan);
        try {
            WindyRPP validInstance;
            WRPPSolver_Benavent_H1 validSolver;
            Route validAns;

            File testInstanceFolder = new File(instanceFolder);
            PrintWriter pw = new PrintWriter(outputFile, "UTF-8");
            long start;
            long end;

            for (final File testInstance : testInstanceFolder.listFiles()) {
                int bestCost = Integer.MAX_VALUE;
                for (int i = 0; i < 1; i++) {
                    String temp = testInstance.getName();
                    System.out.println(temp);
                    if (!temp.startsWith("A") && !temp.startsWith("M") && !temp.startsWith("m"))
                        continue;
                    Graph<?, ?> g = gr.readGraph(instanceFolder + "/" + temp);

                    if (g.getClass() == WindyGraph.class) {
                        WindyGraph g2 = (WindyGraph) g;
                        validInstance = new WindyRPP(g2);
                        validSolver = new WRPPSolver_Benavent_H1(validInstance);
                        start = System.nanoTime();
                        validAns = validSolver.trySolve();
                        end = System.nanoTime();
                        if (validAns.getCost() < bestCost)
                            bestCost = validAns.getCost();
                        System.out.println("It took " + (end - start) / (1e6) + " milliseconds to run our WRPP1 implementation on a graph with " + g2.getEdges().size() + " edges.");
                        System.out.println(validAns.toString());
                        pw.println((end - start) / (1e6) + "," + g2.getEdges().size() + ";");
                    }
                }
                if (bestCost == Integer.MAX_VALUE)
                    continue;
                //pw.println(bestCost + ";");
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static void validateWPPIPSolvers(String instanceFolder, String outputFile) {
        GraphReader gr = new GraphReader(GraphFormat.Name.Corberan);
        try {
            WindyCPP validInstance;
            WPPSolver_Gurobi validSolver;
            WPPSolver_Gurobi_CuttingPlane validSolver2;
            Route validAns;

            File testInstanceFolder = new File(instanceFolder);
            PrintWriter pw = new PrintWriter(outputFile, "UTF-8");
            long start;
            long end;

            int cost = 0;
            int cost2 = 0;

            // run the solver on the instances in the provided folder
            int j = 0;
            for (final File testInstance : testInstanceFolder.listFiles()) {
                for (int i = 0; i < 1; i++) {
                    String temp = testInstance.getName();
                    System.out.println(temp);
                    if (!temp.startsWith("WA") && !temp.startsWith("WB"))
                        continue;
                    Graph<?, ?> g = gr.readGraph(instanceFolder + "/" + temp);

                    if (g.getClass() == WindyGraph.class) {
                        WindyGraph g2 = (WindyGraph) g;
                        validInstance = new WindyCPP(g2);
                        //validSolver = new WPPSolver_Gurobi(validInstance);
                        validSolver2 = new WPPSolver_Gurobi_CuttingPlane(validInstance);

                        //exact
                        //start = System.nanoTime();
                        //validAns = validSolver.trySolve();
                        //end = System.nanoTime();

                        //pw.print((end-start)/(1e6) + ",");
                        //System.out.println("It took " + (end - start)/(1e6) + " milliseconds to run our WPP_Gurobi implementation on a graph with " + g2.getEdges().size() + " edges.");


                        //heuristic
                        start = System.nanoTime();
                        validAns = validSolver2.trySolve();
                        end = System.nanoTime();

                        pw.print((end - start) / (1e6));
                        pw.println();
                        System.out.println("It took " + (end - start) / (1e6) + " milliseconds to run our WPP_Gurobi_CuttingPlane implementation on a graph with " + g2.getEdges().size() + " edges.");
                    }
                }

            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * A method to ensure that our implementation of Hierholzer's algorithm to find an euler tour on
     * an Eulerian graph.
     */
    @SuppressWarnings("unused")
    private static void validateEulerTour() {
        try {
            UndirectedErdosRenyiGraphGenerator ugg = new UndirectedErdosRenyiGraphGenerator();
            UndirectedGraph g;
            long startTime;
            long endTime;
            boolean tourOK;
            for (int i = 10; i < 150; i += 10) {
                tourOK = false;
                g = ugg.generateEulerianGraph(i, 10, true);
                System.out.println("Undirected graph of size " + i + " is Eulerian? " + CommonAlgorithms.isEulerian(g));
                startTime = System.nanoTime();
                ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(g);
                endTime = System.nanoTime();
                System.out.println("It took " + (endTime - startTime) / (1e6) + " milliseconds to run our hierholzer implementation on a graph with " + g.getEdges().size() + " edges.");

                if (ans.size() != g.getEdges().size()) {
                    System.out.println("tourOK: " + tourOK);
                    continue;
                }
                HashSet<Integer> used = new HashSet<Integer>();
                TIntObjectHashMap<Edge> indexedEdges = g.getInternalEdgeMap();
                Edge curr = null;
                Edge prev = null;
                //make sure it's a real tour
                for (int j = 0; j < ans.size(); j++) {
                    // can't walk the same edge
                    if (used.contains(ans.get(j))) {
                        System.out.println("tourOK: " + tourOK);
                        break;
                    }
                    //make sure endpoints match up
                    prev = curr;
                    curr = indexedEdges.get(ans.get(j));
                    if (prev == null)
                        continue;
                    if (!(prev.getEndpoints().getFirst().getId() == curr.getEndpoints().getFirst().getId() ||
                            prev.getEndpoints().getSecond().getId() == curr.getEndpoints().getFirst().getId() ||
                            prev.getEndpoints().getFirst().getId() == curr.getEndpoints().getSecond().getId() ||
                            prev.getEndpoints().getSecond().getId() == curr.getEndpoints().getSecond().getId())) {
                        System.out.println("tourOK: " + tourOK);
                        break;
                    }
                }
                tourOK = true;
                System.out.println("tourOK: " + tourOK);
            }

            DirectedErdosRenyiGraphGenerator dgg = new DirectedErdosRenyiGraphGenerator();
            DirectedGraph g2;
            for (int i = 10; i < 150; i += 10) {
                tourOK = false;
                g2 = (DirectedGraph) dgg.generateEulerianGraph(i, 10, true);
                System.out.println("Directed graph of size " + i + " is Eulerian? " + CommonAlgorithms.isEulerian(g2));
                startTime = System.nanoTime();
                ArrayList<Integer> ans = CommonAlgorithms.tryHierholzer(g2);
                endTime = System.nanoTime();
                System.out.println("It took " + (endTime - startTime) / (1e6) + " milliseconds to run our hierholzer implementation on a graph with " + g2.getEdges().size() + " edges.");

                if (ans.size() != g2.getEdges().size()) {
                    System.out.println("tourOK: " + tourOK);
                    continue;
                }
                HashSet<Integer> used = new HashSet<Integer>();
                TIntObjectHashMap<Arc> indexedEdges = g2.getInternalEdgeMap();
                Arc curr = null;
                Arc prev;
                //make sure it's a real tour
                for (int j = 0; j < ans.size(); j++) {
                    // can't walk the same edge
                    if (used.contains(ans.get(j))) {
                        System.out.println("tourOK: " + tourOK);
                        break;
                    }
                    //make sure endpoints match up
                    prev = curr;
                    curr = indexedEdges.get(ans.get(j));
                    if (prev == null)
                        continue;
                    if (!(prev.getHead().getId() == curr.getTail().getId())) {
                        System.out.println("tourOK: " + tourOK);
                        break;
                    }
                }
                tourOK = true;
                System.out.println("tourOK: " + tourOK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * An example method of how to setup a graph and use a solver.
     */
    @SuppressWarnings("unused")
    private static void snippetUCPPSolver() {
        try {

            long start = System.currentTimeMillis(); // for timing
            UndirectedGraph test = new UndirectedGraph(); // initialize the graph

            // vertices
            UndirectedVertex v1 = new UndirectedVertex("dummy");
            UndirectedVertex v2 = new UndirectedVertex("dummy2");
            UndirectedVertex v3 = new UndirectedVertex("dummy3");

            // endpoints for the edges
            Pair<UndirectedVertex> ep = new Pair<UndirectedVertex>(v1, v2);
            Pair<UndirectedVertex> ep2 = new Pair<UndirectedVertex>(v2, v1);
            Pair<UndirectedVertex> ep3 = new Pair<UndirectedVertex>(v2, v3);
            Pair<UndirectedVertex> ep4 = new Pair<UndirectedVertex>(v3, v1);

            // initialize the edges
            Edge e = new Edge("stuff", ep, 10);
            Edge e2 = new Edge("more stuff", ep2, 20);
            Edge e3 = new Edge("third stuff", ep3, 5);
            Edge e4 = new Edge("fourth stuff", ep4, 7);

            // add all the elements to the graph
            test.addVertex(v1);
            test.addVertex(v2);
            test.addVertex(v3);
            test.addEdge(e);
            test.addEdge(e2);
            test.addEdge(e3);
            test.addEdge(e4);

            // set up the instance, and solve it
            UndirectedCPP testInstance = new UndirectedCPP(test);
            UCPPSolver_Edmonds testSolver = new UCPPSolver_Edmonds(testInstance);
            Route testAns = testSolver.trySolve();
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
