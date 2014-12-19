package core;

import oarlib.exceptions.InvalidEndpointsException;
import oarlib.exceptions.NegativeCycleException;
import oarlib.graph.graphgen.erdosrenyi.DirectedErdosRenyiGraphGenerator;
import oarlib.graph.graphgen.erdosrenyi.WindyErdosRenyiGraphGenerator;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test suite for our various shortest path methods.
 * <p/>
 * Created by oliverlum on 11/11/14.
 */
public class ShortestPathsTestSuite {

    private static final Logger LOGGER = Logger.getLogger(ShortestPathsTestSuite.class);

    @Test
    public void testDijkstras() {


        //=======================================================
        //
        //                   Directed Case
        //
        // ======================================================
        DirectedErdosRenyiGraphGenerator dgg = new DirectedErdosRenyiGraphGenerator();
        DirectedGraph testGraph = dgg.generateGraph(100, 50, true, .5, true);

        //control
        int[][] dist = new int[101][101];
        int[][] path = new int[101][101];
        CommonAlgorithms.fwLeastCostPaths(testGraph, dist, path);

        //validate
        int[] ddist = new int[101];
        int[] dpath = new int[101];
        for (int i = 1; i <= 100; i++) {
            CommonAlgorithms.dijkstrasAlgorithm(testGraph, i, ddist, dpath);
            //check
            for (int j = 1; j <= 100; j++) {
                if (i == j)
                    continue;
                assertEquals("Check distance: ", true, ddist[j] == dist[i][j]);
            }
        }

        //=======================================================
        //
        //                     Windy Case
        //
        // ======================================================
        WindyErdosRenyiGraphGenerator wgg = new WindyErdosRenyiGraphGenerator();
        WindyGraph testGraph2 = wgg.generateGraph(100, 50, true, .5, true);

        //control
        int[][] dist2 = new int[101][101];
        int[][] path2 = new int[101][101];
        CommonAlgorithms.fwLeastCostPaths(testGraph2, dist2, path2);

        //validate
        int[] ddist2 = new int[101];
        int[] dpath2 = new int[101];
        for (int i = 1; i <= 100; i++) {
            CommonAlgorithms.dijkstrasAlgorithm(testGraph2, i, ddist2, dpath2);
            //check
            for (int j = 1; j <= 100; j++) {
                if (i == j)
                    continue;
                assertEquals("Check distance: ", ddist2[j], dist2[i][j]);
            }
        }
    }

    @Test
    public void testBellmanFord() {

        try {
            //=======================================================
            //
            //                    Directed Case
            //
            // ======================================================
            DirectedErdosRenyiGraphGenerator dgg = new DirectedErdosRenyiGraphGenerator();
            DirectedGraph testGraph = dgg.generateGraph(100, 50, true, .5, true);

            //control
            int[][] dist = new int[101][101];
            int[][] path = new int[101][101];
            CommonAlgorithms.fwLeastCostPaths(testGraph, dist, path);

            //validate
            int[] ddist = new int[101];
            int[] dpath = new int[101];
            for (int i = 1; i <= 100; i++) {
                CommonAlgorithms.bellmanFordShortestPaths(testGraph, i, ddist, dpath);
                //check
                for (int j = 1; j <= 100; j++) {
                    if (i == j)
                        continue;
                    assertEquals("Check distance: ", true, ddist[j] == dist[i][j]);
                }
            }

            //=======================================================
            //
            //                      Windy Case
            //
            // ======================================================
            WindyErdosRenyiGraphGenerator wgg = new WindyErdosRenyiGraphGenerator();
            WindyGraph testGraph2 = wgg.generateGraph(100, 50, true, .5, true);

            //control
            int[][] dist2 = new int[101][101];
            int[][] path2 = new int[101][101];
            CommonAlgorithms.fwLeastCostPaths(testGraph2, dist2, path2);

            //validate
            int[] ddist2 = new int[101];
            int[] dpath2 = new int[101];
            for (int i = 1; i <= 100; i++) {
                CommonAlgorithms.bellmanFordShortestPaths(testGraph2, i, ddist2, dpath2);
                //check
                for (int j = 1; j <= 100; j++) {
                    if (i == j)
                        continue;
                    assertEquals("Check distance: ", true, ddist2[j] == dist2[i][j]);
                }
            }

            //=======================================================
            //
            //    B-F should be able to callout Negative Cycles
            //
            // ======================================================

            DirectedGraph negCyc = new DirectedGraph(7);
            negCyc.addEdge(1, 2, 1);
            negCyc.addEdge(2, 3, 1);
            negCyc.addEdge(3, 4, -1);
            negCyc.addEdge(4, 5, -1);
            negCyc.addEdge(5, 3, -1);
            negCyc.addEdge(5, 6, 1);
            negCyc.addEdge(6, 7, 1);

            int[] negDist = new int[8];
            int[] negPath = new int[8];
            int[] negEdgePath = new int[8];
            CommonAlgorithms.bellmanFordShortestPaths(negCyc, 2, negDist, negPath, negEdgePath);

        } catch (InvalidEndpointsException e) {

            //this is bad
            e.printStackTrace();

        } catch (NegativeCycleException e) {
            //now see if the path is what we expect
            int[] violatingPath = e.getViolatingPath();
            assertEquals("Check violatingPath size: ", 3, violatingPath.length);

            HashSet<Integer> violatingVSet = new HashSet<Integer>();
            for (int i = 0; i <= 2; i++) {
                violatingVSet.add(violatingPath[i]);
            }
            assertEquals("Checking expected path elements: ", true, violatingVSet.contains(3) && violatingVSet.contains(4) && violatingVSet.contains(5));

        }

    }

    @Test
    public void testSLF() {
        try {
            //=======================================================
            //
            //                    Directed Case
            //
            // ======================================================
            DirectedErdosRenyiGraphGenerator dgg = new DirectedErdosRenyiGraphGenerator();
            DirectedGraph testGraph = dgg.generateGraph(100, 50, true, .5, true);

            //control
            int[][] dist = new int[101][101];
            int[][] path = new int[101][101];
            CommonAlgorithms.fwLeastCostPaths(testGraph, dist, path);

            //validate
            int[] ddist = new int[101];
            int[] dpath = new int[101];
            for (int i = 1; i <= 100; i++) {
                CommonAlgorithms.slfShortestPaths(testGraph, i, ddist, dpath);
                //check
                for (int j = 1; j <= 100; j++) {
                    if (i == j)
                        continue;
                    assertEquals("Check distance: ", true, ddist[j] == dist[i][j]);
                }
            }

            //=======================================================
            //
            //                      Windy Case
            //
            // ======================================================
            WindyErdosRenyiGraphGenerator wgg = new WindyErdosRenyiGraphGenerator();
            WindyGraph testGraph2 = wgg.generateGraph(100, 50, true, .5, true);

            //control
            int[][] dist2 = new int[101][101];
            int[][] path2 = new int[101][101];
            CommonAlgorithms.fwLeastCostPaths(testGraph2, dist2, path2);

            //validate
            int[] ddist2 = new int[101];
            int[] dpath2 = new int[101];
            for (int i = 1; i <= 100; i++) {
                CommonAlgorithms.slfShortestPaths(testGraph2, i, ddist2, dpath2);
                //check
                for (int j = 1; j <= 100; j++) {
                    if (i == j)
                        continue;
                    assertEquals("Check distance: ", true, ddist2[j] == dist2[i][j]);
                }
            }

            //=======================================================
            //
            //    B-F should be able to callout Negative Cycles
            //
            // ======================================================

            DirectedGraph negCyc = new DirectedGraph(7);
            negCyc.addEdge(1, 2, 1);
            negCyc.addEdge(2, 3, 1);
            negCyc.addEdge(3, 4, -1);
            negCyc.addEdge(4, 5, -1);
            negCyc.addEdge(5, 3, -1);
            negCyc.addEdge(5, 6, 1);
            negCyc.addEdge(6, 7, 1);

            int[] negDist = new int[8];
            int[] negPath = new int[8];
            int[] negEdgePath = new int[8];
            CommonAlgorithms.slfShortestPaths(negCyc, 2, negDist, negPath, negEdgePath);

        } catch (InvalidEndpointsException e) {

            //this is bad
            e.printStackTrace();

        } catch (NegativeCycleException e) {
            //now see if the path is what we expect
            int[] violatingPath = e.getViolatingPath();
            assertEquals("Check violatingPath size: ", 3, violatingPath.length);

            HashSet<Integer> violatingVSet = new HashSet<Integer>();
            for (int i = 0; i <= 2; i++) {
                violatingVSet.add(violatingPath[i]);
            }
            assertEquals("Checking expected path elements: ", true, violatingVSet.contains(3) && violatingVSet.contains(4) && violatingVSet.contains(5));

        }

    }

    @Test
    public void testFloydWarshall() {

        try {

            //setup the most complex case
            WindyGraph testGraph = new WindyGraph(4);
            testGraph.addEdge(1, 2, 2, 1);
            testGraph.addEdge(2, 3, 3, 2);
            testGraph.addEdge(3, 4, 1, 3);
            testGraph.addEdge(4, 1, 4, 4);
            testGraph.addEdge(2, 4, 1, 1);

            int[][] dist = new int[5][5];
            int[][] path = new int[5][5];
            int[][] edgePath = new int[5][5];

            CommonAlgorithms.fwLeastCostPaths(testGraph, dist, path, edgePath);

            //validate via spot check
            assertEquals("Spot checking the Floyd Warshall distance matrix.", 1, dist[2][1]);
            assertEquals("Spot checking the Floyd Warshall distance matrix.", 1, dist[2][4]);
            assertEquals("Spot checking the Floyd Warshall distance matrix.", 2, dist[3][2]);
            assertEquals("Spot checking the Floyd Warshall distance matrix.", 3, dist[1][4]);

        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(true); //auto-fail
        }


    }

    @Test
    public void testWidestPath() {

        try {

            WindyGraph testGraph = new WindyGraph(5);
            testGraph.addEdge(1, 2, 4);
            testGraph.addEdge(2, 3, 5);
            testGraph.addEdge(3, 4, 2);
            testGraph.addEdge(4, 5, 4);
            testGraph.addEdge(3, 5, 3);

            int[] width = new int[6];
            int[] path = new int[6];
            int[] edgePath = new int[6];

            CommonAlgorithms.dijkstrasWidestPathAlgorithm(testGraph, 1, width, path, edgePath);

            //validate
            assertEquals("Check the width of the widest path.", 3, width[5]);
            assertEquals("Check the path of the widest path.", 3, path[5]);
            assertEquals("Check the edgePath of the widest path.", 5, edgePath[5]);

        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(true);
        }

    }

    @Test
    public void testKWidestPath() {

    }

    @Test
    public void testPapes() {

        try {
            //=======================================================
            //
            //                    Directed Case
            //
            // ======================================================
            DirectedErdosRenyiGraphGenerator dgg = new DirectedErdosRenyiGraphGenerator();
            DirectedGraph testGraph = dgg.generateGraph(100, 50, true, .5, true);

            //control
            int[][] dist = new int[101][101];
            int[][] path = new int[101][101];
            CommonAlgorithms.fwLeastCostPaths(testGraph, dist, path);

            //validate
            int[] ddist = new int[101];
            int[] dpath = new int[101];
            for (int i = 1; i <= 100; i++) {
                CommonAlgorithms.papeShortestPaths(testGraph, i, ddist, dpath);
                //check
                for (int j = 1; j <= 100; j++) {
                    if (i == j)
                        continue;
                    assertEquals("Check distance: ", ddist[j], dist[i][j]);
                }
            }

            //=======================================================
            //
            //                      Windy Case
            //
            // ======================================================
            WindyErdosRenyiGraphGenerator wgg = new WindyErdosRenyiGraphGenerator();
            WindyGraph testGraph2 = wgg.generateGraph(100, 50, true, .5, true);

            //control
            int[][] dist2 = new int[101][101];
            int[][] path2 = new int[101][101];
            CommonAlgorithms.fwLeastCostPaths(testGraph2, dist2, path2);

            //validate
            int[] ddist2 = new int[101];
            int[] dpath2 = new int[101];
            for (int i = 1; i <= 100; i++) {
                CommonAlgorithms.papeShortestPaths(testGraph2, i, ddist2, dpath2);
                //check
                for (int j = 1; j <= 100; j++) {
                    if (i == j)
                        continue;
                    assertEquals("Check distance: ", ddist2[j], dist2[i][j]);
                }
            }

            //=======================================================
            //
            //    B-F should be able to callout Negative Cycles
            //
            // ======================================================

            DirectedGraph negCyc = new DirectedGraph(7);
            negCyc.addEdge(1, 2, 1);
            negCyc.addEdge(2, 3, 1);
            negCyc.addEdge(3, 4, -1);
            negCyc.addEdge(4, 5, -1);
            negCyc.addEdge(5, 3, -1);
            negCyc.addEdge(5, 6, 1);
            negCyc.addEdge(6, 7, 1);

            int[] negDist = new int[8];
            int[] negPath = new int[8];
            int[] negEdgePath = new int[8];
            CommonAlgorithms.slfShortestPaths(negCyc, 2, negDist, negPath, negEdgePath);

        } catch (InvalidEndpointsException e) {

            //this is bad
            e.printStackTrace();

        } catch (NegativeCycleException e) {
            //now see if the path is what we expect
            int[] violatingPath = e.getViolatingPath();
            assertEquals("Check violatingPath size: ", 3, violatingPath.length);

            HashSet<Integer> violatingVSet = new HashSet<Integer>();
            for (int i = 0; i <= 2; i++) {
                violatingVSet.add(violatingPath[i]);
            }
            assertEquals("Checking expected path elements: ", true, violatingVSet.contains(3) && violatingVSet.contains(4) && violatingVSet.contains(5));

        }

    }
}
