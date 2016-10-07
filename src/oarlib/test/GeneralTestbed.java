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
package oarlib.test;

import gnu.trove.TIntObjectHashMap;
import oarlib.bmgt831.EquatorialInstanceGenerator;
import oarlib.bmgt831.TruckAndDroneFeasibilityChecker;
import oarlib.bmgt831.TruckAndDroneProblemWriter;
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.display.GraphDisplay;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.graphgen.OSM_Fetcher;
import oarlib.graph.graphgen.Util.BoundingBox;
import oarlib.graph.graphgen.Util.OSM_BoundingBoxes;
import oarlib.graph.graphgen.erdosrenyi.DirectedErdosRenyiGraphGenerator;
import oarlib.graph.graphgen.erdosrenyi.UndirectedErdosRenyiGraphGenerator;
import oarlib.graph.graphgen.rectangular.ZigzagRectangularGraphGenerator;
import oarlib.graph.impl.*;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.IndexedRecord;
import oarlib.graph.util.MSArbor;
import oarlib.graph.util.Pair;
import oarlib.link.impl.Arc;
import oarlib.link.impl.Edge;
import oarlib.link.impl.WindyEdge;
import oarlib.link.impl.ZigZagLink;
import oarlib.metrics.MaxMetric;
import oarlib.metrics.Metric;
import oarlib.metrics.RouteOverlapMetric;
import oarlib.notifications.GmailNotification;
import oarlib.problem.impl.cpp.DirectedCPP;
import oarlib.problem.impl.cpp.MixedCPP;
import oarlib.problem.impl.cpp.UndirectedCPP;
import oarlib.problem.impl.cpp.WindyCPP;
import oarlib.problem.impl.io.ProblemFormat;
import oarlib.problem.impl.io.ProblemReader;
import oarlib.problem.impl.io.ProblemWriter;
import oarlib.problem.impl.io.util.ExportHelper;
import oarlib.problem.impl.multivehicle.MinMaxKWRPP;
import oarlib.problem.impl.rpp.DirectedRPP;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.problem.impl.rpp.WindyRPPZZTW;
import oarlib.route.impl.Tour;
import oarlib.route.util.SolutionImporter;
import oarlib.solver.impl.*;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;
import oarlib.vertex.impl.WindyVertex;
import oarlib.vertex.impl.ZigZagVertex;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.*;
import java.security.spec.MGF1ParameterSpec;
import java.text.Normalizer;
import java.util.*;

public class GeneralTestbed {

    private static final Logger LOGGER = Logger.getLogger(GeneralTestbed.class);

    /**
     * The main method.  Allows for command line calls to the core solvers.
     *
     * @param args
     */
    public static void main(String[] args) {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.WARN);
        PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
        rootLogger.addAppender(new ConsoleAppender(layout));

        /*args = new String[2];
        args[0] = "4";
        args[1] = "/Users/oliverlum/Downloads/testOARLib3.txt";*/


        //empty call gets help
        if(args.length != 2) {
            for(String arg : args)
                System.out.println(arg);
            displayHelp();
        } else {

            try {
                int solver = Integer.parseInt(args[0]);
                switch (solver) {

                    case 1: //DCPP
                        //parse
                        try {
                            ProblemReader pr = new ProblemReader(ProblemFormat.Name.OARLib);
                            DirectedGraph dg = (DirectedGraph)pr.readGraph(args[1]);

                            //solve
                            DirectedCPP dcpp = new DirectedCPP(dg, "instance");
                            DCPPSolver_Edmonds dcppSolver = new DCPPSolver_Edmonds(dcpp);

                            dcppSolver.trySolve();
                            System.out.println(dcppSolver.printCurrentSol());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case 2: //UCPP
                        //parse
                        try {
                            ProblemReader pr2 = new ProblemReader(ProblemFormat.Name.OARLib);
                            UndirectedGraph ug = (UndirectedGraph)pr2.readGraph(args[1]);

                            //solve
                            UndirectedCPP ucpp = new UndirectedCPP(ug, "instance");
                            UCPPSolver_Edmonds ucppSolver = new UCPPSolver_Edmonds(ucpp);

                            ucppSolver.trySolve();
                            System.out.println(ucppSolver.printCurrentSol());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        break;
                    case 3: //MCPP1
                        //parse
                        try {
                            ProblemReader pr3 = new ProblemReader(ProblemFormat.Name.Corberan);
                            MixedGraph mg;
                            try {
                                mg = (MixedGraph) pr3.readGraph(args[1]);
                            } catch (FormatMismatchException fme) {
                                System.out.println("Could not read file in Corberan format; attempting to read in OARLib format.");
                                pr3 = new ProblemReader(ProblemFormat.Name.OARLib);
                                mg = (MixedGraph)pr3.readGraph(args[1]);
                            }

                            //solve
                            MixedCPP mcpp = new MixedCPP(mg, "instance");
                            MCPPSolver_Frederickson mcppSolver = new MCPPSolver_Frederickson(mcpp);

                            mcppSolver.trySolve();
                            System.out.println(mcppSolver.printCurrentSol());
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        break;
                    case 4: //MCPP2
                        //parse
                        try {
                            ProblemReader pr4 = new ProblemReader(ProblemFormat.Name.Corberan);
                            MixedGraph mg2;
                            try {
                                mg2 = (MixedGraph) pr4.readGraph(args[1]);
                            } catch (FormatMismatchException fme) {
                                System.out.println("Could not read file in Corberan format; attempting to read in OARLib format.");
                                pr4 = new ProblemReader(ProblemFormat.Name.OARLib);
                                mg2 = (MixedGraph) pr4.readGraph(args[1]);
                            }

                            //solve
                            MixedCPP mcpp2 = new MixedCPP(mg2, "Instance");
                            MCPPSolver_Yaoyuenyong mcppSolver2 = new MCPPSolver_Yaoyuenyong(mcpp2);

                            mcppSolver2.trySolve();
                            System.out.println(mcppSolver2.printCurrentSol());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case 5: //WPP
                        //parse
                        try {
                            ProblemReader pr5 = new ProblemReader(ProblemFormat.Name.Corberan);
                            WindyGraph wg;
                            try {
                                wg = (WindyGraph) pr5.readGraph(args[1]);
                            } catch (FormatMismatchException fme) {
                                System.out.println("Could not read file in Corberan format; attempting to read in OARLib format.");
                                pr5 = new ProblemReader(ProblemFormat.Name.OARLib);
                                wg = (WindyGraph) pr5.readGraph(args[1]);
                            }

                            //solve
                            WindyCPP wpp = new WindyCPP(wg, "Instance");
                            WRPPSolver_Win wppSolver = new WRPPSolver_Win(wpp);

                            wppSolver.trySolve();
                            System.out.println(wppSolver.printCurrentSol());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case 6: //DRPP
                        //parse
                        try {
                            ProblemReader pr6 = new ProblemReader(ProblemFormat.Name.Campos);
                            DirectedGraph dg2;
                            try {
                                dg2 = (DirectedGraph) pr6.readGraph(args[1]);
                            } catch (FormatMismatchException fme) {
                                System.out.println("Could not read file in Campos format; attempting to read in OARLib format.");
                                pr6 = new ProblemReader(ProblemFormat.Name.OARLib);
                                dg2 = (DirectedGraph) pr6.readGraph(args[1]);
                            }

                            //solve
                            DirectedRPP drpp = new DirectedRPP(dg2, "Instance");
                            DRPPSolver_Christofides drppSolver = new DRPPSolver_Christofides(drpp);

                            drppSolver.trySolve();
                            System.out.println(drppSolver.printCurrentSol());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case 7: //WRPP
                        //parse
                        try {
                            ProblemReader pr7 = new ProblemReader(ProblemFormat.Name.Corberan);
                            WindyGraph wg2;
                            try {
                                wg2 = (WindyGraph) pr7.readGraph(args[1]);
                            } catch (FormatMismatchException fme) {
                                System.out.println("Could not read file in Corberan format; attempting to read in OARLib format.");
                                pr7 = new ProblemReader(ProblemFormat.Name.OARLib);
                                wg2 = (WindyGraph) pr7.readGraph(args[1]);
                            }

                            //solve
                            WindyRPP wrpp = new WindyRPP(wg2, "Instance");
                            WRPPSolver_Benavent_H1 wrppSolver = new WRPPSolver_Benavent_H1(wrpp);

                            wrppSolver.trySolve();
                            System.out.println(wrppSolver.printCurrentSol());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;

                }
            } catch (NumberFormatException ex) {
                displayHelp();
            }
        }

    }

    private static void displayHelp(){

        String helpText = "===================";
        helpText += "\n";
        helpText += "\n";
        helpText += "Welcome to the Open Source Arc Routing Library (OARLib).\n";
        helpText += "If you would like to use this software as a command-line utility,\n";
        helpText += "please use the call structure: oarlib.jar [solver] [instance file path].\n\n";
        helpText += "[solver] - \n\n";
        helpText += "  1 - Directed Chinese Postman Exact Solver (Edmonds's Algorithm).\n";
        helpText += "  2 - Undirected Chinese Postman Exact Solver (Edmonds's Algorithm).\n";
        helpText += "  3 - Mixed Chinese Postman (Frederickson's Heuristic).\n";
        helpText += "  4 - Mixed Chinese Postman (Yaoyuenyong et al.'s Heuristic)\n";
        helpText += "  5 - Windy Chinese Postman (Win's Heuristic)\n";
        helpText += "  6 - Directed Rural Postman (Christofides's Heuristic)\n";
        helpText += "  7 - Windy Rural Postman (Benavent et al.'s Heuristic)\n";
        helpText += "\n";
        helpText += "If you would like to extend this code, or use its API directly,\n";
        helpText += "please see the README and docs for additional details.\n";
        helpText += "\n";
        helpText += "===================\n";

        System.out.println(helpText);
    }

    private static void testMethods(){
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
        //testSolutionImporter();
        //testCollapseSolvers("/Users/oliverlum/Documents/Research/Computational Results/MMkWRPP/CollapseTesting.csv");
        //testGraphDisplay();
        //testOSMQuery();
        //testMMkWRPPSolver();
        //testWidestPath();
        //testTruckAndDroneProblemGenerator();
        //createLegend();
        //String[] args2 = new String[2];
        //args2[1] = "/Users/oliverlum/Downloads/droneRoutes.txt";
        //args2[0] = "/Users/oliverlum/Downloads/instance2_tmp.txt";
        //testFeasibilityChecker(args2);
        //testEquatorialInstanceGenerator();
        //testZigZagParser();
        testMultiVehicleSolvers("/Users/Username/Foldername", "/Users/oliverlum/Documents/Research/Computational Results/MMkWRPP/Timing.csv");
        testZigZagSolver();
        //testIracer(args);
        //testMemoryLeak();
        //compareResults();
        //callPython();
        //stefanInstances();
    }


    private static void testCollapseSolvers(String outputFile) {
        try {

            //WINDY

            LOGGER.info("========================================================");
            LOGGER.info("Beginning Test of the Windy Partitioning Code");
            LOGGER.info("========================================================");

            MinMaxKWRPP validWInstance = null;
            Collection<? extends Route> validWAns;
            ArrayList<Problem<WindyVertex, WindyEdge, WindyGraph>> probs = new ArrayList<Problem<WindyVertex, WindyEdge, WindyGraph>>();
            ArrayList<Metric.Type> metrics = new ArrayList<Metric.Type>();

            metrics.add(Metric.Type.N);
            metrics.add(Metric.Type.M);
            metrics.add(Metric.Type.MAX);
            metrics.add(Metric.Type.AVG);
            metrics.add(Metric.Type.VAR);
            metrics.add(Metric.Type.DEV);
            metrics.add(Metric.Type.ATD);
            metrics.add(Metric.Type.ROI);
            metrics.add(Metric.Type.DEPDIST);
            metrics.add(Metric.Type.CONVEXOVERLAP);

            //run on all instances in the folder
            int limForDebug = 2; //only run on the first 10 instances for now
            int debugCounter = 0;
            String output;
            WindyGraph g;
            GraphDisplay displayer = new GraphDisplay(GraphDisplay.Layout.YifanHu, null, null);

            ProblemReader pr = new ProblemReader(ProblemFormat.Name.Corberan);

            /*for (BoundingBox bb : OSM_BoundingBoxes.CITY_INSTANCES_SMALL) {
                g = (WindyGraph) pr.readGraph("/Users/oliverlum/Downloads/Plots/" + bb.getTitle() + "_small.txt");
                validWInstance = new MinMaxKWRPP(g, bb.getTitle(), 3);
                probs.add(validWInstance);
            }*/

            g = (WindyGraph)pr.readGraph("/Users/oliverlum/Downloads/angel/Minmax_est7_6_3_1.dat");
            g.setDepotId(2);
            validWInstance = new MinMaxKWRPP(g, "Corberan7_6_3_1_3veh", 3);
            probs.add(validWInstance);


            /*g = (WindyGraph)pr.readGraph("/Users/oliverlum/Downloads/angel/Minmax_est6_5_3_2/Minmax_est6_5_3_2.dat");
            g.setDepotId(9);
            validWInstance = new MinMaxKWRPP(g, "Corberan6_5_3_2_3veh", 3);
            probs.add(validWInstance);

            g = (WindyGraph)pr.readGraph("/Users/oliverlum/Downloads/angel/Minmax_est8_8_5_1/Minmax_est8_8_5_1.dat");
            g.setDepotId(28);
            validWInstance = new MinMaxKWRPP(g, "Corberan8_8_5_1_3veh", 2);
            probs.add(validWInstance);*/

            PrintWriter pw = new PrintWriter(new File("/Users/oliverlum/Downloads/CollapseParameterResults_Grid.txt"), "UTF-8");

            //vary numPartitions
            for (int partitions = 10; partitions <= 30; partitions += 5) {
                //vary alpha
                for (double alpha = 0.1; alpha <= 1; alpha += .1) {
                    MultiWRPP_CommunityCollapse validWSolver = new MultiWRPP_CommunityCollapse(validWInstance);
                    validWSolver.setNumPartitions(partitions);
                    validWSolver.setAlpha(alpha);
                    try {
                        ExportHelper.exportToExcel(probs, metrics, validWSolver, outputFile);
                    } catch (NoSuchElementException ex) {
                        ex.printStackTrace();
                        continue;
                    }
                    for (Problem p : probs) {
                        System.out.println(new MaxMetric().evaluate(p.getSol()) + "," + new RouteOverlapMetric(p.getGraph()).evaluate(p.getSol()));
                        pw.println(new MaxMetric().evaluate(p.getSol()) + "," + new RouteOverlapMetric(p.getGraph()).evaluate(p.getSol()) + ";");
                        pw.flush();
                    }
                }
            }

            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void testSolutionImporter() {
        Collection<Tour<DirectedVertex, Arc>> test = SolutionImporter.importRoutes("/Users/oliverlum/Downloads/bnc2/Paris_2_3_50_op.txt", 67, SolutionImporter.RouteFormat.CORBERAN);

        for (Route r : test) {
            System.out.println(r.toString());
        }

        try {
            ProblemReader pr = new ProblemReader(ProblemFormat.Name.Corberan);
            WindyGraph g = (WindyGraph) pr.readGraph("/Users/oliverlum/Downloads/bnc2/Paris.txt");
            HashSet<Tour> finalAns = SolutionImporter.mapToGraph(g, test);

            for (Tour t : finalAns) {
                System.out.println(t.getCost());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

    }

    private static void stefanInstances() {
        try {
            System.out.println("====================================");
            for (BoundingBox bb : OSM_BoundingBoxes.STEFAN_INSTANCES) {
                OSM_Fetcher fetcher = new OSM_Fetcher(bb);
                fetcher.queryForGraph();
                WindyGraph g = fetcher.queryForGraph();
                System.out.println("N: " + g.getVertices().size());
                System.out.println("M: " + g.getEdges().size());
                WindyRPP wrpp = new WindyRPP(g, bb.getTitle());
                ProblemWriter pw = new ProblemWriter(ProblemFormat.Name.Corberan);
                pw.writeInstance(wrpp, "/Users/oliverlum/Documents/" + bb.getTitle() + ".txt");
            }

            System.out.println("====================================");
            for (BoundingBox bb : OSM_BoundingBoxes.CITY_INSTANCES) {
                OSM_Fetcher fetcher = new OSM_Fetcher(bb);
                fetcher.queryForGraph();
                WindyGraph g = fetcher.queryForGraph();
                System.out.println("N: " + g.getVertices().size());
                System.out.println("M: " + g.getEdges().size());
                WindyRPP wrpp = new WindyRPP(g, bb.getTitle());
                ProblemWriter pw = new ProblemWriter(ProblemFormat.Name.Corberan);
                pw.writeInstance(wrpp, "/Users/oliverlum/Documents/" + bb.getTitle() + ".txt");
                GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, g, bb.getTitle());
                gd.export(GraphDisplay.ExportType.PDF);
            }

            System.out.println("====================================");
            for (BoundingBox bb : OSM_BoundingBoxes.CITY_INSTANCES_SMALL) {
                OSM_Fetcher fetcher = new OSM_Fetcher(bb);
                fetcher.queryForGraph();
                WindyGraph g = fetcher.queryForGraph();
                System.out.println("N: " + g.getVertices().size());
                System.out.println("M: " + g.getEdges().size());
                WindyRPP wrpp = new WindyRPP(g, bb.getTitle());
                ProblemWriter pw = new ProblemWriter(ProblemFormat.Name.Corberan);
                pw.writeInstance(wrpp, "/Users/oliverlum/Documents/" + bb.getTitle() + ".txt");
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
                WindyRPP wrpp = new WindyRPP(g, bb.getTitle());
                ProblemWriter pw = new ProblemWriter(ProblemFormat.Name.Corberan);
                pw.writeInstance(wrpp, "/Users/oliverlum/Documents/" + bb.getTitle() + ".txt");
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
                WindyRPP wrpp = new WindyRPP(g, bb.getTitle());
                ProblemWriter pw = new ProblemWriter(ProblemFormat.Name.Corberan);
                pw.writeInstance(wrpp, "/Users/oliverlum/Documents/" + bb.getTitle() + ".txt");
                GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, g, bb.getTitle());
                gd.export(GraphDisplay.ExportType.PDF);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void callPython() {
        try {
            int k = 1;
            for(int i = 1; i <= 5; i++) {
                for(int j = 1; j <= 5; j++) {
                    //k tacitly 1
                    ProcessBuilder pb = new ProcessBuilder("/opt/local/bin/python", "/Users/oliverlum/Downloads/ZigzagCPPT.py", Integer.toString(i), Integer.toString(j), Integer.toString(k));
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
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void compareResults() {

        try {
            BufferedReader br = new BufferedReader(new FileReader("/Users/oliverlum/Downloads/MTZ_PartialOrder_OutputSummary.txt"));
            BufferedReader br2 = new BufferedReader(new FileReader("/Users/oliverlum/Downloads/MTZ_OutputSummary.txt"));
            PrintWriter pw = new PrintWriter(new File("/Users/oliverlum/Downloads/Results2.txt"), "UTF-8");

            String line;
            Double temp;
            Double tempTotal = 0.0;
            //Double tempMin = Double.MAX_VALUE;
            int counter = 0;
            while ((line = br.readLine()) != null) {

                if (!line.contains("RunningTime"))
                    continue;

                temp = Double.valueOf(line.split("RunningTime: ")[1]);
                tempTotal += temp;
                //if(temp < tempMin)
                //tempMin = temp;

                counter++;
                //reset counter
                if (counter == 10) {
                    counter = 0;
                    pw.println(tempTotal);
                    tempTotal = 0.0;
                    //pw.println(tempMin);
                    //tempMin = Double.MAX_VALUE;
                }
            }
            pw.println("BREAK:");
            counter = 0;
            while ((line = br2.readLine()) != null) {

                if (!line.contains("Objective Value: "))
                    continue;

                temp = Double.valueOf(line.split("Objective Value: ")[1]);

                counter++;
                //reset counter
                if (counter == 1) {
                    pw.println(temp);
                } else if (counter == 5) {
                    counter = 0;
                }

            }
            br.close();
            br2.close();
            pw.flush();
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void testMemoryLeak() {
        WindyGraph g = new WindyGraph(10000);
    }

    private static void testIracer(String[] args) {

        //parse the input

        //defaults
        double alpha = 1;
        double beta = 1;
        int iter = 5;
        int perturb = 5;
        String instancePath = "";
        String s = "";

        for (int i = 0; i < args.length; i++) {

            s = args[i];
            if (s.equals("--alpha")) {
                alpha = Double.parseDouble(args[++i]);
            } else if (s.equals("--beta")) {
                beta = Double.parseDouble(args[++i]);
            } else if (s.equals("--iter")) {
                iter = Integer.parseInt(args[++i]);
            } else if (s.equals("--perturb")) {
                perturb = Integer.parseInt(args[++i]);
            } else if (s.equals("-i")) {
                instancePath = args[++i];
            } else {
                System.out.println(1);
                System.out.println("Error: Argument not recognized: " + s);
                break;
            }

        }

        try {
            //Call the code
            ProblemReader pr = new ProblemReader(ProblemFormat.Name.OARLib);
            WindyGraph g = (WindyGraph) pr.readGraph(instancePath);
            MinMaxKWRPP validWInstance = new MinMaxKWRPP(g, "Iracer Instance", 5);
            MultiWRPPSolver validWSolver = new MultiWRPPSolver(validWInstance, "Iracer Instance");
            validWSolver.setAlpha(alpha);
            validWSolver.setBeta(beta);
            validWSolver.setNumIterations(iter);
            validWSolver.setNumPerturbations(perturb);


            System.out.println(new MaxMetric().evaluate(validWSolver.trySolve()));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void testZigZagSolver() {
        try {

            //set up a graph for which we know the optimal solution
            ZigZagGraph circle = new ZigZagGraph(8);
            Random rng = new Random(1000);
            int var, var2;
            ZigZagLink temp;
            for (int i = 1; i <= 8; i++) {
                var = rng.nextInt(6);
                var -= 3;
                var2 = rng.nextInt(6);
                var2 -= 3;
                temp = new ZigZagLink("e" + i, new Pair<ZigZagVertex>(circle.getVertex(i), circle.getVertex(i % 8 + 1)), 10 + var, 10 + var2, 0, 0, 0, ZigZagLink.ZigZagStatus.NOT_AVAILABLE, false);
                circle.addEdge(temp);
            }

            temp = circle.getEdge(3);
            temp.setServiceCost(4);
            temp.setReverseServiceCost(5);
            temp.setZigzagCost(11);
            temp.setRequired(true);
            temp.setTimeWindow(new Pair<Integer>(0, 50));
            temp.setStatus(ZigZagLink.ZigZagStatus.OPTIONAL);

            temp = circle.getEdge(5);
            temp.setServiceCost(4);
            temp.setReverseServiceCost(5);
            temp.setZigzagCost(11);
            temp.setRequired(true);
            temp.setTimeWindow(new Pair<Integer>(0, 70));
            temp.setStatus(ZigZagLink.ZigZagStatus.OPTIONAL);

            temp = circle.getEdge(6);
            temp.setServiceCost(5);
            temp.setReverseServiceCost(4);
            temp.setZigzagCost(11);
            temp.setRequired(true);
            temp.setTimeWindow(new Pair<Integer>(0, 100));
            temp.setStatus(ZigZagLink.ZigZagStatus.OPTIONAL);


            PrintWriter pw = new PrintWriter(new File("RevisedZZHeuristicScalingResults_RelaxedTW5.txt"), "UTF-8");
            File f = new File("RevisedZZHeuristicScalingresults_NewIP.txt");
            if (f.exists())
                f.delete();
            long start, end;


            /*
            for (int i = 1; i <= 1; i++) {
                for (int j = 1; j <= 5; j++) {

                    //read in zigzag instances, and make sure that the graph object is correct
                    ProblemReader pr = new ProblemReader(ProblemFormat.Name.MeanderingPostman);
                    ZigZagGraph graph = (ZigZagGraph) (pr.readGraph("/Users/oliverlum/Downloads/171Nodes/WPPTZ171nodes_" + i + "_" + j + "_1.txt"));

                    int numEdges = graph.getEdges().size();

                    WindyRPPZZTW prob = new WindyRPPZZTW(graph, i + "_" + j + "_1");
                    WRPPZZTW_PFIH solver = new WRPPZZTW_PFIH(prob);
                    solver.setLatePenalty(1000);

                    //start = System.currentTimeMillis();
                    Collection<? extends Route> ans = solver.trySolve();
                    //end = System.currentTimeMillis();
                    //pw.println(ans.iterator().next().getCost() + "," + (end-start) + "," + solver.getBestPartialLength() + "," + solver.getBestPartialNumZigzags() + "," + solver.getBestSumLengthZigzags() + "," + solver.getBestPartialSize() + "," + solver.getAvgPartialLength() + "," + solver.getAvgPartialNumZigzags() + "," + solver.getAvgSumLengthZigzags() + "," + solver.getAvgPartialSize() + "," + solver.getGreatestSumLengthZigzags() + ";");
                }
            }*/


            ZigzagRectangularGraphGenerator zzrgg = new ZigzagRectangularGraphGenerator(1);
            ProblemWriter probw = new ProblemWriter(ProblemFormat.Name.Zhang_Matrix_Zigzag);


            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 10; j++) {

                    ZigZagGraph graph = zzrgg.generate(i+20, 35, (i+20)*35, .5, true);

                    WindyRPPZZTW prob = new WindyRPPZZTW(graph, "11_" + i + "_" + j);
                    probw.writeInstance(prob, "/Users/oliverlum/Downloads/20node/WPPTZ20nodes_11_" + i + "_" + j + ".txt");
                    WRPPZZTW_PFIH solver = new WRPPZZTW_PFIH(prob);
                    solver.setLatePenalty(10000);

                    //start = System.currentTimeMillis();
                    Collection<? extends Route> ans = solver.trySolve();
                    //end = System.currentTimeMillis();
                    //pw.println(ans.iterator().next().getCost() + "," + (end-start) + "," + solver.getBestPartialLength() + "," + solver.getBestPartialNumZigzags() + "," + solver.getBestSumLengthZigzags() + "," + solver.getBestPartialSize() + "," + solver.getAvgPartialLength() + "," + solver.getAvgPartialNumZigzags() + "," + solver.getAvgSumLengthZigzags() + "," + solver.getAvgPartialSize() + "," + solver.getGreatestSumLengthZigzags() + ";");

                }
            }

            pw.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void testZigZagParser() {

        try {
            //read in zigzag instances, and make sure that the graph object is correct
            ProblemReader pr = new ProblemReader(ProblemFormat.Name.MeanderingPostman);
            ZigZagGraph ans = (ZigZagGraph) (pr.readGraph("/Users/oliverlum/Downloads/Instancs_Solutions/WPPZ10nodes_1.txt"));

            System.out.println("VERTICES: " + ans.getVertices().size());
            System.out.println("EDGES: " + ans.getEdges().size());

            for (ZigZagLink l : ans.getEdges()) {
                System.out.println("I:" + l.getFirstEndpointId() + ",J:" + l.getSecondEndpointId() + ",COST:" + l.getCost() + ",STATUS:" + l.getStatus() + ",REVERSECOST:" + l.getReverseCost() + ",SERVICECOST:" + l.getServiceCost() + ",REQUIRED:" + l.isRequired());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void testEquatorialInstanceGenerator() {
        EquatorialInstanceGenerator gen = new EquatorialInstanceGenerator(3, 3);
        UndirectedGraph ans = gen.generateInstance();
    }

    private static void testFeasibilityChecker(String[] args) {

        if (args.length != 2) {
            System.out.println("To run the feasibility checker, please provide the following two arguments in order:");
            System.out.println("Path of instance file for which you are solving the problem, (e.g. /Users/username/Documents/instance.txt)");
            System.out.println("Path of solution file, (e.g. /Users/username/Documents/output.txt)");
            return;
        }

        TruckAndDroneFeasibilityChecker fc = new TruckAndDroneFeasibilityChecker(args[0], args[1]);
        if (fc.checkFeasible())
            System.out.println("Routes are feasible.");
        else
            System.out.println("Routes infeasible.  Please make sure the format is correct.");
    }

    private static void createLegend() {
        try {
            UndirectedGraph toVisualize = new UndirectedGraph(21);

            toVisualize.getVertex(1).setCoordinates(10, 100);
            toVisualize.getVertex(2).setCoordinates(90, 100);
            toVisualize.getVertex(3).setCoordinates(10, 90);
            toVisualize.getVertex(4).setCoordinates(90, 90);
            toVisualize.getVertex(5).setCoordinates(10, 80);
            toVisualize.getVertex(6).setCoordinates(90, 80);
            toVisualize.getVertex(7).setCoordinates(10, 70);
            toVisualize.getVertex(8).setCoordinates(90, 70);
            toVisualize.getVertex(9).setCoordinates(10, 60);
            toVisualize.getVertex(10).setCoordinates(90, 60);
            toVisualize.getVertex(11).setCoordinates(10, 50);
            toVisualize.getVertex(12).setCoordinates(90, 50);
            toVisualize.getVertex(13).setCoordinates(10, 40);
            toVisualize.getVertex(14).setCoordinates(90, 40);
            toVisualize.getVertex(15).setCoordinates(10, 30);
            toVisualize.getVertex(16).setCoordinates(90, 30);
            toVisualize.getVertex(17).setCoordinates(10, 20);
            toVisualize.getVertex(18).setCoordinates(90, 20);
            toVisualize.getVertex(19).setCoordinates(10, 10);
            toVisualize.getVertex(20).setCoordinates(90, 10);
            toVisualize.getVertex(21).setCoordinates(50, 110);

            toVisualize.setDepotId(21);


            toVisualize.addEdge(1, 2, 1);
            toVisualize.addEdge(3, 4, 1);
            toVisualize.addEdge(5, 6, 1);
            toVisualize.addEdge(7, 8, 1);
            toVisualize.addEdge(9, 10, 1);
            toVisualize.addEdge(11, 12, 1);
            toVisualize.addEdge(13, 14, 1);
            toVisualize.addEdge(15, 16, 1);
            toVisualize.addEdge(17, 18, 1);
            toVisualize.addEdge(19, 20, 1);

            HashMap<Integer, Integer> part = new HashMap<Integer, Integer>();
            part.put(1, 1);
            part.put(2, 2);
            part.put(3, 3);
            part.put(4, 4);
            part.put(5, 5);
            part.put(6, 6);
            part.put(7, 7);
            part.put(8, 8);
            part.put(9, 9);
            part.put(10, 10);

            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, toVisualize, "Legend");
            gd.exportWithPartition(GraphDisplay.ExportType.PDF, part);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testTruckAndDroneProblemGenerator() {
        TruckAndDroneProblemWriter.writeTruckAndDroneProblem("/Users/oliverlum/Documents/Research/TruckAndDroneInstances/instance_Calgary.txt");
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
            MinMaxKWRPP wrpp;
            MultiWRPPSolver_Benavent wrppSolver;
            Collection<? extends Route> ans;

            ProblemReader gr = new ProblemReader(ProblemFormat.Name.Corberan);

            WindyGraph test = (WindyGraph) gr.readGraph("/Users/oliverlum/Downloads/WPP/WA0531");

            wrpp = new MinMaxKWRPP(test, 5);
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
    private static void testMultiVehicleSolvers(String instanceFolder, String outputFile) {
        try {
            try {

                long start;
                long end;

                //WINDY

                LOGGER.info("========================================================");
                LOGGER.info("Beginning Test of the Windy Partitioning Code");
                LOGGER.info("========================================================");

                MinMaxKWRPP validWInstance = null;
                Collection<? extends Route> validWAns;
                ArrayList<Problem<WindyVertex, WindyEdge, WindyGraph>> probs = new ArrayList<Problem<WindyVertex, WindyEdge, WindyGraph>>();
                ArrayList<Metric.Type> metrics = new ArrayList<Metric.Type>();

                metrics.add(Metric.Type.N);
                metrics.add(Metric.Type.M);
                metrics.add(Metric.Type.MAX);
                metrics.add(Metric.Type.AVG);
                metrics.add(Metric.Type.VAR);
                metrics.add(Metric.Type.DEV);
                metrics.add(Metric.Type.ATD);
                metrics.add(Metric.Type.ROI);
                metrics.add(Metric.Type.DEPDIST);
                metrics.add(Metric.Type.CONVEXOVERLAP);

                //run on all instances in the folder
                int limForDebug = 2; //only run on the first 10 instances for now
                int debugCounter = 0;
                String output;
                WindyGraph g;
                GraphDisplay displayer = new GraphDisplay(GraphDisplay.Layout.YifanHu, null, null);

                ProblemReader pr = new ProblemReader(ProblemFormat.Name.Corberan);
                /*for (BoundingBox bb : OSM_BoundingBoxes.CITY_INSTANCES) {
                    g = (WindyGraph) pr.readGraph("/Users/oliverlum/Downloads/Plots/" + bb.getTitle() + "_Center.txt");
                    validWInstance = new MinMaxKWRPP(g, bb.getTitle(), 5);
                    probs.add(validWInstance);
                }*/

                OSM_Fetcher fetcher;
                ProblemWriter pw;


                /*for (BoundingBox bb : OSM_BoundingBoxes.CITY_INSTANCES_SMALL) {
                    //fetcher = new OSM_Fetcher(bb);
                    //g = fetcher.queryForGraph();
                    //g.setDepotId(Utils.findCenterVertex(g));
                    g = (WindyGraph) pr.readGraph("/Users/oliverlum/Downloads/Plots/" + bb.getTitle() + "_small.txt");
                    validWInstance = new MinMaxKWRPP(g, bb.getTitle(), 2);
                    probs.add(validWInstance);
                    //pw = new ProblemWriter(ProblemFormat.Name.Corberan);
                    //pw.writeInstance(validWInstance, "/Users/oliverlum/Downloads/Plots/" + bb.getTitle() + "_small.txt");
                }*/

                //now do the rectangular instances
                /*for (int i = 1; i <= 3; i++) {
                    g = (WindyGraph) pr.readGraph("/Users/oliverlum/Downloads/Plots/Random Instance " + i + "_Edge.txt");
                        //g.setDepotId(Utils.findCenterVertex(g));
                    validWInstance = new MinMaxKWRPP(g, "Random Instance " + i, 5);
                        probs.add(validWInstance);

                }*/
                /*
                for (int i = 1; i <= 10; i++) {
                    g = (WindyGraph) pr.readGraph("/Users/oliverlum/Downloads/Plots/Random Instance " + i + "_Edge.txt");
                    //g.setDepotId(Utils.findCenterVertex(g));
                    validWInstance = new MinMaxKWRPP(g, "Random Instance " + i, 5);
                    probs.add(validWInstance);

                }*/

                //now do Corberan's smaller instances

                g = (WindyGraph)pr.readGraph("/Users/oliverlum/Downloads/angel/Minmax_est7_6_3_1.dat");
                g.setDepotId(2);
                validWInstance = new MinMaxKWRPP(g, "Corberan7_6_3_1_3veh", 3);
                probs.add(validWInstance);


                /*g = (WindyGraph)pr.readGraph("/Users/oliverlum/Downloads/angel/Minmax_est6_5_3_2/Minmax_est6_5_3_2.dat");
                g.setDepotId(9);
                validWInstance = new MinMaxKWRPP(g, "Corberan6_5_3_2_3veh", 3);
                probs.add(validWInstance);*/

                /*g = (WindyGraph)pr.readGraph("/Users/oliverlum/Downloads/angel/Minmax_est8_8_5_1/Minmax_est8_8_5_1.dat");
                g.setDepotId(28);
                validWInstance = new MinMaxKWRPP(g, "Corberan8_8_5_1_3veh", 2);
                probs.add(validWInstance);*/

                /*g = makeTestGraph();
                g.setDepotId(3);
                validWInstance = new MinMaxKWRPP(g, "test", 2);
                probs.add(validWInstance);*/

                MultiWRPPSolver validWSolver = new MultiWRPPSolver(validWInstance, "", displayer);
                //MultiWRPPSolver_Benavent validWSolver = new MultiWRPPSolver_Benavent(validWInstance);
                //MultiWRPPSolverHybrid validWSolver = new MultiWRPPSolverHybrid(validWInstance,"",displayer,7);

                ExportHelper.exportToExcel(probs, metrics, validWSolver, outputFile);
                for (Problem p : probs)
                    System.out.println(new MaxMetric().evaluate(p.getSol()) + "," + new RouteOverlapMetric(p.getGraph()).evaluate(p.getSol()));

                String from = "oliver@math.umd.edu";
                String pass = "**REMOVED**";
                String[] to = {"oliver@math.umd.edu"}; // list of recipient email addresses
                String subject = "OARLib Notification";
                String body = "Your runs have completed.  Thank you for using OARLib.";

                GmailNotification.sendFromGMail(from, pass, to, subject, body);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //hexagon graph
    private static WindyGraph makeTestGraph() {

        try {
            WindyGraph ans = new WindyGraph(6);
            ans.addEdge(1, 2, 1, 2, true);
            ans.addEdge(2, 3, 3, 4, false);
            ans.addEdge(3, 4, 5, 6, true);
            ans.addEdge(4, 5, 7, 8, false);
            ans.addEdge(5, 6, 9, 10, true);
            ans.addEdge(6, 1, 11, 12, false);

            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
            Route ans = testSolver.trySolve().iterator().next();
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
        ProblemReader gr = new ProblemReader(ProblemFormat.Name.Campos);
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
                        validAns = validSolver.trySolve().iterator().next();
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
        ProblemReader gr = new ProblemReader(ProblemFormat.Name.Simple);
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
        ProblemReader gr = new ProblemReader(ProblemFormat.Name.Corberan);
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
                    validAns = validSolver.trySolve().iterator().next(); //my ans
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
        ProblemReader gr = new ProblemReader(ProblemFormat.Name.Yaoyuenyong);
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
                    validAns = validSolver.trySolve().iterator().next();
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
        ProblemReader gr = new ProblemReader(ProblemFormat.Name.Corberan);
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
                    validAns = validSolver.trySolve().iterator().next();
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
        ProblemReader gr = new ProblemReader(ProblemFormat.Name.Corberan);
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
                        validAns = validSolver.trySolve().iterator().next();
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
        ProblemReader gr = new ProblemReader(ProblemFormat.Name.Corberan);
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
                        validAns = validSolver.trySolve().iterator().next();
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
        ProblemReader gr = new ProblemReader(ProblemFormat.Name.Corberan);
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
                        validAns = validSolver2.trySolve().iterator().next();
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
            Route testAns = testSolver.trySolve().iterator().next();
            long end = System.currentTimeMillis();
            System.out.println(end - start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
