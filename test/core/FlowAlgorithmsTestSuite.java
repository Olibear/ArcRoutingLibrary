package core;

import gnu.trove.TIntObjectHashMap;
import oarlib.graph.graphgen.DirectedGraphGenerator;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;
import org.junit.Test;

import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by oliverlum on 11/11/14.
 */
public class FlowAlgorithmsTestSuite {

    @Test
    public void testMinCostFlow(){
        DirectedGraphGenerator dgg = new DirectedGraphGenerator();
        DirectedGraph testGraph = (DirectedGraph)dgg.generateGraph(100,50,true,.5,true);
        Random r = new Random();
        DirectedVertex v1, v2;

        TIntObjectHashMap<DirectedVertex> testGraphVertices = testGraph.getInternalVertexMap();
        try {
            for (int i = 1; i < 50; i++) {
                v1 = testGraphVertices.get(r.nextInt(100) + 1);
                v2 = testGraphVertices.get(r.nextInt(100) + 1);

                if (!v1.isDemandSet())
                    v1.setDemand(0);
                if (!v2.isDemandSet())
                    v2.setDemand(0);
                v1.setDemand(v1.getDemand() + 1);
                v2.setDemand(v2.getDemand() - 1);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[][] dist = new int[101][101];
        int[][] path = new int[101][101];
        CommonAlgorithms.fwLeastCostPaths(testGraph,dist,path);

        //cross-validate
        int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(testGraph);
        HashMap<Pair<Integer>, Integer> flowanswer2 = CommonAlgorithms.cycleCancelingMinCostNetworkFlow(testGraph, dist);

        //compare costs
        int m = testGraph.getEdges().size();
        int cost1 = 0;
        for(int i = 1; i <= m; i ++) {
            cost1 += flowanswer[i] * testGraph.getEdge(i).getCost();
        }

        int cost2 = 0;
        for(Pair<Integer> key: flowanswer2.keySet()) {
            cost2 += flowanswer2.get(key) * dist[key.getFirst()][key.getSecond()];
        }

        assertEquals("The flow methods cross-validate:", cost1, cost2);
    }

}
